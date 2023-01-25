package jp.ac.jec.cm0119.mamoru.viewmodels.setupbeacon

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import jp.ac.jec.cm0119.mamoru.R
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.MyApplication
import jp.ac.jec.cm0119.mamoru.repository.DataStoreRepository
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.models.BeaconInfo
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.utils.Constants
import org.altbeacon.beacon.*
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SetUpBeaconViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepository,
    private val dataStoreRepo: DataStoreRepository,
    application: Application
) : AndroidViewModel(application), MonitorNotifier, RangeNotifier {

    private var beaconManager = BeaconManager.getInstanceForApplication(getApplication())

    private var _beacons = MutableLiveData<MutableList<BeaconInfo>>()
    val beacons: LiveData<MutableList<BeaconInfo>>
        get() = _beacons

    fun beaconSetup(intent: Intent) {
        val channelId = "0"

        // TODO: 今のままだとフォアグラウンドで実行されていてもまたスタートする、されていたらそのままにしたい。
            if (!beaconManager.isAnyConsumerBound) {
                // TODO: applicationクラスにもあるが必要か？ 
                beaconManager = BeaconManager.getInstanceForApplication(getApplication())
                val builder = NotificationCompat.Builder(getApplication(), channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
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
                    foregroundBetweenScanPeriod = 5000
                    backgroundBetweenScanPeriod = 5000
                    backgroundScanPeriod = 1100

                    addMonitorNotifier(this@SetUpBeaconViewModel)
                    addRangeNotifier(this@SetUpBeaconViewModel)

                    enableForegroundServiceScanning(builder.build(), 456)
                    setEnableScheduledScanJobs(false)

                    startMonitoring(MyApplication.mRegion)
                    startRangingBeacons(MyApplication.mRegion)
                }
        }
    }

    override fun didEnterRegion(region: Region?) {}

    override fun didExitRegion(region: Region?) {
        Log.d("Test", "exit")
    }

    override fun didDetermineStateForRegion(state: Int, region: Region?) {}

    override fun didRangeBeaconsInRegion(
        beacons: MutableCollection<Beacon>?,
        region: Region?
    ) {
        var detectionBeacons = mutableListOf<BeaconInfo>()

        MyApplication.selectedBeaconId?.let {

        }
        beacons?.forEach { beacon ->
            if (beacon.id1.toString() == MyApplication.selectedBeaconId){
                Log.d("Test", "didRangeBeaconsInRegion: ${beacon.distance}")
                // TODO: firebaseに時間を更新するための処理をしたい
                val distance = MyApplication.comparisonBeaconDistance(beacon.distance)
                if (1 < distance) { //1m以上変化
                    //todo firebaseの時刻更新をする。
                    val date = Date()
                    var beaconObj = HashMap<String, Any>()
                    beaconObj["updateTime"] = date.time
                    updateTimeActionDetected(beaconObj)
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

    fun updateTimeActionDetected(beaconObj: HashMap<String, Any>) {
        firebaseRepo.updateTimeActionDetected(beaconObj)
    }
}