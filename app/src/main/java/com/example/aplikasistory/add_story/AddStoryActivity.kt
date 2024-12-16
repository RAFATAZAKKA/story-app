package com.example.aplikasistory.add_story


import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasistory.MainActivity
import com.example.aplikasistory.R
import com.example.aplikasistory.SessionManager
import com.example.aplikasistory.data.api.ApiConfig
import com.example.aplikasistory.data.response.FileUploadResponse
import com.example.aplikasistory.databinding.ActivityAddStoryBinding
import com.google.gson.Gson
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var currentImageUri: Uri? = null
    private var previousImageUri: Uri? = null
    private var hasImageChanged: Boolean = false

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
            if (uri != currentImageUri) {
                previousImageUri = currentImageUri
                currentImageUri = uri
                hasImageChanged = true
                showImage()
            } else {
                showToast("Gambar ini sudah dipilih sebelumnya.")
            }
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCamera() {
        previousImageUri = currentImageUri
        currentImageUri = getImageUri(this)
        launcherIntentCamera.launch(currentImageUri!!)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { isSuccess ->
        if (isSuccess) {
            hasImageChanged = true
            showImage()
        } else {

            currentImageUri = previousImageUri
            showToast("Gagal mengambil gambar, kembali ke gambar sebelumnya.")
        }
    }

    private fun uploadImage() {
        if (currentImageUri == null || !hasImageChanged) {
            showToast(getString(R.string.empty_image_warning))
            return
        }

        val description = binding.descriptionEditText.text.toString()
        if (description.isBlank()) {
            showToast(getString(R.string.empty_description_warning))
            return
        }

        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()
        if (token.isNullOrBlank()) {
            showToast(getString(R.string.token_missing_warning))
            return
        }

        showLoading(true)

        currentImageUri?.let { uri ->
            val imageFile = uriToFile(uri, this).reduceFileImage()
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
                    val successResponse = apiService.uploadImageWithAuth(
                        token = "Bearer $token",
                        multipartBody,
                        requestBody
                    )
                    successResponse.message?.let { showToast(it) }
                    showLoading(false)

                    previousImageUri = currentImageUri
                    hasImageChanged = false

                    val intent = Intent(this@AddStoryActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)

                } catch (e: HttpException) {
                    val errorBody = e.response()?.errorBody()?.string()
                    val errorResponse = Gson().fromJson(errorBody, FileUploadResponse::class.java)
                    errorResponse.message?.let { showToast(it) }
                    showLoading(false)
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }
}


