package net.swiftzer.etademo.presentation.stationlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.STATE_FLOW_STOP_TIMEOUT_MILLIS
import net.swiftzer.etademo.common.Station
import net.swiftzer.etademo.domain.GetLinesAndStationsUseCase
import javax.inject.Inject

@HiltViewModel
class StationListViewModel @Inject constructor(
    getLinesAndStations: GetLinesAndStationsUseCase,
) : ViewModel(), StationListAdapter.Callback {
    private val lineAndStations = flowOf(getLinesAndStations())
    private val expandedGroups = MutableStateFlow<Set<Line>>(emptySet())
    val list: StateFlow<List<StationListItem>> =
        combine(lineAndStations, expandedGroups) { lineAndStations, expandedGroups ->
            lineAndStations.flatMap { (line, stations) ->
                sequence {
                    val isExpanded = expandedGroups.contains(line)
                    yield(
                        StationListItem.Group(
                            line = line,
                            isExpanded = isExpanded,
                        )
                    )
                    if (isExpanded) {
                        yieldAll(stations.map { StationListItem.Child(line = line, station = it) })
                    }
                }.toList()
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT_MILLIS),
            initialValue = emptyList(),
        )
    private val _launchEtaScreen = Channel<Pair<Line, Station>>(Channel.BUFFERED)
    val launchEtaScreen = _launchEtaScreen.receiveAsFlow()

    override fun toggleExpanded(line: Line) {
        viewModelScope.launch {
            expandedGroups.update {
                val newSet = it.toHashSet()
                if (newSet.contains(line)) {
                    newSet.remove(line)
                } else {
                    newSet.add(line)
                }
                newSet
            }
        }
    }

    override fun onClickLineAndStation(line: Line, station: Station) {
        viewModelScope.launch {
            _launchEtaScreen.send(line to station)
        }
    }
}
