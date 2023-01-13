package jp.ac.jec.cm0119.mamoru.viewmodels

import android.net.Uri
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.uistate.StorageState
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.uistate.DatabaseState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SetupProfileViewModel @Inject constructor(private val firebaseRepo: FirebaseRepository) :
    ViewModel() {

    var nameText = ObservableField<String>()
    var description = ObservableField<String>()
    var phoneNumber = ObservableField<String>()
    var birthDay = ObservableField<String>()

    private var _profileImageData = MutableStateFlow(StorageState())
    val profileImageData: StateFlow<StorageState> = _profileImageData

    private var _userState = MutableStateFlow(DatabaseState())
    val userState: StateFlow<DatabaseState> = _userState

    //storageのプロフィールイメージにアクセスするUrl
    private var profileImageUrl: String? = null

    //登録するuser情報
    private var user: User? = null

    //firebaseStorageへのアップロード
    fun addImageToStorage(imageUrl: Uri) {
            firebaseRepo.addImageToFirebaseStorage(imageUrl).onEach { response ->
                when (response) {
                    is Response.Loading ->
                        _profileImageData.value = StorageState(isLoading = true)
                    is Response.Success -> {
                        _profileImageData.value = StorageState(data = response.data)
                        profileImageUrl = response.data.toString()
                    }
                    is Response.Failure ->
                        _profileImageData.value = StorageState(error = response.errorMessage)
                }
            }.launchIn(viewModelScope)
    }


    fun setMyState() {
        makeMyState()
            firebaseRepo.setMyStateToDatabase(user!!).onEach { response ->
                when (response) {
                    is Response.Loading ->
                        _userState.value = DatabaseState(isLoading = true)
                    is Response.Failure ->
                        _userState.value = DatabaseState(error = response.errorMessage!!)
                    is Response.Success -> {

                        _userState.value = DatabaseState(isSuccess = true)
                    }
                }
            }.launchIn(viewModelScope)
    }

    //カレンダー生成
    fun makeCalender(fragmentManager: FragmentManager) {
        // TODO: レイアウト調整
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("誕生日を選択")
            .build()

        datePicker.addOnPositiveButtonClickListener {
            birthDay.set(datePicker.headerText.toString())
        }

        datePicker.show(fragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun makeMyState() {
        user = User(
            name = nameText.get() ?: "",
            uid = firebaseRepo.currentUser!!.uid,
            mail = firebaseRepo.currentUser!!.email ?: "",
            phoneNumber = phoneNumber.get(),
            profileImage = profileImageUrl,
            description = description.get(),
            birthDay = birthDay.get(),
            beacon = false
        )
    }
}