package jp.ac.jec.cm0119.mamoru.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    var uid: String? = null,
    var name: String? = null,
    var phoneNumber: String? = null,
    var profileImage: String? = null,
    var description: String? = null,
    var birthDay: String? = null,
    var beacon: String? = null
): Parcelable