package ir.uni.polaris.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.uni.polaris.data.auth.AuthPrefs
import ir.uni.polaris.domain.AuthRepository
import ir.uni.polaris.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    private val prefs: AuthPrefs
) : ViewModel() {
    val token: StateFlow<String?> =
        prefs.tokenFlow.stateIn(viewModelScope, SharingStarted.Lazily, null)
    val user: StateFlow<AuthUser?> =
        prefs.userFlow.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        _state.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            val res = repo.login(username, password)
            res.onSuccess { user ->
                prefs.save(user)
                _state.value = AuthUiState()
                onSuccess()
            }.onFailure {
                _state.value = AuthUiState(error = it.message ?: "Login failed")
            }
        }
    }

    fun signup(username: String, password: String, onSuccess: () -> Unit) {
        _state.value = AuthUiState(isLoading = true)
        viewModelScope.launch {
            val res = repo.register(username, password)
            res.onSuccess { user ->
                prefs.save(user)
                _state.value = AuthUiState()
                onSuccess()
            }.onFailure {
                _state.value = AuthUiState(error = it.message ?: "Sign up failed")
            }
        }
    }
}