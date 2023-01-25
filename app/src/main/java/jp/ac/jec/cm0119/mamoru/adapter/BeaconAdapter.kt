package jp.ac.jec.cm0119.mamoru.adapter

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.ListAdapter
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.ac.jec.cm0119.mamoru.MyApplication
import jp.ac.jec.cm0119.mamoru.R
import jp.ac.jec.cm0119.mamoru.databinding.RowBeaconBinding
import jp.ac.jec.cm0119.mamoru.databinding.RowFamilyBinding
import jp.ac.jec.cm0119.mamoru.models.BeaconInfo
import jp.ac.jec.cm0119.mamoru.models.User
import jp.ac.jec.cm0119.mamoru.ui.fragments.CompleteResetDialog
import jp.ac.jec.cm0119.mamoru.ui.fragments.family.FamilyFragmentDirections


class BeaconAdapter(private val childFragmentMng: FragmentManager) :
    ListAdapter<BeaconInfo, BeaconAdapter.BeaconViewHolder>(BeaconCallBack()) {

    inner class BeaconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: RowBeaconBinding = RowBeaconBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeaconViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.row_beacon, parent, false)
        return BeaconViewHolder(view)
    }

    override fun onBindViewHolder(holder: BeaconViewHolder, position: Int) {
        val beacon: BeaconInfo = getItem(position)
        holder.binding.beaconId.text = beacon.uuid
        val distance = beacon.distance.removeRange(5, beacon.distance.length) + "m"
        holder.binding.beaconDistance.text = distance
        if (beacon.uuid == MyApplication.selectedBeaconId) {
            holder.binding.beaconImage.setImageResource(R.drawable.ic_wifi)
            holder.binding.linkBeacon.text = "接続中"
        } else {
            holder.binding.beaconImage.setImageResource(R.drawable.ic_wifi_gray)
            holder.binding.linkBeacon.text = "未接続"
        }
        holder.binding.beaconRowLayout.setOnClickListener {
            if (beacon.uuid == MyApplication.selectedBeaconId) {
                showDialog(null)
            } else {
                showDialog(beacon.uuid)
            }
        }
    }

    private fun showDialog(beaconId: String? = null) {
        val newFragment = SelectResetDialog(beaconId)
        newFragment.show(childFragmentMng, "reset")
    }

}

class BeaconCallBack : DiffUtil.ItemCallback<BeaconInfo>() {

    override fun areItemsTheSame(oldBeacons: BeaconInfo, newBeacons: BeaconInfo): Boolean {
        return oldBeacons.uuid == newBeacons.uuid
    }

    override fun areContentsTheSame(oldBeacons: BeaconInfo, newBeacons: BeaconInfo): Boolean {
        return oldBeacons == newBeacons
    }
}

//todo https://developer.android.com/guide/topics/ui/dialogs?hl=ja 別の書き方にする？
class SelectResetDialog(private val selectedBeaconUid: String? = null) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            var builder: AlertDialog.Builder?
            if (selectedBeaconUid != null) {
                builder = AlertDialog.Builder(it)
                builder.setTitle("選択したビーコンで開始しますか？")
                    .setMessage("このビーコンを利用して自身の行動を他のユーザーに知らせることができます。")
                    .setPositiveButton("はい",
                        DialogInterface.OnClickListener { _, _ ->
                            MyApplication.updateSelectedBeacon(selectedBeaconUid)
                        })
                    .setNegativeButton("いいえ", DialogInterface.OnClickListener { _, _ ->

                    })
            } else {
                builder = AlertDialog.Builder(it)
                builder.setTitle("選択したビーコンを解除しますか？")
                    .setMessage("このビーコンを使った機能を停止します。")
                    .setPositiveButton("はい",
                        DialogInterface.OnClickListener { _, _ ->
                            MyApplication.updateSelectedBeacon(null)
                        })
                    .setNegativeButton("いいえ", DialogInterface.OnClickListener { _, _ ->

                    })
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}