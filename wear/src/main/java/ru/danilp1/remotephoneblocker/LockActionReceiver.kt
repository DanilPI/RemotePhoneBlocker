package ru.danilp1.remotephoneblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** The widget host invokes this through an immutable PendingIntent. */
class LockActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        WatchController.lock(context.applicationContext) { pending.finish() }
    }
}
