package net.swiftzer.etademo.domain

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import net.swiftzer.etademo.common.Language
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station
import org.junit.Before
import org.junit.Test
import strikt.api.expectThat
import strikt.assertions.get
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import java.time.Instant

class GetEtaUseCaseImplTest {

    private lateinit var useCase: GetEtaUseCase

    @MockK
    private lateinit var repository: EtaRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetEtaUseCaseImpl(repository)
    }

    @Test
    fun `success, sort by direction`() = runBlockingTest {
        coEvery {
            repository.getEta(
                Language.ENGLISH,
                Line.TCL,
                Station.OLY
            )
        } returns EtaResult.Success(
            schedule = listOf(
                EtaResult.Success.Eta(
                    direction = EtaResult.Success.Eta.Direction.DOWN,
                    platform = "1",
                    time = Instant.ofEpochSecond(1630700001),
                    destination = Station.HOK,
                    sequence = 2,
                ),
                EtaResult.Success.Eta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    platform = "1",
                    time = Instant.ofEpochSecond(1630700002),
                    destination = Station.TSY,
                    sequence = 2,
                ),
                EtaResult.Success.Eta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    platform = "1",
                    time = Instant.ofEpochSecond(1630700002),
                    destination = Station.TUC,
                    sequence = 1,
                ),
                EtaResult.Success.Eta(
                    direction = EtaResult.Success.Eta.Direction.DOWN,
                    platform = "1",
                    time = Instant.ofEpochSecond(1630700004),
                    destination = Station.KOW,
                    sequence = 1,
                ),
            ),
        )
        val result =
            useCase.invoke(Language.ENGLISH, Line.TCL, Station.OLY, GetEtaUseCase.SortBy.DIRECTION)
        expectThat(result).isA<EtaResult.Success>().get(EtaResult.Success::schedule).hasSize(4)
            .and {
                get(0).get(EtaResult.Success.Eta::destination).isEqualTo(Station.TUC)
                get(1).get(EtaResult.Success.Eta::destination).isEqualTo(Station.TSY)
                get(2).get(EtaResult.Success.Eta::destination).isEqualTo(Station.KOW)
                get(3).get(EtaResult.Success.Eta::destination).isEqualTo(Station.HOK)
            }
    }

    @Test
    fun `success, sort by time`() = runBlockingTest {
        coEvery {
            repository.getEta(
                Language.ENGLISH,
                Line.TCL,
                Station.OLY
            )
        } returns EtaResult.Success(
            schedule = listOf(
                EtaResult.Success.Eta(
                    direction = EtaResult.Success.Eta.Direction.DOWN,
                    platform = "1",
                    time = Instant.ofEpochSecond(1630700001),
                    destination = Station.HOK,
                    sequence = 2,
                ),
                EtaResult.Success.Eta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    platform = "1",
                    time = Instant.ofEpochSecond(1630700001),
                    destination = Station.TUC,
                    sequence = 1,
                ),
                EtaResult.Success.Eta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    platform = "1",
                    time = Instant.ofEpochSecond(1630700004),
                    destination = Station.TSY,
                    sequence = 2,
                ),
                EtaResult.Success.Eta(
                    direction = EtaResult.Success.Eta.Direction.DOWN,
                    platform = "1",
                    time = Instant.ofEpochSecond(1630700003),
                    destination = Station.KOW,
                    sequence = 1,
                ),
            ),
        )
        val result =
            useCase.invoke(Language.ENGLISH, Line.TCL, Station.OLY, GetEtaUseCase.SortBy.TIME)
        expectThat(result).isA<EtaResult.Success>().get(EtaResult.Success::schedule).hasSize(4)
            .and {
                get(0).get(EtaResult.Success.Eta::destination).isEqualTo(Station.TUC)
                get(1).get(EtaResult.Success.Eta::destination).isEqualTo(Station.HOK)
                get(2).get(EtaResult.Success.Eta::destination).isEqualTo(Station.KOW)
                get(3).get(EtaResult.Success.Eta::destination).isEqualTo(Station.TSY)
            }
    }

    @Test
    fun delay() = runBlockingTest {
        coEvery {
            repository.getEta(
                Language.ENGLISH,
                Line.TCL,
                Station.OLY
            )
        } returns EtaResult.Delay
        val result =
            useCase.invoke(Language.ENGLISH, Line.TCL, Station.OLY, GetEtaUseCase.SortBy.DIRECTION)
        expectThat(result).isA<EtaResult.Delay>()
    }

    @Test
    fun incident() = runBlockingTest {
        val incident = EtaResult.Incident("Out of order!", "http://example.com")
        coEvery {
            repository.getEta(
                Language.ENGLISH,
                Line.TCL,
                Station.OLY
            )
        } returns incident
        val result =
            useCase.invoke(Language.ENGLISH, Line.TCL, Station.OLY, GetEtaUseCase.SortBy.DIRECTION)
        expectThat(result).isEqualTo(incident)
    }

    @Test
    fun tooManyRequests() = runBlockingTest {
        coEvery {
            repository.getEta(
                Language.ENGLISH,
                Line.TCL,
                Station.OLY
            )
        } returns EtaResult.TooManyRequests
        val result =
            useCase.invoke(Language.ENGLISH, Line.TCL, Station.OLY, GetEtaUseCase.SortBy.DIRECTION)
        expectThat(result).isA<EtaResult.TooManyRequests>()
    }

    @Test
    fun internalServerError() = runBlockingTest {
        coEvery {
            repository.getEta(
                Language.ENGLISH,
                Line.TCL,
                Station.OLY
            )
        } returns EtaResult.InternalServerError
        val result =
            useCase.invoke(Language.ENGLISH, Line.TCL, Station.OLY, GetEtaUseCase.SortBy.DIRECTION)
        expectThat(result).isA<EtaResult.InternalServerError>()
    }

    @Test
    fun error() = runBlockingTest {
        val error = EtaResult.Error(RuntimeException("error"))
        coEvery {
            repository.getEta(
                Language.ENGLISH,
                Line.TCL,
                Station.OLY
            )
        } returns error
        val result =
            useCase.invoke(Language.ENGLISH, Line.TCL, Station.OLY, GetEtaUseCase.SortBy.DIRECTION)
        expectThat(result).isEqualTo(error)
    }
}
