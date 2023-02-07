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


//@HiltViewModel
//class SetUpBeaconViewModel @Inject constructor(
//    private val firebaseRepo: FirebaseRepository,
//    application: Application
//) : AndroidViewModel(application), MonitorNotifier, RangeNotifier {
//
//    private var beaconManager = BeaconManager.getInstanceForApplication(getApplication())
//
//    private val _isBeacon = MutableStateFlow<BeaconState?>(null)
//    val isBeacon: StateFlow<BeaconState?> = _isBeacon
//
//    fun beaconSetup(intent: Intent) {
//        if (!beaconManager.isAnyConsumerBound) {
//            val channelId = "beacon"
//            beaconManager = BeaconManager.getInstanceForApplication(getApplication())
//            val builder = NotificationCompat.Builder(getApplication(), channelId)
//                .setSmallIcon(R.mipmap.ic_launcher) //todo　アイコン変更
//                .setContentTitle("Beacon検知中")
//                .setContentText("領域監視を実行しています")
//
//            //PendingIntentを作成
//            val pendingIntent =
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    PendingIntent.getActivity(
//                        getApplication(),
//                        0,
//                        intent,
//                        PendingIntent.FLAG_IMMUTABLE
//                    )
//                } else {
//                    null
//                }
//
//            pendingIntent?.let { intent ->
//                builder.setContentIntent(intent)
//            }
//
//            beaconManager.apply {
//                beaconParsers.add(BeaconParser().setBeaconLayout(Constants.IBEACON_FORMAT)) //iBeacon対応
//                foregroundBetweenScanPeriod = 10000     //フォアグラウンド10秒
//                backgroundBetweenScanPeriod = 30000    //バックグラウンド30秒
//                addMonitorNotifier(this@SetUpBeaconViewModel)
//                addRangeNotifier(this@SetUpBeaconViewModel)
//
//                enableForegroundServiceScanning(builder.build(), 456)
//                setEnableScheduledScanJobs(false)
//            }
//        }
//    }
//
//    fun startBeacon() {
//        beaconManager.startMonitoring(MyApplication.mRegion)
//        beaconManager.startRangingBeacons(MyApplication.mRegion)
//    }
//
//    private fun updateTimeActionDetected() {
//        firebaseRepo.updateTimeActionDetected()
//    }
//
//    fun updateMyBeacon(beaconFlg: Boolean, newBeaconId: String?, newBeaconMac: String?) {
//        firebaseRepo.updateMyBeacon(beaconFlg).onEach { response ->
//            when (response) {
//                is Response.Success -> _isBeacon.set(
//                    BeaconState(
//                        isSuccess = true,
//                        beaconId = newBeaconId,
//                        beaconMacAddress = newBeaconMac
//                    )
//                )
//                is Response.Failure -> _isBeacon.set(
//                    BeaconState(
//                        isSuccess = false,
//                        errorMessage = response.errorMessage
//                    )
//                )
//                else -> {}
//            }
//        }.launchIn(viewModelScope)
//    }
//
//    private fun sendNotificationBeaconToFamily(noticeMessage: String) {
//        viewModelScope.launch(Dispatchers.IO) {
//            firebaseRepo.sendNotificationBeaconToFamily(noticeMessage)
//        }
//    }
//
//    private fun refreshNotified(time: Int) {
//        if (time % 30 == 29 && MyApplication.isNotified) { //通知する一分前
//            MyApplication.updateIsNotified(false)
//        }
//    }
//
//    override fun didEnterRegion(region: Region?) {
//        Log.d("Test", "didEnterRegion: ${region?.id1}")
//        if (MyApplication.selectedBeaconId != null) {
//            firebaseRepo.updateExitToMyBeacon(false)
//            MyApplication.updateIsExitBeacon(false)
//            MyApplication.updateBeaconTime()
//            updateTimeActionDetected()
//        }
//    }
//
//    //todo 複数ビーコンがある場合は
//    override fun didExitRegion(region: Region?) {
//        Log.d("Test", "didExitRegion: ${region?.id1}")
//        if (MyApplication.selectedBeaconId != null) {
//            firebaseRepo.updateExitToMyBeacon(true)
//            MyApplication.updateIsExitBeacon(true)
//        }
//    }
//
//    override fun didDetermineStateForRegion(state: Int, region: Region?) {}
//
//    override fun didRangeBeaconsInRegion(
//        beacons: MutableCollection<Beacon>?,
//        region: Region?
//    ) {
//        var detectionBeacons = mutableListOf<BeaconInfo>()
//
//        beacons?.forEach { beacon ->
//            Log.d("Test", "didRangeBeaconsInRegion: ")
//            if (MyApplication.selectedBeaconId == beacon.id1.toString()) {   //監視対象ビーコンあり
//                val currentTimeMinutes = System.currentTimeMillis() / 1000 / 60
//
//                if (MyApplication.isExitBeacon == true) {   //外出中 or ビーコン不良
//                    MyApplication.beaconTime?.let {
//                        val timeDifference = (currentTimeMinutes - it).toInt()
//                        var noticeText: String? = null
//                        if (timeDifference == 29 && MyApplication.isNotified) {
//                            MyApplication.updateIsNotified(false)
//                        }
//                        if (timeDifference == 30) { //30分経過
//                            if (MyApplication.isNotified) {  //時間経過通知済み
//                                return@forEach
//                            } else {
//                                noticeText = "外出から${timeDifference}分経過しました。"
//                                sendNotificationBeaconToFamily(noticeText)
//                                MyApplication.updateIsNotified(true)
//                            }
//                        }
//                        refreshNotified(timeDifference) //1時間単位の1分前
//
//                        if (timeDifference >= 60 && timeDifference % 60 == 0) { //一時間単位での経過
//                            if (MyApplication.isNotified) {
//                                return@forEach
//                            } else {
//                                val hour = (timeDifference / 60)
//                                noticeText = "外出から${hour}時間経過しました。"
//                                sendNotificationBeaconToFamily(noticeText)
//                                MyApplication.updateIsNotified(true)
//                            }
//                        }
//                    }
//                } else {
//                    MyApplication.beaconTime?.let {
//                        val timeDifference = (currentTimeMinutes - it).toInt()
//                        var noticeText: String? = null
//                        if (timeDifference == 29 && MyApplication.isNotified) { //30分通知の一分前
//                            MyApplication.updateIsNotified(false)
//                        }
//                        if (timeDifference == 30) { //30分経過
//                            if (MyApplication.isNotified) {  //時間経過通知済み
//                                return@forEach
//                            } else {
//                                noticeText = "${timeDifference}分行動を検知していません。"
//                                sendNotificationBeaconToFamily(noticeText!!)
//                                MyApplication.updateIsNotified(true)
//                            }
//                        }
//
//                        refreshNotified(timeDifference) //1時間単位の1分前
//
//                        if (timeDifference >= 60 && timeDifference % 60 == 0) { //一時間単位での経過
//                            if (MyApplication.isNotified) {
//                                return@forEach
//                            } else {
//                                val hour = timeDifference / 60
//                                noticeText = "${hour}時間行動を検知していません。"
//                                sendNotificationBeaconToFamily(noticeText!!)
//                                MyApplication.updateIsNotified(true)
//                            }
//                        }
//                    }
//                }
//                val distance = MyApplication.comparisonBeaconDistance(beacon.distance)
//                if (1.0 < distance) { //1m以上変化
//                    updateTimeActionDetected()
//                    MyApplication.updateBeaconTime()
//                }
//            }
//            val beaconInfo = BeaconInfo(
//                uuid = beacon.id1.toString(),
//                distance = beacon.distance.toString(),
//                macAddress = beacon.bluetoothAddress
//            )
//            detectionBeacons.add(beaconInfo)
//        }
//        if (!MyApplication.isNotActive) {
//            MyApplication.updateBeacons(detectionBeacons)
//        }
//    }
//}