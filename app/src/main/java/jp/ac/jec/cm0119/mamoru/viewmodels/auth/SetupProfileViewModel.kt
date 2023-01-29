package jp.ac.jec.cm0119.mamoru.viewmodels.auth

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
import jp.ac.jec.cm0119.mamoru.utils.set
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

    private var _profileImageData = MutableStateFlow<StorageState?>(null)
    val profileImageData: StateFlow<StorageState?> = _profileImageData

    private var _userState = MutableStateFlow<DatabaseState?>(null)
    val userState: StateFlow<DatabaseState?> = _userState

    //storageのプロフィールイメージにアクセスするUrl
    private var profileImageUrl: String? = null

    //登録するuser情報
    private var user: User? = null

    //firebaseStorageへのアップロード
    fun addImageToStorage(imageUrl: Uri) {
            firebaseRepo.addImageToFirebaseStorageProfile(imageUrl).onEach { response ->
                when (response) {
                    is Response.Success -> {
                        _profileImageData.set(StorageState(isSuccess = true, data = response.data))
                        profileImageUrl = response.data.toString()
                    }
                    is Response.Failure -> _profileImageData.set(StorageState(isFailure = true, error = response.errorMessage))
                    else -> {}
                }
            }.launchIn(viewModelScope)
    }


    fun setMyInfo() {
        makeMyState()
            firebaseRepo.setMyInfoToDatabase(user!!).onEach { response ->
                when (response) {
                    is Response.Loading ->
                        _userState.set(DatabaseState(isLoading = true))
                    is Response.Success ->
                        _userState.set(DatabaseState(isSuccess = true))
                    is Response.Failure ->
                        _userState.set(DatabaseState(isFailure = true, error = response.errorMessage))
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
            beacon = false,
            exitBeacon = false
        )
    }
}