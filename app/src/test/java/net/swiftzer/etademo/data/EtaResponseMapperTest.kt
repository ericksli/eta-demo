package net.swiftzer.etademo.data

import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runBlockingTest
import net.swiftzer.etademo.common.DEFAULT_TIMEZONE
import net.swiftzer.etademo.common.Mapper
import net.swiftzer.etademo.common.Station
import net.swiftzer.etademo.domain.EtaResult
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.get
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.time.Instant
import java.time.ZonedDateTime

class EtaResponseMapperTest {

    private lateinit var mapper: Mapper<HttpResponse, EtaResult>

    @Before
    fun setUp() {
        mapper = EtaResponseMapper()
    }

    @Test
    fun `internal server error`() = runBlockingTest {
        val response = mockHttpResponse(
            statusCode = HttpStatusCode.InternalServerError,
            etaResponse = EtaResponse(
                status = EtaResponse.STATUS_ERROR_OR_ALERT,
                message = "Error",
            ),
        )
        expectThat(mapper.map(response)).isA<EtaResult.InternalServerError>()
    }

    @Test
    fun `too many requests`() = runBlockingTest {
        val response = mockHttpResponse(
            statusCode = HttpStatusCode.TooManyRequests,
            etaResponse = EtaResponse(
                status = EtaResponse.STATUS_ERROR_OR_ALERT,
                message = "Error",
            ),
        )
        expectThat(mapper.map(response)).isA<EtaResult.TooManyRequests>()
    }

    @Test
    fun `other http status code`() = runBlockingTest {
        val response = mockHttpResponse(
            statusCode = HttpStatusCode.Forbidden,
            etaResponse = EtaResponse(
                status = EtaResponse.STATUS_ERROR_OR_ALERT,
                message = "Error",
            ),
        )
        expectThat(mapper.map(response)).isA<EtaResult.Error>().and {
            get(EtaResult.Error::e).isA<IllegalStateException>() and {
                get(IllegalStateException::message).isEqualTo("Unsupported HTTP status code 403 Forbidden")
            }
        }
    }

    @Test
    fun incident() = runBlockingTest {
        val response = mockHttpResponse(
            statusCode = HttpStatusCode.OK,
            etaResponse = EtaResponse(
                status = EtaResponse.STATUS_ERROR_OR_ALERT,
                message = "Special train service arrangements are now in place on this line.",
                url = "https://www.mtr.com.hk/alert/alert_title_wap.html",
            ),
        )
        expectThat(mapper.map(response)).isA<EtaResult.Incident>().and {
            get(EtaResult.Incident::message).isEqualTo("Special train service arrangements are now in place on this line.")
            get(EtaResult.Incident::url).isEqualTo("https://www.mtr.com.hk/alert/alert_title_wap.html")
        }
    }

    @Test
    fun delay() = runBlockingTest {
        val response = mockHttpResponse(
            statusCode = HttpStatusCode.OK,
            etaResponse = EtaResponse(
                status = EtaResponse.STATUS_NORMAL,
                message = "successful",
                isDelay = EtaResponse.IS_DELAY_TRUE,
                data = mapOf(
                    "TKL-TKO" to EtaResponse.Data(),
                ),
            ),
        )
        expectThat(mapper.map(response)).isA<EtaResult.Delay>()
    }

    @Test
    fun normal() = runBlockingTest {
        val response = mockHttpResponse(
            statusCode = HttpStatusCode.OK,
            etaResponse = EtaResponse(
                status = EtaResponse.STATUS_NORMAL,
                message = "successful",
                isDelay = EtaResponse.IS_DELAY_FALSE,
                data = mapOf(
                    "TKL-TKO" to EtaResponse.Data(
                        up = listOf(
                            EtaResponse.Eta(
                                plat = "1",
                                time = "2020-01-11 14:28:00",
                                dest = "POA",
                                seq = "1",
                            ),
                            EtaResponse.Eta(
                                plat = "1",
                                time = "2020-01-11 14:36:00",
                                dest = "LHP",
                                seq = "2",
                            ),
                        ),
                        down = listOf(
                            EtaResponse.Eta(
                                plat = "2",
                                time = "2020-01-11",
                                dest = "XXX",
                                seq = "",
                            ),
                        ),
                    ),
                ),
            ),
        )
        expectThat(mapper.map(response)).isA<EtaResult.Success>().and {
            get(EtaResult.Success::schedule).hasSize(3).and {
                get(0).and {
                    get(EtaResult.Success.Eta::direction).isEqualTo(EtaResult.Success.Eta.Direction.UP)
                    get(EtaResult.Success.Eta::platform).isEqualTo("1")
                    get(EtaResult.Success.Eta::time).isEqualTo(
                        ZonedDateTime.of(
                            2020, 1, 11, 14, 28, 0, 0,
                            DEFAULT_TIMEZONE
                        ).toInstant()
                    )
                    get(EtaResult.Success.Eta::destination).isEqualTo(Station.POA)
                    get(EtaResult.Success.Eta::sequence).isEqualTo(1)
                }
                get(1).and {
                    get(EtaResult.Success.Eta::direction).isEqualTo(EtaResult.Success.Eta.Direction.UP)
                    get(EtaResult.Success.Eta::platform).isEqualTo("1")
                    get(EtaResult.Success.Eta::time).isEqualTo(
                        ZonedDateTime.of(
                            2020, 1, 11, 14, 36, 0, 0,
                            DEFAULT_TIMEZONE
                        ).toInstant()
                    )
                    get(EtaResult.Success.Eta::destination).isEqualTo(Station.LHP)
                    get(EtaResult.Success.Eta::sequence).isEqualTo(2)
                }
                get(2).and {
                    get(EtaResult.Success.Eta::direction).isEqualTo(EtaResult.Success.Eta.Direction.DOWN)
                    get(EtaResult.Success.Eta::platform).isEqualTo("2")
                    get(EtaResult.Success.Eta::time).isEqualTo(Instant.EPOCH)
                    get(EtaResult.Success.Eta::destination).isEqualTo(Station.UNKNOWN)
                    get(EtaResult.Success.Eta::sequence).isEqualTo(0)
                }
            }
        }
    }

    private fun mockHttpResponse(
        statusCode: HttpStatusCode,
        etaResponse: EtaResponse
    ): HttpResponse {
        val response = mockk<HttpResponse>()
        val httpClientCall = mockk<HttpClientCall>()
        every { response.status } returns statusCode
        every { response.call } returns httpClientCall
        coEvery { httpClientCall.receive<EtaResponse>() } returns etaResponse
        return response
    }
}
