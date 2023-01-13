package jp.ac.jec.cm0119.mamoru.viewmodels.chat

import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(firebaseRepo: FirebaseRepository): ViewModel() {

    var message = ObservableField<String>()

}