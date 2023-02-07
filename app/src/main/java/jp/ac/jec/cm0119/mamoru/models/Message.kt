package jp.ac.jec.cm0119.mamoru.models

data class Message (
    var message: String? = null,
    var senderId: String? = null,   //送り主？
    var imageUrl: String? = null,
    var read: Boolean = false,
    var timeStamp: Long = 0
)