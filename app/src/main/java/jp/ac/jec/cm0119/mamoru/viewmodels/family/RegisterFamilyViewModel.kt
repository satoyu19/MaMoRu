package jp.ac.jec.cm0119.mamoru.viewmodels.family

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.repository.DataStoreRepository
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.uistate.DatabaseState
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class RegisterFamilyViewModel @Inject constructor(private val firebaseRepo: FirebaseRepository, private val dataStoreRepo: DataStoreRepository) :
    ViewModel() {

    var userUid = ObservableField<String>()

    private val _searchUser = MutableStateFlow(DatabaseState())
    val searchUser: StateFlow<DatabaseState> = _searchUser

    private val _registerUser = MutableStateFlow(DatabaseState())
    val registerUser: StateFlow<DatabaseState> = _registerUser

    private var searchUserState: User? = null

    val readMyUser: Flow<User> = dataStoreRepo.readMyState
    private var myState: User? = null

    fun searchUser() {
        userUid.get()?.let {
            // TODO: it
            firebaseRepo.searchUser("J9xFBV6TTwQPt0GA9YS3kIqIFQf1").onEach { response ->
                when (response) {
                    is Response.Loading -> _searchUser.value = DatabaseState(isLoading = true)
                    is Response.Success -> _searchUser.value = DatabaseState(isSuccess = true, user = response.data)
                    is Response.Failure -> _searchUser.value = DatabaseState(isFailure = true, error = response.errorMessage)
                }
            }.launchIn(viewModelScope)
        }
    }

    fun registerFamily() {
        searchUserState?.let { searchUserState ->
            myState?.let { myState ->
                firebaseRepo.registerFamily(searchUserState, myState).onEach { response ->
                    when (response) {
                        is Response.Loading -> _registerUser.value = DatabaseState(isLoading = true)
                        is Response.Success -> _registerUser.value = DatabaseState(isSuccess = true)
                        is Response.Failure -> _registerUser.value = DatabaseState(isFailure = true, error = response.errorMessage)
                    }
                }.launchIn(viewModelScope)
            }
        }
    }

    fun setUserState(searchUserState: User) {
        this.searchUserState = searchUserState
    }

    fun setMyState(myState: User) {
        this.myState = myState
    }
}