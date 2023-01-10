package jp.ac.jec.cm0119.mamoru.viewmodels.family

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.repository.FirebaseRepository
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_CHILD1
import jp.ac.jec.cm0119.mamoru.utils.Constants.DATABASE_CHILD_FAMILY
import javax.inject.Inject

@HiltViewModel
class FamilyViewModel @Inject constructor(private val firebaseRepo: FirebaseRepository) :
    ViewModel() {

    var userName = ObservableField<String>()

    private var authCurrentUser: FirebaseUser? = firebaseRepo.currentUser

    var options: FirebaseRecyclerOptions<User>? = null
        private set

    fun buildOptions() {
        authCurrentUser?.uid?.let {

            val query: Query = FirebaseDatabase.getInstance().reference.child(DATABASE_CHILD1).child(authCurrentUser!!.uid).child(
                DATABASE_CHILD_FAMILY)

            options = FirebaseRecyclerOptions.Builder<User>()
                .setQuery(query, User::class.java)
                .build()

            Log.d("Test", options.toString())
        }
    }
}