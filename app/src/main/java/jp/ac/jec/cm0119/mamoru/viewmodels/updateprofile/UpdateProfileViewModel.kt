package jp.ac.jec.cm0119.mamoru.viewmodels.updateprofile

import android.net.Uri
import androidx.databinding.ObservableField
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.datepicker.MaterialDatePicker
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.repository.DataStoreRepository
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.uistate.DatabaseState
import jp.ac.jec.cm0119.mamoru.utils.uistate.StorageState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateProfileViewModel @Inject constructor(private val firebaseRepo: FirebaseRepository, private val dataStoreRepo: DataStoreRepository): ViewModel() {

    var nameText = ObservableField<String>()
    var description = ObservableField<String>()
    var phoneNumber = ObservableField<String>()
    var birthDay = ObservableField<String>()

    val readMyUser: Flow<User> = dataStoreRepo.readMyState
    private var profileImageUrl: String? = null
    val currentUserUid = firebaseRepo.currentUser!!.uid

    private var _profileImageData = MutableStateFlow(StorageState())
    val profileImageData: StateFlow<StorageState> = _profileImageData

    private var _updateMyState = MutableStateFlow(DatabaseState())
    val updateMyState: StateFlow<DatabaseState> = _updateMyState

    fun setMyState(myState: User) {
        nameText.set(myState.name)
        description.set(myState.description)
        phoneNumber.set(myState.phoneNumber)
        birthDay.set(myState.birthDay)
    }

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

    fun updateMyState() {

        val myState = User(name = nameText.get(), profileImage = profileImageUrl, description = description.get(), birthDay = birthDay.get())
        firebaseRepo.updateMyState(myState).onEach { response ->
            when (response) {
                is Response.Loading ->
                    _updateMyState.value = DatabaseState(isLoading = true)
                is Response.Success ->
                    _updateMyState.value = DatabaseState(isSuccess = true, user = response.data)
                is Response.Failure ->
                    _updateMyState.value = DatabaseState(error = response.errorMessage)
            }
        }.launchIn(viewModelScope)
    }

    //カレンダー生成
    fun makeCalender(fragmentManager: FragmentManager) {

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("誕生日を選択")
            .build()

        datePicker.addOnPositiveButtonClickListener {
            birthDay.set(datePicker.headerText.toString())
        }

        datePicker.show(fragmentManager, "MATERIAL_DATE_PICKER")
    }

    fun renewalMyState(newMyState: User) {
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepo.renewalMyState(newMyState)
        }
    }
}