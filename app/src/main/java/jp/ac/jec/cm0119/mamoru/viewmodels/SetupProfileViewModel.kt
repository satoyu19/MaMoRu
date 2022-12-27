package jp.ac.jec.cm0119.mamoru.viewmodels

import android.net.Uri
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    val firebaseDatabase = firebaseRepo.firebaseDatabase

    private var _profileImageData = MutableStateFlow(StorageState())
    val profileImageData: StateFlow<StorageState> = _profileImageData

    private var _user = MutableStateFlow(DatabaseState())
    val user: StateFlow<DatabaseState> = _user

    private var _profileImageUrl: MutableLiveData<Uri>? = null
    val profileImageUrl: LiveData<Uri>? get() = _profileImageUrl

    // TODO: こいつらの呼び出し箇所の選定
    //firebaseStorageへのアップロード
    fun addImageToStorage() {
        _profileImageUrl?.value?.let { imageUri ->
            firebaseRepo.addImageToFirebaseStorage(imageUri).onEach { response ->
                when (response) {
                    is Response.Loading ->
                        _profileImageData.value = StorageState(isLoading = true)
                    is Response.Failure ->
                        _profileImageData.value = StorageState(error = response.errorMessage!!)
                    is Response.Success ->
                        /** Storageの画像Urlが入る様にしたい **/
                        _profileImageData.value = StorageState(data = response.data)
                }
            }.launchIn(viewModelScope)
        }
    }

    fun setUserState(user: User) {
        firebaseRepo.setUserToDatabase(user).onEach { response ->
            when (response) {
                is Response.Loading ->
                    _user.value = DatabaseState(isLoading = true)
                is Response.Failure ->
                    _user.value = DatabaseState(error = response.errorMessage!!)
                is Response.Success ->
                    _user.value = DatabaseState(isSuccess = true)
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

    fun upLoadProfile(imageUrl: Uri) {
        _profileImageUrl?.value = imageUrl
    }

}