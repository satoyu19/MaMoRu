package jp.ac.jec.cm0119.mamoru.utils

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object Constants {

    //firebase
    const val PROFILE_IMAGE = "profile"
    const val DATABASE_CHILD1 = "users"
    const val DATABASE_CHILD_FAMILY = "family"

    //dataStore
    const val PREFERENCES_NAME = "mamoru_preferences"
    const val PREFERENCES_MY_UID = "myUid"
    const val PREFERENCES_MY_NAME = "myName"
    const val PREFERENCES_MY_MAIL = "myMail"
    const val PREFERENCES_MY_PHONENUMBER = "myPhoneNumber"
    const val PREFERENCES_MY_PROFILEIMAGE = "myProfileImage"
    const val PREFERENCES_MY_DESCRIPTION = "myDescription"
    const val PREFERENCES_MY_BIRTHDAY = "myBirthDay"
    const val PREFERENCES_MY_BEACON = "myBeacon"
}