package net.swiftzer.etademo.data

import dagger.Binds
import dagger.BindsOptionalOf
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import net.swiftzer.etademo.domain.EtaRepository
import okhttp3.OkHttpClient
import java.util.*
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @BindsOptionalOf
    fun bindLogging(): HttpClientFeature<Logging.Config, Logging>

    @Binds
    fun bindEtaRepository(impl: EtaRepositoryImpl): EtaRepository

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .build()

        @Provides
        fun provideHttpClientEngine(okHttpClient: OkHttpClient): HttpClientEngine = OkHttp.create {
            preconfigured = okHttpClient
        }

        @Provides
        fun provideLogging(): HttpClientFeature<Logging.Config, Logging> = Logging.apply {
            prepare {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }

        @Provides
        @Singleton
        fun provideKtorHttpClient(
            engine: HttpClientEngine,
            logging: Optional<HttpClientFeature<Logging.Config, Logging>>,
        ): HttpClient = HttpClient(engine) {
            expectSuccess = true
            logging.ifPresent { install(it) }
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    coerceInputValues = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}
