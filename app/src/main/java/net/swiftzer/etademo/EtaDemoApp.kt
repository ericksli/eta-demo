package net.swiftzer.etademo

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import net.swiftzer.etademo.flipper.FlipperHelper
import javax.inject.Inject

@HiltAndroidApp
class EtaDemoApp : Application() {

    @Inject
    lateinit var flipperHelper: FlipperHelper

    override fun onCreate() {
        super.onCreate()
        flipperHelper.init()
    }
}
