package net.swiftzer.etademo.presentation.eta

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.DelayController
import kotlinx.coroutines.test.runBlockingTest
import net.swiftzer.etademo.MainCoroutineScopeRule
import net.swiftzer.etademo.common.DEFAULT_TIMEZONE
import net.swiftzer.etademo.common.Language
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station
import net.swiftzer.etademo.domain.EtaResult
import net.swiftzer.etademo.domain.GetEtaUseCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.extra.MutableClock
import strikt.api.Assertion
import strikt.api.expectThat
import strikt.assertions.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import kotlin.time.toJavaDuration
import kotlin.time.Duration as KotlinDuration

private val DEFAULT_LOCAL_DATE = LocalDate.of(2021, 9, 1)
private val DEFAULT_LOCAL_TIME = LocalTime.of(13, 0, 0)
private val DEFAULT_INSTANT =
    ZonedDateTime.of(DEFAULT_LOCAL_DATE, DEFAULT_LOCAL_TIME, DEFAULT_TIMEZONE).toInstant()

@RunWith(AndroidJUnit4::class)
class EtaViewModelTest {

    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @MockK
    private lateinit var getEtaUseCase: GetEtaUseCase

    private lateinit var clock: MutableClock

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        clock = MutableClock.of(DEFAULT_INSTANT, DEFAULT_TIMEZONE)
    }

    @Test
    fun line() = coroutineScope.runBlockingTest {
        val viewModel = EtaViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "line" to Line.TCL,
                    "station" to Station.TUC,
                )
            ),
            clock = clock,
            getEta = getEtaUseCase,
        )

        viewModel.line.test {
            expectThat(awaitItem()).isEqualTo(Line.TCL)
            expectNoEvents()
        }
    }

    @Test
    fun station() = coroutineScope.runBlockingTest {
        val viewModel = EtaViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "line" to Line.TCL,
                    "station" to Station.TUC,
                )
            ),
            clock = clock,
            getEta = getEtaUseCase,
        )

        viewModel.station.test {
            expectThat(awaitItem()).isEqualTo(Station.TUC)
            expectNoEvents()
        }
    }

    @Test
    fun navigateBack() = coroutineScope.runBlockingTest {
        val viewModel = EtaViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "line" to Line.TCL,
                    "station" to Station.TUC,
                )
            ),
            clock = clock,
            getEta = getEtaUseCase,
        )

        viewModel.navigateBack.test {
            viewModel.goBack()
            awaitEvent()
            expectNoEvents()
        }
    }

    //region viewIncidentDetail
    @Test
    fun `viewIncidentDetail delay`() = coroutineScope.runBlockingTest {
        coEvery {
            getEtaUseCase(
                Language.ENGLISH,
                Line.TCL,
                Station.TUC,
                GetEtaUseCase.SortBy.DIRECTION,
            )
        } returns EtaResult.Delay

        val viewModel = EtaViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "line" to Line.TCL,
                    "station" to Station.TUC,
                )
            ),
            clock = clock,
            getEta = getEtaUseCase,
        )

        viewModel.viewIncidentDetail.test {
            viewModel.startAutoRefresh()
            viewModel.viewIncidentDetail()
            expectNoEvents()
        }
    }

    @Test
    fun `viewIncidentDetail incident`() = coroutineScope.runBlockingTest {
        coEvery {
            getEtaUseCase(
                Language.ENGLISH,
                Line.TCL,
                Station.TUC,
                GetEtaUseCase.SortBy.DIRECTION,
            )
        } returns EtaResult.Incident("Message", "https://example.com")

        val viewModel = EtaViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "line" to Line.TCL,
                    "station" to Station.TUC,
                )
            ),
            clock = clock,
            getEta = getEtaUseCase,
        )

        viewModel.viewIncidentDetail.test {
            viewModel.startAutoRefresh()
            viewModel.viewIncidentDetail()
            expectThat(awaitItem()).isEqualTo("https://example.com")
            expectNoEvents()
        }
    }
    //endregion

    @Test
    fun showLoading() = coroutineScope.runBlockingTest {
        coEvery {
            getEtaUseCase(
                Language.ENGLISH,
                Line.TCL,
                Station.TUC,
                GetEtaUseCase.SortBy.DIRECTION,
            )
        } returns EtaResult.InternalServerError

        val viewModel = EtaViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "line" to Line.TCL,
                    "station" to Station.TUC,
                )
            ),
            clock = clock,
            getEta = getEtaUseCase,
        )

        viewModel.showLoading.test {
            viewModel.startAutoRefresh()
            expectThat(awaitItem()).isEqualTo(true)
            expectThat(awaitItem()).isEqualTo(false)
            expectNoEvents()
        }
    }

    @Test
    fun showFullScreenError() = coroutineScope.runBlockingTest {
        coEvery {
            getEtaUseCase(
                Language.ENGLISH,
                Line.TCL,
                Station.TUC,
                GetEtaUseCase.SortBy.DIRECTION,
            )
        }.returnsMany(
            EtaResult.InternalServerError,
            EtaResult.Success(),
            EtaResult.TooManyRequests,
            EtaResult.Delay,
        )

        val viewModel = EtaViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "line" to Line.TCL,
                    "station" to Station.TUC,
                )
            ),
            clock = clock,
            getEta = getEtaUseCase,
        )

        viewModel.showFullScreenError.test {
            viewModel.startAutoRefresh()
            expectThat(awaitItem()).isEqualTo(false)
            advanceTimeBy(AUTO_REFRESH_INTERVAL)
            expectThat(awaitItem()).isEqualTo(true)
            advanceTimeBy(AUTO_REFRESH_INTERVAL)
            expectThat(awaitItem()).isEqualTo(false)
            advanceTimeBy(AUTO_REFRESH_INTERVAL)
            expectThat(awaitItem()).isEqualTo(true)
            expectNoEvents()
        }
    }

    @Test
    fun showEtaList() = coroutineScope.runBlockingTest {
        coEvery {
            getEtaUseCase(
                Language.ENGLISH,
                Line.TCL,
                Station.TUC,
                GetEtaUseCase.SortBy.DIRECTION,
            )
        }.returnsMany(
            EtaResult.InternalServerError,
            EtaResult.Success(),
            EtaResult.TooManyRequests,
            EtaResult.Delay,
        )

        val viewModel = EtaViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "line" to Line.TCL,
                    "station" to Station.TUC,
                )
            ),
            clock = clock,
            getEta = getEtaUseCase,
        )

        viewModel.showEtaList.test {
            viewModel.startAutoRefresh()
            expectThat(awaitItem()).isEqualTo(false)
            advanceTimeBy(AUTO_REFRESH_INTERVAL)
            expectThat(awaitItem()).isEqualTo(true)
            advanceTimeBy(AUTO_REFRESH_INTERVAL)
            expectNoEvents()
            advanceTimeBy(AUTO_REFRESH_INTERVAL)
            expectThat(awaitItem()).isEqualTo(false)
            expectNoEvents()
        }
    }

    //region etaList
    @Test
    fun `etaList sorting`() = coroutineScope.runBlockingTest {
        coEvery {
            getEtaUseCase(
                Language.ENGLISH,
                Line.TML,
                Station.KSR,
                any(),
            )
        } returns EtaResult.Success(
            schedule = listOf(
                EtaResult.Success.Eta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    destination = Station.TUM,
                    platform = "1",
                    time = ZonedDateTime.of(
                        DEFAULT_LOCAL_DATE,
                        LocalTime.of(13, 1, 1),
                        DEFAULT_TIMEZONE
                    ).toInstant()
                ),
                EtaResult.Success.Eta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    destination = Station.SIH,
                    platform = "1",
                    time = ZonedDateTime.of(
                        DEFAULT_LOCAL_DATE,
                        LocalTime.of(13, 7, 59),
                        DEFAULT_TIMEZONE
                    ).toInstant()
                ),
                EtaResult.Success.Eta(
                    direction = EtaResult.Success.Eta.Direction.DOWN,
                    destination = Station.HUH,
                    platform = "2",
                    time = ZonedDateTime.of(
                        DEFAULT_LOCAL_DATE,
                        LocalTime.of(13, 2, 2),
                        DEFAULT_TIMEZONE
                    ).toInstant()
                ),
            ),
        )

        val viewModel = EtaViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "line" to Line.TML,
                    "station" to Station.KSR,
                )
            ),
            clock = clock,
            getEta = getEtaUseCase,
        )

        viewModel.etaList.test {
            expectThat(awaitItem()).isEmpty()
            viewModel.startAutoRefresh()
            expectThat(awaitItem()).hasSize(5).and {
                get(0).assertHeader(EtaResult.Success.Eta.Direction.UP)
                get(1).assertEta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    destination = Station.TUM,
                    platform = "1",
                    minuteCountdown = 1,
                )
                get(2).assertEta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    destination = Station.SIH,
                    platform = "1",
                    minuteCountdown = 7,
                )
                get(3).assertHeader(EtaResult.Success.Eta.Direction.DOWN)
                get(4).assertEta(
                    direction = EtaResult.Success.Eta.Direction.DOWN,
                    destination = Station.HUH,
                    platform = "2",
                    minuteCountdown = 2,
                )
            }
            viewModel.toggleSorting()
            expectThat(awaitItem()).hasSize(3).and {
                get(0).assertEta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    destination = Station.TUM,
                    platform = "1",
                    minuteCountdown = 1,
                )
                get(1).assertEta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    destination = Station.SIH,
                    platform = "1",
                    minuteCountdown = 7,
                )
                get(2).assertEta(
                    direction = EtaResult.Success.Eta.Direction.DOWN,
                    destination = Station.HUH,
                    platform = "2",
                    minuteCountdown = 2,
                )
            }
            viewModel.toggleSorting()
            expectThat(awaitItem()).hasSize(5).and {
                get(0).assertHeader(EtaResult.Success.Eta.Direction.UP)
                get(1).assertEta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    destination = Station.TUM,
                    platform = "1",
                    minuteCountdown = 1,
                )
                get(2).assertEta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    destination = Station.SIH,
                    platform = "1",
                    minuteCountdown = 7,
                )
                get(3).assertHeader(EtaResult.Success.Eta.Direction.DOWN)
                get(4).assertEta(
                    direction = EtaResult.Success.Eta.Direction.DOWN,
                    destination = Station.HUH,
                    platform = "2",
                    minuteCountdown = 2,
                )
            }
            expectNoEvents()
        }
        coVerify(exactly = 2) { getEtaUseCase(any(), any(), any(), GetEtaUseCase.SortBy.DIRECTION) }
        coVerify(exactly = 1) { getEtaUseCase(any(), any(), any(), GetEtaUseCase.SortBy.TIME) }
    }

    @Test
    fun `etaList auto refresh automatically after loaded`() = coroutineScope.runBlockingTest {
        coEvery {
            getEtaUseCase(
                Language.ENGLISH,
                Line.TML,
                Station.KSR,
                GetEtaUseCase.SortBy.DIRECTION,
            )
        }.returnsMany(
            EtaResult.Success(
                schedule = listOf(
                    EtaResult.Success.Eta(
                        direction = EtaResult.Success.Eta.Direction.UP,
                        destination = Station.TUM,
                        platform = "1",
                        time = ZonedDateTime.of(
                            DEFAULT_LOCAL_DATE,
                            LocalTime.of(13, 1, 1),
                            DEFAULT_TIMEZONE
                        ).toInstant()
                    ),
                ),
            ),
            EtaResult.Success(
                schedule = listOf(
                    EtaResult.Success.Eta(
                        direction = EtaResult.Success.Eta.Direction.DOWN,
                        destination = Station.TIS,
                        platform = "8",
                        time = ZonedDateTime.of(
                            DEFAULT_LOCAL_DATE,
                            LocalTime.of(13, 14, 0),
                            DEFAULT_TIMEZONE
                        ).toInstant()
                    ),
                ),
            )
        )

        val viewModel = EtaViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    "line" to Line.TML,
                    "station" to Station.KSR,
                )
            ),
            clock = clock,
            getEta = getEtaUseCase,
        )

        viewModel.etaList.test {
            viewModel.startAutoRefresh()
            expectThat(awaitItem()).isEmpty()
            expectThat(awaitItem()).hasSize(2).and {
                get(0).assertHeader(EtaResult.Success.Eta.Direction.UP)
                get(1).assertEta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    destination = Station.TUM,
                    platform = "1",
                    minuteCountdown = 1,
                )
            }
            advanceTimeBy(AUTO_REFRESH_INTERVAL)
            expectThat(awaitItem()).hasSize(2).and {
                get(0).assertHeader(EtaResult.Success.Eta.Direction.UP)
                get(1).assertEta(
                    direction = EtaResult.Success.Eta.Direction.UP,
                    destination = Station.TUM,
                    platform = "1",
                    minuteCountdown = 0,
                )
            }
            expectThat(awaitItem()).hasSize(2).and {
                get(0).assertHeader(EtaResult.Success.Eta.Direction.DOWN)
                get(1).assertEta(
                    direction = EtaResult.Success.Eta.Direction.DOWN,
                    destination = Station.TIS,
                    platform = "8",
                    minuteCountdown = 13,
                )
            }
            expectNoEvents()
        }
        coVerify(exactly = 2) {
            getEtaUseCase(
                Language.ENGLISH,
                Line.TML,
                Station.KSR,
                GetEtaUseCase.SortBy.DIRECTION,
            )
        }
    }

    @Test
    fun `etaList stop and resume auto refresh`() =
        coroutineScope.runBlockingTest {
            coEvery {
                getEtaUseCase(
                    Language.ENGLISH,
                    Line.TKL,
                    Station.QUB,
                    GetEtaUseCase.SortBy.DIRECTION,
                )
            }.returnsMany(
                EtaResult.Success(
                    schedule = listOf(
                        EtaResult.Success.Eta(
                            direction = EtaResult.Success.Eta.Direction.UP,
                            destination = Station.LHP,
                            platform = "1",
                            time = ZonedDateTime.of(
                                DEFAULT_LOCAL_DATE,
                                LocalTime.of(13, 20, 30),
                                DEFAULT_TIMEZONE
                            ).toInstant(),
                        ),
                    ),
                ),
                EtaResult.Success(
                    schedule = listOf(
                        EtaResult.Success.Eta(
                            direction = EtaResult.Success.Eta.Direction.UP,
                            destination = Station.LHP,
                            platform = "2",
                            time = ZonedDateTime.of(
                                DEFAULT_LOCAL_DATE,
                                LocalTime.of(13, 30, 0),
                                DEFAULT_TIMEZONE
                            ).toInstant(),
                        ),
                    ),
                ),
                EtaResult.Success(
                    schedule = listOf(
                        EtaResult.Success.Eta(
                            direction = EtaResult.Success.Eta.Direction.UP,
                            destination = Station.LHP,
                            platform = "3",
                            time = ZonedDateTime.of(
                                DEFAULT_LOCAL_DATE,
                                LocalTime.of(13, 30, 0),
                                DEFAULT_TIMEZONE
                            ).toInstant(),
                        ),
                    ),
                ),
            )

            val viewModel = EtaViewModel(
                savedStateHandle = SavedStateHandle(
                    mapOf(
                        "line" to Line.TKL,
                        "station" to Station.QUB,
                    )
                ),
                clock = clock,
                getEta = getEtaUseCase,
            )

            viewModel.etaList.test {
                viewModel.startAutoRefresh()
                expectThat(awaitItem()).isEmpty()
                expectThat(awaitItem()).hasSize(2).and {
                    get(0).assertHeader(EtaResult.Success.Eta.Direction.UP)
                    get(1).assertEta(
                        direction = EtaResult.Success.Eta.Direction.UP,
                        destination = Station.LHP,
                        platform = "1",
                        minuteCountdown = 20,
                    )
                }
                viewModel.stopAutoRefresh()
                advanceTimeBy(KotlinDuration.seconds(5))
                expectNoEvents()
                viewModel.startAutoRefresh()
                expectNoEvents()
                advanceTimeBy(KotlinDuration.seconds(5))
                expectThat(awaitItem()).hasSize(2).and {
                    get(0).assertHeader(EtaResult.Success.Eta.Direction.UP)
                    get(1).assertEta(
                        direction = EtaResult.Success.Eta.Direction.UP,
                        destination = Station.LHP,
                        platform = "2",
                        minuteCountdown = 29,
                    )
                }
                expectNoEvents()
                viewModel.stopAutoRefresh()
                advanceTimeBy(KotlinDuration.minutes(20))
                viewModel.startAutoRefresh()
                expectThat(awaitItem()).hasSize(2).and {
                    get(0).assertHeader(EtaResult.Success.Eta.Direction.UP)
                    get(1).assertEta(
                        direction = EtaResult.Success.Eta.Direction.UP,
                        destination = Station.LHP,
                        platform = "2",
                        minuteCountdown = 9,
                    )
                }
                expectThat(awaitItem()).hasSize(2).and {
                    get(0).assertHeader(EtaResult.Success.Eta.Direction.UP)
                    get(1).assertEta(
                        direction = EtaResult.Success.Eta.Direction.UP,
                        destination = Station.LHP,
                        platform = "3",
                        minuteCountdown = 9,
                    )
                }
                expectNoEvents()
            }
            coVerify(exactly = 3) {
                getEtaUseCase(
                    Language.ENGLISH,
                    Line.TKL,
                    Station.QUB,
                    GetEtaUseCase.SortBy.DIRECTION,
                )
            }
        }
    //endregion

    private fun DelayController.advanceTimeBy(amount: KotlinDuration) {
        clock.add(amount.toJavaDuration())
        advanceTimeBy(amount.inWholeMilliseconds)
    }

    //region custom assertions
    private fun Assertion.Builder<EtaListItem>.assertHeader(
        direction: EtaResult.Success.Eta.Direction,
    ) = isA<EtaListItem.Header>().and {
        get(EtaListItem.Header::direction).isEqualTo(direction)
    }

    private fun Assertion.Builder<EtaListItem>.assertEta(
        direction: EtaResult.Success.Eta.Direction,
        destination: Station,
        platform: String,
        minuteCountdown: Int,
    ) = isA<EtaListItem.Eta>().and {
        get(EtaListItem.Eta::direction).isEqualTo(direction)
        get(EtaListItem.Eta::destination).isEqualTo(destination)
        get(EtaListItem.Eta::platform).isEqualTo(platform)
        get(EtaListItem.Eta::minuteCountdown).isEqualTo(minuteCountdown)
    }
    //endregion
}
