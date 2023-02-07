package jp.ac.jec.cm0119.mamoru.Service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import jp.ac.jec.cm0119.mamoru.MyApplication
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.ui.MainActivity
import jp.ac.jec.cm0119.mamoru.utils.Constants.CHANNEL_ID
import jp.ac.jec.cm0119.mamoru.utils.Constants.CHANNEL_NAME

class PushNotificationService: FirebaseMessagingService() {

    companion object {
        const val requestCode = 0
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    //通知受信処理
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        sendNotification(message)
    }

    private fun sendNotification(remoteMessage: RemoteMessage) {
        // 受け取った後の遷移先の設定
        Log.d("Test", "sendNotification: ${MyApplication.isNotActive}")
        if (MyApplication.isNotActive) {
            val title = remoteMessage.data["title"]
            val message = remoteMessage.data["message"]

            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            // Android12でクラッシュするのでPendingIntent.FLAG_IMMUTABLEを使う
            val pendingIntent = PendingIntent.getActivity(this, requestCode, intent,
                PendingIntent.FLAG_MUTABLE)

            // channelIdを設定(変更可能)
            val channelId = CHANNEL_ID
            // チャネル名を設定(変更可能)
            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setShowWhen(true)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Android8以降設定が必要
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
                channel.setShowBadge(true)
                channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                notificationManager.createNotificationChannel(channel)
            }

            // 通知処理(PendingIntent.getActivityで指定したリクエストコードを通知に設定する)
            notificationManager.notify(requestCode, notificationBuilder.build())
        }
    }
}