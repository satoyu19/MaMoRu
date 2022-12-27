package jp.ac.jec.cm0119.mamoru.utils.uistate

import jp.ac.jec.cm0119.mamoru.utils.Response

data class DatabaseState (
    val isSuccess: Boolean = false,
    val error: String = "",
    val isFailure: Boolean = false,
    val isLoading: Boolean = false
)