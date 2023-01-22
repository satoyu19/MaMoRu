package jp.ac.jec.cm0119.mamoru.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
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
        viewModel.getUserData()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                /**Flow collect**/
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
        }

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
}