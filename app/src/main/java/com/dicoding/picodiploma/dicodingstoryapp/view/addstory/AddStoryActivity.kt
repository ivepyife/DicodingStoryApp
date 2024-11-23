package com.dicoding.picodiploma.dicodingstoryapp.view.addstory

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.picodiploma.dicodingstoryapp.R
import com.dicoding.picodiploma.dicodingstoryapp.databinding.ActivityAddStoryBinding
import com.dicoding.picodiploma.dicodingstoryapp.utils.getImageUri
import com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel.AddStoryViewModel
import com.dicoding.picodiploma.dicodingstoryapp.view.viewmodel.ViewModelFactory
import com.dicoding.picodiploma.dicodingstoryapp.data.source.Result

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private val viewModel by viewModels<AddStoryViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.add_story)

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
        viewModel.uploadImage(this, description)
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