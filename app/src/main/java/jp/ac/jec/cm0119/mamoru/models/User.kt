package jp.ac.jec.cm0119.mamoru.models

import android.net.Uri

//メールアドレスは必要か？

data class User (
    var uid: String = "",
    var name: String = "",
    val mail: String = "",
    var phoneNumber: String? = null,
    var profileImage: Uri? = null,
    var description: String? = null,
    var birthDay: String? = null,
    var beacon: Boolean = false
)