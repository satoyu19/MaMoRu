package jp.ac.jec.cm0119.mamoru.ui.fragments.family

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.adapter.FamilyAdapter
import jp.ac.jec.cm0119.mamoru.databinding.FragmentFamilyBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.family.FamilyViewModel

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        viewModel.buildOptions()
        if (viewModel.authCurrentUser != null) {
            viewModel.getMyFamily()
        }

        viewModel.myFamily.observe(viewLifecycleOwner) { myFamily ->
            adapter = FamilyAdapter(myFamily)
            manager = LinearLayoutManager(requireContext())
            val dividerItemDecoration = DividerItemDecoration(
                requireContext(),
                LinearLayoutManager(requireContext()).orientation
            )
            binding.FamilyRecyclerView.addItemDecoration(dividerItemDecoration)
            binding.FamilyRecyclerView.layoutManager = manager
            binding.FamilyRecyclerView.adapter = adapter
        }


//        viewModel.options?.let { option ->
//            viewModel.myFamily?.let {  myFamily ->
//                adapter = FamilyAdapter(option, myFamily)
//                manager = LinearLayoutManager(requireContext())
//                val dividerItemDecoration = DividerItemDecoration(
//                    requireContext(),
//                    LinearLayoutManager(requireContext()).orientation
//                )
//                binding.FamilyRecyclerView.addItemDecoration(dividerItemDecoration)
//                binding.FamilyRecyclerView.layoutManager = manager
//                binding.FamilyRecyclerView.adapter = adapter
//            }
//        }

//        adapter.registerAdapterDataObserver()
        binding.addUserBtn.setOnClickListener {
            val action = FamilyFragmentDirections.actionFamilyFragmentToRegisterFamilyFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
    }

    override fun onPause() {
        super.onPause()
        //Firebase Realtime Database からの更新のリッスンを終了
//        adapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        //Firebase Realtime Database からの更新のリッスンを開始
//        if (viewModel.authCurrentUser != null) {
////            adapter.startListening()
//            viewModel.getMyFamily()
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}