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
import jp.ac.jec.cm0119.mamoru.databinding.FragmentRegisterBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.auth.RegisterViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(layoutInflater)
        binding.viewModel = viewModel

        /**Flow collect**/
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.user.collect { state ->
                    if (state.isLoading) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.registerLayout.visibility = View.INVISIBLE
                    }
                    if (state.isSuccess) {   //成功
                        gotoSetupProfileFragment()
                    }
                    if (state.isFailure) {
                        binding.progressBar.visibility = View.INVISIBLE
                        binding.registerLayout.visibility = View.VISIBLE
                        Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //バックボタン
        binding.back.setOnClickListener {
            val action = RegisterFragmentDirections.actionRegisterFragmentToLoginFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }

        //registerボタン 有効 or 無効
        binding.registerMailAddress.addTextChangedListener(RegisterButtonObserver())
        binding.registerPassword.addTextChangedListener(RegisterButtonObserver())
    }

    private fun gotoSetupProfileFragment() {
        val action = RegisterFragmentDirections.actionRegisterFragmentToSetupProfileFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class RegisterButtonObserver() : TextWatcher {

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            var mailInput: String = binding.registerMailAddress.text.toString()
            var passwordInput: String = binding.registerPassword.text.toString()

            if (mailInput.isNotBlank() && passwordInput.isNotBlank()) {
                binding.registerBtn.isEnabled = true
                binding.registerBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.active))
            } else {
                binding.registerBtn.isEnabled = false
                binding.registerBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.inactive))
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(p0: Editable?) {}

    }
}