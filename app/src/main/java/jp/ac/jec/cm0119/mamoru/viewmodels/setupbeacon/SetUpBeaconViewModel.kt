package jp.ac.jec.cm0119.mamoru.viewmodels.setupbeacon

import android.app.Application
import android.content.Intent
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.MyApplication
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.models.BeaconInfo
import jp.ac.jec.cm0119.mamoru.Service.BeaconService
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.set
import jp.ac.jec.cm0119.mamoru.utils.uistate.BeaconState
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class SetUpBeaconViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepository,
    application: Application
) : AndroidViewModel(application) {

    private val beaconService = BeaconService(firebaseRepo, getApplication())

    private val _isBeacon = MutableStateFlow<BeaconState?>(null)
    val isBeacon: StateFlow<BeaconState?> = _isBeacon

    val beacons: LiveData<MutableList<BeaconInfo>> = MyApplication.beacons

    fun updateMyBeacon(beaconFlg: Boolean, newBeaconId: String?) {
        firebaseRepo.updateMyBeacon(beaconFlg).onEach { response ->
            when (response) {
                is Response.Success -> _isBeacon.set(
                    BeaconState(
                        isSuccess = true,
                        beaconId = newBeaconId
                    )
                )
                is Response.Failure -> _isBeacon.set(
                    BeaconState(
                        isSuccess = false,
                        errorMessage = response.errorMessage
                    )
                )
                else -> {}
            }
        }.launchIn(viewModelScope)
    }

    fun beaconSetup(intent: Intent) {
        beaconService.beaconSetup(intent)
    }

    fun startBeacon() {
        beaconService.startBeacon()
    }

}
