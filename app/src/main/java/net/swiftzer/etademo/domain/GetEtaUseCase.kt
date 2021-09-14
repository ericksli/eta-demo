package net.swiftzer.etademo.domain

import net.swiftzer.etademo.common.Language
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station

interface GetEtaUseCase {
    suspend operator fun invoke(
        language: Language,
        line: Line,
        station: Station,
        sortBy: SortBy,
    ): EtaResult

    enum class SortBy { DIRECTION, TIME }
}
