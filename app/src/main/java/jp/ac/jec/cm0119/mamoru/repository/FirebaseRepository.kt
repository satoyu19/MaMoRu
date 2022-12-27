package jp.ac.jec.cm0119.mamoru.repository

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.utils.Constants.PROFILE_IMAGE
import jp.ac.jec.cm0119.mamoru.utils.Response
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

class FirebaseRepository @Inject constructor() {

    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // TODO: 使う？
    private var loggedOutLiveData: MutableLiveData<Boolean> = MutableLiveData()

    var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    init {
        //ログイン済みであればloggedOutLiveDataをfalse
        if (firebaseAuth.currentUser != null) {
            loggedOutLiveData.postValue(false)
        }
    }

    /**
     * FirebaseAuth
     */
    // TODO: error処理
    fun register(email: String, password: String) = flow {
        emit(Response.Loading)

        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).await()   //.await() →　スレッドをブロックすることなく、タスクの完了を待つ。

            emit(Response.Success())

            loggedOutLiveData.postValue(false)
            // TODO: firebaseStorageのエラーも下記のように対応させる？ 
        } catch (e: HttpException) {
            emit(Response.Failure("インターネット接続を確認してください。"))
        } catch (e: FirebaseAuthException) {
            // TODO: eを使って処理する
            emit(Response.Failure("メールアドレスが既に登録されている、メールアドレスの形式が間違えている、パスワードが強力でない", e))
        }catch (e: IOException) {
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
            emit(Response.Loading)  //最初はローディング状態

            val uid = firebaseAuth.currentUser!!.uid  //とりあえず仮

            // TODO: resultのtoStringジャないとむり？
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

            firebaseDatabase.reference.child("Users").child(currentUserUid)
                .setValue(user).await()

            val snapshot = firebaseDatabase.reference.child("User").child(currentUserUid).get().await()

            if (snapshot.exists()){ //nullじゃなかったら
                emit(Response.Success<Nothing>())
            } else {
                // TODO: こいつがnullの場合には登録がされてないってことで接pに遷移させる処理にする
                emit(Response.Failure())
            }

        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    fun getUserData() = flow {
        emit(Response.Loading)

        val currentUser = firebaseAuth.currentUser  //取得できなければnull?
        if (currentUser != null) {
            val snapshot = firebaseDatabase.reference.child("User").child(currentUser.uid).get().await()
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