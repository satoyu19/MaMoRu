package jp.ac.jec.cm0119.mamoru.models


data class NotificationModel(
    val to: String,
    val data: Data
)

data class Data(
    val title: String,
    val message: String
)