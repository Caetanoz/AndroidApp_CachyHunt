package pt.ipp.estg.cachyhunt.data.local

import android.app.Application
import android.content.Context

class InitDb : Application() {
    override fun onCreate() {
        super.onCreate()
        appDatabase = AppDatabase.getDatabase(this)
    }
    companion object {
        var appDatabase: AppDatabase? = null
    }
}