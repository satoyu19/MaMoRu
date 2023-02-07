package jp.ac.jec.cm0119.mamoru

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import jp.ac.jec.cm0119.mamoru.models.BeaconInfo
import jp.ac.jec.cm0119.mamoru.utils.ListeningToActivityCallbacks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region
import java.util.UUID
import kotlin.math.log

@HiltAndroidApp
class MyApplication : Application() {

    companion object {

        var isNotified: Boolean = false
        var isNotActive: Boolean = true
        var mRegion = Region("beacon", null, null, null)
            private set
        var selectedBeaconId: String? = null
            private set
        private var beforeBeaconDistance = 0.0
        var beaconTime: Long? = null
            private set
        var isExitBeacon: Boolean? = null
            private set

        val applicationScope = CoroutineScope(SupervisorJob())

        private var _beacons = MutableLiveData<MutableList<BeaconInfo>>()
        val beacons: LiveData<MutableList<BeaconInfo>> get() = _beacons
//
        fun updateBeaconTime() {
            val currentTimeMinutes = System.currentTimeMillis() / 1000 / 60
            beaconTime = currentTimeMinutes
        }

        fun updateBeacons(beacons: MutableList<BeaconInfo>) {
            _beacons.value = beacons
        }

        fun updateIsExitBeacon(flg: Boolean) {
            isExitBeacon = flg
        }

        fun comparisonBeaconDistance(afterDistance: Double): Double {
            val max = beforeBeaconDistance.coerceAtLeast(afterDistance)
            val min = beforeBeaconDistance.coerceAtMost(afterDistance)
            beforeBeaconDistance = afterDistance
            return max - min
        }

        fun updateSelectedBeacon(beaconId: String?) {
            selectedBeaconId = beaconId
        }

        fun updateRegion() {
            mRegion = if (selectedBeaconId != null) {
                Region("beacon", Identifier.parse(selectedBeaconId), null, null)
            } else {
                Region("beacon", null, null, null)
            }
        }

        fun updateIsNotified(isNotified: Boolean) {
            this.isNotified = isNotified
        }

    }

    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        registerActivityLifecycleCallbacks(ListeningToActivityCallbacks())
        val beaconManager = BeaconManager.getInstanceForApplication(this)
        if (!beaconManager.isAnyConsumerBound) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "beacon"
                val channel = NotificationChannel(
                    channelId,
                    "Beacon service",
                    NotificationManager.IMPORTANCE_HIGH
                )
                val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }
    }
}