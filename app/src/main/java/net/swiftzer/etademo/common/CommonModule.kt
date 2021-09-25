package net.swiftzer.etademo.common

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {
    @Provides
    fun provideClock(): Clock = Clock.systemDefaultZone()
}
