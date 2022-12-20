package jp.ac.jec.cm0119.mamoru.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentLoginBinding
import jp.ac.jec.cm0119.mamoru.databinding.FragmentRegisterBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.LoginViewModel

@AndroidEntryPoint
class LoginFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentLoginBinding? = null
    private val binding: FragmentLoginBinding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(layoutInflater)
        binding.register.setOnClickListener(this)
        binding.passwordReset.setOnClickListener(this)
        binding.loginBtn.setOnClickListener(this)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.getCurrentUser() != null) {
           val action = LoginFragmentDirections.actionLoginFragmentToMainActivity()
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(view: View?) {
        if (view != null) {
            when (view.id) {
                binding.register.id -> {
                    val action = LoginFragmentDirections.actionLoginFragmentToProfileSetupFragment()
                    NavHostFragment.findNavController(this).navigate(action)
                }
                binding.passwordReset.id -> {
                    return
                }
                binding.loginBtn.id -> {
                    return
                }
            }
        }
    }
}