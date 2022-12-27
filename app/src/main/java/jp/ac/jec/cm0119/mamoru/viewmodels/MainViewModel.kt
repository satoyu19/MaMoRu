package jp.ac.jec.cm0119.mamoru.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.uistate.AuthState
import jp.ac.jec.cm0119.mamoru.utils.uistate.DatabaseState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val firebaseRepo: FirebaseRepository): ViewModel() {

    private val _userState = MutableStateFlow(DatabaseState())
    val userState: StateFlow<DatabaseState> = _userState

    fun getUserData() {
        firebaseRepo.getUserData().onEach { response ->
            when (response) {
                is Response.Loading -> {
                    _userState.value = DatabaseState(isLoading = true)
                }
                is Response.Success -> {
                    _userState.value = DatabaseState(isSuccess = true)
                }
                is Response.Failure -> {
                    _userState.value = DatabaseState(isFailure = true)
                }
            }
        }.launchIn(viewModelScope)
    }
}