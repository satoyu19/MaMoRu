package jp.ac.jec.cm0119.mamoru.viewmodels.auth

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.uistate.AuthState
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(private val firebaseRepo: FirebaseRepository) :
    ViewModel() {

    var mailAddress = ObservableField<String>()
    var password = ObservableField<String>()

    private val _user = MutableStateFlow<AuthState?>(null)
    val user: StateFlow<AuthState?> = _user

    //Auth認証
    fun register() {
        firebaseRepo.registerAuth(mailAddress.get()!!, password.get()!!).onEach { response ->
                when (response) {
                    is Response.Loading -> _user.set(AuthState(isLoading = true))
                    is Response.Failure -> _user.set(AuthState(error = response.errorMessage, isFailure = true))
                    is Response.Success -> _user.set(AuthState(isSuccess = true))
                }
            }.launchIn(viewModelScope)
    }
}