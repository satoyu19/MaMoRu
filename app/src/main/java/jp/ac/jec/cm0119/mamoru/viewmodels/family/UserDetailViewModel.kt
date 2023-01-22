package jp.ac.jec.cm0119.mamoru.viewmodels.family

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(private val firebaseRepo: FirebaseRepository): ViewModel() {


}