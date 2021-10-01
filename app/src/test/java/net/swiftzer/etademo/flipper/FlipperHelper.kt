package net.swiftzer.etademo.flipper

import android.content.Context
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
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
        // no-op
    }
}
