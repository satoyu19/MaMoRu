package jp.ac.jec.cm0119.mamoru.data

import jp.ac.jec.cm0119.mamoru.models.NotificationModel
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {

    @Headers(
        "Authorization: key=AAAA4TOTcmM:APA91bH0xHwFIUJSfxIH_K-7LdcwtB6rkO_OAFFmnwOZZ6jh17wN_O5PYXfhs2mY6JKQHE8lf-xiy0wtis_SDwspxE04-y2_ifNfklMAMTEQ06UIJfR5X88KdF4el65Z45KAdZWZU6qS"
    ,
        "Content-Type:application/json"
    )
    @POST("fcm/send")
    suspend fun sendNotification(@Body notificationModel: NotificationModel): Response<ResponseBody>
}