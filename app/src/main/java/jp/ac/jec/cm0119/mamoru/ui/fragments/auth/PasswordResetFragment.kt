package jp.ac.jec.cm0119.mamoru.ui.fragments.auth

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentPasswordResetBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.auth.PasswordResetViewModel
import kotlinx.coroutines.launch

// TODO: SHAの登録が必要？メールが送信されない。処理はOK。アプリ消去でもログアウト状態とされ、処理される 
@AndroidEntryPoint
class PasswordResetFragment : Fragment() {

    private var _binding: FragmentPasswordResetBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PasswordResetViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPasswordResetBinding.inflate(layoutInflater)
        binding.viewModel = viewModel

        /**Flow collect**/
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {

                viewModel.resetResult.collect { state ->
                    if (state?.isLoading == true) {
                        binding.progressBar5.visibility = View.VISIBLE
                        binding.passwordResetLayout.visibility = View.INVISIBLE
                    }
                    if (state?.isSuccess == true) {   //成功
                        showDialog()
                        gotoLoginFragment()
                    }
                    if (state?.isFailure == true) {
                        binding.progressBar5.visibility = View.INVISIBLE
                        binding.passwordResetLayout.visibility = View.VISIBLE
                        Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resetMailAddress.addTextChangedListener(PasswordResetButtonObserver(binding.resetBtn))
        binding.back2.setOnClickListener { gotoLoginFragment() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showDialog() {
        val newFragment = CompleteResetDialog()
        newFragment.show(childFragmentManager, "reset")
    }

    private fun gotoLoginFragment() {
        val action = PasswordResetFragmentDirections.actionPasswordResetFragmentToLoginFragment()
        NavHostFragment.findNavController(this).navigate(action)
    }

    inner class PasswordResetButtonObserver(private val resetBtn: Button) : TextWatcher {

        override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            resetBtn.isEnabled = charSequence.toString().trim().isNotEmpty()
            if (charSequence.toString().trim().isNotEmpty()) {
                binding.resetBtn.isEnabled = true
                binding.resetBtn.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.active
                    )
                )
            } else {
                binding.resetBtn.isEnabled = false
                binding.resetBtn.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.inactive
                    )
                )
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(p0: Editable?) {}

    }
}

class CompleteResetDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("指定されたメールアドレスにパスワードリセットメールを送信しました。")
                .setPositiveButton("OK",
                    DialogInterface.OnClickListener { _, _ ->

                    })

            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}