package jp.ac.jec.cm0119.mamoru.viewmodels.auth

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
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
class LoginViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepository
) : ViewModel() {

    var mailAddress = ObservableField<String>()
    var password = ObservableField<String>()

    private val _verification = MutableStateFlow(AuthState())
    val verification: StateFlow<AuthState> = _verification

    var authCurrentUser:  FirebaseUser? = firebaseRepo.currentUser
        private set

    fun login() {
        Log.d("Test", "mailAddress/${mailAddress.get().toString()}, password/${password.get().toString()}")
        firebaseRepo.login(mailAddress.get().toString(), password.get().toString())
            .onEach { response ->
                when (response) {
                    is Response.Loading -> {
                        _verification.value = AuthState(isLoading = true)
                    }
                    is Response.Failure -> {
                        _verification.value = AuthState(error = response.errorMessage, isFailure = true)
                    }
                    is Response.Success -> {
                        _verification.value = AuthState(isSuccess = true)
                    }
                }
            }.launchIn(viewModelScope)
    }
}