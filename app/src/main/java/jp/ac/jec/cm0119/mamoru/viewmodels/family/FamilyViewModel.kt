package jp.ac.jec.cm0119.mamoru.viewmodels.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.set
import jp.ac.jec.cm0119.mamoru.utils.uistate.DatabaseState
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class FamilyViewModel @Inject constructor(private val firebaseRepo: FirebaseRepository) :
    ViewModel() {

    var authCurrentUser: FirebaseUser? = firebaseRepo.currentUser
        private set

    private var _myFamily = MutableStateFlow<DatabaseState?>(null)
    val myFamily: StateFlow<DatabaseState?> = _myFamily

    fun getMyFamily() {
        firebaseRepo.getMyFamily().onEach { response ->
            if (response is Response.Success) {
                _myFamily.set(DatabaseState(isSuccess = true, myFamily = response.data))
            }
            if (response is Response.Failure) {
                _myFamily.set(DatabaseState(isFailure = true, error = response.errorMessage))
            }
        }.launchIn(viewModelScope)
    }
}


