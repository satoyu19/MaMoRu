package jp.ac.jec.cm0119.mamoru.ui.fragments.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentUpImageBinding

class UpImageFragment : Fragment() {

    private var _binding: FragmentUpImageBinding? = null
    private val binding: FragmentUpImageBinding get() = _binding!!
    private val args by navArgs<UpImageFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =  FragmentUpImageBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(requireContext())
            .load(args.imageUri)
            .placeholder(R.drawable.ic_account)
            .into(binding.imageView)

        binding.back6.setOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }
    }
}