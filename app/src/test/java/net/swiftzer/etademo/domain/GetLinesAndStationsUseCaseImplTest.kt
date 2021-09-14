package net.swiftzer.etademo.domain

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

private val DUMMY_DATA = mapOf(
    Line.AEL to setOf(Station.HOK, Station.KOW),
    Line.TKL to setOf(Station.NOP, Station.YAT, Station.LHP),
)

class GetLinesAndStationsUseCaseImplTest {

    private lateinit var useCase: GetLinesAndStationsUseCase

    @MockK
    private lateinit var repository: EtaRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetLinesAndStationsUseCaseImpl(repository)
        every { repository.getLinesAndStations() } returns DUMMY_DATA

    }

    @Test
    fun invoke() {
        expectThat(useCase()).isEqualTo(DUMMY_DATA)
    }
}
