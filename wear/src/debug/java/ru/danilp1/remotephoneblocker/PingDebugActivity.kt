package ru.danilp1.remotephoneblocker

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.android.gms.wearable.Wearable

/** ADB-only entry point for exercising the Data Layer before the widget is installed. */
class PingDebugActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Wearable.getNodeClient(this).connectedNodes
            .addOnSuccessListener { nodes ->
                Log.i("WearLockData", "Connected nodes: ${nodes.size}")
                nodes.forEach { node ->
                    Wearable.getMessageClient(this).sendMessage(node.id, PING, byteArrayOf())
                        .addOnSuccessListener { Log.i("WearLockData", "PING sent") }
                        .addOnFailureListener { Log.w("WearLockData", "PING failed", it) }
                }
            }
            .addOnFailureListener { Log.w("WearLockData", "Node lookup failed", it) }
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, 5_000)
    }

    companion object {
        private const val PING = "/phone-lock/ping"
    }
}
