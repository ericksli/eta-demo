package net.swiftzer.etademo.presentation.eta

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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
import java.time.Instant
import javax.inject.Inject
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration
import java.time.Duration as JavaDuration
import kotlin.time.Duration as KotlinDuration

private const val SORT_BY = "sort_by"
private val AUTO_REFRESH_INTERVAL = KotlinDuration.seconds(10)

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
        .onEach { autoRefreshScope.cancel() }
    val line: StateFlow<Line> = MutableStateFlow(args.line)
    val station: StateFlow<Station> = MutableStateFlow(args.station)
    private val _navigateBack = Channel<Unit>(Channel.BUFFERED)
    val navigateBack: Flow<Unit> = _navigateBack.receiveAsFlow()
    private val triggerRefresh = Channel<Unit>(Channel.BUFFERED)
    private lateinit var _autoRefreshScope: CoroutineScope
    private val autoRefreshScope: CoroutineScope
        get() {
            if (!::_autoRefreshScope.isInitialized || !_autoRefreshScope.isActive) {
                _autoRefreshScope =
                    CoroutineScope(
                        viewModelScope.coroutineContext +
                                SupervisorJob(viewModelScope.coroutineContext.job) +
                                CoroutineName("auto-refresh")
                    )
            }
            return _autoRefreshScope
        }

    private val etaResult: StateFlow<TimedValue<Loadable<EtaResult>>> = triggerRefresh
        .consumeAsFlow()
        .flatMapLatest {
            flowOf(
                flowOf(TimedValue(value = Loadable.Loading, updatedAt = clock.instant())),
                combine(
                    language,
                    line,
                    station,
                    sortedBy,
                ) { language, line, station, sortedBy ->
                    TimedValue(
                        value = Loadable.Loaded(getEta(language, line, station, sortedBy)),
                        updatedAt = clock.instant(),
                    )
                }.onEach {
                    // schedule the next refresh after loading
                    autoRefreshScope.launch {
                        delay(AUTO_REFRESH_INTERVAL)
                        triggerRefresh.send(Unit)
                    }
                },
            ).flattenConcat()
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT_MILLIS),
            initialValue = TimedValue(value = Loadable.Loading, updatedAt = Instant.EPOCH),
        )
    private val loadedEtaResult = etaResult
        .map { it.value }
        .filterIsInstance<Loadable.Loaded<EtaResult>>()
        .map { it.value }
    val showLoading: StateFlow<Boolean> = etaResult
        .map { it.value == Loadable.Loading }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_FLOW_STOP_TIMEOUT_MILLIS),
            initialValue = true,
        )
    val showError = etaResult
        .map { it.value is Loadable.Loaded && it.value.value is EtaFailResult }
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
        .map { it.value is Loadable.Loaded && it.value.value is EtaResult.Success }
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
        autoRefreshScope.cancel()
        autoRefreshScope.launch {
            triggerRefresh.send(Unit)
        }
    }

    fun startAutoRefresh() {
        autoRefreshScope.launch {
            val delayDuration = JavaDuration.between(etaResult.value.updatedAt, clock.instant())
            if (delayDuration >= AUTO_REFRESH_INTERVAL.toJavaDuration()) {
                triggerRefresh.send(Unit)
            } else {
                // schedule the next refresh base on the previous loaded time
                delay(delayDuration.toKotlinDuration())
                triggerRefresh.send(Unit)
            }
        }
    }

    fun stopAutoRefresh() {
        autoRefreshScope.cancel()
    }

    fun viewIncidentDetail() {
        val result = etaResult.value.value
        if (result !is Loadable.Loaded) return
        if (result.value !is EtaResult.Incident) return
        viewModelScope.launch {
            _viewIncidentDetail.send(result.value.url)
        }
    }

    private data class TimedValue<out T>(
        val value: T,
        val updatedAt: Instant,
    )
}
