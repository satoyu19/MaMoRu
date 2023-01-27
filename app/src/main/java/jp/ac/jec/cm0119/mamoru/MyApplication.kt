package jp.ac.jec.cm0119.mamoru

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import dagger.hilt.android.HiltAndroidApp
import org.altbeacon.beacon.BeaconManager
import org.altbeacon.beacon.Identifier
import org.altbeacon.beacon.Region

@HiltAndroidApp
class MyApplication : Application() {

    companion object {

        var mRegion = Region("beacon", null, null, null)
            private set

        var selectedBeaconId: String? = null
            private set
        private var beforeBeaconDistance = 0.0

        fun comparisonBeaconDistance(afterDistance: Double): Double {
            var max = beforeBeaconDistance.coerceAtLeast(afterDistance)
            var min = beforeBeaconDistance.coerceAtMost(afterDistance)
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
        // TODO: オフラインキャッシュについて
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)

        val beaconManager = BeaconManager.getInstanceForApplication(this)
        if (!beaconManager.isAnyConsumerBound) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = "0"
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