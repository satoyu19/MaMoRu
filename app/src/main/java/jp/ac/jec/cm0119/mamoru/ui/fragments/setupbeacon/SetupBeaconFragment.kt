package jp.ac.jec.cm0119.mamoru.ui.fragments.setupbeacon

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import jp.ac.jec.cm0119.mamoru.MyApplication
import jp.ac.jec.cm0119.mamoru.adapter.BeaconAdapter
import jp.ac.jec.cm0119.mamoru.databinding.FragmentSetupBeaconBinding
import jp.ac.jec.cm0119.mamoru.ui.MainActivity
import jp.ac.jec.cm0119.mamoru.viewmodels.setupbeacon.SetUpBeaconViewModel
import kotlinx.coroutines.launch
import org.altbeacon.beacon.*

@AndroidEntryPoint
class SetupBeaconFragment : Fragment() {

    private var _binding: FragmentSetupBeaconBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SetUpBeaconViewModel by viewModels()

    private var isBeaconFeature = true

    private lateinit var adapter: BeaconAdapter
    private lateinit var manager: LinearLayoutManager

    private val permissionResult =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result: Map<String, Boolean> ->
            checkPermission(result)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSetupBeaconBinding.inflate(layoutInflater)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isBeacon.collect { state ->
                    if (state?.isSuccess == true) {   //更新成功
                        MyApplication.updateSelectedBeacon(state.beaconId)
                        MyApplication.updateRegion()
                        viewModel.startBeacon()
                    }
                    if (state?.isSuccess == false){
                        Toast.makeText(requireContext(), state.errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("Test", "onViewCreated: ")
        requestPermission()
        adapter = BeaconAdapter(childFragmentManager, viewModel)
        manager = LinearLayoutManager(requireContext())
        binding.beaconRecycleView.layoutManager = manager
        binding.beaconRecycleView.adapter = adapter

        viewModel.beacons.observe(viewLifecycleOwner) { beacons ->
            Log.d("Test", "onViewCreated: ${beacons.size}")
            if (beacons.isNotEmpty()) {
                Log.d("Test", "onViewCreated: observe")
                adapter.submitList(beacons)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isBeaconFeature) {
            val intent = Intent(requireContext(), MainActivity::class.java)
            viewModel.beaconSetup(intent)
            viewModel.startBeacon()
            binding.nothingPermissionLayout.visibility = View.INVISIBLE
            binding.beaconRecycleView.visibility = View.VISIBLE
            binding.beaconTitle.visibility = View.VISIBLE
        } else {
            binding.nothingPermissionLayout.visibility = View.VISIBLE
            binding.beaconRecycleView.visibility = View.INVISIBLE
            binding.beaconTitle.visibility = View.INVISIBLE
            viewModel.updateMyBeacon(false, null)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkPermission(result: Map<String, Boolean>) {
        //permission(権限名), isGrant(有効 or 無効)
        result.forEach { (permission, isGrant) ->
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {    //SDKバージョンが31(Android12)以下の場合
                when (permission) {
                    Manifest.permission.ACCESS_FINE_LOCATION -> if (!isGrant) isBeaconFeature =
                        false
                    Manifest.permission.ACCESS_COARSE_LOCATION -> if (!isGrant) isBeaconFeature =
                        false
                }
            } else {    //SDKバージョンが31(Android12)以上ではBLUETOOTH周りの権限が必要
                if (!isGrant) {
                    isBeaconFeature = false
                }
            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {    //SDKバージョンが31(Android12)以下の場合
            permissionResult.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {    //SDKバージョンが31(Android12)以上ではBLUETOOTH周りの権限が必要
            permissionResult.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            )
        }
    }
}

