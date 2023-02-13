package jp.ac.jec.cm0119.mamoru.models

import android.os.Parcelable

data class BeaconInfo(
    val uuid: String,
    val distance: String,
    var isConnection: Boolean = false
)
