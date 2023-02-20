package jp.ac.jec.cm0119.mamoru.ui.fragments.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentLoginBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.auth.LoginViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding  get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(layoutInflater)
        binding.viewModel = viewModel

        if (viewModel.authCurrentUser != null) {    //auth登録が済んでおり、user情報登録が済んでいない場合
            val action = LoginFragmentDirections.actionLoginFragmentToSetupProfileFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.verification.collect { State ->
                    if (State?.isLoading == true) {
                        binding.progressBar4.visibility = View.VISIBLE
                        binding.loginLayout.visibility = View.INVISIBLE
                    }
                    if (State?.isSuccess == true) {
                        gotoMainActivity()
                    }
                    if (State?.isFailure == true) {
                        binding.progressBar4.visibility = View.INVISIBLE
                        binding.loginLayout.visibility = View.VISIBLE
                        Toast.makeText(context, State.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginMailAddress.addTextChangedListener(LoginButtonObserver())
        binding.loginPassword.addTextChangedListener(LoginButtonObserver())

        binding.register.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
        binding.passwordReset.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToPasswordResetFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun gotoMainActivity() {
        val action = LoginFragmentDirections.actionLoginFragmentToMainActivity()
        NavHostFragment.findNavController(this).navigate(action)
    }

    inner class LoginButtonObserver : TextWatcher {

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            val mailInput: String = binding.loginMailAddress.text.toString()
            val passwordInput: String = binding.loginPassword.text.toString()

            if (mailInput.isNotBlank() && passwordInput.isNotBlank()) {
                binding.loginBtn.isEnabled = true
                binding.loginBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.active))
            } else {
                binding.loginBtn.isEnabled = false
                binding.loginBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.inactive))
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(p0: Editable?) {}

    }
}