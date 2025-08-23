package ir.uni.polaris.ui.home

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import ir.uni.polaris.domain.MeasurementsRepository
import ir.uni.polaris.model.Measurement
import ir.uni.polaris.service.ForegroundMonitorService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: MeasurementsRepository,
    @ApplicationContext private val ctx: Context
) : ViewModel() {

    val history: StateFlow<List<Measurement>> =
        repo.measurements.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            history.collect { list ->
                _state.update { st ->
                    st.copy(measurementsCount = list.size)
                }
            }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            HomeIntent.RequestPermissionsIfNeeded -> requestPerms()
            HomeIntent.ToggleMonitor -> toggle()
        }
    }

    private fun toggle() {
        val newRunning = !_state.value.isRunning
        _state.update { it.copy(isRunning = newRunning) }
        if (newRunning) startService() else stopService()
    }

    private fun startService() {
        val i = Intent(ctx, ForegroundMonitorService::class.java)
        ContextCompat.startForegroundService(ctx, i)
    }

    private fun stopService() {
        ctx.stopService(Intent(ctx, ForegroundMonitorService::class.java))
    }

    private fun requestPerms() {
    }
}
