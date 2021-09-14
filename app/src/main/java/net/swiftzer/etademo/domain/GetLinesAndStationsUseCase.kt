package net.swiftzer.etademo.domain

import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station

interface GetLinesAndStationsUseCase {
    operator fun invoke(): Map<Line, Set<Station>>
}
