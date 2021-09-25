package net.swiftzer.etademo.presentation.eta

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.swiftzer.etademo.common.Language
import net.swiftzer.etademo.common.Line
import net.swiftzer.etademo.common.STATE_FLOW_STOP_TIMEOUT_MILLIS
import net.swiftzer.etademo.common.Station
import net.swiftzer.etademo.domain.EtaFailResult
import net.swiftzer.etademo.domain.EtaResult
import net.swiftzer.etademo.domain.GetEtaUseCase
import net.swiftzer.etademo.presentation.Loadable
import net.swiftzer.etademo.presentation.navArgs
import java.time.Clock
import javax.inject.Inject
import java.time.Duration as JavaDuration

private const val SORT_BY = "sort_by"

@HiltViewModel
class EtaViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val clock: Clock,
    private val getEta: GetEtaUseCase,
) : ViewModel() {
    private val args by navArgs<EtaFragmentArgs>(savedStateHandle)
    private val language = MutableStateFlow(Language.ENGLISH)
    private val sortedBy = savedStateHandle.getLiveData(SORT_BY, 0).asFlow()
        .map { GetEtaUseCase.SortBy.values()[it] }
    val line: StateFlow<Line> = MutableStateFlow(args.line)
    val station: StateFlow<Station> = MutableStateFlow(args.station)
    private val _navigateBack = Channel<Unit>(Channel.BUFFERED)
    val navigateBack: Flow<Unit> = _navigateBack.receiveAsFlow()
    private val triggerRefresh = Channel<Unit>(Channel.BUFFERED)
    private val etaResult: StateFlow<Loadable<EtaResult>> = combineTransform(
        language,
        line,
        station,
        sortedBy,
        triggerRefresh.receiveAsFlow(),
    ) { language, line, station, sortedBy, _ ->
        emit(Loadable.Loading)
        emit(Loadable.Loaded(getEta(language, line, station, sortedBy)))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT_MILLIS),
        initialValue = Loadable.Loading,
    )
    private val loadedEtaResult = etaResult.filterIsInstance<Loadable.Loaded<EtaResult>>()
        .map { it.value }
    val showLoading: StateFlow<Boolean> = etaResult
        .map { it == Loadable.Loading }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT_MILLIS),
            initialValue = true,
        )
    val showError = etaResult
        .map { it is Loadable.Loaded && it.value is EtaFailResult }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT_MILLIS),
            initialValue = false,
        )
    val showViewDetail = loadedEtaResult.map { it is EtaResult.Incident }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT_MILLIS),
        initialValue = false,
    )
    val showTryAgain = loadedEtaResult.map {
        when (it) {
            EtaResult.Delay,
            is EtaResult.Incident,
            is EtaResult.Success -> false
            is EtaResult.Error,
            EtaResult.InternalServerError,
            EtaResult.TooManyRequests -> true
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT_MILLIS),
        initialValue = false,
    )
    val errorResult = loadedEtaResult
        .filterIsInstance<EtaFailResult>()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT_MILLIS),
            initialValue = EtaResult.InternalServerError,
        )
    val showEtaList = etaResult
        .map { it is Loadable.Loaded && it.value is EtaResult.Success }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT_MILLIS),
            initialValue = false,
        )
    val etaList = loadedEtaResult
        .filterIsInstance<EtaResult.Success>()
        .map { it.schedule }
        .combine(sortedBy) { schedule, sortedBy ->
            sequence {
                var lastDirection: EtaResult.Success.Eta.Direction? = null
                schedule.forEach {
                    if (lastDirection != it.direction && sortedBy == GetEtaUseCase.SortBy.DIRECTION) {
                        yield(EtaListItem.Header(it.direction))
                    }
                    yield(
                        EtaListItem.Eta(
                            direction = it.direction,
                            destination = it.destination,
                            platform = it.platform,
                            minuteCountdown = JavaDuration.between(clock.instant(), it.time)
                                .toMinutes()
                                .toInt()
                        )
                    )
                    lastDirection = it.direction
                }
            }.toList()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT_MILLIS),
            initialValue = emptyList(),
        )
    private val _viewIncidentDetail = Channel<String>(Channel.BUFFERED)
    val viewIncidentDetail: Flow<String> = _viewIncidentDetail.receiveAsFlow()

    init {
        viewModelScope.launch {
            triggerRefresh.send(Unit)
        }
    }

    fun setLanguage(language: Language) {
        this.language.value = language
    }

    fun goBack() {
        viewModelScope.launch {
            _navigateBack.send(Unit)
        }
    }

    fun toggleSorting() {
        val values = GetEtaUseCase.SortBy.values()
        val oldSortByOrdinal: Int = savedStateHandle.get<Int?>(SORT_BY) ?: 0
        savedStateHandle[SORT_BY] = (oldSortByOrdinal + 1) % values.size
    }

    fun refresh() {
        viewModelScope.launch {
            triggerRefresh.send(Unit)
        }
    }

    fun viewIncidentDetail() {
        val result = etaResult.value
        if (result !is Loadable.Loaded) return
        if (result.value !is EtaResult.Incident) return
        viewModelScope.launch {
            _viewIncidentDetail.send(result.value.url)
        }
    }
}
