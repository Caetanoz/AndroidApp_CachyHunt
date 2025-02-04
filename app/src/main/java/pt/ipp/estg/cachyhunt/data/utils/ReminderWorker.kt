package pt.ipp.estg.cachyhunt.data.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import pt.ipp.estg.cachyhunt.data.utils.NotificationUtils

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val sharedPref = applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val lastAccess = sharedPref.getLong("last_access_time", 0L)
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastAccess > 360000) {
            NotificationUtils.showNotification(
                applicationContext,
                "We miss you!",
                "Come back and continue your adventure on CachyHunt!"
            )
        }
        return Result.success()
    }
}
