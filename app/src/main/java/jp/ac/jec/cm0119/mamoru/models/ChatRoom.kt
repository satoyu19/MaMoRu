package jp.ac.jec.cm0119.mamoru.models

data class ChatRoom(
    val roomUid: String? = null,
    val receiverUid: String? = null,
    val name: String? = null,
    val profileImage: String? = null,
    val lastMsg: String? = null,
    val newChatCount: Int? = null
)
