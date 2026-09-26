package ru.danilp1.remotephoneblocker

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.pm.PackageManager
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import java.util.concurrent.TimeUnit

class PhoneMessageService : WearableListenerService() {
    override fun onMessageReceived(event: MessageEvent) {
        when (event.path) {
            PING -> {
                if (event.data.isNotEmpty()) return
                Log.i(TAG, "PING received")
                reply(event.sourceNodeId, PONG, byteArrayOf())
            }
            LOCK -> {
                if (event.data.size != REQUEST_ID_SIZE) {
                    Log.w(TAG, "Malformed LOCK ignored")
                    return
                }
                Log.i(TAG, "LOCK received")
                val result = lockPhone()
                reply(event.sourceNodeId, LOCK_RESULT, byteArrayOf(result) + event.data)
            }
        }
    }

    private fun lockPhone(): Byte {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_DEVICE_ADMIN)) return UNSUPPORTED
        val manager = getSystemService(DevicePolicyManager::class.java) ?: return UNSUPPORTED
        val admin = ComponentName(this, PhoneAdminReceiver::class.java)
        if (!manager.isAdminActive(admin)) return ADMIN_INACTIVE
        val keyguard = getSystemService(KeyguardManager::class.java) ?: return UNSUPPORTED
        if (!keyguard.isDeviceSecure) return NO_CREDENTIAL
        return try {
            manager.lockNow()
            Log.i(TAG, "lockNow completed")
            SUCCESS
        } catch (e: SecurityException) {
            Log.w(TAG, "lockNow denied", e)
            UNSUPPORTED
        } catch (e: UnsupportedOperationException) {
            Log.w(TAG, "lockNow unsupported", e)
            UNSUPPORTED
        } catch (e: RuntimeException) {
            Log.w(TAG, "lockNow failed", e)
            ERROR
        }
    }

    private fun reply(nodeId: String, path: String, payload: ByteArray) {
        try {
            Tasks.await(Wearable.getMessageClient(this).sendMessage(nodeId, path, payload), 5, TimeUnit.SECONDS)
            Log.i(TAG, "$path sent")
        } catch (e: Exception) {
            Log.w(TAG, "$path delivery failed", e)
        }
    }

    companion object {
        private const val TAG = "PhoneLockData"
        private const val PING = "/phone-lock/ping"
        private const val PONG = "/phone-lock/pong"
        private const val LOCK = "/phone-lock/lock"
        private const val LOCK_RESULT = "/phone-lock/lock-result"
        private const val REQUEST_ID_SIZE = 16
        private const val SUCCESS: Byte = 0
        private const val ADMIN_INACTIVE: Byte = 1
        private const val UNSUPPORTED: Byte = 2
        private const val NO_CREDENTIAL: Byte = 3
        private const val ERROR: Byte = 4
    }
}
