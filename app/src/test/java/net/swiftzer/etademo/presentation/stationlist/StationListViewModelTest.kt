package net.swiftzer.etademo.presentation.stationlist

import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import net.swiftzer.etademo.MainCoroutineScopeRule
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station
import net.swiftzer.etademo.domain.GetLinesAndStationsUseCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.get
import strikt.assertions.hasSize
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class StationListViewModelTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private lateinit var viewModel: StationListViewModel

    @MockK
    private lateinit var getLinesAndStations: GetLinesAndStationsUseCase

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        every { getLinesAndStations() } returns linkedMapOf(
            Line.TKL to linkedSetOf(Station.LHP, Station.TKO),
            Line.TCL to linkedSetOf(Station.TUC, Station.SUN, Station.TSY),
            Line.TML to linkedSetOf(Station.TUM, Station.SIH, Station.TIS),
        )
        viewModel = StationListViewModel(getLinesAndStations)
    }

    @Test
    fun `station list default state`() = coroutineScope.runBlockingTest {
        viewModel.list.test {
            expectThat(awaitItem()).hasSize(3).and {
                get(0).assertGroup(Line.TKL, false)
                get(1).assertGroup(Line.TCL, false)
                get(2).assertGroup(Line.TML, false)
            }
            expectNoEvents()
        }
    }

    @Test
    fun `station list expand line`() = coroutineScope.runBlockingTest {
        viewModel.list.test {
            expectThat(awaitItem()).hasSize(3)
            viewModel.toggleExpanded(Line.TCL)
            expectThat(awaitItem()).hasSize(6).and {
                get(0).assertGroup(Line.TKL, false)
                get(1).assertGroup(Line.TCL, true)
                get(2).assertChild(Line.TCL, Station.TUC)
                get(3).assertChild(Line.TCL, Station.SUN)
                get(4).assertChild(Line.TCL, Station.TSY)
                get(5).assertGroup(Line.TML, false)
            }
            expectNoEvents()
        }
    }

    @Test
    fun `station list collapse line`() = coroutineScope.runBlockingTest {
        viewModel.list.test {
            expectThat(awaitItem()).hasSize(3)
            viewModel.toggleExpanded(Line.TCL)
            expectThat(awaitItem()).hasSize(6)
            viewModel.toggleExpanded(Line.TCL)
            expectThat(awaitItem()).hasSize(3).and {
                get(0).assertGroup(Line.TKL, false)
                get(1).assertGroup(Line.TCL, false)
                get(2).assertGroup(Line.TML, false)
            }
            expectNoEvents()
        }
    }

    @Test
    fun `station list expand multiple lines`() = coroutineScope.runBlockingTest {
        viewModel.list.test {
            expectThat(awaitItem()).hasSize(3)
            viewModel.toggleExpanded(Line.TCL)
            expectThat(awaitItem()).hasSize(6)
            viewModel.toggleExpanded(Line.TKL)
            expectThat(awaitItem()).hasSize(8).and {
                get(0).assertGroup(Line.TKL, true)
                get(1).assertChild(Line.TKL, Station.LHP)
                get(2).assertChild(Line.TKL, Station.TKO)
                get(3).assertGroup(Line.TCL, true)
                get(4).assertChild(Line.TCL, Station.TUC)
                get(5).assertChild(Line.TCL, Station.SUN)
                get(6).assertChild(Line.TCL, Station.TSY)
                get(7).assertGroup(Line.TML, false)
            }
            expectNoEvents()
        }
    }

    @Test
    fun `station list expand unavailable line`() = coroutineScope.runBlockingTest {
        viewModel.list.test {
            expectThat(awaitItem()).hasSize(3)
            viewModel.toggleExpanded(Line.AEL)
            expectNoEvents()
        }
    }

    @Test
    fun `launch eta screen`() = coroutineScope.runBlockingTest {
        viewModel.launchEtaScreen.test {
            viewModel.onClickLineAndStation(Line.AEL, Station.AIR)
            expectThat(awaitItem()).isEqualTo(Line.AEL to Station.AIR)
            expectNoEvents()
        }
    }

    private fun Assertion.Builder<StationListItem>.assertGroup(line: Line, isExpanded: Boolean) =
        isA<StationListItem.Group>().and {
            get(StationListItem.Group::line).isEqualTo(line)
            get(StationListItem.Group::isExpanded).isEqualTo(isExpanded)
        }

    private fun Assertion.Builder<StationListItem>.assertChild(line: Line, station: Station) =
        isA<StationListItem.Child>().and {
            get(StationListItem.Child::line).isEqualTo(line)
            get(StationListItem.Child::station).isEqualTo(station)
        }
}
