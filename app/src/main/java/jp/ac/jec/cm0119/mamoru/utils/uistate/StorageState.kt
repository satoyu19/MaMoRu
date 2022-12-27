package jp.ac.jec.cm0119.mamoru.utils.uistate

import android.net.Uri

data class StorageState (
    val data: Uri? = null,
    var error: String =  "",
    val isLoading: Boolean = false
        )
