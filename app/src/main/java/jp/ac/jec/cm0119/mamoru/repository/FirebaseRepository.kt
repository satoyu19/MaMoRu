package jp.ac.jec.cm0119.mamoru.repository

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.*
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.getValue
import com.google.firebase.storage.FirebaseStorage
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_CHILD1
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_CHILD_FAMILY
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
        // TODO: 調べる 
//        firebaseDatabase.setPersistenceEnabled(true)
    }

    /**
     * FirebaseAuth
     */

    //Auth登録
    fun register(email: String, password: String) = flow {
        emit(Response.Loading)

        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .await()   //.await() →　スレッドをブロックすることなく、タスクの完了を待つ。

            emit(Response.Success())

            loggedOutLiveData.postValue(false)
        } catch (e: HttpException) {
            emit(Response.Failure(e.localizedMessage ?: "インターネット接続を確認してください。"))
        } catch (e: FirebaseAuthException) {
            when (e) {
                is FirebaseAuthWeakPasswordException -> emit(Response.Failure(errorMessage = "パスワードを強力にしてください。"))
                is FirebaseAuthInvalidCredentialsException -> emit(Response.Failure(errorMessage = "アドレスの形式が正しくありません。"))
                is FirebaseAuthUserCollisionException -> emit(Response.Failure(errorMessage = "指定されたアドレスのアカウントが既に存在しています。"))
                else -> emit(Response.Failure(errorMessage = "エラーです。もう一度やり直してください。"))
            }
        } catch (e: IOException) {
            emit(Response.Failure(e.localizedMessage ?: "不明のエラーが発生しました。"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    //todo: 何度も同じアカウントで失敗するとログインできなくなる。パスワード変更等で処置可能？
    //ログイン
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
        } catch (e: FirebaseAuthException) {
            when (e) {
                is FirebaseAuthInvalidUserException -> emit(Response.Failure(errorMessage = "入力されたメールアドレスでの登録が見つかりませんでした。"))
                is FirebaseAuthInvalidCredentialsException -> emit(Response.Failure(errorMessage = "アドレスの形式が正しくありません。"))
                else -> emit(Response.Failure(errorMessage = "エラーです。もう一度やり直してください。"))
            }
        } catch (e: IOException) {
            Log.d("Test", e.localizedMessage?.toString() ?: "IOError")
            emit(Response.Failure("不明のエラーが発生しました。"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }

    }

    //パスワードリセット
    fun passwordReset(email: String) = flow {
        emit(Response.Loading)
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(Response.Success())
        } catch (e: HttpException) {
            Log.d("Test", e.localizedMessage?.toString() ?: "HttpError")
            emit(Response.Failure("インターネット接続を確認してください。"))
        } catch (e: FirebaseAuthException) {
            when (e) {
                is FirebaseAuthInvalidUserException -> emit(Response.Failure(errorMessage = "入力されたメールアドレスでの登録が見つかりませんでした。"))
                is FirebaseAuthInvalidCredentialsException -> emit(Response.Failure(errorMessage = "アドレスの形式が正しくありません。"))
                else -> emit(Response.Failure(errorMessage = "エラーです。もう一度やり直してください。"))
            }
        } catch (e: IOException) {
            Log.d("Test", e.localizedMessage?.toString() ?: "IOError")
            emit(Response.Failure("不明のエラーが発生しました。"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    /**
     * FirebaseStorage
     */
    //firebaseStorageに画像アップロード
    fun addImageToFirebaseStorage(imageUri: Uri) = flow {
        try {
            emit(Response.Loading)

            val currentUserUid = currentUser!!.uid

            // 同じuidの場合更新される
            val downloadUrl = firebaseStorage.reference.child(PROFILE_IMAGE)
                .child(currentUserUid)
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
    //ユーザー情報登録
    fun setUserToDatabase(user: User) = flow {
        try {
            emit(Response.Loading)
            val currentUserUid = currentUser!!.uid

            firebaseDatabase.reference.child(DATABASE_CHILD1).child(currentUserUid)
                .setValue(user).await()

            val snapshot =
                firebaseDatabase.reference.child(DATABASE_CHILD1).child(currentUserUid).get()
                    .await()

            if (snapshot.exists()) { //nullじゃなかったら
                emit(Response.Success())
            } else {    //null = 登録がされていないため、処理を促す
                emit(Response.Failure(errorMessage = "登録ができませんでした。もう一度やり直してください。"))
            }

        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    //ユーザー情報取得
    fun getUserData() = flow {
        emit(Response.Loading)

        val currentUser = firebaseAuth.currentUser  //取得できなければnull?
        if (currentUser != null) {
            val snapshot = firebaseDatabase.reference.child(DATABASE_CHILD1).child(currentUser.uid).get().await()
            if (snapshot.exists()) {    //データあり
                val myState: User? = snapshot.getValue(User::class.java)
                Log.d("Test", myState!!.uid!!)
                emit(Response.Success(myState))
            } else {   //データなし
                emit(Response.Failure())
            }
        } else {
            emit(Response.Failure())
        }
    }

    //ユーザー検索
    fun searchUser(userUid: String) = flow {
        emit(Response.Loading)
        try {
            val snapshot =
                firebaseDatabase.reference.child(DATABASE_CHILD1).child(userUid).get().await()
            if (snapshot.exists()) {
                val user: User? = snapshot.getValue(User::class.java)
                emit(Response.Success(user))
            }
        } catch (e: HttpException) {
            emit(Response.Failure(e.localizedMessage ?: "インターネット接続を確認してください。"))
        } catch (e: IOException) {
            emit(Response.Failure(e.localizedMessage ?: "不明のエラーが発生しました。"))
        } catch (e: NullPointerException) {
            emit(Response.Failure("ユーザーが存在しません。"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
            Log.d("Test", "Exception/${e.message.toString()}")
        }
    }

    //familyユーザー追加
    fun registerFamily(user: User, myState: User) = flow {
        emit(Response.Loading)

        try {

            val currentUserUid = currentUser!!.uid
            var isRegistered = false

            val snapshots =
                firebaseDatabase.reference.child(DATABASE_CHILD1).child(currentUserUid).child(
                    DATABASE_CHILD_FAMILY
                ).get().await()

            if (snapshots.exists()) {
                for (snapshot in snapshots.children) {
                    val registeredUser: User? = snapshot.getValue(User::class.java)
                    if (user.uid == registeredUser?.uid) {  //既に登録されているユーザーの場合
                        isRegistered = true
                        emit(Response.Failure("登録済みのユーザーです。"))
                    }
                }
            }
            if (!isRegistered) {    //登録済みでなければ自身側、相手側で登録
                firebaseDatabase.reference.child(DATABASE_CHILD1)
                    .child(currentUserUid)
                    .child(DATABASE_CHILD_FAMILY)
                    .child(user.uid!!).setValue(user)

                firebaseDatabase.reference.child(DATABASE_CHILD1)
                    .child(user.uid!!)
                    .child(DATABASE_CHILD_FAMILY)
                    .child(currentUserUid).setValue(myState)

                emit(Response.Success())
            }
        } catch (e: HttpException) {
            emit(Response.Failure(e.localizedMessage ?: "インターネット接続を確認してください。"))
        } catch (e: IOException) {
            emit(Response.Failure(e.localizedMessage ?: "不明のエラーが発生しました。"))
        } catch (e: NullPointerException) {
            emit(Response.Failure("ユーザーが存在しません。"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
            Log.d("Test", "Exception/${e.message.toString()}")
        }
    }
}