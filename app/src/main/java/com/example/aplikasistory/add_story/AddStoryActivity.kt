package com.example.aplikasistory.add_story

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.aplikasistory.R
import com.example.aplikasistory.ViewModelFactory
import com.example.aplikasistory.data.Injection
import com.example.aplikasistory.data.api.ApiConfig
import com.example.aplikasistory.data.response.FileUploadResponse
import com.example.aplikasistory.databinding.ActivityAddStoryBinding
import com.example.aplikasistory.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException


//class AddStoryActivity : AppCompatActivity() {
//
//    private lateinit var binding: ActivityAddStoryBinding
//    private var currentImageUri: Uri? = null
//    private val viewModel: AddStoryViewModel by viewModels {
//        ViewModelFactory(Injection.provideRepository(this))
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityAddStoryBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        binding.galleryButton.setOnClickListener { startGallery() }
//        binding.cameraButton.setOnClickListener { startCamera() }
//        binding.uploadButton.setOnClickListener { uploadImage() }
//    }
//
//    private fun startGallery() {
//        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//    }
//
//    private val launcherGallery = registerForActivityResult(
//        ActivityResultContracts.PickVisualMedia()
//    ) { uri: Uri? ->
//        if (uri != null) {
//            currentImageUri = uri
//            showImage()
//        } else {
//            Log.d("Photo Picker", "No media selected")
//        }
//    }
//
//    private fun startCamera() {
//        currentImageUri = getImageUri(this)
//        launcherIntentCamera.launch(currentImageUri!!)
//    }
//
//    private val launcherIntentCamera = registerForActivityResult(
//        ActivityResultContracts.TakePicture()
//    ) { isSuccess ->
//        if (isSuccess) {
//            showImage()
//        } else {
//            currentImageUri = null
//        }
//    }
//
//    private fun uploadImage() {
//        currentImageUri?.let { uri ->
//            val imageFile = uriToFile(uri, this)
//            val description = binding.descriptionEditText.text.toString()
//
//            if (description.isEmpty()) {
//                showToast(getString(R.string.empty_description_warning))
//                return
//            }
//
//            showLoading(true)
//
//            val requestBody = description.toRequestBody("text/plain".toMediaType())
//            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
//            val multipartBody = MultipartBody.Part.createFormData(
//                "photo",
//                imageFile.name,
//                requestImageFile
//            )
//
//            // Ambil token dari repository
//            val token = userRepository.getToken()
//
//            viewModel.uploadImage(multipartBody, requestBody,
//                onSuccess = { message ->
//                    showLoading(false)
//                    showToast(message)
//                    finish()
//                },
//                onError = { error ->
//                    showLoading(false)
//                    showToast(error)
//                })
//        } ?: showToast(getString(R.string.empty_image_warning))
//    }
//
//    private fun showLoading(isLoading: Boolean) {
//        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
//    }
//
//    private fun showImage() {
//        currentImageUri?.let {
//            Log.d("Image URI", "showImage: $it")
//            binding.previewImageView.setImageURI(it)
//        }
//    }
//}




class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var currentImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener { startGallery() }
        binding.cameraButton.setOnClickListener { startCamera() }
        binding.uploadButton.setOnClickListener { uploadImage() }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCamera() {
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            showImage()
        } else {
            currentImageUri = null
        }
    }


    private fun uploadImage() {
        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
            Log.d("Image File", "showImage: ${imageFile.path}")
            val description = "Ini adalah deksripsi gambar"

            showLoading(true)

            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )
            lifecycleScope.launch {
                try {
                    val apiService = ApiConfig.getApiService()
                    val successResponse = apiService.uploadImage(multipartBody, requestBody)
                    successResponse.message?.let { showToast(it) }
                    showLoading(false)
                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
                    errorResponse.message?.let { showToast(it) }
                    showLoading(false)
                }
            }
        } ?: showToast(getString(R.string.empty_image_warning))
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



    private fun showImage() {
        currentImageUri?.let {
            Log.d("Image URI", "showImage: $it")
            binding.previewImageView.setImageURI(it)
        }
    }
}