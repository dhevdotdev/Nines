package dev.dhev.nines

import android.app.Application
import dev.dhev.nines.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class NinesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@NinesApplication)
            modules(appModule)
        }
    }
}
