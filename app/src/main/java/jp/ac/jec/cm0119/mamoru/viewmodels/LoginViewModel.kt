package jp.ac.jec.cm0119.mamoru.viewmodels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseRepo: FirebaseRepository
    ): ViewModel() {

//        fun getCurrentUser(): FirebaseUser? = firebaseRepo.firebaseAuth.currentUser

}