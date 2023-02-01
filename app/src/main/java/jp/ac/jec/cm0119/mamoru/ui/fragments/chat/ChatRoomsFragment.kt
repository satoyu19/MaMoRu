package jp.ac.jec.cm0119.mamoru.ui.fragments.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.adapter.ChatRoomAdapter
import jp.ac.jec.cm0119.mamoru.databinding.FragmentChatRoomsBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.chat.ChatRoomViewModel
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ChatRoomsFragment : Fragment() {

    private var _binding: FragmentChatRoomsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatRoomViewModel by viewModels()

    private lateinit var adapter: ChatRoomAdapter
    private lateinit var manager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatRoomsBinding.inflate(layoutInflater)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerReceiverInfoFailure.collect { response ->
                    if (response?.isFailure == true) {
                        Toast.makeText(requireContext(), response.error, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.registerReceiverInfoToSenderRoom()
        viewModel.setOptions()

        viewModel.options?.let {
            adapter = ChatRoomAdapter(it)
            manager = LinearLayoutManager(requireContext())
            val dividerItemDecoration = DividerItemDecoration(
                requireContext(),
                LinearLayoutManager(requireContext()).orientation
            )
            binding.chatRoomRecycleView.addItemDecoration(dividerItemDecoration)
            binding.chatRoomRecycleView.layoutManager = manager
            binding.chatRoomRecycleView.adapter = adapter
        }

        adapter.startListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.stopListening()
        _binding = null
    }
}