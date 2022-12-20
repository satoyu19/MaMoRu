package jp.ac.jec.cm0119.mamoru.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebase: FirebaseRepository
    ): ViewModel() {

        fun getCurrentUser(): FirebaseUser? = firebase.firebaseAuth.currentUser

}