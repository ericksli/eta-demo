package net.swiftzer.etademo.domain

import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station
import javax.inject.Inject

class GetLinesAndStationsUseCaseImpl @Inject constructor(
    private val repository: EtaRepository,
) : GetLinesAndStationsUseCase {
    override fun invoke(): Map<Line, Set<Station>> = repository.getLinesAndStations()
}
