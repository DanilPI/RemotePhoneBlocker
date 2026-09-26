package ru.danilp1.remotephoneblocker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.compose.remote.creation.compose.action.pendingIntentAction
import androidx.compose.remote.creation.compose.capture.RemoteImageVector
import androidx.compose.remote.creation.compose.capture.vectorResource
import androidx.compose.remote.creation.compose.layout.RemoteAlignment
import androidx.compose.remote.creation.compose.layout.RemoteBox
import androidx.compose.remote.creation.compose.layout.RemoteComposable
import androidx.compose.remote.creation.compose.modifier.RemoteModifier
import androidx.compose.remote.creation.compose.modifier.fillMaxSize
import androidx.compose.remote.creation.compose.modifier.size
import androidx.compose.remote.creation.compose.state.rdp
import androidx.compose.remote.creation.compose.state.rc
import androidx.compose.remote.creation.compose.state.rs
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.glance.wear.AssociateWithGlanceWearWidget
import androidx.glance.wear.GlanceWearWidget
import androidx.glance.wear.GlanceWearWidgetService
import androidx.glance.wear.WearWidgetBrush
import androidx.glance.wear.WearWidgetData
import androidx.glance.wear.WearWidgetDocument
import androidx.glance.wear.color
import androidx.glance.wear.core.WearWidgetParams
import androidx.wear.compose.remote.material3.RemoteCircularProgressIndicator
import androidx.wear.compose.remote.material3.RemoteIcon
import androidx.wear.compose.remote.material3.RemoteIconButton

@AssociateWithGlanceWearWidget(PhoneLockWidget::class)
class PhoneLockWidgetService : GlanceWearWidgetService() {
    override val widget: GlanceWearWidget = PhoneLockWidget()
}

class PhoneLockWidget : GlanceWearWidget() {
    override suspend fun provideWidgetData(context: Context, params: WearWidgetParams): WearWidgetData {
        val status = WatchController.status(context)
        return WearWidgetDocument(background = WearWidgetBrush.color(Color(0xFF242831).rc)) {
            PhoneLockContent(status)
        }
    }
}

@SuppressLint("RestrictedApi")
@RemoteComposable
@Composable
private fun PhoneLockContent(status: WatchStatus) {
    RemoteBox(RemoteModifier.fillMaxSize(), contentAlignment = RemoteAlignment.Center) {
        RemoteIconButton(
            onClick = pendingIntentAction { context ->
                PendingIntent.getBroadcast(
                    context, 0, Intent(context, LockActionReceiver::class.java),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            },
            modifier = RemoteModifier.size(60.rdp),
        ) {
            if (status == WatchStatus.LOCKING) {
                RemoteCircularProgressIndicator(modifier = RemoteModifier.size(32.rdp))
            } else {
                val icon = when (status) {
                    WatchStatus.LOCKED -> R.drawable.ic_check
                    WatchStatus.ERROR -> R.drawable.ic_error
                    else -> R.drawable.ic_lock
                }
                val description = when (status) {
                    WatchStatus.LOCKED -> "Phone locked"
                    WatchStatus.ERROR -> "Lock failed. Tap to retry"
                    else -> "Lock phone"
                }
                RemoteIcon(RemoteImageVector.vectorResource(icon), description.rs,
                    modifier = RemoteModifier.size(32.rdp))
            }
        }
    }
}
