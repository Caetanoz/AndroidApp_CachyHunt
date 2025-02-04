package pt.ipp.estg.cachyhunt

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import pt.ipp.estg.cachyhunt.data.utils.LocaleUtils
import pt.ipp.estg.cachyhunt.data.utils.NotificationUtils
import pt.ipp.estg.cachyhunt.data.utils.ReminderWorker
import pt.ipp.estg.cachyhunt.data.utils.SyncWorker
import pt.ipp.estg.cachyhunt.ui.navigation.AppNavigation
import pt.ipp.estg.cachyhunt.ui.sensors.BatteryReceiverManager
import pt.ipp.estg.cachyhunt.ui.sensors.LightSensorComponent
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        SyncWorker.scheduleSyncWorker(this)
        BatteryReceiverManager.register(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        setContent {
            AppNavigation()
        }

        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putLong("last_access_time", System.currentTimeMillis())
            apply()
        }

        NotificationUtils.createNotificationChannel(this)
        scheduleReminderWorker()
    }

    private fun scheduleReminderWorker() {
        val reminderRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(this).enqueue(reminderRequest)
    }



    override fun attachBaseContext(newBase: Context) {
        val language = LocaleUtils.getSavedLanguage(newBase)
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }
    override fun onDestroy() {
        super.onDestroy()

        BatteryReceiverManager.unregister(this)

    }
}