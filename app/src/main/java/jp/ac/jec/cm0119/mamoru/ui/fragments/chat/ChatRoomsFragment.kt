package jp.ac.jec.cm0119.mamoru.ui.fragments.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentChatRoomsBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.chat.ChatRoomViewModel

@AndroidEntryPoint
class ChatRoomsFragment : Fragment() {

    private var _binding: FragmentChatRoomsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatRoomViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       _binding = FragmentChatRoomsBinding.inflate(layoutInflater)

        return binding.root
    }
}