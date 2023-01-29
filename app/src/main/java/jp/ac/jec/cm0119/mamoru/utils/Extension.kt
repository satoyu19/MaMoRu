package jp.ac.jec.cm0119.mamoru.utils

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow

suspend fun <T> MutableStateFlow<T?>.set(value: T, idle: T? = null, delayTime: Long = 100) {
    this.value = value
    delay(delayTime)
    this.value = idle
}