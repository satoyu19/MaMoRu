package jp.ac.jec.cm0119.mamoru.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.utils.Resource
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import javax.inject.Inject

class FirebaseRepository @Inject constructor() {

    var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private var loggedOutLiveData: MutableLiveData<Boolean> = MutableLiveData()

    // TODO: 最終的に処理書かなかったらレポジトリではなくModuleでDIする
    init {
        //ログイン済みであればloggedOutLiveDataをfalse
        if (firebaseAuth.currentUser != null) {
            loggedOutLiveData.postValue(false)
        }
    }

    /**
     * FirebaseAuth
     */
    fun register(email: String, password: String, user: User): Flow<Resource<FirebaseUser>> = flow {
        emit(Resource.Loading())

        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()   //.await() →　スレッドをブロックすることなく、タスクの完了を待つ。

            /**この処理はユーザー情報登録時*/
            firebaseDatabase.reference.child("Users").child(firebaseAuth.currentUser!!.uid)
                .setValue(user).await()

            emit((result.user?.let {
                Resource.Success(data = it)
            }!!))

            loggedOutLiveData.postValue(false)
        } catch (e: HttpException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Unknown Error"))
        } catch (e: IOException) {
            emit(Resource.Error(message = e.localizedMessage ?: "Check Your Internet Connection"))
        } catch (e: Exception) {
            emit(Resource.Error(message = e.localizedMessage ?: ""))
        }


    }

}