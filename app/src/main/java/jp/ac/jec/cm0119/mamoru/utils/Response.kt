package jp.ac.jec.cm0119.mamoru.utils

import android.os.Message

sealed class Response<T> {  //Tが継承されていればOK？
    object Loading: Response<Nothing>()
    data class Success<T>(val data: T? = null): Response<T>()
    data class Failure(val errorMessage: String = "エラー") : Response<Nothing>()
}