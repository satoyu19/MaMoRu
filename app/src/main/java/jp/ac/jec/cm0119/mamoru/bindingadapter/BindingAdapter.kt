package jp.ac.jec.cm0119.mamoru.bindingadapter

import android.content.Context
import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import com.bumptech.glide.Glide
import jp.ac.jec.cm0119.mamoru.R

class BindingAdapter {
    object ProfileSetupBindingAdapter {

        @BindingAdapter("profileImage", requireAll = true)
        @JvmStatic
        fun setProfileImage(imageView: ImageView, imageUrl: ObservableField<String>?) {
//            imageUrl?.get()?.let {
//                Log.d(" Test", it)
//            }
//            Log.d(" Test", "imageUrlがnull?")
//            Glide.with(imageView.context)
//                .load(imageUrl?.get())
//                .placeholder(R.drawable.ic_account)
//                .error(R.drawable.ic_account)
//                .into(imageView)
        }
    }
}