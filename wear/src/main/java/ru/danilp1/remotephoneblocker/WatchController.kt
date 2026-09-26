package ru.danilp1.remotephoneblocker

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.security.SecureRandom

enum class WatchStatus { IDLE, LOCKING, LOCKED, ERROR }

/** Persisted widget state shared by its action and the Data Layer result listener. */
object WatchController {
    private const val TAG = "WearLockData"
    private const val LOCK = "/phone-lock/lock"
    private const val PREFS = "phone_lock_widget"
    private const val LOCK_TIMEOUT_MS = 8_000L
    private const val RESULT_DISPLAY_MS = 1_500L
    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Synchronized
    fun status(context: Context): WatchStatus {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val current = readStatus(context)
        val deadline = prefs.getLong("deadline", 0)
        if (deadline > 0 && System.currentTimeMillis() >= deadline) {
            if (current == WatchStatus.LOCKING) {
                finish(context, WatchStatus.ERROR)
                return WatchStatus.ERROR
            }
            if (current == WatchStatus.LOCKED || current == WatchStatus.ERROR) {
                setStatus(context, WatchStatus.IDLE)
                return WatchStatus.IDLE
            }
        }
        return current
    }

    @Synchronized
    fun lock(context: Context, onSendFinished: () -> Unit) {
        if (status(context) == WatchStatus.LOCKING) {
            onSendFinished()
            return
        }
        val requestId = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val idString = Base64.encodeToString(requestId, Base64.NO_WRAP)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit().putString("status", WatchStatus.LOCKING.name)
            .putString("request", idString)
            .putLong("deadline", System.currentTimeMillis() + LOCK_TIMEOUT_MS)
            .remove("node").apply()
        update(context)
        handler.postDelayed({ failLock(context, idString) }, LOCK_TIMEOUT_MS)
        Log.i(TAG, "Finding connected phone for LOCK")
        try {
            Wearable.getNodeClient(context).connectedNodes
                .addOnSuccessListener { nodes ->
                    val node = nodes.sortedByDescending { it.isNearby }.firstOrNull()
                    if (node == null) {
                        Log.w(TAG, "No connected phone node")
                        failLock(context, idString)
                        onSendFinished()
                        return@addOnSuccessListener
                    }
                    synchronized(this) {
                        if (readStatus(context) != WatchStatus.LOCKING ||
                            prefs.getString("request", null) != idString) {
                            onSendFinished()
                            return@synchronized
                        }
                        prefs.edit().putString("node", node.id).apply()
                        try {
                            Wearable.getMessageClient(context).sendMessage(node.id, LOCK, requestId)
                                .addOnSuccessListener { Log.i(TAG, "LOCK sent; awaiting phone result") }
                                .addOnFailureListener {
                                    Log.w(TAG, "LOCK send failed", it)
                                    failLock(context, idString)
                                }
                                .addOnCompleteListener { onSendFinished() }
                        } catch (e: RuntimeException) {
                            Log.w(TAG, "LOCK unavailable", e)
                            failLock(context, idString)
                            onSendFinished()
                        }
                    }
                }
                .addOnFailureListener {
                    Log.w(TAG, "Node lookup failed", it)
                    failLock(context, idString)
                    onSendFinished()
                }
        } catch (e: RuntimeException) {
            Log.w(TAG, "Data Layer unavailable", e)
            failLock(context, idString)
            onSendFinished()
        }
    }

    @Synchronized
    fun onLockResult(context: Context, nodeId: String, data: ByteArray) {
        if (data.size != 17 || readStatus(context) != WatchStatus.LOCKING) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (nodeId != prefs.getString("node", null)) return
        if (Base64.encodeToString(data.copyOfRange(1, 17), Base64.NO_WRAP) != prefs.getString("request", null)) return
        val result = data[0]
        Log.i(TAG, "LOCK_RESULT received: code $result")
        finish(context, if (result.toInt() == 0) WatchStatus.LOCKED else WatchStatus.ERROR)
    }

    @Synchronized
    private fun failLock(context: Context, request: String) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (readStatus(context) != WatchStatus.LOCKING || prefs.getString("request", null) != request) return
        Log.w(TAG, "Lock request failed or timed out")
        finish(context, WatchStatus.ERROR)
    }

    private fun finish(context: Context, result: WatchStatus) {
        val deadline = System.currentTimeMillis() + RESULT_DISPLAY_MS
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("status", result.name).putLong("deadline", deadline)
            .remove("request").remove("node").apply()
        update(context)
        handler.postDelayed({ resetResult(context, deadline) }, RESULT_DISPLAY_MS)
    }

    @Synchronized
    private fun resetResult(context: Context, deadline: Long) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getLong("deadline", 0) != deadline ||
            readStatus(context) !in listOf(WatchStatus.LOCKED, WatchStatus.ERROR)) return
        setStatus(context, WatchStatus.IDLE)
        update(context)
    }

    private fun readStatus(context: Context): WatchStatus =
        runCatching {
            WatchStatus.valueOf(context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getString("status", WatchStatus.IDLE.name)!!)
        }.getOrDefault(WatchStatus.IDLE)

    private fun setStatus(context: Context, value: WatchStatus) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("status", value.name).remove("deadline").remove("request").remove("node").apply()
    }

    private fun update(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            try {
                PhoneLockWidget().triggerUpdateAll(appContext)
            } catch (e: Exception) {
                Log.w(TAG, "Widget refresh failed", e)
            }
        }
    }
}
