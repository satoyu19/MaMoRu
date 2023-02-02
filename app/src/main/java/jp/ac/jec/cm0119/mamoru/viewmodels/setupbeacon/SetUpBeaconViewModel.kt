package jp.ac.jec.cm0119.mamoru.viewmodels.setupbeacon

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import jp.ac.jec.cm0119.mamoru.R
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.MyApplication
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.models.BeaconInfo
import jp.ac.jec.cm0119.mamoru.utils.Constants
import jp.ac.jec.cm0119.mamoru.utils.Response
import jp.ac.jec.cm0119.mamoru.utils.set
import jp.ac.jec.cm0119.mamoru.utils.uistate.BeaconState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.altbeacon.beacon.*
import javax.inject.Inject

@HiltViewModel
class SetUpBeaconViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepository,
    application: Application
) : AndroidViewModel(application), MonitorNotifier, RangeNotifier {

    private var beaconManager = BeaconManager.getInstanceForApplication(getApplication())

    private var _beacons = MutableLiveData<MutableList<BeaconInfo>>()
    val beacons: LiveData<MutableList<BeaconInfo>> get() = _beacons

    private val _isBeacon = MutableStateFlow<BeaconState?>(null)
    val isBeacon: StateFlow<BeaconState?> = _isBeacon

    fun beaconSetup(intent: Intent) {
        if (!beaconManager.isAnyConsumerBound) {
            val channelId = "beacon"
            beaconManager = BeaconManager.getInstanceForApplication(getApplication())
            val builder = NotificationCompat.Builder(getApplication(), channelId)
                .setSmallIcon(R.mipmap.ic_launcher) //todo　アイコン変更
                .setContentTitle("Beacon検知中")
                .setContentText("領域監視を実行しています")

            //PendingIntentを作成
            val pendingIntent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.getActivity(
                        getApplication(),
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    null
                }

            pendingIntent?.let { intent ->
                builder.setContentIntent(intent)
            } //通知のクリック時の遷移

            beaconManager.apply {
                beaconParsers.add(BeaconParser().setBeaconLayout(Constants.IBEACON_FORMAT))
                foregroundBetweenScanPeriod = 10000     //フォアグラウンド10秒単位
                backgroundBetweenScanPeriod = 100000    //バックグラウンド1分単位
                backgroundScanPeriod = 1100

                addMonitorNotifier(this@SetUpBeaconViewModel)
                addRangeNotifier(this@SetUpBeaconViewModel)

                enableForegroundServiceScanning(builder.build(), 456)
                setEnableScheduledScanJobs(false)

            }
        }
    }

    fun startBeacon() {
        if (!beaconManager.isAnyConsumerBound) {
            beaconManager.startMonitoring(MyApplication.mRegion)
            beaconManager.startRangingBeacons(MyApplication.mRegion)
        }
    }

    fun stopBeacon() {
        if (beaconManager.isAnyConsumerBound) {
            beaconManager.stopMonitoring(MyApplication.mRegion)
            beaconManager.stopRangingBeacons(MyApplication.mRegion)
        }
    }

    private fun updateTimeActionDetected() {
        firebaseRepo.updateTimeActionDetected()
    }

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

    private fun sendNotificationBeaconToFamily(noticeMessage: String) {
        viewModelScope.launch(Dispatchers.IO) {
            firebaseRepo.sendNotificationBeaconToFamily(noticeMessage)
        }
    }

    //todo ビーコンの流れ、regionを変更したときのexit等の呼び出し挙動、MyApplication.updateIsExitBeacon()のタイミングが適切か確認する
    override fun didEnterRegion(region: Region?) {
        if (MyApplication.selectedBeaconId != null) {
            firebaseRepo.updateExitToMyBeacon(false)
            MyApplication.updateIsExitBeacon(false)
            val currentTime = System.currentTimeMillis()
            MyApplication.updateBeaconTime(currentTime)
        }
    }

    //todo 外出中にも通知表示
    override fun didExitRegion(region: Region?) {
        if (MyApplication.selectedBeaconId != null) {
            val currentTime = System.currentTimeMillis()
            firebaseRepo.updateExitToMyBeacon(true)
            MyApplication.updateIsExitBeacon(true)
            MyApplication.updateBeaconTime(currentTime)
        }
    }

    override fun didDetermineStateForRegion(state: Int, region: Region?) {}

    override fun didRangeBeaconsInRegion(
        beacons: MutableCollection<Beacon>?,
        region: Region?
    ) {
        var detectionBeacons = mutableListOf<BeaconInfo>()

        beacons?.forEach { beacon ->
            if (MyApplication.selectedBeaconId == beacon.id1.toString()) {   //監視対象ビーコンあり
                val currentTime = System.currentTimeMillis()
                if (MyApplication.isExitBeacon == true) {   //外出中 or ビーコン不良
                    MyApplication.beaconTime?.let {
                        val timeDifference = (currentTime - it).toInt()
                        var noticeText: String? = null
                        if (timeDifference == 30) { //30分経過
                            noticeText = "外出から${timeDifference}分経過しました。"
                            sendNotificationBeaconToFamily(noticeText)
                        }

                        if (timeDifference >= 60 && timeDifference % 60 == 0) { //一時間単位での経過
                            // TODO: 1 / 6　のようなint型で割り算した結果は0?小数点にならない？
                            val hour = (timeDifference / 60)
                            noticeText = "外出から${hour}時間経過しました。"
                            sendNotificationBeaconToFamily(noticeText)
                        }
                    }
                } else {
                    MyApplication.beaconTime?.let {
                        val timeDifference = (currentTime - it).toInt()
                        var noticeText: String? = null
                        if (timeDifference == 30) { //30分経過
                            noticeText = "${timeDifference}分行動を検知していません。"
                            sendNotificationBeaconToFamily(noticeText!!)
                        }

                        if (timeDifference >= 60 && timeDifference % 60 == 0) { //一時間単位での経過
                            val hour = timeDifference / 60
                            noticeText = "${hour}時間行動を検知していません。"
                            sendNotificationBeaconToFamily(noticeText!!)
                        }
                    }
                }
                val distance = MyApplication.comparisonBeaconDistance(beacon.distance)
                if (1 < distance) { //1m以上変化
                    updateTimeActionDetected()
                    MyApplication.updateBeaconTime(currentTime)
                }
            }
            val beaconInfo = BeaconInfo(
                uuid = beacon.id1.toString(),
                distance = beacon.distance.toString()
            )
            detectionBeacons.add(beaconInfo)
        }
        _beacons.value = detectionBeacons
    }
}