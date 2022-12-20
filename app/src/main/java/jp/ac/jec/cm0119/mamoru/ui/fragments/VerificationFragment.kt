package jp.ac.jec.cm0119.mamoru.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentVerificationBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.LoginViewModel

@AndroidEntryPoint
class VerificationFragment : Fragment() {

    private var _binding: FragmentVerificationBinding? = null
    private val binding: FragmentVerificationBinding get() = _binding!!

//    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVerificationBinding.inflate(layoutInflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        if (viewModel.getCurrentUser() != null) {
//           val action = VerificationFragmentDirections.actionVerificationFragmentToMainActivity()
//            NavHostFragment.findNavController(this).navigate(action)
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}