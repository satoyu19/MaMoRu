package jp.ac.jec.cm0119.mamoru.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.repository.DataStoreRepository.PreferenceKeys.dataStore
import jp.ac.jec.cm0119.mamoru.utils.Constants.PREFERENCES_MY_BEACON
import jp.ac.jec.cm0119.mamoru.utils.Constants.PREFERENCES_MY_BIRTHDAY
import jp.ac.jec.cm0119.mamoru.utils.Constants.PREFERENCES_MY_DESCRIPTION
import jp.ac.jec.cm0119.mamoru.utils.Constants.PREFERENCES_MY_MAIL
import jp.ac.jec.cm0119.mamoru.utils.Constants.PREFERENCES_MY_NAME
import jp.ac.jec.cm0119.mamoru.utils.Constants.PREFERENCES_MY_PHONE_NUMBER
import jp.ac.jec.cm0119.mamoru.utils.Constants.PREFERENCES_MY_PROFILE_IMAGE
import jp.ac.jec.cm0119.mamoru.utils.Constants.PREFERENCES_MY_UID
import jp.ac.jec.cm0119.mamoru.utils.Constants.PREFERENCES_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

@ActivityRetainedScoped
class DataStoreRepository @Inject constructor(@ApplicationContext private val context: Context) {

    // TODO: 項目の精査
    private object PreferenceKeys {
        val myUid = stringPreferencesKey(PREFERENCES_MY_UID)
        val myName = stringPreferencesKey(PREFERENCES_MY_NAME)
        val myMail = stringPreferencesKey(PREFERENCES_MY_MAIL)
        val myPhoneNumber = stringPreferencesKey(PREFERENCES_MY_PHONE_NUMBER)
        val myProfileImage = stringPreferencesKey(PREFERENCES_MY_PROFILE_IMAGE)
        val myDescription = stringPreferencesKey(PREFERENCES_MY_DESCRIPTION)
        val myBirthDay = stringPreferencesKey(PREFERENCES_MY_BIRTHDAY)
        var myBeacon = booleanPreferencesKey(PREFERENCES_MY_BEACON)
        val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)
    }

    //初期書き込み
    suspend fun saveMyInfo(myState: User) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.myUid] = myState.uid ?: ""
            preferences[PreferenceKeys.myName] = myState.name ?: ""
            preferences[PreferenceKeys.myMail] = myState.mail ?: ""
            preferences[PreferenceKeys.myPhoneNumber] = myState.phoneNumber ?: ""
            preferences[PreferenceKeys.myProfileImage] = myState.profileImage ?: ""
            preferences[PreferenceKeys.myDescription] = myState.description ?: ""
            preferences[PreferenceKeys.myBirthDay] = myState.birthDay ?: ""
            preferences[PreferenceKeys.myBeacon] = myState.beacon ?: false
        }
    }

    //変更点書き換え
    suspend fun renewalMyInfo(myState: User) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.myName] = myState.name ?: ""
            preferences[PreferenceKeys.myPhoneNumber] = myState.phoneNumber ?: ""
            preferences[PreferenceKeys.myProfileImage] = myState.profileImage ?: ""
            preferences[PreferenceKeys.myDescription] = myState.description ?: ""
            preferences[PreferenceKeys.myBirthDay] = myState.birthDay ?: ""
        }
    }

    val readMyInfo: Flow<User> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val myUid = preferences[PreferenceKeys.myUid] ?: ""
            val myName = preferences[PreferenceKeys.myName] ?: ""
            val myMail = preferences[PreferenceKeys.myMail] ?: ""
            val myPhoneNumber = preferences[PreferenceKeys.myPhoneNumber] ?: ""
            val myProfileImage = preferences[PreferenceKeys.myProfileImage] ?: ""
            val myDescription = preferences[PreferenceKeys.myDescription] ?: ""
            val myBirthDay = preferences[PreferenceKeys.myBirthDay] ?: ""
            val myBeacon = preferences[PreferenceKeys.myBeacon] ?: false
            User(
                myUid,
                myName,
                myMail,
                myPhoneNumber,
                myProfileImage,
                myDescription,
                myBirthDay,
                myBeacon
            )
        }
}