package jp.ac.jec.cm0119.mamoru.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.RowChatRoomBinding
import jp.ac.jec.cm0119.mamoru.models.ChatRoom
import jp.ac.jec.cm0119.mamoru.ui.fragments.chat.ChatRoomsFragmentDirections

class ChatRoomAdapter (
    private val options: FirebaseRecyclerOptions<ChatRoom>
    ) : FirebaseRecyclerAdapter<ChatRoom, ChatRoomAdapter.ChatUserViewHolder>(options) {

    inner class ChatUserViewHolder(itemView: View): ViewHolder(itemView) {
        val binding: RowChatRoomBinding = RowChatRoomBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_chat_room, parent, false)
        return ChatUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatUserViewHolder, position: Int, model: ChatRoom) {
        val chatRoom = options.snapshots[position]

        Glide.with(holder.binding.root.context)
            .load(chatRoom.profileImage)
            .placeholder(R.drawable.ic_account)
            .into(holder.binding.userImage)

        holder.binding.userName.text = chatRoom.name
        chatRoom.newChatCount?.let {
            if (it != 0) {
                holder.binding.newMessageCount.visibility = View.VISIBLE
                holder.binding.newMessageCount.text = it.toString()
            } else {
                holder.binding.newMessageCount.visibility = View.INVISIBLE
                holder.binding.newMessageCount.visibility = View.INVISIBLE
            }
        }
        holder.binding.lastMessage.text = chatRoom.lastMsg

        holder.binding.rowChat.setOnClickListener {
            val receiverUid = chatRoom.receiverUid
            val receiverName = chatRoom.name
            val receiverProfileImage = chatRoom.profileImage
            val action = ChatRoomsFragmentDirections.actionChatRoomsFragmentToChatFragment(userName = receiverName!!, userId = receiverUid!!, profileImage = receiverProfileImage)
            holder.itemView.findNavController().navigate(action)
        }
    }
}

