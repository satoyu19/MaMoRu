package jp.ac.jec.cm0119.mamoru.utils.uistate

data class MessagingState(
    val isSuccess: Boolean = false,
    val isFailure: Boolean = false,
    val token: String? = null
)