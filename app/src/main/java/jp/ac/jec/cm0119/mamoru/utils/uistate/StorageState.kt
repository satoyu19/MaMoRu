package jp.ac.jec.cm0119.mamoru.utils.uistate

import android.net.Uri

data class StorageState (
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isFailure: Boolean = false,
    val data: Uri? = null,
    var error: String =  ""
        )
