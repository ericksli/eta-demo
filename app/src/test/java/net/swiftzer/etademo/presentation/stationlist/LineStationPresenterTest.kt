package net.swiftzer.etademo.presentation.stationlist

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@RunWith(AndroidJUnit4::class)
class LineStationPresenterTest {

    private lateinit var presenter: LineStationPresenter

    @Before
    fun setUp() {
        presenter = LineStationPresenter(ApplicationProvider.getApplicationContext())
    }

    @Test
    @Config(qualifiers = "en-rUS")
    fun `mapLine english`() {
        expectThat(presenter.mapLine(Line.AEL)).isEqualTo("Airport Express")
    }

    @Test
    @Config(qualifiers = "fr-rFR")
    fun `mapLine french`() {
        expectThat(presenter.mapLine(Line.AEL)).isEqualTo("Airport Express")
    }

    @Test
    @Config(qualifiers = "zh-rTW")
    fun `mapLine chinese taiwan`() {
        expectThat(presenter.mapLine(Line.AEL)).isEqualTo("機場快綫")
    }

    @Test
    @Config(qualifiers = "zh-rHK")
    fun `mapLine chinese hong kong`() {
        expectThat(presenter.mapLine(Line.AEL)).isEqualTo("機場快綫")
    }

    @Test
    @Config(qualifiers = "en-rUK")
    fun `mapStation english`() {
        expectThat(presenter.mapStation(Station.QUB)).isEqualTo("Quarry Bay")
    }

    @Test
    @Config(qualifiers = "fr-rFR")
    fun `mapStation french`() {
        expectThat(presenter.mapStation(Station.QUB)).isEqualTo("Quarry Bay")
    }

    @Test
    @Config(qualifiers = "zh-rTW")
    fun `mapStation chinese taiwan`() {
        expectThat(presenter.mapStation(Station.QUB)).isEqualTo("鰂魚涌")
    }

    @Test
    @Config(qualifiers = "zh-rHK")
    fun `mapStation chinese hong kong`() {
        expectThat(presenter.mapStation(Station.QUB)).isEqualTo("鰂魚涌")
    }
}
