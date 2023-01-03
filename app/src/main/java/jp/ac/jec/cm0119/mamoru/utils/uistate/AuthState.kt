package jp.ac.jec.cm0119.mamoru.utils.uistate

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val isFailure: Boolean = false,
    val error: String = ""
)