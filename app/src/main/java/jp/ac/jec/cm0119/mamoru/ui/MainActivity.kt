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
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.ActivityMainBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                /**Flow collect**/
                viewModel.userState.collect { State ->
                    if (State.isLoading) {
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    if (State.isSuccess) {   //成功
                        //登録済み
                        binding.progressBar2.visibility = View.GONE
                    }
                    if (State.isFailure) {
                        Log.d("Test", "databaseにuserの登録がないか、auth登録が済んでいない")
                       val intent = Intent(this@MainActivity, LoginActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }
        viewModel.getUserData()

        setContentView(binding.root)
    }
}