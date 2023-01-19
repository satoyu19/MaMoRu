package jp.ac.jec.cm0119.mamoru.ui.fragments.family

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentUserDetailBinding
import jp.ac.jec.cm0119.mamoru.models.User

class UserDetailFragment : Fragment() {

    private var _binding: FragmentUserDetailBinding? = null
    val binding get() = _binding!!

    private val args by navArgs<UserDetailFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserDetailBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user: User = args.user
        Glide.with(requireContext())
            .load(user.profileImage)
            .placeholder(R.drawable.ic_account)
            .into(binding.profileImage)

        binding.familyUserName.text = user.name
        binding.userId.text = user.uid
        binding.familyDescription.text = user.description
        
        if (!user.birthDay.isNullOrEmpty()) {
            binding.familyBirthday.text = user.birthDay
            binding.birthdayIcon.visibility = View.VISIBLE
        }

        binding.back4.setOnClickListener { 
            val action = UserDetailFragmentDirections.actionUserDetailFragmentToFamilyFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }

        // TODO: チャット画面に遷移
        binding.chatBtn.setOnClickListener { 
            val action = UserDetailFragmentDirections.actionUserDetailFragmentToChatFragment(userName = user.name!!, userId = user.uid!!, profileImage = user.profileImage)
            NavHostFragment.findNavController(this) .navigate(action)
        }
        
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}