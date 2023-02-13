package jp.ac.jec.cm0119.mamoru.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.ActivityMainBinding
import jp.ac.jec.cm0119.mamoru.ui.fragments.chat.ChatFragment
import jp.ac.jec.cm0119.mamoru.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.userState.collect { state ->
                        if (state.isLoading) {
                            binding.progressBar2.visibility = View.VISIBLE
                            binding.navHostFragment2.visibility = View.INVISIBLE
                            binding.bottomNavigationView.visibility = View.INVISIBLE
                        }
                        if (state.isSuccess) {
                            binding.progressBar2.visibility = View.GONE
                            binding.navHostFragment2.visibility = View.VISIBLE
                            binding.bottomNavigationView.visibility = View.VISIBLE
                            state.user?.let { viewModel.saveMyInfo(it) }
                        }
                        if (state.isFailure) {  //databaseにuserの登録がないか、auth登録が済んでいない
                            val intent = Intent(this@MainActivity, LoginActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
                launch {
                    viewModel.allNewChatCount.collect { state ->
                        if (state.isSuccess) {
                            state.allNewChatCount?.let {
                                if (it != 0) {
                                    val badge =
                                        binding.bottomNavigationView.getOrCreateBadge(R.id.chatRoomsFragment)
                                    badge.number = state.allNewChatCount
                                } else {
                                    binding.bottomNavigationView.removeBadge(R.id.chatRoomsFragment)
                                }
                            }
                        }
                        if (state.isFailure) {
                            Toast.makeText(this@MainActivity, state.error, Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
            }
        }

        viewModel.getAllNewChatCount()
        viewModel.getUserData()

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment2) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.userDetailFragment || destination.id == R.id.chatFragment || destination.id == R.id.registerFamilyFragment || destination.id == R.id.upImageFragment) {
                binding.bottomNavigationView.visibility = View.GONE
            } else {
                binding.bottomNavigationView.visibility = View.VISIBLE
            }
        }

        binding.bottomNavigationView.setupWithNavController(navController)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        navHostFragment.childFragmentManager.primaryNavigationFragment?.let { fragment ->
            if (fragment is ChatFragment) {
                binding.bottomNavigationView.visibility = View.GONE
            }
        }
    }


    override fun dispatchTouchEvent(motionEvent: MotionEvent?): Boolean {
        val event = motionEvent ?: return false
        if (event.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val inputManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
        return super.dispatchTouchEvent(event)
    }
}