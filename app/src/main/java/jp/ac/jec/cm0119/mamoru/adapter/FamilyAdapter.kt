package jp.ac.jec.cm0119.mamoru.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.RowFamilyBinding
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.ui.fragments.family.FamilyFragmentDirections
import jp.ac.jec.cm0119.mamoru.ui.fragments.family.UserDetailFragmentDirections

class FamilyAdapter(private val options: FirebaseRecyclerOptions<User>): FirebaseRecyclerAdapter<User, FamilyAdapter.UserViewHolder>(options) {

    class UserViewHolder(val binding: RowFamilyBinding): ViewHolder(binding.root) {
        fun bind(item: User) {
            Log.d("Test", "OK")
            Glide.with(binding.userImage.context)
                .load(item.profileImage)
                .error(R.drawable.ic_account)
                .into(binding.userImage)

            binding.userName.text = item.name
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        val view = inflater.inflate(R.layout.row_family, parent, false)
        val binding = RowFamilyBinding.bind(view)

        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int, model: User) {

        val user: User = options.snapshots[position]
        holder.bind(user)

        holder.binding.familyRowLayout.setOnClickListener {
            val action = FamilyFragmentDirections.actionFamilyFragmentToUserDetailFragment(user)
            holder.itemView.findNavController().navigate(action)
        }
    }
}