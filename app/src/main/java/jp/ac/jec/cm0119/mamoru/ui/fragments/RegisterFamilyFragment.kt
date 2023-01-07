package jp.ac.jec.cm0119.mamoru.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentRegisterBinding
import jp.ac.jec.cm0119.mamoru.databinding.FragmentRegisterFamilyBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.RegisterFamilyViewModel
import kotlinx.coroutines.launch

// TODO: firebaseRepoに登録するメソッド(ユーザーカードの追加ボタンで動作(currentuser.uidの配下にfamilyを作成、そこに追加))、ユーザー検索メソッド(edit入力後呼び出し)追加。
@AndroidEntryPoint
class RegisterFamilyFragment : Fragment() {

    private var _binding: FragmentRegisterFamilyBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterFamilyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterFamilyBinding.inflate(layoutInflater)
        binding.viewModel = viewModel

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.readMyUser.collect { myState ->
                        viewModel.setMyState(myState)
                    }
                }
                launch {
                    viewModel.searchUser.collect { state ->
                        if (state.isLoading) {
                            binding.progressBar6.visibility = View.VISIBLE
                        }
                        if (state.isSuccess) {   //成功
                            binding.userCardView.visibility = View.VISIBLE
                            binding.progressBar6.visibility = View.INVISIBLE
                            binding.userName.text = state.user!!.name
                            viewModel.setUserState(state.user)
                            Glide.with(requireContext())
                                .load(state.user.profileImage)
                                .error(R.drawable.ic_account)
                                .into(binding.userImage)
                        }
                        if (state.isFailure) {
                            binding.userCardView.visibility = View.INVISIBLE
                            binding.progressBar6.visibility = View.INVISIBLE
                            Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                launch {
                    viewModel.registerUser.collect { state ->
                        if (state.isLoading) {
                            binding.progressBar6.visibility = View.VISIBLE
                        }
                        if (state.isSuccess) {
                            binding.progressBar6.visibility = View.INVISIBLE
                            Toast.makeText(context, "ファミリーに追加しました。", Toast.LENGTH_SHORT).show()
                        }
                        if (state.isFailure) {
                            binding.progressBar6.visibility = View.INVISIBLE
                            Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchUserBtn.setOnClickListener {
            viewModel.searchUser()
        }

        binding.back3.setOnClickListener {
            val action =
                RegisterFamilyFragmentDirections.actionRegisterFamilyFragmentToFamilyFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }

        binding.addUserBtn.setOnClickListener {
            viewModel.registerFamily()
        }

        binding.userSearch.addTextChangedListener(SearchButtonObserver(binding.searchUserBtn))

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class SearchButtonObserver(private val button: ImageView) : TextWatcher {
        override fun onTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
            if (charSequence.toString().trim().isNotEmpty()) {
                button.isEnabled = true
                button.setImageResource(R.drawable.ic_search)
            } else {
                button.isEnabled = false
                button.setImageResource(R.drawable.ic_search_gray)
            }
        }

        override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {}
    }
}