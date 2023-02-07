package jp.ac.jec.cm0119.mamoru.ui.fragments.family

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.adapter.FamilyAdapter
import jp.ac.jec.cm0119.mamoru.databinding.FragmentFamilyBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.family.FamilyViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FamilyFragment : Fragment() {

    private var _binding: FragmentFamilyBinding? = null
    private val binding get() = _binding!!
    private lateinit var manager: LinearLayoutManager

    private val viewModel: FamilyViewModel by viewModels()

    private lateinit var adapter: FamilyAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFamilyBinding.inflate(layoutInflater)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.myFamily.collect { state ->
                    if (state?.isSuccess == true) {
                        //todo firebaseのfamilyに追加されても表示されない。別フラグメントから戻ると表示される、とりあえず次ログ入れたの確認
                        if (state.myFamily!!.isNotEmpty()) {
                            adapter.submitList(state.myFamily)
                            binding.familyRecycleView.visibility = View.VISIBLE
                            binding.notFamilyImage.visibility = View.INVISIBLE
                            binding.notFamilyTxt.visibility = View.INVISIBLE
                        } else {
                            binding.familyRecycleView.visibility = View.INVISIBLE
                            binding.notFamilyImage.visibility = View.VISIBLE
                            binding.notFamilyTxt.visibility = View.VISIBLE
                        }
                        binding.swipedLayout.isRefreshing = false
                    }
                    if (state?.isFailure == true) {

                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (viewModel.authCurrentUser != null) {
            viewModel.getMyFamily()
        }

        adapter = FamilyAdapter()
        manager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            LinearLayoutManager(requireContext()).orientation
        )
        binding.familyRecycleView.addItemDecoration(dividerItemDecoration)
        binding.familyRecycleView.layoutManager = manager
        binding.familyRecycleView.adapter = adapter

        binding.swipedLayout.setOnRefreshListener {
            viewModel.getMyFamily()
        }
        binding.swipedLayout.setProgressBackgroundColorSchemeColor(Color.TRANSPARENT)
        binding.swipedLayout.setColorSchemeColors(Color.TRANSPARENT)

        binding.addUserBtn.setOnClickListener {
            val action = FamilyFragmentDirections.actionFamilyFragmentToRegisterFamilyFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}