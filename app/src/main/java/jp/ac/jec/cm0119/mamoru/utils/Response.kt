package jp.ac.jec.cm0119.mamoru.utils

sealed class Response<T> {  //Tが継承されていればOK？
    data class Loading<T>(val data: T? = null): Response<T>()
    data class Success<T>(val data: T? = null): Response<T>()
    data class Failure<T>(val errorMessage: String = "エラー", val data: T? = null) : Response<T>()
}

