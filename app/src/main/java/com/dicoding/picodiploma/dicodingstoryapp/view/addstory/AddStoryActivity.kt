package com.dicoding.picodiploma.dicodingstoryapp.view.addstory

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.dicoding.picodiploma.dicodingstoryapp.databinding.ActivityAddStoryBinding
import com.dicoding.picodiploma.dicodingstoryapp.utils.getImageUri
import com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel.AddStoryViewModel
import com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel.ViewModelFactory
import com.dicoding.picodiploma.dicodingstoryapp.data.source.Result
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Toast.makeText(this, getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
            binding.switchLocation.isChecked = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.add_story)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupObservers()
        setupButtons()
    }

    private fun setupObservers() {
        viewModel.currentImgUri.observe(this) { uri ->
            uri?.let { showImage(it) }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            showLoading(isLoading)
        }

        viewModel.uploadResult.observe(this) { result ->
            when (result) {
                is Result.Success -> {
                    showToast(result.data.message ?: getString(R.string.upload_successful))
                    setResult(RESULT_OK)
                    finish()
                }
                is Result.Error -> {
                    showToast(result.error)
                }
                is Result.Loading -> {
                    showLoading(true)
                }
            }
        }

        // Location switch listener
        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkLocationPermission()
            } else {
                viewModel.setCurrentLocation(null)
            }
        }
    }

    private fun checkLocationPermission() {
        when {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                getCurrentLocation()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    viewModel.setCurrentLocation(it)
                    Toast.makeText(
                        this,
                        getString(R.string.location_added),
                        Toast.LENGTH_SHORT
                    ).show()
                } ?: run {
                    Toast.makeText(
                        this,
                        getString(R.string.location_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.switchLocation.isChecked = false
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.location_error),
                    Toast.LENGTH_SHORT
                ).show()
                binding.switchLocation.isChecked = false
            }
    }

    private fun setupButtons() {
        binding.apply {
            btnGallery.setOnClickListener { startGallery() }
            btnCamera.setOnClickListener { startCamera() }
            btnUpload.setOnClickListener { uploadImage() }
        }
    }

    private fun uploadImage() {
        val description = binding.etDescription.text.toString()
        if (description.isEmpty()) {
            binding.etDescription.error = getString(R.string.description_is_required)
            return
        }

        // Upload with location if switch is checked and location is available
        val includeLocation = binding.switchLocation.isChecked
        viewModel.uploadImage(this, description, includeLocation)
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun startCamera() {
        val uri = getImageUri(this)
        Log.d("Camera URI", "URI created: $uri")
        viewModel.setCurrentImage(uri)
        launcherIntentCamera.launch(uri)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            viewModel.currentImgUri.value?.let { uri ->
                showImage(uri)
            }
        } else {
            viewModel.setCurrentImage(null)
        }
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCurrentImage(uri)
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage(uri: Uri) {
        binding.imgPreview.setImageURI(uri)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}