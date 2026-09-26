package ru.danilp1.remotephoneblocker

import android.app.Activity
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

/** Open through the debug-only activity alias; no launcher entry is installed. */
class SetupActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        status = TextView(this).apply { textSize = 18f }
        val button = Button(this).apply {
            text = "Enable Device Admin"
            setOnClickListener {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                    putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent())
                    putExtra(
                        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        "Allows your paired watch to lock this phone when you tap its widget."
                    )
                }
                startActivity(intent)
            }
        }
        layout.addView(status)
        layout.addView(button)
        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        val manager = getSystemService(DevicePolicyManager::class.java)
        status.text = when {
            !manager.isAdminActive(adminComponent()) ->
                "Enable Device Admin to allow the watch to lock this phone."
            getSystemService(KeyguardManager::class.java)?.isDeviceSecure != true ->
                "Device Admin is enabled. Set a PIN, pattern, or password to secure the phone."
            else -> "Device Admin is enabled. You can use the watch widget."
        }
    }

    private fun adminComponent() = ComponentName(this, PhoneAdminReceiver::class.java)
}
