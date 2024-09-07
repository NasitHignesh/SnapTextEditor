package com.example.addsticker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.addsticker.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    companion object {
        const val PUT_EXTRA_IMAGE_URI = "PUT_EXTRA_IMAGE_URI"
    }

    private lateinit var binding : ActivityMainBinding

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            val intent = Intent(this@MainActivity, AddStickerActivity::class.java)
            intent.putExtra(PUT_EXTRA_IMAGE_URI, imageUri.toString())
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.picImage.setOnClickListener {
            pickImage()
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

}
