package net.swiftzer.etademo.domain

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DomainModule {

    @Binds
    fun bindGetEtaUseCase(impl: GetEtaUseCaseImpl): GetEtaUseCase

    @Binds
    fun bindGetLinesAndStationsUseCase(impl: GetLinesAndStationsUseCaseImpl): GetLinesAndStationsUseCase
}
