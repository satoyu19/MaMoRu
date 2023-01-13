package jp.ac.jec.cm0119.mamoru.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.ReceiveMessageBinding
import jp.ac.jec.cm0119.mamoru.databinding.SendMessageBinding
import jp.ac.jec.cm0119.mamoru.models.Message
import jp.ac.jec.cm0119.mamoru.utils.Constants.ITEM_RECEIVE
import jp.ac.jec.cm0119.mamoru.utils.Constants.ITEM_SEND

class MessageAdapter(private val options: FirebaseRecyclerOptions<Message>, private val myUid: String): FirebaseRecyclerAdapter<Message, ViewHolder>(options) {

    inner class SentMsgHolder(itemView: View): ViewHolder(itemView) {
       val binding: SendMessageBinding = SendMessageBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView: View): ViewHolder(itemView) {
        val binding: ReceiveMessageBinding = ReceiveMessageBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return if (viewType == ITEM_SEND) {
            val view = inflater.inflate(R.layout.send_message, parent, false)
            SentMsgHolder(view)
        } else {
            val view = inflater.inflate(R.layout.receive_message, parent, false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = options.snapshots[position]
        return if (myUid == message.senderId) { //自分
            ITEM_SEND
        } else {    //相手
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, model: Message) {
        val message = options.snapshots[position]

        if (holder.javaClass == SentMsgHolder::class.java) {  //自分が送信したメッセージの場合
            val viewHolder = holder as SentMsgHolder
            if (message.message.equals("photo")) {  //写真メッセージ
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLiner.visibility = View.GONE
                Glide.with(viewHolder.binding.root.context).load(message.imageUrl).placeholder(R.drawable.ic_image)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message
        } else {
            val viewHolder = holder as ReceiveMsgHolder
            if (message.message.equals("photo")) {  //写真メッセージ
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLiner.visibility = View.GONE
                Glide.with(viewHolder.binding.root.context).load(message.imageUrl).placeholder(R.drawable.ic_image)
                    .into(viewHolder.binding.image)
            }
        }
    }
}