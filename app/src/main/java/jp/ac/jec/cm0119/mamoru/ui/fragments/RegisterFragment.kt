package jp.ac.jec.cm0119.mamoru.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.databinding.FragmentRegisterBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.RegisterViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding  get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(layoutInflater)
        binding.viewModel = viewModel

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                // TODO: 各処理ログにて確認
                viewModel.user.collect { State ->
                    if (State.isLoading) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.registerLayout.visibility = View.GONE
                    }
                    if (State.isSuccess) {   //成功
                        gotoSetupProfileFragment()
                    }
                    if (State.error.isNotBlank()) {
                        State.errorType?.let {
                            binding.progressBar.visibility = View.GONE
                            binding.registerLayout.visibility = View.VISIBLE
                            // TODO: ここで型によって詳しくトーストを出力する
                            Toast.makeText(context, State.error, Toast.LENGTH_SHORT).show()
                        }
                        Log.d("Test", State.error)
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
        binding.mailAddress.addTextChangedListener(RegisterButtonObserver())
        binding.password.addTextChangedListener(RegisterButtonObserver())
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
            var mailInput: String = binding.mailAddress.text.toString()
            var passwordInput: String = binding.password.text.toString()

            binding.registerBtn.isEnabled =
                (mailInput.isNotEmpty() && passwordInput.isNotEmpty())

        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(p0: Editable?) {}

    }
}