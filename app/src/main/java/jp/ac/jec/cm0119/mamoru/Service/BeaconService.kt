package jp.ac.jec.cm0119.mamoru.Service

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.applicationScope
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.beaconTime
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.comparisonBeaconDistance
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.isExitBeacon
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.isNotActive
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.isNotified
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.mRegion
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.selectedBeaconId
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.updateBeaconTime
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.updateBeacons
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.updateIsExitBeacon
import jp.ac.jec.cm0119.mamoru.MyApplication.Companion.updateIsNotified
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.models.BeaconInfo
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.altbeacon.beacon.*

class BeaconService(
    private val firebaseRepo: FirebaseRepository,
    private val application: Application,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : MonitorNotifier, RangeNotifier {

    private var beaconManager = BeaconManager.getInstanceForApplication(application)

    fun beaconSetup(intent: Intent) {
        if (!beaconManager.isAnyConsumerBound) {
            val channelId = "beacon"
            beaconManager = BeaconManager.getInstanceForApplication(application)
            val builder = NotificationCompat.Builder(application, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Beacon検知中")
                .setContentText("領域監視を実行しています")

            //PendingIntentを作成
            val pendingIntent =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.getActivity(
                        application,
                        0,
                        intent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                } else {
                    null
                }

            pendingIntent?.let {
                builder.setContentIntent(it)
            }

            beaconManager.apply {
                beaconParsers.add(BeaconParser().setBeaconLayout(Constants.IBEACON_FORMAT)) //iBeacon対応
                foregroundBetweenScanPeriod = 10000     //フォアグラウンド10秒
                backgroundBetweenScanPeriod = 30000    //バックグラウンド30秒
                addMonitorNotifier(this@BeaconService)
                addRangeNotifier(this@BeaconService)

                enableForegroundServiceScanning(builder.build(), 456)
                setEnableScheduledScanJobs(false)
            }
        }
    }

    fun startBeacon() {
        beaconManager.startMonitoring(mRegion)
        beaconManager.startRangingBeacons(mRegion)
    }

    private fun updateTimeActionDetected() {
        firebaseRepo.updateTimeActionDetected()
    }

    private fun sendNotificationBeaconToFamily(noticeMessage: String) {
        applicationScope.launch(ioDispatcher) {
            firebaseRepo.sendNotificationBeaconToFamily(noticeMessage)
        }
    }

    private fun refreshNotified(time: Int) {
        if (time % 30 == 29 && isNotified) { //通知する一分前
            updateIsNotified(false)
        }
    }

    override fun didEnterRegion(region: Region?) {
        Log.d("Test", "didEnterRegion: ")
        if (selectedBeaconId != null) {
            firebaseRepo.updateExitToMyBeacon(false)
            updateIsExitBeacon(false)
            updateBeaconTime()
            updateTimeActionDetected()
        }
    }

    override fun didExitRegion(region: Region?) {
        Log.d("Test", "didExitRegion")
        if (selectedBeaconId != null) {
            firebaseRepo.updateExitToMyBeacon(true)
            updateIsExitBeacon(true)
        }
    }

    override fun didDetermineStateForRegion(state: Int, region: Region?) {
    }

    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
        val detectionBeacons = mutableListOf<BeaconInfo>()
        Log.d("Test", "Beacons / ${beacons?.size}")
        beacons?.forEach { beacon ->
            if (selectedBeaconId == beacon.id1.toString()) {   //監視対象ビーコンあり
                val currentTimeMinutes = System.currentTimeMillis() / 1000 / 60

                if (isExitBeacon == true) {   //外出中 or ビーコン不良
                    beaconTime?.let {
                        val timeDifference = (currentTimeMinutes - it).toInt()
                        var noticeText: String?
                        if (timeDifference == 29 && isNotified) {
                            updateIsNotified(false)
                        }
                        if (timeDifference == 30) { //30分経過
                            if (isNotified) {  //時間経過通知済み
                                return@forEach
                            } else {
                                noticeText = "外出から${timeDifference}分経過しました。"
                                sendNotificationBeaconToFamily(noticeText)
                                updateIsNotified(true)
                            }
                        }
                        refreshNotified(timeDifference) //1時間単位の1分前

                        if (timeDifference >= 60 && timeDifference % 60 == 0) { //一時間単位での経過
                            if (isNotified) {
                                return@forEach
                            } else {
                                val hour = (timeDifference / 60)
                                noticeText = "外出から${hour}時間経過しました。"
                                sendNotificationBeaconToFamily(noticeText)
                                updateIsNotified(true)
                            }
                        }
                    }
                } else {
                    beaconTime?.let {
                        val timeDifference = (currentTimeMinutes - it).toInt()
                        var noticeText: String?
                        if (timeDifference == 29 && isNotified) { //30分通知の一分前
                            updateIsNotified(false)
                        }
                        if (timeDifference == 30) { //30分経過
                            if (isNotified) {  //時間経過通知済み
                                return@forEach
                            } else {
                                noticeText = "${timeDifference}分行動を検知していません。"
                                sendNotificationBeaconToFamily(noticeText!!)
                                updateIsNotified(true)
                            }
                        }

                        refreshNotified(timeDifference) //1時間単位の1分前

                        if (timeDifference >= 60 && timeDifference % 60 == 0) { //一時間単位での経過
                            if (isNotified) {
                                return@forEach
                            } else {
                                val hour = timeDifference / 60
                                noticeText = "${hour}時間行動を検知していません。"
                                sendNotificationBeaconToFamily(noticeText!!)
                                updateIsNotified(true)
                            }
                        }
                    }
                }
                val distance = comparisonBeaconDistance(beacon.distance)
                if (1.0 < distance) { //1m以上変化
                    updateTimeActionDetected()
                    updateBeaconTime()
                }
            }
            val beaconInfo = BeaconInfo(
                uuid = beacon.id1.toString(),
                distance = beacon.distance.toString()
            )
            detectionBeacons.add(beaconInfo)
        }
        if (!isNotActive) {
            updateBeacons(detectionBeacons)
        }
    }
}