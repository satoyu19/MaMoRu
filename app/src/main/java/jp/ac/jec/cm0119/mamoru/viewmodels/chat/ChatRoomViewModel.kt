package jp.ac.jec.cm0119.mamoru.viewmodels.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.database.FirebaseRecyclerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.models.ChatRoom
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.set
import jp.ac.jec.cm0119.mamoru.utils.uistate.DatabaseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(private val firebaseRepo: FirebaseRepository): ViewModel() {

    var options: FirebaseRecyclerOptions<ChatRoom>? = null

    private var _registerReceiverInfoFailure = MutableStateFlow<DatabaseState?>(null)
    val registerReceiverInfoFailure: StateFlow<DatabaseState?> = _registerReceiverInfoFailure

    fun setOptions() {
        options = firebaseRepo.getChatRoomOptions()
    }

    fun registerReceiverInfoToSenderRoom() {
        firebaseRepo.registerReceiverInfoToSenderRoom().onEach { response ->
            if (response is Response.Failure) {
                _registerReceiverInfoFailure.set(DatabaseState(isFailure = true, error = response.errorMessage))
            }
        }.launchIn(viewModelScope)
    }
}
