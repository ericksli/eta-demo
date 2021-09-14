package net.swiftzer.etademo.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import net.swiftzer.etademo.common.Language
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Mapper
import net.swiftzer.etademo.common.Station
import net.swiftzer.etademo.domain.EtaResult
import org.junit.Before
import org.junit.Test
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.*
import strikt.mockk.withCaptured
import java.util.*

class EtaRepositoryImplTest {

    @MockK
    private lateinit var etaResponseMapper: Mapper<HttpResponse, EtaResult>

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun getStations() {
        val client = mockk<HttpClient>()
        val repository = EtaRepositoryImpl(client, etaResponseMapper)
        expectThat(repository.getLinesAndStations()).hasSize(4)
    }

    @Test
    fun `getEta normal`() {
        runBlocking {
            val (client, requestSlot) = mockHttpClient(
                HttpStatusCode.OK,
                "api/schedule_tkl_tko_normal.json"
            )
            val repository = EtaRepositoryImpl(client, etaResponseMapper)
            val responseSlot = slot<HttpResponse>()
            coEvery { etaResponseMapper.map(capture(responseSlot)) } returns EtaResult.Success()
            val result = repository.getEta(Language.ENGLISH, Line.TKL, Station.TKO)
            expectThat(requestSlot).assert("EN", "TKL", "TKO")
            expectThat(responseSlot.captured.receive<EtaResponse>()).assert(
                status = EtaResponse.STATUS_NORMAL,
                message = "successful",
                url = "",
                isDelay = EtaResponse.IS_DELAY_FALSE,
                dataKey = "TKL-TKO",
                upAssertionBlock = {
                    hasSize(4).and {
                        get(0).assert("1", "2020-01-11 14:28:00", "POA", "1")
                        get(1).assert("1", "2020-01-11 14:32:00", "POA", "2")
                        get(2).assert("1", "2020-01-11 14:36:00", "LHP", "3")
                        get(3).assert("1", "2020-01-11 14:38:00", "POA", "4")
                    }
                },
                downAssertionBlock = {
                    hasSize(4).and {
                        get(0).assert("2", "2020-01-11 14:26:00", "NOP", "1")
                        get(1).assert("2", "2020-01-11 14:29:00", "NOP", "2")
                        get(2).assert("2", "2020-01-11 14:35:00", "NOP", "3")
                        get(3).assert("2", "2020-01-11 14:37:00", "TIK", "4")
                    }
                }
            )
            expectThat(result).isA<EtaResult.Success>()
        }
    }

    @Test
    fun `getEta delay`() {
        runBlocking {
            val (client, requestSlot) = mockHttpClient(
                HttpStatusCode.OK,
                "api/schedule_tkl_tko_delay.json"
            )
            val repository = EtaRepositoryImpl(client, etaResponseMapper)
            val responseSlot = slot<HttpResponse>()
            coEvery { etaResponseMapper.map(capture(responseSlot)) } returns EtaResult.Incident()
            val result = repository.getEta(Language.CHINESE, Line.TML, Station.LOP)
            expectThat(requestSlot).assert("TC", "TML", "LOP")
            expectThat(responseSlot.captured.receive<EtaResponse>()).assert(
                status = EtaResponse.STATUS_NORMAL,
                message = "successful",
                url = "",
                isDelay = EtaResponse.IS_DELAY_TRUE,
                dataKey = "TKL-TKO",
                upAssertionBlock = { isEmpty() },
                downAssertionBlock = { isEmpty() }
            )
            expectThat(result).isA<EtaResult.Incident>()
        }
    }

    @Test
    fun `getEta incident`() {
        runBlocking {
            val (client, requestSlot) = mockHttpClient(
                HttpStatusCode.OK,
                "api/schedule_incident.json"
            )
            val repository = EtaRepositoryImpl(client, etaResponseMapper)
            val responseSlot = slot<HttpResponse>()
            coEvery { etaResponseMapper.map(capture(responseSlot)) } returns EtaResult.Success()
            val result = repository.getEta(Language.CHINESE, Line.TKL, Station.TKO)
            expectThat(requestSlot).assert("TC", "TKL", "TKO")
            expectThat(responseSlot.captured.receive<EtaResponse>()).assert(
                status = EtaResponse.STATUS_ERROR_OR_ALERT,
                message = "Special train service arrangements are now in place on this line. Please click here for more information.",
                url = "https://www.mtr.com.hk/alert/alert_title_wap.html",
                isDelay = EtaResponse.IS_DELAY_FALSE,
            )
            expectThat(result).isA<EtaResult.Success>()
        }
    }

    @Test
    fun `getEta throw exception`() {
        runBlocking {
            val (client, _) = mockHttpClient(
                HttpStatusCode.OK,
                "api/schedule_incident.json",
                RuntimeException("Something went wrong")
            )
            val repository = EtaRepositoryImpl(client, etaResponseMapper)
            val result = repository.getEta(Language.CHINESE, Line.TKL, Station.TKO)
            expectThat(result).isA<EtaResult.Error>().and {
                get(EtaResult.Error::e).isA<RuntimeException>()
            }
        }
    }

    private fun mockHttpClient(
        status: HttpStatusCode,
        resourceName: String,
        exception: Exception? = null,
    ): Pair<HttpClient, CapturingSlot<HttpRequestData>> {
        val requestBlock = mockk<(HttpRequestData) -> Unit>()
        val requestSlot = slot<HttpRequestData>()
        every { requestBlock(capture(requestSlot)) } just Runs
        val engine = MockEngine { request ->
            requestBlock(request)
            if (exception != null) throw exception
            respond(
                content = ByteReadChannel(
                    javaClass.classLoader?.getResourceAsStream(resourceName)
                        ?.readBytes() ?: ByteArray(0)
                ),
                status = status,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
        return DataModule.provideKtorHttpClient(
            engine = engine,
            logging = Optional.empty()
        ) to requestSlot
    }

    @JvmName("assert_CapturingSlot_HttpRequestData")
    private fun Assertion.Builder<CapturingSlot<HttpRequestData>>.assert(
        lang: String,
        line: String,
        sta: String,
    ): Assertion.Builder<CapturingSlot<HttpRequestData>> = withCaptured {
        get { url.parameters }.and {
            get(Parameters::names).containsExactlyInAnyOrder("lang", "line", "sta")
            get { get("lang") }.isEqualTo(lang)
            get { get("line") }.isEqualTo(line)
            get { get("sta") }.isEqualTo(sta)
        }
    }

    @JvmName("assert_EtaResponse")
    private fun Assertion.Builder<EtaResponse>.assert(
        status: Int,
        message: String,
        url: String,
        isDelay: String,
        dataKey: String? = null,
        upAssertionBlock: Assertion.Builder<List<EtaResponse.Eta>>.() -> Unit = {},
        downAssertionBlock: Assertion.Builder<List<EtaResponse.Eta>>.() -> Unit = {},
    ): Assertion.Builder<EtaResponse> = and {
        get(EtaResponse::status).isEqualTo(status)
        get(EtaResponse::message).isEqualTo(message)
        get(EtaResponse::url).isEqualTo(url)
        get(EtaResponse::isDelay).isEqualTo(isDelay)
        if (dataKey == null) {
            get(EtaResponse::data).isEmpty()
        } else {
            get(EtaResponse::data).hasSize(1).and {
                get(dataKey).isNotNull().and {
                    upAssertionBlock(get(EtaResponse.Data::up))
                    downAssertionBlock(get(EtaResponse.Data::down))
                }
            }
        }
    }

    @JvmName("assert_EtaResponse_Eta")
    private fun Assertion.Builder<EtaResponse.Eta>.assert(
        plat: String,
        time: String,
        dest: String,
        seq: String,
    ): Assertion.Builder<EtaResponse.Eta> = and {
        get(EtaResponse.Eta::plat).isEqualTo(plat)
        get(EtaResponse.Eta::time).isEqualTo(time)
        get(EtaResponse.Eta::dest).isEqualTo(dest)
        get(EtaResponse.Eta::seq).isEqualTo(seq)
    }
}
