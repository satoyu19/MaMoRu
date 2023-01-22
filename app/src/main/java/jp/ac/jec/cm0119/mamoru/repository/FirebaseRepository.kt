package jp.ac.jec.cm0119.mamoru.repository

import android.net.Uri
import android.util.Log
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpException
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import jp.ac.jec.cm0119.mamoru.models.ChatRoom
import jp.ac.jec.cm0119.mamoru.models.Message
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_CHAT_ROOMS
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_USERS
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_FAMILY
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_NEW_CHATS
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_READ_CHATS
import jp.ac.jec.cm0119.mamoru.utils.Constants.MESSAGE_IMAGE
import jp.ac.jec.cm0119.mamoru.utils.Constants.PROFILE_IMAGE
import jp.ac.jec.cm0119.mamoru.utils.Response
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
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

    var isNewChatListener: Boolean = false

    var currentUser: FirebaseUser? = null
        private set

    init {
        if (firebaseAuth.currentUser != null) {
            currentUser = firebaseAuth.currentUser
        }
    }

    /**
     * FirebaseAuth
     */

    //Auth登録
    fun register(email: String, password: String): Flow<Response<Nothing>> = flow {
        emit(Response.Loading())

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
    fun login(email: String, password: String): Flow<Response<FirebaseUser>> = flow {
        emit(Response.Loading())

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
    fun passwordReset(email: String): Flow<Response<Nothing>> = flow {
        emit(Response.Loading())
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
    fun addImageToFirebaseStorageProfile(imageUri: Uri): Flow<Response<Uri>> = flow {
        emit(Response.Loading())
        try {
            val currentUserUid = currentUser!!.uid
            // 同じuidの場合更新される
            val downloadUrl = firebaseStorage.reference.child(PROFILE_IMAGE)
                .child(currentUserUid)
                .putFile(imageUri).await()
                .storage.downloadUrl.await()

            emit(Response.Success(data = downloadUrl)) //うまくいった時

        } catch (e: Throwable) {
            emit(Response.Failure("画像のアップロードに失敗しました。"))
        }
    }

    fun addImageToFirebaseStorageMessage(imageUri: Uri): Flow<Response<Uri>> = flow {
        val imageId = UUID.randomUUID().toString()
        emit(Response.Loading())
        try {

            val downloadUrl = firebaseStorage.reference.child(MESSAGE_IMAGE)
                .child(imageId)
                .putFile(imageUri).await()
                .storage.downloadUrl.await()

            Log.d("onCreateView", "addImageToFirebaseStorageMessage: ${downloadUrl.toString()}")
            emit(Response.Success(data = downloadUrl))

        } catch (e: Throwable) {
            emit(Response.Failure("画像のアップロードに失敗しました。"))
        }
    }

    /**
     * FirebaseDatabase
     */
    // TODO: オフライン時のsetValueで止まるところとか,Throwableで処理したらどうなる？
    //ユーザー(自分)情報登録
    fun setMyInfoToDatabase(myInfo: User): Flow<Response<Nothing>> = flow {
        emit(Response.Loading())

        try {
            val currentUserUid = currentUser!!.uid

            firebaseDatabase.reference.child(DATABASE_USERS).child(currentUserUid)
                .setValue(myInfo).await()
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
    fun updateMyInfo(newMyInfo: User): Flow<Response<User>> = flow {
        emit(Response.Loading())

        try {
            val currentUserUid = currentUser!!.uid
            val updateData: DatabaseReference =
                firebaseDatabase.reference.child(DATABASE_USERS).child(currentUserUid)

            val myInfoObj = HashMap<String, Any>()
            myInfoObj["name"] = newMyInfo.name.toString()
            myInfoObj["birthDay"] = newMyInfo.birthDay.toString()
            myInfoObj["description"] = newMyInfo.description.toString()

            newMyInfo.profileImage?.let {
                myInfoObj["profileImage"] = newMyInfo.profileImage.toString()
            }

            updateData.updateChildren(myInfoObj).await()
            emit(Response.Success(data = newMyInfo))

        } catch (e: Exception) {
            emit(Response.Failure("更新に失敗しました。"))
        }
    }

    //ユーザー情報取得
    fun getUserData(): Flow<Response<User>> = flow {
        emit(Response.Loading())

        if (currentUser != null) {
            val snapshot =
                firebaseDatabase.reference.child(DATABASE_USERS).child(currentUser!!.uid).get()
                    .await()
            if (snapshot.exists()) {    //データあり
                val myInfo: User? = snapshot.getValue(User::class.java)
                emit(Response.Success(myInfo))
            } else {   //データなし
                emit(Response.Failure())
            }
        } else {
            emit(Response.Failure())
        }
    }

    // TODO: 自身の場合の処理 
    //ユーザー検索
    fun searchUser(userUid: String): Flow<Response<User>> = flow {
        emit(Response.Loading())
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
    fun registerFamily(userUid: String): Flow<Response<Nothing>> = flow {
        emit(Response.Loading())
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
    fun getMyFamily(): Flow<Response.Success<MutableList<User>>> = callbackFlow {

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
                            val userInfo: User? = user.getValue(User::class.java)
                            userInfo?.let { myFamily.add(it) }
                        }
                    }
                }
                trySendBlocking(Response.Success(data = myFamily))
            }

            override fun onCancelled(error: DatabaseError) {}
        })
        awaitClose {}
    }

    //メッセージ送信
    fun sendMessage(
        receiverUid: String,
        newMessage: String?,
        imageUri: Uri?,
    ): Flow<Response.Failure<Nothing>> = flow {

        try {
            val senderRoom = currentUser!!.uid + receiverUid
            val senderRoomRef =
                firebaseDatabase.reference.child(DATABASE_CHAT_ROOMS).child(currentUser!!.uid)
                    .child(senderRoom)
            val receiverRoom = receiverUid + currentUser!!.uid
            val receiverRoomRef =
                firebaseDatabase.reference.child(DATABASE_CHAT_ROOMS).child(receiverUid)
                    .child(receiverRoom)

            val date = Date()
            var lastMsgObj = HashMap<String, Any>()
            val message: Message

            if (imageUri != null) { //画像メッセージ
                message = Message("photo", currentUser!!.uid, imageUri.toString(), date.time)
                lastMsgObj["roomUid"] = senderRoom
                lastMsgObj["lastMsg"] = "photo"
                lastMsgObj["time"] = date.time
            } else {    //文字メッセージ
                message = Message(newMessage, currentUser!!.uid, null, date.time)
                lastMsgObj["roomUid"] = senderRoom
                lastMsgObj["lastMsg"] = newMessage!!
                lastMsgObj["time"] = date.time
            }

            val randomKey = firebaseDatabase.reference.push().key

            senderRoomRef.child(DATABASE_READ_CHATS).child(randomKey!!).setValue(message).await()
            receiverRoomRef.child(DATABASE_NEW_CHATS).child(randomKey).setValue(message).await()
            receiverRoomRef.updateChildren(lastMsgObj).await()
            senderRoomRef.updateChildren(lastMsgObj).await()

            val receiverNewChats = receiverRoomRef.child(DATABASE_NEW_CHATS).get().await()
            var newChatCountObj = HashMap<String, Any>()
            newChatCountObj["newChatCount"] = receiverNewChats.childrenCount
            receiverRoomRef.updateChildren(newChatCountObj).await()

        } catch (e: Throwable) {
            emit(Response.Failure(errorMessage = "メッセージの送信に失敗しました"))
        }
    }

    //新しいメッセージ受信のコールバック
    fun newReceiveMessageToRead(receiverUid: String): Flow<Response.Failure<Nothing>> =
        callbackFlow {
            var senderRoomRef: DatabaseReference? = null
            var newChatRef: DatabaseReference? = null
            var readChatRef: DatabaseReference? = null
            isNewChatListener = true
            try {
                val senderRoom = currentUser!!.uid + receiverUid
                senderRoomRef =
                    firebaseDatabase.reference.child(DATABASE_CHAT_ROOMS).child(currentUser!!.uid)
                        .child(senderRoom)
                //未読ノード
                newChatRef =
                    senderRoomRef
                        .child(DATABASE_NEW_CHATS)
                //既読ずみノード
                readChatRef =
                    senderRoomRef
                        .child(DATABASE_READ_CHATS)
            } catch (e: Throwable) {
                trySendBlocking(Response.Failure("メッセージの読み込みに失敗しました。"))
            }

            newChatRef?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshots: DataSnapshot) {
                    if (isNewChatListener) {
                        for (snapshot in snapshots.children) {
                            val nodeId = snapshot.key
                            val message: Message? = snapshot.getValue(Message::class.java)
                            readChatRef!!.child(nodeId!!).setValue(message).addOnSuccessListener {
                                newChatRef.child(nodeId).setValue(null)
                                var newChatCountObj = HashMap<String, Any>()
                                newChatCountObj["newChatCount"] = 0
                                senderRoomRef!!.updateChildren(newChatCountObj)
                            }
                        }
                    } else {
                        newChatRef.removeEventListener(this)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    trySendBlocking(Response.Failure("メッセージの読み込みに失敗しました。"))
                }
            })
            awaitClose {}
        }

    //既存チャットルーム、新規チャットルーム追加時receiver情報追加
    fun registerReceiverInfoToSenderRoom(): Flow<Response.Failure<Nothing>> = callbackFlow {
        var receiverUidArray = mutableListOf<String>()
        var chatRooms: DatabaseReference? = null
        try {
            chatRooms = firebaseDatabase.reference.child(DATABASE_CHAT_ROOMS).child(currentUser!!.uid)
            val chatRoomsSnap = chatRooms.get().await()

            val users = firebaseDatabase.reference.child(DATABASE_USERS).get().await()

            if (chatRoomsSnap.exists()) {
                for (snapshot in chatRoomsSnap.children) {
                    val senderRoomUid = snapshot.key.toString()
                    val receiver = senderRoomUid.removeRange(0, currentUser!!.uid.length)
                    receiverUidArray.add(receiver)
                }
            }

            for (receiverUid in receiverUidArray) {
                Log.d("Test", receiverUid)
                for (user in users.children) {
                    Log.d("Test", user.key.toString())
                    if (receiverUid == user.key) {
                        val userInfo: User? = user.getValue(User::class.java)
                        val receiverInfoObj = HashMap<String, Any>()
                        if (userInfo != null) {
                            receiverInfoObj["name"] = userInfo.name!!
                            receiverInfoObj["receiverUid"] = userInfo.uid!!
                            userInfo.profileImage?.let {
                                receiverInfoObj["profileImage"] = it
                            }
                        }
                        chatRooms
                            .child(currentUser!!.uid + receiverUid)
                            .updateChildren(receiverInfoObj).await()
                    }
                }
            }
        } catch (e: Throwable) {
            trySendBlocking(Response.Failure(errorMessage = "チャットの取得に失敗しました。"))
        }

        chatRooms?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                firebaseDatabase.reference.child(DATABASE_USERS).get().addOnSuccessListener { users ->
                    if (snapshots.exists()) {
                        for (snapshot in snapshots.children) {
                            val senderRoomUid = snapshot.key.toString()
                            val receiverUid = senderRoomUid.removeRange(0, currentUser!!.uid.length)
                            for (user in users.children) {
                                if (receiverUid == user.key) {
                                    val userInfo: User? = user.getValue(User::class.java)
                                    val receiverInfoObj = HashMap<String, Any>()
                                    if (userInfo != null) {
                                        receiverInfoObj["name"] = userInfo.name!!
                                        receiverInfoObj["receiverUid"] = userInfo.uid!!
                                        userInfo.profileImage?.let {
                                            receiverInfoObj["profileImage"] = it
                                        }
                                        chatRooms
                                            .child(currentUser!!.uid + receiverUid)
                                            .updateChildren(receiverInfoObj)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                trySendBlocking(Response.Failure("チャットの取得に失敗しました。"))
            }
        })
        awaitClose {}
    }

    fun getMessageOptions(receiverUid: String): FirebaseRecyclerOptions<Message> {
        val senderRoom = currentUser!!.uid + receiverUid
        val readChatRef =
            firebaseDatabase.reference.child(DATABASE_CHAT_ROOMS).child(currentUser!!.uid)
                .child(senderRoom)
                .child(DATABASE_READ_CHATS)

        return FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(readChatRef, Message::class.java)
            .build()
    }

    fun getChatRoomOptions(): FirebaseRecyclerOptions<ChatRoom> {
        val chatRooms =
            firebaseDatabase.reference.child(DATABASE_CHAT_ROOMS).child(currentUser!!.uid)

        return FirebaseRecyclerOptions.Builder<ChatRoom>()
            .setQuery(chatRooms, ChatRoom::class.java)
            .build()
    }
}