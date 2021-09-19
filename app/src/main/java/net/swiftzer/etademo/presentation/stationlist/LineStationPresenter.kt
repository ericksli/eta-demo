package net.swiftzer.etademo.presentation.stationlist

import android.content.Context
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import net.swiftzer.etademo.common.Language
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.Station
import net.swiftzer.etademo.presentation.appLanguage
import javax.inject.Inject

@ActivityScoped
class LineStationPresenter @Inject constructor(@ActivityContext context: Context) {
    private val language = context.resources.configuration.appLanguage

    fun mapLine(line: Line): String = when (language) {
        Language.CHINESE -> line.zh
        Language.ENGLISH -> line.en
    }

    fun mapStation(station: Station): String = when (language) {
        Language.CHINESE -> station.zh
        Language.ENGLISH -> station.en
    }
}
