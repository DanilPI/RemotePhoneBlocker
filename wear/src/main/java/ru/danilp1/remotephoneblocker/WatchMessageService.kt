package ru.danilp1.remotephoneblocker

import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class WatchMessageService : WearableListenerService() {
    override fun onMessageReceived(event: MessageEvent) {
        when (event.path) {
            LOCK_RESULT -> WatchController.onLockResult(this, event.sourceNodeId, event.data)
        }
    }

    companion object {
        const val LOCK_RESULT = "/phone-lock/lock-result"
    }
}
