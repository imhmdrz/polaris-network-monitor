package ir.uni.polaris.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.uni.polaris.domain.NetworkTestsRepository
import ir.uni.polaris.model.TestDefinition
import ir.uni.polaris.model.TestResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TestsViewModel @Inject constructor(
    private val repo: NetworkTestsRepository
) : ViewModel() {

    val definitions: StateFlow<List<TestDefinition>> =
        repo.observeDefinitions().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val latestCache = mutableMapOf<Long, StateFlow<TestResult?>>()
    fun lastResult(defId: Long): StateFlow<TestResult?> =
        latestCache.getOrPut(defId) {
            repo.observeLatestResult(defId)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }

    fun save(def: TestDefinition, onSaved: (Long) -> Unit = {}) = viewModelScope.launch {
        val id = repo.upsertDefinition(def)
        onSaved(id)
    }
}
