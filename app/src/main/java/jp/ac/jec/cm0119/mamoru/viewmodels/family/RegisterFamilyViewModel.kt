package jp.ac.jec.cm0119.mamoru.viewmodels.family

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.set
import jp.ac.jec.cm0119.mamoru.utils.uistate.DatabaseState
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RegisterFamilyViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepository
) :
    ViewModel() {

    var userUid = ObservableField<String>()

    private val _searchUser = MutableStateFlow<DatabaseState?>(null)
    val searchUser: StateFlow<DatabaseState?> = _searchUser

    private val _registerUser = MutableStateFlow<DatabaseState?>(null)
    val registerUser: StateFlow<DatabaseState?> = _registerUser

    private var searchUserUid: String? = null

    fun searchUser() {
        userUid.get()?.let { userId ->
            // TODO: it
            firebaseRepo.searchUser("J1fv0WOXn2UDEtx0lr4fFvPgY0H3").onEach { response ->
                when (response) {
                    is Response.Loading -> _searchUser.set(DatabaseState(isLoading = true))
                    is Response.Success -> _searchUser.set(DatabaseState(isSuccess = true, user = response.data))
                    is Response.Failure -> _searchUser.set(DatabaseState(isFailure = true, error = response.errorMessage))
                }
            }.launchIn(viewModelScope)
        }
    }

    fun addUserToFamily() {
        searchUserUid?.let { searchUserUid ->
            firebaseRepo.addUserToFamily(searchUserUid).onEach { response ->
                when (response) {
                    is Response.Loading -> _registerUser.set(DatabaseState(isLoading = true))
                    is Response.Success -> _registerUser.set(DatabaseState(isSuccess = true))
                    is Response.Failure -> _registerUser.set(DatabaseState(isFailure = true, error = response.errorMessage))
                }
            }.launchIn(viewModelScope)
        }
    }

    fun setUserState(searchUserState: User) {
        searchUserUid = searchUserState.uid
    }
}