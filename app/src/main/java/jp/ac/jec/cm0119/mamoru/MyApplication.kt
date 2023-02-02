package jp.ac.jec.cm0119.mamoru

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import jp.ac.jec.cm0119.mamoru.utils.ListeningToActivityCallbacks
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region

@HiltAndroidApp
class MyApplication : Application() {

    companion object {

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

        fun updateBeaconTime(time: Long) {
            beaconTime = time
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