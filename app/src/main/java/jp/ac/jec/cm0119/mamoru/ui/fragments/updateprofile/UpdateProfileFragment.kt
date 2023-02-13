package jp.ac.jec.cm0119.mamoru.ui.fragments.updateprofile

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
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentUpdateProfileBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.updateprofile.UpdateProfileViewModel
import kotlinx.coroutines.launch


@AndroidEntryPoint
class UpdateProfileFragment : Fragment() {

    private var _binding: FragmentUpdateProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UpdateProfileViewModel by viewModels()

    private var isLoadImage = false

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
        _binding = FragmentUpdateProfileBinding.inflate(layoutInflater)
        binding.viewModel = viewModel

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                launch {
                    viewModel.profileImageData.collect { state ->
                        if (state?.isLoading == true) {
                            binding.progressBar9.visibility = View.VISIBLE
                            binding.updateBtn.isEnabled = false
                            isLoadImage = true
                        }
                        if (state?.isSuccess == true) {
                            Glide.with(requireContext())
                                .load(state.data.toString())
                                .placeholder(R.drawable.ic_account)
                                .into(binding.profileImage)
                            binding.progressBar9.visibility = View.INVISIBLE
                            isLoadImage = false
                            if (!binding.name.text.isNullOrEmpty()) {
                                binding.updateBtn.isEnabled = true
                                changeButtonColor(R.color.active)
                            }
                        }
                        if (state?.isFailure == true) {
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                            binding.progressBar9.visibility = View.INVISIBLE
                            isLoadImage = false
                            if (!binding.name.text.isNullOrEmpty()) {
                                binding.updateBtn.isEnabled = true
                                changeButtonColor(R.color.active)
                            }
                        }
                    }
                }
                launch {
                    viewModel.readMyUser.collect { myState ->
                        viewModel.setProfileImage(myState.profileImage)
                        Glide.with(requireContext())
                            .load(myState.profileImage)
                            .placeholder(R.drawable.ic_account)
                            .into(binding.profileImage)
                        viewModel.setMyState(myState)
                    }
                }
                launch {
                    viewModel.updateMyState.collect { state ->
                        if (state?.isLoading == true) {
                            binding.progressBar7.visibility = View.VISIBLE
                            binding.updateProfileLayout.visibility = View.INVISIBLE
                        }
                        if (state?.isSuccess == true) {
                            binding.progressBar7.visibility = View.INVISIBLE
                            binding.updateProfileLayout.visibility = View.VISIBLE
                            viewModel.renewalMyState(state.user!!)

                            viewModel.profileImageUrl?.let {
                                Glide.with(requireContext())
                                    .load(it)
                                    .into(binding.profileImage)
                            }
                            Toast.makeText(requireContext(), "更新しました。", Toast.LENGTH_SHORT).show()
                        }
                        if (state?.isFailure == true) {
                            viewModel.readMyUser()
                            binding.progressBar7.visibility = View.INVISIBLE
                            binding.updateProfileLayout.visibility = View.VISIBLE
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
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
        binding.updateBtn.setOnClickListener {
            viewModel.updateMyState()
        }

        binding.name.addTextChangedListener(UpdateButtonObserver(binding.updateBtn))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun changeButtonColor(color: Int) {
        binding.updateBtn.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                color
            )
        )
    }

    inner class UpdateButtonObserver(private val setupBtn: Button) : TextWatcher {

        override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            setupBtn.isEnabled = charSequence.toString().trim().isNotEmpty()
            if (charSequence.toString().trim().isNotEmpty() && !isLoadImage) {
                binding.updateBtn.isEnabled = true
                changeButtonColor(R.color.active)
            } else {
                binding.updateBtn.isEnabled = false
                changeButtonColor(R.color.inactive)
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(p0: Editable?) {}

    }
}