package jp.ac.jec.cm0119.mamoru.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentSetupProfileBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.SetupProfileViewModel
import kotlinx.coroutines.launch

// TODO: この画面にこれたらauth認証は終わっているため、それまでのviewのスタック(navigationで消す)は消す。この状態でアプリの閉じて再度開かれた時には、profile画面からスタートするようにする
@AndroidEntryPoint
class SetupProfileFragment : Fragment() {

    private var _binding: FragmentSetupProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SetupProfileViewModel by viewModels()

    private val galleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            imageUri?.let {
                viewModel.upLoadProfile(imageUri)
            }
        }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupProfileBinding.inflate(layoutInflater)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.profileImageData.collect { state ->
                        if (state.isLoading) {
                            //todo: viewにローディングViewを表示
                        }
                        if (state.data != null) {   //成功
                            Log.d("Test", state.data.toString())

                        }
                        if (state.error.isNotBlank()) {
                            Log.d("Test", state.error)
                        }
                    }
                }
//                launch {
//                    viewModel.user.collect { state ->   //DatabaseState
//                        // TODO: 分岐で処理、
//                        if (state.isLoading) {
//                            //todo: viewにローディングViewを表示
//                        }
//                        if (state.data != null) {   //成功
//                            Log.d("Test", state.data.toString())
//
//                        }
//                        if (state.error.isNotBlank()) {
//                            Log.d("Test", state.error)
//                        }
//                    }
//                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("Test", viewModel.profileImageUrl.toString())
        Log.d("Test", viewModel.firebaseDatabase.reference.child("c").database.toString())
        //プロフィール画像
        viewModel.profileImageUrl?.observe(viewLifecycleOwner) { imageUrl ->
            Glide.with(requireContext())
                .load(imageUrl.toString())
                .error(R.drawable.ic_account)
                .into(binding.profileImage)
        }
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
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(p0: Editable?) {}

    }
}