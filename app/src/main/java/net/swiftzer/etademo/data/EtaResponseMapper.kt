package net.swiftzer.etademo.data

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.swiftzer.etademo.common.DEFAULT_TIMEZONE
import net.swiftzer.etademo.common.Mapper
import net.swiftzer.etademo.common.Station
import net.swiftzer.etademo.domain.EtaResult
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject

private val TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

class EtaResponseMapper @Inject constructor() : Mapper<HttpResponse, EtaResult> {
    override suspend fun map(o: HttpResponse): EtaResult = when (o.status) {
        HttpStatusCode.OK -> mapResponse(o.receive())
        HttpStatusCode.TooManyRequests -> EtaResult.TooManyRequests
        HttpStatusCode.InternalServerError -> EtaResult.InternalServerError
        else -> EtaResult.Error(IllegalStateException("Unsupported HTTP status code ${o.status}"))
    }

    private fun mapResponse(response: EtaResponse): EtaResult = with(response) {
        when {
            status == EtaResponse.STATUS_ERROR_OR_ALERT -> EtaResult.Incident(
                message = message,
                url = url,
            )
            isDelay == EtaResponse.IS_DELAY_TRUE -> EtaResult.Delay
            else -> EtaResult.Success(
                schedule = sequence {
                    yieldAll(data.values.asSequence()
                        .flatMap { it.up }
                        .map { mapEta(EtaResult.Success.Eta.Direction.UP, it) })
                    yieldAll(data.values.asSequence()
                        .flatMap { it.down }
                        .map { mapEta(EtaResult.Success.Eta.Direction.DOWN, it) })
                }.toList()
            )
        }
    }

    private fun mapEta(direction: EtaResult.Success.Eta.Direction, eta: EtaResponse.Eta) =
        with(eta) {
            EtaResult.Success.Eta(
                direction = direction,
                platform = plat,
                time = try {
                    ZonedDateTime.of(
                        LocalDateTime.parse(time, TIMESTAMP_FORMATTER),
                        DEFAULT_TIMEZONE
                    ).toInstant()
                } catch (e: DateTimeParseException) {
                    Instant.EPOCH
                },
                destination = try {
                    Station.valueOf(dest)
                } catch (e: IllegalArgumentException) {
                    Station.UNKNOWN
                },
                sequence = seq.toIntOrNull() ?: 0,
            )
        }
}
