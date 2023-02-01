package jp.ac.jec.cm0119.mamoru.ui.fragments.chat

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.adapter.ChatAdapter
import jp.ac.jec.cm0119.mamoru.databinding.FragmentChatBinding
import jp.ac.jec.cm0119.mamoru.viewmodels.chat.ChatViewModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatViewModel by viewModels()

    private val args by navArgs<ChatFragmentArgs>()

    private lateinit var adapter: ChatAdapter
    private lateinit var manager: LinearLayoutManager

    private val galleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
            imageUri?.let {
                viewModel.addImageToStorage(it)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(layoutInflater)
        binding.viewModel = viewModel

        /**Flow collect**/
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.imageMessage.collect { state ->
                        if (state?.isSuccess == true) {
                            viewModel.sendMessage(state.data)
                        }
                        if (state?.isFailure == true) {
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                launch {
                    viewModel.sendMessage.collect {state ->
                        if (state?.isSuccess == true) {
                            Log.d("Test", "onCreateView: ${state.token}")
                            state.token?.let { viewModel.sendNotification(it) }
                        }
                        if (state?.isFailure == true) {
                            Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setReceiverUid(args.userId)
        viewModel.receiveMessageToRead()
        viewModel.setOptions()

        Glide.with(requireContext())
            .load(args.profileImage)
            .placeholder(R.drawable.ic_account)
            .into(binding.profileImage)

        binding.name.text = args.userName

        binding.back5.setOnClickListener {
            NavHostFragment.findNavController(this).navigateUp()
        }

        binding.send.setOnClickListener {
            viewModel.sendMessage()
        }

        binding.image.setOnClickListener {
            galleryResult.launch("image/*")
        }

        binding.messageBox.addTextChangedListener(SendButtonObserver(binding.send))


        viewModel.authCurrentUser?.let { my ->
            var options = viewModel.options!!
            adapter = ChatAdapter(options, my.uid)
            manager = LinearLayoutManager(requireContext())
            binding.chatRecycleView.layoutManager = manager
            binding.chatRecycleView.adapter = adapter
        }

        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.chatRecycleView.smoothScrollToPosition(adapter.itemCount)
            }
        })

        viewModel.readReceiveMessageFailure.observe(viewLifecycleOwner, Observer { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        })

        adapter.startListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.stopListening()
        _binding = null
    }

    inner class SendButtonObserver(private val sendBtn: ImageView) : TextWatcher {

        override fun onTextChanged(charSequence: CharSequence?, p1: Int, p2: Int, p3: Int) {
            if (charSequence.toString().trim().isNotEmpty()) {
                binding.send.isEnabled = true
                sendBtn.setImageResource(R.drawable.ic_send)
            } else {
                binding.send.isEnabled = false
                sendBtn.setImageResource(R.drawable.ic_send_gray)
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(p0: Editable?) {}

    }
}