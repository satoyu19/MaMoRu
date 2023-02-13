package jp.ac.jec.cm0119.mamoru.adapter

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.ReceiveImageMessageBinding
import jp.ac.jec.cm0119.mamoru.databinding.ReceiveMessageBinding
import jp.ac.jec.cm0119.mamoru.databinding.SendImageMessageBinding
import jp.ac.jec.cm0119.mamoru.databinding.SendMessageBinding
import jp.ac.jec.cm0119.mamoru.models.Message
import jp.ac.jec.cm0119.mamoru.ui.fragments.chat.ChatFragmentDirections
import jp.ac.jec.cm0119.mamoru.ui.fragments.family.FamilyFragmentDirections
import jp.ac.jec.cm0119.mamoru.utils.Constants.ITEM_RECEIVE
import jp.ac.jec.cm0119.mamoru.utils.Constants.ITEM_RECEIVE_IMG
import jp.ac.jec.cm0119.mamoru.utils.Constants.ITEM_SEND
import jp.ac.jec.cm0119.mamoru.utils.Constants.ITEM_SEND_IMG

class ChatAdapter(
    private val options: FirebaseRecyclerOptions<Message>,
    private val myUid: String): FirebaseRecyclerAdapter<Message, ViewHolder>(options) {

    inner class SentMsgHolder(itemView: View): ViewHolder(itemView) {
        val binding: SendMessageBinding = SendMessageBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView: View): ViewHolder(itemView) {
        val binding: ReceiveMessageBinding = ReceiveMessageBinding.bind(itemView)
    }

    inner class SendImgMsgHolder(itemView: View): ViewHolder(itemView) {
        val binding: SendImageMessageBinding = SendImageMessageBinding.bind(itemView)
    }

    inner class ReceiveImgMsgHolder(itemView: View): ViewHolder(itemView) {
        val binding: ReceiveImageMessageBinding = ReceiveImageMessageBinding.bind(itemView)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_SEND -> {
                val view = inflater.inflate(R.layout.send_message, parent, false)
                SentMsgHolder(view)
            }
            ITEM_RECEIVE -> {
                val view = inflater.inflate(R.layout.receive_message, parent, false)
                ReceiveMsgHolder(view)
            }
            ITEM_SEND_IMG -> {
                val view = inflater.inflate(R.layout.send_image_message, parent, false)
                SendImgMsgHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.receive_image_message, parent, false)
                ReceiveImgMsgHolder(view)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = options.snapshots[position]
        val isMyMessage = message.senderId == myUid
        return if (isMyMessage) { //自分
            if (message.imageFlg) ITEM_SEND_IMG else ITEM_SEND
        } else {    //相手
            if (message.imageFlg) ITEM_RECEIVE_IMG else ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int, model: Message) {
        val message = options.snapshots[position]
        when (viewHolder) {
            is SentMsgHolder -> {
                viewHolder.binding.message.text = message.message
                if (message.read) {
                    viewHolder.binding.readMessage.visibility = View.VISIBLE
                }
            }
            is SendImgMsgHolder -> {
                Glide.with(viewHolder.binding.root.context).load(message.imageUrl).placeholder(R.drawable.ic_image)
                    .into(viewHolder.binding.image)
                viewHolder.binding.image.setOnClickListener {
                    val action = ChatFragmentDirections.actionChatFragmentToUpImageFragment(message.imageUrl!!)
                    viewHolder.itemView.findNavController().navigate(action)
                }
                if (message.read) {
                    viewHolder.binding.readImageMessage.visibility = View.VISIBLE
                }
            }
            is ReceiveMsgHolder -> {
                viewHolder.binding.message.text = message.message
            }
            else -> {
                if (viewHolder !is ReceiveImgMsgHolder) {
                    return
                }
                Glide.with(viewHolder.binding.root.context).load(message.imageUrl).placeholder(R.drawable.ic_image)
                    .into(viewHolder.binding.image)
                viewHolder.binding.image.setOnClickListener {
                    val action = ChatFragmentDirections.actionChatFragmentToUpImageFragment(message.imageUrl!!)
                    viewHolder.itemView.findNavController().navigate(action)
                }
            }
        }
    }
}