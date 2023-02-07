package jp.ac.jec.cm0119.mamoru.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.RowFamilyBinding
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.ui.fragments.family.FamilyFragmentDirections

class FamilyAdapter : ListAdapter<User, FamilyAdapter.UserViewHolder>(FamilyCallback()) {

    inner class UserViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: RowFamilyBinding = RowFamilyBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_family, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val user: User = getItem(position)
        Glide.with(holder.binding.userImage.context)
            .load(user.profileImage)
            .placeholder(R.drawable.ic_account)
            .into(holder.binding.userImage)
//
        holder.binding.userName.text = user.name

        if (user.beacon == true) {
            holder.binding.beaconUseTxt.visibility = View.VISIBLE
            if (user.exitBeacon == true) {
                holder.binding.beaconUseTxt.text = "外出中"
            } else {
                val currentTimeMinutes = (System.currentTimeMillis() / 1000 / 60) + 1 //必ず一分ずれがあるため
                val timeDifference = currentTimeMinutes - user.updateTime!!
                if (timeDifference >= 60) {
                    holder.binding.beaconUseTxt.text = "1時間"
                } else {
                    holder.binding.beaconUseTxt.text = "${timeDifference}分前"
                }
            }
            holder.binding.beaconImage.setImageResource(R.drawable.ic_beacon)
        } else {
            holder.binding.beaconImage.setImageResource(R.drawable.ic_permission_off)
            holder.binding.beaconUseTxt.visibility = View.INVISIBLE
        }
        holder.binding.familyRowLayout.setOnClickListener {
            val action =
                FamilyFragmentDirections.actionFamilyFragmentToUserDetailFragment(user)
            holder.itemView.findNavController().navigate(action)
        }
    }
}

class FamilyCallback : DiffUtil.ItemCallback<User>() {

    override fun areItemsTheSame(oldFamily: User, newFamily: User): Boolean {
        return oldFamily.uid == newFamily.uid
    }

    override fun areContentsTheSame(oldFamily: User, newFamily: User): Boolean {
        return oldFamily == newFamily
    }
}