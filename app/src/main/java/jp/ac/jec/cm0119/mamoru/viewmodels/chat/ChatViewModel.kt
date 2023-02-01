package jp.ac.jec.cm0119.mamoru.viewmodels.chat

import android.net.Uri
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.data.ApiInterface
import jp.ac.jec.cm0119.mamoru.models.Data
import jp.ac.jec.cm0119.mamoru.models.Message
import jp.ac.jec.cm0119.mamoru.models.NotificationModel
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.set
import jp.ac.jec.cm0119.mamoru.utils.uistate.DatabaseState
import jp.ac.jec.cm0119.mamoru.utils.uistate.MessagingState
import jp.ac.jec.cm0119.mamoru.utils.uistate.StorageState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(private val firebaseRepo: FirebaseRepository) :
    ViewModel() {

    var message = ObservableField<String>()
    var options: FirebaseRecyclerOptions<Message>? = null

    var authCurrentUser: FirebaseUser? = firebaseRepo.currentUser
        private set

    private var receiverUid: String? = null

    private var _sendMessage = MutableStateFlow<DatabaseState?>(null)
    val sendMessage: StateFlow<DatabaseState?> = _sendMessage

    private var _readReceiveMessageFailure = MutableLiveData<String>()
    val readReceiveMessageFailure: LiveData<String> get() = _readReceiveMessageFailure

    private var _imageMessage = MutableStateFlow<StorageState?>(null)
    val imageMessage: StateFlow<StorageState?> = _imageMessage

    fun setOptions() {
        options = receiverUid?.let { firebaseRepo.getMessageOptions(it) }
    }

    fun sendMessage(imageUri: Uri? = null) {
        receiverUid?.let { receiverUid ->
            firebaseRepo.sendMessage(receiverUid, message.get(), imageUri).onEach { response ->
                if (response is Response.Success) {
                    _sendMessage.set(DatabaseState(isSuccess = true, token = response.data))
                }
                if (response is Response.Failure) {
                    _sendMessage.set(DatabaseState(isFailure = true, error = response.errorMessage))
                }
            }.launchIn(viewModelScope)
        }
        message.set("")
    }

    fun receiveMessageToRead() {
        receiverUid?.let {
            firebaseRepo.newReceiveMessageToRead(it).onEach { response ->
                _readReceiveMessageFailure.value = response.errorMessage
            }.launchIn(viewModelScope)
        }
    }

    fun addImageToStorage(imageUrl: Uri) {
        firebaseRepo.addImageToFirebaseStorageMessage(imageUrl).onEach { response ->
            when (response) {
                is Response.Success -> _imageMessage.set(
                    StorageState(
                        isSuccess = true,
                        data = response.data
                    )
                )
                is Response.Failure -> _imageMessage.set(
                    StorageState(
                        isFailure = true,
                        error = response.errorMessage
                    )
                )
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun setReceiverUid(receiverUid: String) {
        this.receiverUid = receiverUid
    }

    fun sendNotification(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val notificationModel = NotificationModel(to = token, data = Data("MaMoRu", "メッセージを受信しました。"))
            firebaseRepo.sendNotification(notificationModel)
        }
    }

//    /**
//     * FirebaseMessaging
//     */
//
//    suspend fun sendNotification(notificationModel: NotificationModel) {
//        try {
//            api.sendNotification(notificationModel)
//        }catch (e: Throwable) {
//            Log.d("Mamoru", "sendNotification: ${e.message}")
//        }
//    }
}