package com.example.picasso1
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
class MainActivity : AppCompatActivity() {
    private lateinit var button: Button
    private lateinit var button2: Button
    private lateinit var imageView: ImageView
    companion object {
        val IMAGE_REQUEST_CODE = 100
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button = findViewById(R.id.btn_pick_img)
        button2 = findViewById(R.id.gotosec)
        imageView = findViewById(R.id.img)
        button.setOnClickListener {
            pickImageGallery()
        }
        button2.setOnClickListener{
            try {
                val intent = Intent(this@MainActivity, Connection::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            imageView.setImageURI(data?.data)
        }
    }
}