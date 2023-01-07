package jp.ac.jec.cm0119.mamoru.utils.uistate

import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.utils.Response

data class DatabaseState (
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isFailure: Boolean = false,
    val user: User? = null,
    val error: String = ""
)