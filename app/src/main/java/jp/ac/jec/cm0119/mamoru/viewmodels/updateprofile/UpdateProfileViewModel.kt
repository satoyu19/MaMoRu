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
import jp.ac.jec.cm0119.mamoru.utils.set
import jp.ac.jec.cm0119.mamoru.utils.uistate.DatabaseState
import jp.ac.jec.cm0119.mamoru.utils.uistate.StorageState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateProfileViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepository,
    private val dataStoreRepo: DataStoreRepository
) : ViewModel() {

    var nameText = ObservableField<String>()
    var description = ObservableField<String>()
    var birthDay = ObservableField<String>()

    var readMyUser: Flow<User> = dataStoreRepo.readMyInfo
    private set

    var profileImageUrl: String? = null
    private set

    val currentUserUid = firebaseRepo.currentUser!!.uid

    private var _profileImageData = MutableStateFlow<StorageState?>(null)
    val profileImageData: StateFlow<StorageState?> = _profileImageData

    private var _updateMyState = MutableStateFlow<DatabaseState?>(null)
    val updateMyState: StateFlow<DatabaseState?> = _updateMyState

    fun setMyState(myState: User) {
        nameText.set(myState.name)
        description.set(myState.description)
        birthDay.set(myState.birthDay)
    }

    fun addImageToStorage(imageUrl: Uri) {
        firebaseRepo.addImageToFirebaseStorageProfile(imageUrl).onEach { response ->
            when (response) {
                is Response.Loading -> {
                    _profileImageData.set(StorageState(isLoading = true))
                }
                is Response.Success -> {
                    _profileImageData.set(StorageState(isSuccess = true,data = response.data))
                    profileImageUrl = response.data.toString()
                }
                is Response.Failure ->
                    _profileImageData.set(StorageState(isFailure = true, error = response.errorMessage))
            }
        }.launchIn(viewModelScope)
    }

    fun updateMyState() {

        val myState = User(
            name = nameText.get(),
            profileImage = profileImageUrl,
            description = description.get(),
            birthDay = birthDay.get()
        )
        firebaseRepo.updateMyInfo(myState).onEach { response ->
            when (response) {
                is Response.Loading ->
                    _updateMyState.set(DatabaseState(isLoading = true))
                is Response.Success ->
                    _updateMyState.set(DatabaseState(isSuccess = true, user = response.data))
                is Response.Failure ->
                    _updateMyState.set(DatabaseState(isFailure = true,error = response.errorMessage))
            }
        }.launchIn(viewModelScope)
    }

    fun readMyUser() {
        readMyUser = dataStoreRepo.readMyInfo
    }

    fun setProfileImage(imageUrl: String?) {
        profileImageUrl = imageUrl
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
            dataStoreRepo.renewalMyInfo(newMyState)
        }
    }
}


