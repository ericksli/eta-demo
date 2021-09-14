package net.swiftzer.etademo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EtaDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
