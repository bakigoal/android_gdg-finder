package com.bakigoal.gdgfinder.search

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bakigoal.gdgfinder.databinding.FragmentGdgListBinding
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar

private const val LOCATION_PERMISSION_REQUEST = 1

private const val ACCESS_FINE_LOCATION_PERMISSION = "android.permission.ACCESS_FINE_LOCATION"
private const val ACCESS_COARSE_LOCATION_PERMISSION = "android.permission.ACCESS_COARSE_LOCATION"

class GdgListFragment : Fragment() {


    private val viewModel: GdgListViewModel by lazy {
        ViewModelProvider(this).get(GdgListViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentGdgListBinding.inflate(inflater)

        // Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
        binding.setLifecycleOwner(this)

        // Giving the binding access to the OverviewViewModel
        binding.viewModel = viewModel

        val adapter = GdgListAdapter(GdgClickListener { chapter ->
            val destination = Uri.parse(chapter.website)
            startActivity(Intent(Intent.ACTION_VIEW, destination))
        })

        // Sets the adapter of the RecyclerView
        binding.gdgChapterList.adapter = adapter

        viewModel.showNeedLocation.observe(viewLifecycleOwner, object : Observer<Boolean> {
            override fun onChanged(show: Boolean?) {
                // Snackbar is like Toast but it lets us show forever
                if (show == true) {
                    Snackbar.make(
                        binding.root,
                        "No location. Enable location in settings (hint: test with Maps) then check app permissions!",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLastLocation()
    }

    /**
     * Show the user a dialog asking for permission to use location.
     */
    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                ACCESS_FINE_LOCATION_PERMISSION,
                ACCESS_COARSE_LOCATION_PERMISSION
            ), LOCATION_PERMISSION_REQUEST
        )
    }

    /**
     * Request the last location of this device, if known, otherwise start location updates.
     *
     * The last location is cached from the last application to request location.
     */
    private fun requestLastLocation() {
        // if we don't have permission ask for it and wait until the user grants it
        if (ContextCompat.checkSelfPermission(requireContext(), ACCESS_FINE_LOCATION_PERMISSION)
            != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(requireContext(), ACCESS_COARSE_LOCATION_PERMISSION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            viewModel.onLocationUpdated(location)
        }
    }

    /**
     * This will be called by Android when the user responds to the permission request.
     *
     * If granted, continue with the operation that the user gave us permission to do.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestLastLocation()
                }
            }
        }
    }
}


