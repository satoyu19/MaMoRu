package jp.ac.jec.cm0119.mamoru.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.repository.DataStoreRepository
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.uistate.DatabaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepository,
    private val dataStoreRepo: DataStoreRepository
) : ViewModel() {

    private val _userState = MutableStateFlow(DatabaseState())
    val userState: StateFlow<DatabaseState> = _userState

    private var _allNewChatCount = MutableStateFlow(DatabaseState())
    val allNewChatCount: StateFlow<DatabaseState> = _allNewChatCount

    fun getUserData() {
        firebaseRepo.getUserData().onEach { response ->
            when (response) {
                is Response.Loading -> {
                    _userState.value = DatabaseState(isLoading = true)
                }
                is Response.Success -> {
                    _userState.value = DatabaseState(isSuccess = true, user = response.data)
                }
                is Response.Failure -> {
                    _userState.value = DatabaseState(isFailure = true)
                }
            }
        }.launchIn(viewModelScope)
    }

    fun getAllNewChatCount() {
        if (firebaseRepo.currentUser != null) {
            firebaseRepo.getAllNewChatCount().onEach { response ->
                if (response is Response.Success) {
                    _allNewChatCount.value =
                        DatabaseState(isSuccess = true, allNewChatCount = response.data)
                }
                if (response is Response.Failure) {
                    _allNewChatCount.value =
                        DatabaseState(isFailure = true, error = response.errorMessage)
                }
            }.launchIn(viewModelScope)
        }
    }

    fun saveMyInfo(myInfo: User) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepo.saveMyInfo(myInfo)
        }
    }
}