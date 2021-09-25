package net.swiftzer.etademo.domain

import net.swiftzer.etademo.common.Station
import java.time.Instant

sealed interface EtaFailResult

sealed interface EtaResult {
    data class Success(
        val schedule: List<Eta> = emptyList(),
    ) : EtaResult {
        data class Eta(
            val direction: Direction = Direction.UP,
            val platform: String = "",
            val time: Instant = Instant.EPOCH,
            val destination: Station = Station.UNKNOWN,
            val sequence: Int = 0,
        ) {
            enum class Direction { UP, DOWN }
        }
    }

    object Delay : EtaResult, EtaFailResult

    data class Incident(
        val message: String = "",
        val url: String = "",
    ) : EtaResult, EtaFailResult

    object TooManyRequests : EtaResult, EtaFailResult

    object InternalServerError : EtaResult, EtaFailResult

    data class Error(val e: Throwable?) : EtaResult, EtaFailResult
}
