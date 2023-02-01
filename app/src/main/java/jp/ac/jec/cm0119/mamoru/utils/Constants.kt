package jp.ac.jec.cm0119.mamoru.utils

object Constants {

    //firebase
    const val PROFILE_IMAGE = "profile"
    const val MESSAGE_IMAGE = "message"
    const val DATABASE_USERS = "users"
    const val DATABASE_FAMILY = "families"
    const val DATABASE_CHAT = "chats"
    const val DATABASE_CHAT_ROOMS = "chatRooms"
    const val DATABASE_NEW_CHATS = "newChats"
    const val DATABASE_READ_CHATS = "readChats"

    //dataStore
    const val PREFERENCES_NAME = "mamoru_preferences"
    const val PREFERENCES_MY_UID = "myUid"
    const val PREFERENCES_MY_NAME = "myName"
    const val PREFERENCES_MY_MAIL = "myMail"
    const val PREFERENCES_MY_PHONE_NUMBER = "myPhoneNumber"
    const val PREFERENCES_MY_PROFILE_IMAGE = "myProfileImage"
    const val PREFERENCES_MY_DESCRIPTION = "myDescription"
    const val PREFERENCES_MY_BIRTHDAY = "myBirthDay"
    const val PREFERENCES_MY_BEACON = "myBeacon"

    //message
    const val ITEM_SEND = 1   //送り
    const val ITEM_RECEIVE = 2    //受け
    const val ITEM_SEND_IMG = 3
    const val ITEM_RECEIVE_IMG = 4

    //beacon
    const val IBEACON_FORMAT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"

    //notification
    const val CHANNEL_ID = "Channel_id_default"
    const val CHANNEL_NAME = "Channel_name_default"

    //retrofit
    const val BASE_URL = "https://fcm.googleapis.com"

}