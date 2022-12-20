package jp.ac.jec.cm0119.mamoru.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentProfileSetupBinding

/**
 * this.startDateLayout.setEndIconOnClickListener(this);
 */

class ProfileSetupFragment : Fragment() {

    private var _binding: FragmentProfileSetupBinding? = null
    private val binding: FragmentProfileSetupBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileSetupBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nextBtn.setOnClickListener{

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}