package jp.ac.jec.cm0119.mamoru.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.ActivityMainBinding
import jp.ac.jec.cm0119.mamoru.ui.fragments.FamilyFragment
import jp.ac.jec.cm0119.mamoru.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                /**Flow collect**/
                viewModel.userState.collect { state ->
                    if (state.isLoading) {
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    if (state.isSuccess) {
                        binding.progressBar2.visibility = View.GONE
                        binding.navHostFragment2.visibility = View.VISIBLE
                        binding.bottomNavigationView.visibility = View.VISIBLE
                        state.user?.let { viewModel.saveMyState(it) }
                    }
                    if (state.isFailure) {
                        Log.d("Test", "databaseにuserの登録がないか、auth登録が済んでいない")
                       val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
        viewModel.getUserData()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment2) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavigationView.setupWithNavController(navController)

        setContentView(binding.root)
    }
}