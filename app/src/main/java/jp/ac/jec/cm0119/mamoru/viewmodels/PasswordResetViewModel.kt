package jp.ac.jec.cm0119.mamoru.viewmodels

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.uistate.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class PasswordResetViewModel @Inject constructor(private val firebaseRepo: FirebaseRepository): ViewModel() {

    var mailAddress = ObservableField<String>()

    private val _resetResult = MutableStateFlow(AuthState())
    val resetResult: StateFlow<AuthState> = _resetResult

    fun passwordReset() {
        mailAddress.get()?.let {
            firebaseRepo.passwordReset(it).onEach { response ->
                when (response) {
                    is Response.Loading -> {
                        _resetResult.value = AuthState(isLoading = true)
                    }
                    is Response.Failure -> {
                        _resetResult.value = AuthState(error = response.errorMessage, isFailure = true)
                    }
                    is Response.Success -> {
                        _resetResult.value = AuthState(isSuccess = true)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }
}