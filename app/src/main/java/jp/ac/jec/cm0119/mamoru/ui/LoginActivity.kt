package jp.ac.jec.cm0119.mamoru.ui

import jp.ac.jec.cm0119.mamoru.R
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.databinding.ActivityLoginBinding
import jp.ac.jec.cm0119.mamoru.databinding.ActivityMainBinding
import jp.ac.jec.cm0119.mamoru.ui.fragments.LoginFragment


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

//        if (savedInstanceState == null) {
//            Log.d("Test", "LoginActivity")
//            val fragmentTransaction = supportFragmentManager.beginTransaction()
//            fragmentTransaction.commit()
//        }
        setContentView(binding.root)
    }
}