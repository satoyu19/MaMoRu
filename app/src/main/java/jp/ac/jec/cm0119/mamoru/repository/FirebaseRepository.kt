package jp.ac.jec.cm0119.mamoru.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpException
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import jp.ac.jec.cm0119.mamoru.models.Message
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_CHAT_ROOMS
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_USERS
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_FAMILY
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_NEW_CHATS
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_READ_CHATS
import jp.ac.jec.cm0119.mamoru.utils.Constants.PROFILE_IMAGE
import jp.ac.jec.cm0119.mamoru.utils.Response
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class FirebaseRepository @Inject constructor() {

    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()

    var currentUser: FirebaseUser? = null

    init {
        //ログイン済みであればloggedOutLiveDataをfalse
        if (firebaseAuth.currentUser != null) {
            currentUser = firebaseAuth.currentUser
        }
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

            emit(Response.Success(data = null))

        } catch (e: FirebaseNetworkException) {
            emit(Response.Failure("インターネット接続を確認してください。"))
        } catch (e: FirebaseAuthException) {
            when (e) {
                is FirebaseAuthWeakPasswordException -> emit(Response.Failure(errorMessage = "パスワードを強力にしてください。"))
                is FirebaseAuthInvalidCredentialsException -> emit(Response.Failure(errorMessage = "アドレスの形式が正しくありません。"))
                is FirebaseAuthUserCollisionException -> emit(Response.Failure(errorMessage = "指定されたアドレスのアカウントが既に存在しています。"))
                else -> emit(Response.Failure(errorMessage = "エラーです。もう一度やり直してください。"))
            }
        } catch (e: IOException) {
            emit(Response.Failure("不明のエラーが発生しました。"))
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
            emit(Response.Success(data = null))
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

            emit(Response.Success(data = downloadUrl)) //うまくいった時

        } catch (e: Exception) {
            emit(Response.Failure("画像のアップロードに失敗しました。"))   //エラーだった時
        }
    }

    /**
     * FirebaseDatabase
     */
    // TODO: オフライン時のsetValueで止まるところとか,Throwableで処理したらどうなる？
    //ユーザー(自分)情報登録
    fun setMyStateToDatabase(myState: User) = flow {
        emit(Response.Loading)

        try {
            val currentUserUid = currentUser!!.uid

            firebaseDatabase.reference.child(DATABASE_USERS).child(currentUserUid)
                .setValue(myState).await()
            val snapshot =
                firebaseDatabase.reference.child(DATABASE_USERS).child(currentUserUid).get()
                    .await()
            if (snapshot.exists()) { //nullじゃなかったら
                emit(Response.Success(data = null))
            } else {    //null = 登録がされていない
                emit(Response.Failure(errorMessage = "登録ができませんでした。もう一度やり直してください。"))
            }

        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    //ユーザー(自分)情報の更新
    fun updateMyState(newMyState: User) = flow {
        emit(Response.Loading)

        try {
            val currentUserUid = currentUser!!.uid
            val updateData: DatabaseReference =
                firebaseDatabase.reference.child(DATABASE_USERS).child(currentUserUid)

            val myStateObj = HashMap<String, Any>()
            myStateObj["name"] = newMyState.name.toString()
            myStateObj["birthDay"] = newMyState.birthDay.toString()
            myStateObj["description"] = newMyState.description.toString()

            newMyState.profileImage?.let {
                myStateObj["profileImage"] = newMyState.profileImage.toString()
            }

            updateData.updateChildren(myStateObj).await()
            emit(Response.Success(data = newMyState))

        } catch (e: Exception) {
            emit(Response.Failure("更新に失敗しました。"))
        }
    }

    //ユーザー情報取得
    fun getUserData() = flow {
        emit(Response.Loading)

        if (currentUser != null) {
            val snapshot =
                firebaseDatabase.reference.child(DATABASE_USERS).child(currentUser!!.uid).get()
                    .await()
            if (snapshot.exists()) {    //データあり
                val myState: User? = snapshot.getValue(User::class.java)
                emit(Response.Success(myState))
            } else {   //データなし
                emit(Response.Failure())
            }
        } else {
            emit(Response.Failure())
        }
    }

    // TODO: 自身の場合の処理 
    //ユーザー検索
    fun searchUser(userUid: String) = flow {
        emit(Response.Loading)
        try {
            val snapshot =
                firebaseDatabase.reference.child(DATABASE_USERS).child(userUid).get().await()
            if (snapshot.exists()) {
                val user: User? = snapshot.getValue(User::class.java)
                emit(Response.Success(user))
            } else {
                emit(Response.Failure("ユーザーが存在しません。"))
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
    fun registerFamily(userUid: String) = flow {
        emit(Response.Loading)
        try {

            val currentUserUid = currentUser!!.uid
            var isRegistered = false
            val familyRf = firebaseDatabase.reference.child(DATABASE_FAMILY)
            val snapshots = familyRf.child(currentUserUid).get().await()

            if (snapshots.exists()) {
                for (snapshot in snapshots.children) {
                    if (userUid == snapshot.key) {  //既に登録されているユーザーの場合
                        isRegistered = true
                        emit(Response.Failure("登録済みのユーザーです。"))
                    }
                }
            }
            if (!isRegistered) {    //登録済みでなければ自身側、相手側で登録
                val familyUserObj = HashMap<String, Any>()

                familyUserObj["uid"] = userUid
                familyRf.child(currentUserUid).child(userUid).setValue(familyUserObj)

                familyUserObj["uid"] = currentUserUid
                familyRf.child(userUid).child(currentUserUid).setValue(familyUserObj)

                emit(Response.Success(data = null))
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

    //ファミリーに変更があればrecycleViewを更新したいため、callbackFlowを使う
    fun getMyFamily() = callbackFlow {

        var myFamilyRef: DatabaseReference? = null
        var users: DataSnapshot? = null
        try {
            myFamilyRef = firebaseDatabase.reference.child(DATABASE_FAMILY).child(currentUser!!.uid)
            users = firebaseDatabase.reference.child(DATABASE_USERS).get().await()

        } catch (e: Throwable) {
            close(e)
        }

        myFamilyRef?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                val myFamilyUid = mutableListOf<String>()
                val myFamily = mutableListOf<User>()

                snapshot.children.forEach {
                    myFamilyUid.add(it.key.toString())
                }
                //todo 新規登録のユーザーからすぐ登録されるとusersにまだいなくて反映されない。
                // どーする？この中でfirebaseDatabase.reference.child(DATABASE_USERS).get().addOnSuccessListener {  }呼んでとる？
                if (users?.exists() == true) {    //データあり
                    for (user in users.children) {
                        if (myFamilyUid.contains(user.key)) {
                            val userState: User? = user.getValue(User::class.java)
                            userState?.let { myFamily.add(it) }
                        }
                    }
                }
                trySendBlocking(Response.Success(data = myFamily))
            }

            override fun onCancelled(error: DatabaseError) {
                trySendBlocking(Response.Failure(error.message))
            }
        })
        awaitClose {}
    }

    //メッセージ送信
    fun sendMessage(receiverUid: String, newMessage: String) = flow {
        emit(Response.Loading)

        try {
            val randomKey = firebaseDatabase.reference.push().key
            val receiverRoom = receiverUid + currentUser!!.uid
            val receiverNewChatRef = firebaseDatabase.reference.child(DATABASE_CHAT_ROOMS).child(receiverRoom).child(DATABASE_NEW_CHATS).child(randomKey!!)
            val senderRoom = currentUser!!.uid + receiverUid
            val senderReadChatRef = firebaseDatabase.reference.child(DATABASE_CHAT_ROOMS).child(senderRoom).child(DATABASE_READ_CHATS).child(randomKey!!)

            val date = Date()
            val message = Message(null, newMessage, currentUser!!.uid, null, date.time)

            receiverNewChatRef.setValue(message).await()
            senderReadChatRef.setValue(message).await()

            emit(Response.Success())

        } catch (e: Throwable) {
            emit(Response.Failure(errorMessage = "メッセージの送信に失敗しました"))
        }
    }

    //新しいメッセージの受信コールバック
    fun receiveMessageToRead(receiverUid: String) = callbackFlow {
        var newChatRef: DatabaseReference? = null
        var readChatRef: DatabaseReference? = null
        try {
            val senderRoom = currentUser!!.uid + receiverUid
            newChatRef = firebaseDatabase.reference.child(DATABASE_CHAT_ROOMS).child(senderRoom).child(DATABASE_NEW_CHATS)
            readChatRef = firebaseDatabase.reference.child(DATABASE_CHAT_ROOMS).child(senderRoom).child(DATABASE_READ_CHATS)
        } catch (e: Throwable) {
            close(e)
        }

        newChatRef?.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (message in snapshot.children) {
                    val nodeId = message.key
                    readChatRef!!.child(nodeId!!).setValue(message).addOnSuccessListener {
                        newChatRef.child(nodeId).setValue(null)
                    }
                }
                trySendBlocking(Response.Success(data = null))
            }

            override fun onCancelled(error: DatabaseError) {
                trySendBlocking(Response.Failure(error.message))
            }

        })
        awaitClose {}
    }
}