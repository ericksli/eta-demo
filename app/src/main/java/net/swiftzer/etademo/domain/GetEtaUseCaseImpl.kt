package net.swiftzer.etademo.domain

import net.swiftzer.etademo.common.Language
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station
import javax.inject.Inject

class GetEtaUseCaseImpl @Inject constructor(
    private val repository: EtaRepository,
) : GetEtaUseCase {
    override suspend fun invoke(
        language: Language,
        line: Line,
        station: Station,
        sortBy: GetEtaUseCase.SortBy,
    ): EtaResult = when (val result = repository.getEta(language, line, station)) {
        is EtaResult.Success -> {
            val comparator: Comparator<EtaResult.Success.Eta> = when (sortBy) {
                GetEtaUseCase.SortBy.DIRECTION -> compareBy({ it.direction }, { it.sequence })
                GetEtaUseCase.SortBy.TIME -> compareBy({ it.time }, { it.sequence })
            }
            result.copy(schedule = result.schedule.sortedWith(comparator))
        }
        else -> result
    }
}
