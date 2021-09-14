package net.swiftzer.etademo.domain

import net.swiftzer.etademo.common.Language
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station

interface EtaRepository {
    fun getLinesAndStations(): Map<Line, Set<Station>>
    suspend fun getEta(language: Language, line: Line, station: Station): EtaResult
}
