package jp.ac.jec.cm0119.mamoru.repository

import android.net.Uri
import android.util.Log
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.HttpException
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import jp.ac.jec.cm0119.mamoru.data.ApiInterface
import jp.ac.jec.cm0119.mamoru.models.*
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_CHAT
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_CHAT_ROOMS
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_FAMILY
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_NEW_CHATS
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_READ_CHATS
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_USERS
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
import javax.inject.Singleton
import kotlin.collections.HashMap


@Singleton
class FirebaseRepository @Inject constructor(private val api: ApiInterface) {

    private var firebaseAuth: FirebaseAuth = Firebase.auth
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var firebaseStorage: FirebaseStorage = Firebase.storage
    private var firebaseMessaging: FirebaseMessaging = FirebaseMessaging.getInstance()

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

    //Auth??????
    fun registerAuth(email: String, password: String): Flow<Response<Nothing>> = flow {
        emit(Response.Loading())

        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .await()   //.await() ????????????????????????????????????????????????????????????????????????????????????

            emit(Response.Success(data = null))

        } catch (e: FirebaseNetworkException) {
            emit(Response.Failure("?????????????????????????????????????????????????????????"))
        } catch (e: FirebaseAuthException) {
            when (e) {
                is FirebaseAuthWeakPasswordException -> emit(Response.Failure(errorMessage = "????????????????????????????????????????????????"))
                is FirebaseAuthInvalidCredentialsException -> emit(Response.Failure(errorMessage = "???????????????????????????????????????????????????"))
                is FirebaseAuthUserCollisionException -> emit(Response.Failure(errorMessage = "??????????????????????????????????????????????????????????????????????????????"))
                else -> emit(Response.Failure(errorMessage = "????????????????????????????????????????????????????????????"))
            }
        } catch (e: IOException) {
            emit(Response.Failure("??????????????????????????????????????????"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    //????????????
    fun login(email: String, password: String): Flow<Response<FirebaseUser>> = flow {
        emit(Response.Loading())

        try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            emit((result.user?.let {
                Response.Success(data = it)
            }!!))

        } catch (e: HttpException) {
            emit(Response.Failure("?????????????????????????????????????????????????????????"))
        } catch (e: FirebaseAuthException) {
            when (e) {
                is FirebaseAuthInvalidUserException -> emit(Response.Failure(errorMessage = "????????????????????????????????????????????????????????????????????????????????????"))
                is FirebaseAuthInvalidCredentialsException -> emit(Response.Failure(errorMessage = "???????????????????????????????????????????????????"))
                else -> emit(Response.Failure(errorMessage = "????????????????????????????????????????????????????????????"))
            }
        } catch (e: IOException) {
            emit(Response.Failure("??????????????????????????????????????????"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }

    }

    //???????????????????????????
    fun passwordReset(email: String): Flow<Response<Nothing>> = flow {
        emit(Response.Loading())
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(Response.Success(data = null))
        } catch (e: HttpException) {
            emit(Response.Failure("?????????????????????????????????????????????????????????"))
        } catch (e: FirebaseAuthException) {
            when (e) {
                is FirebaseAuthInvalidUserException -> emit(Response.Failure(errorMessage = "????????????????????????????????????????????????????????????????????????????????????"))
                is FirebaseAuthInvalidCredentialsException -> emit(Response.Failure(errorMessage = "???????????????????????????????????????????????????"))
                else -> emit(Response.Failure(errorMessage = "????????????????????????????????????????????????????????????"))
            }
        } catch (e: IOException) {
            emit(Response.Failure("??????????????????????????????????????????"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    /**
     * FirebaseStorage
     */
    //firebaseStorage???????????????????????????
    fun addImageToFirebaseStorageProfile(imageUri: Uri): Flow<Response<Uri>> = flow {
        emit(Response.Loading())
        try {
            val currentUserUid = currentUser!!.uid
            // ??????uid????????????????????????
            val downloadUrl = firebaseStorage.reference.child(PROFILE_IMAGE)
                .child(currentUserUid)
                .putFile(imageUri).await()
                .storage.downloadUrl.await()

            emit(Response.Success(data = downloadUrl)) //?????????????????????

        } catch (e: Throwable) {
            emit(Response.Failure("???????????????????????????????????????????????????"))
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

            emit(Response.Success(data = downloadUrl))

        } catch (e: Throwable) {
            emit(Response.Failure("???????????????????????????????????????????????????"))
        }
    }

    /**
     * FirebaseMessaging
     */

    //???????????????????????????????????????
    suspend fun sendNotificationMessageToReceiver(notificationModel: NotificationModel) {
        try {
            api.sendNotification(notificationModel)
        } catch (e: Throwable) {
            Log.d("Mamoru", "sendNotification: ${e.message}")
        }
    }

    //????????????family????????????????????????
    suspend fun sendNotificationBeaconToFamily(noticeMessage: String) {
        try {
            val myFamilyUid = mutableListOf<String>()
            val myFamily =
                firebaseDatabase.reference.child(DATABASE_FAMILY).child(currentUser!!.uid).get()
                    .await()
            val my = firebaseDatabase.reference.child(DATABASE_USERS).child(currentUser!!.uid).get()
                .await()
            val myInfo: User? = my.getValue(User::class.java)

            if (myFamily.exists()) {
                myFamily.children.forEach {
                    myFamilyUid.add(it.key!!)
                }
            }
            myFamilyUid.forEach { userUid ->
                val userInfo = fetchUserInfo(userUid)
                userInfo?.let {
                    val notificationModel = NotificationModel(
                        to = userInfo.myToken!!,
                        data = Data("MaMoRu", "${myInfo!!.name}?????? $noticeMessage")
                    )
                    api.sendNotification(notificationModel)
                }
            }
        } catch (e: Throwable) {
            return
        }
    }


    /**
     * FirebaseDatabase
     */
    //????????????(??????)????????????
    fun setMyInfoToDatabase(myInfo: User): Flow<Response<Nothing>> = flow {
        emit(Response.Loading())

        try {
            val currentUserUid = currentUser!!.uid

            val myToken = firebaseMessaging.token.await()
            myInfo.myToken = myToken

            firebaseDatabase.reference.child(DATABASE_USERS).child(currentUserUid)
                .setValue(myInfo).await()
            val snapshot =
                firebaseDatabase.reference.child(DATABASE_USERS).child(currentUserUid).get()
                    .await()
            if (snapshot.exists()) { //null?????????????????????
                emit(Response.Success(data = null))
            } else {    //null = ???????????????????????????
                emit(Response.Failure(errorMessage = "??????????????????????????????????????????????????????????????????????????????"))
            }

        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    //????????????(??????)???????????????
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
            emit(Response.Failure("??????????????????????????????"))
        }
    }

    //????????????????????????
    fun getUserData(): Flow<Response<User>> = flow {
        emit(Response.Loading())

        if (currentUser != null) {
            val snapshot =
                firebaseDatabase.reference.child(DATABASE_USERS).child(currentUser!!.uid).get()
                    .await()
            if (snapshot.exists()) {    //???????????????
                val myInfo: User? = snapshot.getValue(User::class.java)
                emit(Response.Success(myInfo))
            } else {   //???????????????
                emit(Response.Failure())
            }
        } else {
            emit(Response.Failure())
        }
    }

    //??????????????????
    fun searchUser(userUid: String): Flow<Response<User>> = flow {
        emit(Response.Loading())
        try {
            val snapshot =
                firebaseDatabase.reference.child(DATABASE_USERS).child(userUid).get().await()
            if (snapshot.exists()) {
                val user: User? = snapshot.getValue(User::class.java)
                if (user?.uid == currentUser!!.uid) {
                    emit(Response.Failure("????????????????????????????????????"))
                } else {
                    emit(Response.Success(user))
                }
            } else {
                emit(Response.Failure("????????????????????????????????????"))
            }

        } catch (e: HttpException) {
            emit(Response.Failure(e.localizedMessage ?: "?????????????????????????????????????????????????????????"))
        } catch (e: IOException) {
            emit(Response.Failure(e.localizedMessage ?: "??????????????????????????????????????????"))
        } catch (e: NullPointerException) {
            emit(Response.Failure("????????????????????????????????????"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    //family??????????????????
    fun addUserToFamily(userUid: String): Flow<Response<Nothing>> = flow {
        emit(Response.Loading())
        try {

            var isRegistered = false
            val currentUserUid = currentUser!!.uid
            val familyRf = firebaseDatabase.reference.child(DATABASE_FAMILY)
            val snapshots = familyRf.child(currentUserUid).get().await()

            if (snapshots.exists()) {
                for (snapshot in snapshots.children) {
                    if (userUid == snapshot.key) {  //????????????????????????????????????????????????
                        isRegistered = true
                        emit(Response.Failure("????????????????????????????????????"))
                    }
                }
            }
            if (!isRegistered) {    //?????????????????????????????????????????????????????????
                val familyUserObj = HashMap<String, Any>()

                familyUserObj["uid"] = userUid
                familyRf.child(currentUserUid).child(userUid).setValue(familyUserObj)

                familyUserObj["uid"] = currentUserUid
                familyRf.child(userUid).child(currentUserUid).setValue(familyUserObj)

                emit(Response.Success(data = null))
            }
        } catch (e: HttpException) {
            emit(Response.Failure(e.localizedMessage ?: "?????????????????????????????????????????????????????????"))
        } catch (e: IOException) {
            emit(Response.Failure(e.localizedMessage ?: "??????????????????????????????????????????"))
        } catch (e: NullPointerException) {
            emit(Response.Failure("????????????????????????????????????"))
        } catch (e: Exception) {
            emit(Response.Failure(e.message.toString()))
        }
    }

    //?????????????????????
    fun getMyFamily(): Flow<Response<MutableList<User>>> = flow {
        try {
            val family = mutableListOf<User>()
            val myFamilyRef =
                firebaseDatabase.reference.child(DATABASE_FAMILY).child(currentUser!!.uid).get()
                    .await()

            myFamilyRef.children.forEach { userSnapshot ->
                userSnapshot.key?.let { userUid ->
                    val userInfo = fetchUserInfo(userUid)
                    userInfo?.let { family.add(it) }
                }
            }
            emit(Response.Success(data = family))
        } catch (e: Throwable) {
            emit(Response.Failure(errorMessage = "????????????????????????????????????????????????"))
        }
    }

    private suspend fun fetchUserInfo(userUid: String): User? {
        return try {
            val user = firebaseDatabase.reference.child(DATABASE_USERS).child(userUid).get().await()
            val userInfo: User? = user.getValue(User::class.java)
            userInfo
        } catch (e: Throwable) {
            throw (e)
        }
    }

    //????????????????????????????????????
    fun createImageMessageFrame(receiverUid: String): Flow<Response<String>> =
        flow {
            try {
                val currentUserUid = currentUser!!.uid
                val senderRoom = currentUserUid + receiverUid
                val senderRoomRef =
                    firebaseDatabase.reference.child(DATABASE_CHAT).child(currentUserUid)
                        .child(DATABASE_CHAT_ROOMS).child(senderRoom)
                val receiverRoom = receiverUid + currentUserUid
                val receiverRoomRef =
                    firebaseDatabase.reference.child(DATABASE_CHAT).child(receiverUid)
                        .child(DATABASE_CHAT_ROOMS).child(receiverRoom)

                val imageMessageKey = firebaseDatabase.reference.push().key

                val message = Message("photo", currentUserUid, null, imageFlg = true)
                senderRoomRef.child(DATABASE_READ_CHATS).child(imageMessageKey!!).setValue(message)
                    .await()
                receiverRoomRef.child(DATABASE_NEW_CHATS).child(imageMessageKey).setValue(message)
                    .await()

                emit(Response.Success(data = imageMessageKey))

            } catch (e: Throwable) {
                emit(Response.Failure())
            }
        }

    //?????????????????????
    fun sendMessage(
        receiverUid: String,
        newMessage: String?,
        imageUri: Uri?,
        imageMessageKey: String?
    ): Flow<Response<String>> = flow {

        try {
            val currentUserUid = currentUser!!.uid
            val senderRoom = currentUserUid + receiverUid
            val senderRoomRef =
                firebaseDatabase.reference.child(DATABASE_CHAT).child(currentUserUid)
                    .child(DATABASE_CHAT_ROOMS).child(senderRoom)
            val receiverRoom = receiverUid + currentUserUid
            val receiverRef =
                firebaseDatabase.reference.child(DATABASE_CHAT).child(receiverUid)
            val receiverRoomRef =
                receiverRef
                    .child(DATABASE_CHAT_ROOMS).child(receiverRoom)

            val receiver =
                firebaseDatabase.reference.child(DATABASE_USERS).child(receiverUid).get().await()
            val receiverInfo: User? = receiver.getValue(User::class.java)

            //receiver???allNewChatCount????????????????????????
            val snapshot = receiverRef.get().await()
            val receiverAllNewChatCount: AllNewChatCount? =
                snapshot.getValue(AllNewChatCount::class.java)
            val allNewChatCountObj = HashMap<String, Any>()
            if (receiverAllNewChatCount?.allNewChatCount != null) {
                allNewChatCountObj["allNewChatCount"] =
                    receiverAllNewChatCount.allNewChatCount!! + 1
                receiverRef.updateChildren(allNewChatCountObj)
            } else {
                allNewChatCountObj["allNewChatCount"] = 1
                receiverRef.updateChildren(allNewChatCountObj)
            }

            val date = Date()
            val msgObj = HashMap<String, Any>()
            val message: Message

            msgObj["time"] = date.time
            msgObj["roomUid"] = senderRoom

            if (imageUri != null) { //?????????????????????
                message =
                    Message(
                        "photo",
                        currentUserUid,
                        imageUri.toString(),
                        timeStamp = date.time,
                        imageFlg = true
                    )
                msgObj["lastMsg"] = "photo"
            } else {    //?????????????????????
                message = Message(newMessage, currentUserUid, null, timeStamp = date.time)
                msgObj["lastMsg"] = newMessage!!
            }

            if (imageMessageKey != null) {  //?????????????????????
                setMessageToChatRoom(
                    senderRoomRef,
                    receiverRoomRef,
                    imageMessageKey,
                    message,
                    msgObj
                )
            } else {
                val randomKey = firebaseDatabase.reference.push().key
                setMessageToChatRoom(senderRoomRef, receiverRoomRef, randomKey!!, message, msgObj)
            }

            emit(Response.Success(data = receiverInfo?.myToken))
        } catch (e: Throwable) {
            emit(Response.Failure(errorMessage = "?????????????????????????????????????????????"))
        }
    }

    private suspend fun setMessageToChatRoom(
        senderRoomRef: DatabaseReference,
        receiverRoomRef: DatabaseReference,
        nodeKey: String,
        message: Message,
        msgObj: HashMap<String, Any>
    ) {
        senderRoomRef.child(DATABASE_READ_CHATS).child(nodeKey).setValue(message).await()
        senderRoomRef.updateChildren(msgObj).await()

        val receiverNewChatCount =
            receiverRoomRef.child(DATABASE_NEW_CHATS).get().await().childrenCount

        //???????????????????????????????????????????????????????????????newReciveMessageToRead???????????????????????????newChatCount????????????????????????newChatCount???0??????????????????????????????
        //newChat????????????????????????????????????????????????????????????1?????????????????????????????????????????????????????????????????????????????????setValue???????????????
        msgObj["newChatCount"] = receiverNewChatCount + 1
        receiverRoomRef.updateChildren(msgObj).await()
        receiverRoomRef.child(DATABASE_NEW_CHATS).child(nodeKey).setValue(message).await()
    }

    //???????????????????????????????????????????????????
    fun newReceiveMessageToRead(receiverUid: String): Flow<Response.Failure<Nothing>> =
        callbackFlow {
            var senderRef: DatabaseReference? = null
            var senderRoomRef: DatabaseReference? = null
            var receiverRoomRef: DatabaseReference? = null
            var newChatRef: DatabaseReference? = null
            var readChatRef: DatabaseReference? = null
            try {
                val currentUserUid = currentUser!!.uid
                //????????????????????????????????????
                val senderRoom = currentUserUid + receiverUid
                //?????????????????????
                val receiverRoom = receiverUid + currentUserUid
                senderRef =
                    firebaseDatabase.reference.child(DATABASE_CHAT).child(currentUserUid)
                senderRoomRef =
                    senderRef
                        .child(DATABASE_CHAT_ROOMS).child(senderRoom)
                receiverRoomRef = firebaseDatabase.reference.child(DATABASE_CHAT).child(receiverUid)
                    .child(DATABASE_CHAT_ROOMS).child(receiverRoom)
                //???????????????
                newChatRef =
                    senderRoomRef
                        .child(DATABASE_NEW_CHATS)
                //?????????????????????
                readChatRef =
                    senderRoomRef
                        .child(DATABASE_READ_CHATS)
            } catch (e: Throwable) {
                close(e)
            }

            val valueListener = object : ValueEventListener {
                override fun onDataChange(snapshots: DataSnapshot) {
                    var allNewChatCount: AllNewChatCount? = null
                    senderRef!!.get().addOnSuccessListener { snapshot ->
                        allNewChatCount = snapshot.getValue(AllNewChatCount::class.java)
                    }.addOnFailureListener {
                        close(it)
                    }

                    //?????????????????? and ?????????????????????????????????????????????????????????
                    if (snapshots.exists()) {
                        val currentChatCount = snapshots.childrenCount
                        val allNewChatCountObj = HashMap<String, Any>()
                        if (allNewChatCount?.allNewChatCount == null || allNewChatCount?.allNewChatCount == 0) {
                            allNewChatCountObj["allNewChatCount"] = 0
                            senderRef.updateChildren(allNewChatCountObj)
                        } else {
                            allNewChatCountObj["allNewChatCount"] =
                                allNewChatCount!!.allNewChatCount!!.minus(currentChatCount)
                            senderRef.updateChildren(allNewChatCountObj)
                        }
                        val newChatCountObj = HashMap<String, Any>()
                        newChatCountObj["newChatCount"] = 0
                        senderRoomRef!!.updateChildren(newChatCountObj)

                        //??????????????????
                        for (snapshot in snapshots.children) {
                            val nodeId = snapshot.key
                            val message: Message? = snapshot.getValue(Message::class.java)
                            val readMessageObj = HashMap<String, Any>()
                            readMessageObj["read"] = true
                            nodeId?.let {
                                receiverRoomRef!!.child(DATABASE_READ_CHATS).child(it)
                                    .updateChildren(readMessageObj)
                            }
                            readChatRef!!.child(nodeId!!).setValue(message).addOnCompleteListener {
                                if (it.isSuccessful) {
                                    newChatRef?.child(nodeId)?.setValue(null)
                                }
                            }.addOnFailureListener {
                                close(it)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            newChatRef?.addValueEventListener(valueListener)
            awaitClose {
                trySendBlocking(Response.Failure("??????????????????????????????????????????????????????"))
                newChatRef?.removeEventListener(valueListener)
            }
        }

    //??????????????????????????????????????????????????????????????????receiver????????????
    fun registerReceiverInfoToSenderRoom(): Flow<Response<Nothing>> = callbackFlow {
        trySendBlocking(Response.Loading())
        val receiverUidArray = mutableListOf<String>()
        var chatRooms: DatabaseReference? = null
        try {
            val currentUserUid = currentUser!!.uid
            chatRooms =
                firebaseDatabase.reference.child(DATABASE_CHAT).child(currentUserUid)
                    .child(DATABASE_CHAT_ROOMS)
            val chatRoomsSnap = chatRooms.get().await()

            val users = firebaseDatabase.reference.child(DATABASE_USERS).get().await()

            if (chatRoomsSnap.exists()) {
                for (snapshot in chatRoomsSnap.children) {
                    val senderRoomUid = snapshot.key.toString()
                    val receiver = senderRoomUid.removeRange(0, currentUserUid.length)
                    receiverUidArray.add(receiver)
                }
            }

            for (receiverUid in receiverUidArray) {

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
                        }
                        chatRooms
                            .child(currentUser!!.uid + receiverUid)
                            .updateChildren(receiverInfoObj).await()
                    }
                }
            }
        } catch (e: Throwable) {
            close(e)
        }

        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                firebaseDatabase.reference.child(DATABASE_USERS).get()
                    .addOnSuccessListener { users ->
                        if (snapshots.exists()) {
                            for (snapshot in snapshots.children) {
                                val senderRoomUid = snapshot.key.toString()
                                val receiverUid =
                                    senderRoomUid.removeRange(0, currentUser!!.uid.length)
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
                                                ?.child(currentUser!!.uid + receiverUid)
                                                ?.updateChildren(receiverInfoObj)
                                        }
                                    }
                                }
                            }
                        }
                    }.addOnFailureListener {
                        close(it)
                    }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        chatRooms?.addValueEventListener(valueListener)
        awaitClose {
            trySendBlocking(Response.Failure(errorMessage = "?????????????????????????????????????????????"))
            chatRooms?.removeEventListener(valueListener)
        }
    }

    //??????????????????????????????
    fun updateTimeActionDetected() {
        val currentTimeMinutes = System.currentTimeMillis() / 1000 / 60
        val beaconObj = HashMap<String, Any>()
        beaconObj["updateTime"] = currentTimeMinutes
        try {
            firebaseDatabase.reference.child(DATABASE_USERS).child(currentUser!!.uid)
                .updateChildren(beaconObj)
        } catch (e: Throwable) {
            Log.d("Mamoru", e.message.toString())
        }
    }


    fun updateExitToMyBeacon(isExitBeacon: Boolean) {
        val beaconObj = HashMap<String, Any>()
        beaconObj["exitBeacon"] = isExitBeacon
        try {
            firebaseDatabase.reference.child(DATABASE_USERS).child(currentUser!!.uid)
                .updateChildren(beaconObj)
        } catch (e: Throwable) {
            Log.d("Mamoru", e.message.toString())
        }
    }

    //????????????????????????????????????????????????????????????????????????ID?????????null?????????
    fun updateMyBeacon(beaconFlg: Boolean): Flow<Response<Nothing>> = flow {
        try {
            val beaconObj = HashMap<String, Any>()
            val currentTimeMinutes = System.currentTimeMillis() / 1000 / 60
            beaconObj["beacon"] = beaconFlg
            beaconObj["updateTime"] = currentTimeMinutes

            firebaseDatabase.reference.child(DATABASE_USERS).child(currentUser!!.uid)
                .updateChildren(beaconObj).await()
            emit(Response.Success())
        } catch (e: Throwable) {
            emit(Response.Failure(errorMessage = "?????????????????????????????????????????????"))
        }
    }

    fun getAllNewChatCount(): Flow<Response<Int>> = callbackFlow {
        var userChatRef: DatabaseReference? = null
        try {
            userChatRef = firebaseDatabase.reference.child(DATABASE_CHAT).child(currentUser!!.uid)

            val snapshot = userChatRef.get().await()
            if (snapshot.exists()) {
                val allNewChatCount: AllNewChatCount? =
                    snapshot.getValue(AllNewChatCount::class.java)
                if (allNewChatCount?.allNewChatCount == 0 || allNewChatCount?.allNewChatCount == null) {
                    trySendBlocking(Response.Success(data = 0))
                } else {
                    trySendBlocking(Response.Success(data = allNewChatCount.allNewChatCount))
                }
            }
        } catch (e: Throwable) {
            close(e)
        }

        val valueListener = object : ValueEventListener {
            override fun onDataChange(snapshots: DataSnapshot) {
                if (snapshots.exists()) {
                    val allNewChatCount: AllNewChatCount? =
                        snapshots.getValue(AllNewChatCount::class.java)

                    if (allNewChatCount?.allNewChatCount == 0 || allNewChatCount?.allNewChatCount == null) {
                        trySendBlocking(Response.Success(data = 0))
                    } else {
                        trySendBlocking(Response.Success(data = allNewChatCount.allNewChatCount))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        userChatRef?.addValueEventListener(valueListener)
        awaitClose {
            trySendBlocking(Response.Failure("????????????????????????????????????????????????????????????"))
            userChatRef?.removeEventListener(valueListener)
        }
    }

    fun getMessageOptions(receiverUid: String): FirebaseRecyclerOptions<Message> {
        val senderRoom = currentUser!!.uid + receiverUid
        val readChatRef =
            firebaseDatabase.reference.child(DATABASE_CHAT).child(currentUser!!.uid)
                .child(DATABASE_CHAT_ROOMS).child(senderRoom)
                .child(DATABASE_READ_CHATS)

        return FirebaseRecyclerOptions.Builder<Message>()
            .setQuery(readChatRef, Message::class.java)
            .build()
    }

    fun getChatRoomOptions(): FirebaseRecyclerOptions<ChatRoom> {
        val chatRooms =
            firebaseDatabase.reference.child(DATABASE_CHAT).child(currentUser!!.uid)
                .child(DATABASE_CHAT_ROOMS)

        return FirebaseRecyclerOptions.Builder<ChatRoom>()
            .setQuery(chatRooms, ChatRoom::class.java)
            .build()
    }
}