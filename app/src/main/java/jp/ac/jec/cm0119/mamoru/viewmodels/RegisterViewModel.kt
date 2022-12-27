package jp.ac.jec.cm0119.mamoru.viewmodels

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.uistate.AuthState
import jp.ac.jec.cm0119.mamoru.utils.Response
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

    private val _user = MutableStateFlow(AuthState())
    val user: StateFlow<AuthState> = _user

    //Auth認証
    fun register() {
        firebaseRepo.register(mailAddress.get()!!, password.get()!!).onEach { response ->
                when (response) {
                    is Response.Loading -> {
                        _user.value = AuthState(isLoading = true)
                    }
                    is Response.Failure -> {
                        response.errorType?.let {   //FirebaseAuthでのエラーの場合
                            when (it) {
                                is FirebaseAuthWeakPasswordException ->  _user.value = AuthState(error = "パスワードを強力にしてください。", errorType = it)
                                is FirebaseAuthInvalidCredentialsException ->  _user.value = AuthState(error = "アドレスの形式が正しくありません。", errorType = it)
                                is FirebaseAuthUserCollisionException ->  _user.value = AuthState(error = "指定されたアドレスのアカウントが既に存在しています。" , errorType = it)
                            }
                        }
                        _user.value = AuthState(error = response.errorMessage ?: "")
                    }
                    is Response.Success -> {
                        _user.value = AuthState(isSuccess = true)
                    }
                }
            }.launchIn(viewModelScope)
    }
}