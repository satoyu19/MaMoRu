package jp.ac.jec.cm0119.mamoru.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

//メールアドレスは必要か？

@Parcelize
data class User (
    var uid: String? = null,
    var name: String? = null,
    var mail: String? = null,
    var profileImage: String? = null,
    var description: String? = null,
    var birthDay: String? = null,
    var beacon: Boolean? = null,
    var exitBeacon: Boolean? = null,
    var myToken: String? = null,
    var updateTime: Long? = null
): Parcelable