package jp.ac.jec.cm0119.mamoru.utils.uistate

import com.google.firebase.auth.FirebaseUser

data class AuthState(
    val isSuccess: Boolean = false,
    val error: String = "",
    val errorType: Exception? = null,
    val isLoading: Boolean = false
)