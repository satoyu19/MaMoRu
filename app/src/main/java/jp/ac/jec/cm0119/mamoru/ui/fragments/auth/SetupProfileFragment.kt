package jp.ac.jec.cm0119.mamoru.ui.fragments.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentSetupProfileBinding
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.viewmodels.auth.SetupProfileViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SetupProfileFragment : Fragment() {

    private var _binding: FragmentSetupProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SetupProfileViewModel by viewModels()

    private val galleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            imageUri?.let {
                viewModel.addImageToStorage(imageUri)
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupProfileBinding.inflate(layoutInflater)
        binding.viewModel = viewModel

        /**Flow collect**/
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.profileImageData.collect { state ->
                        if (state.isSuccess) {   //成功
                            Glide.with(requireContext())
                                .load(state.data.toString())
                                .placeholder(R.drawable.ic_account)
                                .into(binding.profileImage)
                        }
                        if (state.isSuccess) {
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                launch {
                    viewModel.userState.collect { state ->   //DatabaseState
                        if (state.isLoading) {
                            binding.progressBar3.visibility = View.VISIBLE
                            binding.setupLayout.visibility = View.INVISIBLE
                        }
                        if (state.isSuccess) {   //成功
                            val action = SetupProfileFragmentDirections.actionSetupProfileFragmentToMainActivity()
                            NavHostFragment.findNavController(this@SetupProfileFragment).navigate(action)
                        }
                        if (state.error.isNotBlank()) {
                            binding.progressBar3.visibility = View.INVISIBLE
                            binding.setupLayout.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //カレンダーアイコン
        binding.birthdayLayout.setEndIconOnClickListener {
            viewModel.makeCalender(childFragmentManager)
        }

        //プロフィールイメージ
        binding.profileImage.setOnClickListener {
            galleryResult.launch("image/*")
        }

        //setupボタン
        binding.setupBtn.setOnClickListener {
            viewModel.setMyState()
        }

        binding.name.addTextChangedListener(SetupButtonObserver(binding.setupBtn))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class SetupButtonObserver(private val setupBtn: Button) : TextWatcher {

        override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            setupBtn.isEnabled = charSequence.toString().trim().isNotEmpty()
            if (charSequence.toString().trim().isNotEmpty()) {
                binding.setupBtn.isEnabled = true
                binding.setupBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.active_btn))
            } else {
                binding.setupBtn.isEnabled = false
                binding.setupBtn.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.inactive_btn))
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(p0: Editable?) {}

    }
}