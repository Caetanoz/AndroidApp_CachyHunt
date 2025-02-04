package pt.ipp.estg.cachyhunt.ui.sensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.Toast
import android.net.Uri
import android.provider.Settings

object BatteryReceiverManager {

    private var batteryReceiver: BroadcastReceiver? = null

    fun register(context: Context) {
        if (batteryReceiver == null) {
            batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == Intent.ACTION_BATTERY_CHANGED) {
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        val batteryPct = level / scale.toFloat() * 100

                        if (batteryPct <= 15) {
                            Toast.makeText(
                                context,
                                "Battery is low ($batteryPct%). Switching to power-saving mode.",
                                Toast.LENGTH_SHORT
                            ).show()

                            handleLowBattery(context)
                        }
                    }
                }
            }
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(batteryReceiver, intentFilter)
        }
    }

    fun unregister(context: Context) {
        batteryReceiver?.let {
            context.unregisterReceiver(it)
            batteryReceiver = null
        }
    }

    private fun handleLowBattery(context: Context) {
        if (Settings.System.canWrite(context)) {
            val contentResolver = context.contentResolver
            Settings.System.putInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                50
            )

            android.content.ContentResolver.setMasterSyncAutomatically(false)

            Toast.makeText(context, "Low battery mode activated.", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            context.startActivity(intent)
        }
    }
}