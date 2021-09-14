package net.swiftzer.etademo.data

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import net.swiftzer.etademo.common.Language
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Line.*
import net.swiftzer.etademo.common.Mapper
import net.swiftzer.etademo.common.Station
import net.swiftzer.etademo.common.Station.*
import net.swiftzer.etademo.domain.EtaRepository
import net.swiftzer.etademo.domain.EtaResult
import javax.inject.Inject

class EtaRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
    private val etaResponseMapper: Mapper<HttpResponse, EtaResult>,
) : EtaRepository {
    override fun getLinesAndStations(): Map<Line, Set<Station>> = linkedMapOf(
        AEL to linkedSetOf(HOK, KOW, TSY, AIR, AWE),
        TCL to linkedSetOf(HOK, KOW, OLY, NAC, LAK, TSY, SUN, TUC),
        TML to linkedSetOf(
            WKS, MOS, HEO, TSH, SHM, CIO, STW, CKT, TAW, HIK, DIH, KAT, SUW, TKW,
            HOM, HUH, ETS, AUS, MEF, TWW, KSR, YUL, LOP, TIS, SIH, TUM,
        ),
        TKL to linkedSetOf(NOP, QUB, YAT, TIK, TKO, HAH, POA, LHP),
    )

    override suspend fun getEta(language: Language, line: Line, station: Station) = try {
        val response =
            httpClient.get<HttpResponse>("https://rt.data.gov.hk/v1/transport/mtr/getSchedule.php") {
                parameter("line", line.name)
                parameter("sta", station.name)
                parameter(
                    "lang", when (language) {
                        Language.CHINESE -> "TC"
                        Language.ENGLISH -> "EN"
                    }
                )
            }
        etaResponseMapper.map(response)
    } catch (e: Throwable) {
        EtaResult.Error(e)
    }
}
