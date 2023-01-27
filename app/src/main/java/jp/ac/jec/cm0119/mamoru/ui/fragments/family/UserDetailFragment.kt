package jp.ac.jec.cm0119.mamoru.ui.fragments.family

import android.os.Bundle
import android.util.Log
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

        if (user.beacon == true) {
            binding.beaconIcon.visibility = View.VISIBLE
            Log.d("Test", user.exitBeacon.toString())
            if (user.exitBeacon == true) {
                binding.beaconTime.text = "外出中"
            } else {
                val currentTimeMinutes = (System.currentTimeMillis() / 1000 / 60) + 1
                val timeDifference = currentTimeMinutes - user.updateTime!!
                if (timeDifference >= 60) {
                    binding.beaconTime.text = "1時間以上前に行動を検知"
                } else {
                    binding.beaconTime.text = "${timeDifference}分前に行動を検知"
                }
            }
        }

        binding.back4.setOnClickListener {
            val action = UserDetailFragmentDirections.actionUserDetailFragmentToFamilyFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }

        binding.chatBtn.setOnClickListener {
            val action = UserDetailFragmentDirections.actionUserDetailFragmentToChatFragment(
                userName = user.name!!,
                userId = user.uid!!,
                profileImage = user.profileImage
            )
            NavHostFragment.findNavController(this).navigate(action)
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}