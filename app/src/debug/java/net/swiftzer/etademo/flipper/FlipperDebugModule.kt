package net.swiftzer.etademo.flipper

import android.content.Context
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.flipper.plugins.sharedpreferences.SharedPreferencesFlipperPlugin
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.swiftzer.etademo.data.FlipperInterceptor
import okhttp3.Interceptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FlipperDebugModule {

    @Provides
    fun provideInspectorFlipperPlugin(@ApplicationContext context: Context): InspectorFlipperPlugin =
        InspectorFlipperPlugin(context, DescriptorMapping.withDefaults())

    @Provides
    fun provideCrashReporterPlugin(): CrashReporterPlugin = CrashReporterPlugin.getInstance()

    @Provides
    fun provideDatabasesFlipperPlugin(@ApplicationContext context: Context): DatabasesFlipperPlugin =
        DatabasesFlipperPlugin(context)

    @Provides
    fun provideSharedPreferencesFlipperPlugin(@ApplicationContext context: Context): SharedPreferencesFlipperPlugin =
        SharedPreferencesFlipperPlugin(context)

    @Provides
    @Singleton
    fun provideNetworkFlipperPlugin(): NetworkFlipperPlugin = NetworkFlipperPlugin()

    @Provides
    @FlipperInterceptor
    fun provideFlipperInterceptor(networkFlipperPlugin: NetworkFlipperPlugin): Interceptor =
        FlipperOkhttpInterceptor(networkFlipperPlugin)
}
