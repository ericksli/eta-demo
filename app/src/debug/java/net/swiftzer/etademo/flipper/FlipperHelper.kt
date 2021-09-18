package net.swiftzer.etademo.flipper

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.core.FlipperClient
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import com.facebook.soloader.SoLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class FlipperHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val inspectorFlipperPlugin: InspectorFlipperPlugin,
    private val crashReporterPlugin: CrashReporterPlugin,
    private val databasesFlipperPlugin: DatabasesFlipperPlugin,
    private val sharedPreferencesFlipperPlugin: SharedPreferencesFlipperPlugin,
    private val networkFlipperPlugin: NetworkFlipperPlugin,
) {
    fun init() {
        SoLoader.init(context, false)
        if (!FlipperUtils.shouldEnableFlipper(context)) return
        val client: FlipperClient = AndroidFlipperClient.getInstance(context)
        client.addPlugin(inspectorFlipperPlugin)
        client.addPlugin(crashReporterPlugin)
        client.addPlugin(databasesFlipperPlugin)
        client.addPlugin(sharedPreferencesFlipperPlugin)
        client.addPlugin(networkFlipperPlugin)
        client.start()
    }
}
