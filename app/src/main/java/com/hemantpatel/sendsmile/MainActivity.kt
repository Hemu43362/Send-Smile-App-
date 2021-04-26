package com.hemantpatel.sendsmile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    public val REQUEST_CODE: Int = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadMemes()
    }

    fun shareBtnClick(view: View) {
        requestStoragePermission()
    }

    fun shareImage() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_STREAM, getImagePath())
        intent.putExtra(Intent.EXTRA_TEXT, "Hey! \nI found this amazing meme in SendSmile App")
        startActivity(Intent.createChooser(intent, "Share this image with .."))
    }

    fun getImagePath(): Uri? {
        //to get the image from the ImageView (say iv)
        val bitmap = findViewById<ImageView>(R.id.memeImg).drawable.toBitmap()

        var os: FileOutputStream? = null

        val sdCard: File = Environment.getExternalStorageDirectory()

        val dir: File = File(sdCard.absolutePath + "/Send Smile")
        dir.mkdir()

        val fileName: String = "${System.currentTimeMillis()}.jpg"

        val outfile: File = File(dir, fileName)

        os = FileOutputStream(outfile)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
        os.flush()
        os.close()
        return Uri.parse("$dir/$fileName")
    }

    fun next(view: View) {
        loadMemes()
    }

    private fun loadMemes() {
        findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "https://meme-api.herokuapp.com/gimme"

// Request a string response from the provided URL.
        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                Glide.with(this).load(response.getString("url"))
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Toast.makeText(this@MainActivity, e?.message, Toast.LENGTH_SHORT).show()
                            findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                            findViewById<ImageView>(R.id.memeImg).setImageResource(R.drawable.ic_gallery)
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                            return false
                        }
                    }).into(findViewById(R.id.memeImg))
                (findViewById<TextView>(R.id.titleText)).text = response.getString("title")
            },
            { error ->
                Toast.makeText(this, "check your Internet connection", Toast.LENGTH_SHORT).show()
                findViewById<ImageView>(R.id.memeImg).setImageResource(R.drawable.ic_gallery)
            })

// Add the request to the RequestQueue.
        queue.add(request)

    }

    private fun checkPermissionGrant(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestStoragePermission() {
        if (!checkPermissionGrant()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), REQUEST_CODE
            )
        } else shareImage()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                shareImage()
            } else {
                Toast.makeText(
                    this,
                    "Please Grant permission for use share feature",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}