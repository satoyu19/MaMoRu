package jp.ac.jec.cm0119.mamoru.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.FragmentChatRoomsBinding
import jp.ac.jec.cm0119.mamoru.databinding.RowChatRoomBinding
import jp.ac.jec.cm0119.mamoru.models.ChatRoom

//ChatRoomAdapter
//class MessageRoomAdapter (private val options: FirebaseRecyclerOptions<ChatRoom>): FirebaseRecyclerAdapter<ChatRoom, MessageRoomAdapter.ChatUserViewHolder>(options) {
//
//    inner class ChatUserViewHolder(itemView: View): ViewHolder(itemView) {
//        fun bind(item: ChatRoom) {
//            Glide.with(binding.root.context)
//                .load(item.profileImage)
//                .placeholder(R.drawable.ic_account)
//                .into(binding.userImage)
//
//            binding.userName.text = item.name
//            binding.lastMessage.text = item.lastMsg
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatUserViewHolder {
//        val inflater = LayoutInflater.from(parent.context)
//
//        val view = inflater.inflate(R.layout.row_chat_room, parent, false)
//        val binding = RowChatRoomBinding.bind(view)
//        return ChatUserViewHolder(binding)
//    }
//
//    override fun onBindViewHolder(holder: ChatUserViewHolder, position: Int, model: ChatRoom) {
//        holder.bind(options.snapshots[position])
//    }
//}

