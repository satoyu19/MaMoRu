package jp.ac.jec.cm0119.mamoru.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.*
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_CHILD1
import jp.ac.jec.cm0119.mamoru.utils.Constants.PROFILE_IMAGE
import jp.ac.jec.cm0119.mamoru.utils.Response
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

class FirebaseRepository @Inject constructor() {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // TODO: 使う？
    private var loggedOutLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    var currentUser: FirebaseUser? = null

    init {
        //ログイン済みであればloggedOutLiveDataをfalse
        if (firebaseAuth.currentUser != null) {
            loggedOutLiveData.postValue(false)
            currentUser = firebaseAuth.currentUser
        }
    }

    /**
     * FirebaseAuth
     */
    fun register(email: String, password: String) = flow {
        emit(Response.Loading)

        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()   //.await() →　スレッドをブロックすることなく、タスクの完了を待つ。

            emit(Response.Success())

            loggedOutLiveData.postValue(false)
        } catch (e: HttpException) {
            emit(Response.Failure(e.localizedMessage ?:"インターネット接続を確認してください。"))
        } catch (e: FirebaseAuthException) {
            when (e) {
                is FirebaseAuthWeakPasswordException -> emit(Response.Failure(errorMessage = "パスワードを強力にしてください。"))
                is FirebaseAuthInvalidCredentialsException -> emit(Response.Failure(errorMessage = "アドレスの形式が正しくありません。"))
                is FirebaseAuthUserCollisionException -> emit(Response.Failure(errorMessage = "指定されたアドレスのアカウントが既に存在しています。"))
                else -> emit(Response.Failure(errorMessage = "エラーです。もう一度やり直してください。"))
            }
        }catch (e: IOException) {
            emit(Response.Failure(e.localizedMessage ?:"不明のエラーが発生しました。"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    //todo: 何度も同じアカウントで失敗するとログインできなくなる。パスワード変更等で処置可能？
    fun login(email: String, password: String) = flow {

        emit(Response.Loading)

        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit((result.user?.let {
                Response.Success(data = it)
            }!!))

            loggedOutLiveData.postValue(false)

        } catch (e: HttpException) {
            Log.d("Test", e.localizedMessage?.toString() ?: "HttpError")
            emit(Response.Failure("インターネット接続を確認してください。"))
        }catch (e: FirebaseAuthException) {
            when (e) {
                is FirebaseAuthInvalidUserException -> emit(Response.Failure(errorMessage = "入力されたメールアドレスでの登録が見つかりませんでした。"))
                is FirebaseAuthInvalidCredentialsException -> emit(Response.Failure(errorMessage = "入力されたパスワードに誤りがあります。"))
                else -> emit(Response.Failure(errorMessage = "エラーです。もう一度やり直してください。"))
            }
        } catch (e: IOException) {
            Log.d("Test", e.localizedMessage?.toString() ?: "IOError")
            emit(Response.Failure("不明のエラーが発生しました。"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }

    }

    fun passwordReset(email: String) = flow {
        emit(Response.Loading)
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(Response.Success())
        }catch (e: HttpException) {
            Log.d("Test", e.localizedMessage?.toString() ?: "HttpError")
            emit(Response.Failure("インターネット接続を確認してください。"))
        }catch (e: FirebaseAuthInvalidUserException) {
            emit(Response.Failure(errorMessage = "入力されたメールアドレスでの登録が見つかりませんでした。"))
        }catch (e: IOException) {
            Log.d("Test", e.localizedMessage?.toString() ?: "IOError")
            emit(Response.Failure("不明のエラーが発生しました。"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    /**
     * FirebaseStorage
     */
     fun addImageToFirebaseStorage(imageUri: Uri) = flow {
        try {
            emit(Response.Loading)

            val uid = firebaseAuth.currentUser!!.uid

            // 同じuidの場合更新される
            val downloadUrl = firebaseStorage.reference.child(PROFILE_IMAGE)
                .child(uid)
                .putFile(imageUri).await()
                .storage.downloadUrl.await()

            emit(Response.Success(downloadUrl)) //うまくいった時

        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))   //エラーだった時
        }
    }

    /**
     * FirebaseDatabase
     */
    fun setUserToDatabase(user: User) = flow {
        try {
            emit(Response.Loading)
            val currentUserUid = firebaseAuth.currentUser!!.uid

            firebaseDatabase.reference.child(DATABASE_CHILD1).child(currentUserUid)
                .setValue(user).await()

            val snapshot = firebaseDatabase.reference.child(DATABASE_CHILD1).child(currentUserUid).get().await()

            if (snapshot.exists()){ //nullじゃなかったら
                emit(Response.Success())
            } else {    //null = 登録がされていないため、処理を促す
                emit(Response.Failure(errorMessage = "登録ができませんでした。もう一度やり直してください。"))
            }

        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    fun getUserData() = flow {
        emit(Response.Loading)

        val currentUser = firebaseAuth.currentUser  //取得できなければnull?
        if (currentUser != null) {
            val snapshot = firebaseDatabase.reference.child(DATABASE_CHILD1).child(currentUser.uid).get().await()
            if (snapshot.exists()) {    //データあり
                emit(Response.Success())
            } else {   //データなし
                emit(Response.Failure())
            }
        } else {
            emit(Response.Failure())
        }
    }
}