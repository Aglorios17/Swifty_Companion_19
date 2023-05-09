package com.example.swiftycompanion

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL

class ProfileActivity : AppCompatActivity(){
    //private lateinit var binding: ActivityMainBinding
    //private lateinit var webView: WebView
    private lateinit var token : String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_layout)
        val login: Button = findViewById(R.id.back)
        login.setOnClickListener {
            val log = Intent(this, MainActivity::class.java)
            startActivity(log)
        }

        val uri = intent.data

        if (uri != null) {
            token = uri.getQueryParameter("code").toString()
            println("token : $token")
        }
        println("END TOKEN")
        try {
            //val url = URL("https://api.intra.42.fr/v2/me")
            val url = URL("https://api.intra.42.fr/oauth/token/info")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.requestMethod = "GET"
            connection.connect()

            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage
            println("Response code: $responseCode")
            println("Response message: $responseMessage")
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }

                // Use the response body as needed
                println(response)
            } else {
                // Handle errors
                println("Error: ${connection.responseCode} ${connection.responseMessage}")
            }
        }
        catch (e: Exception) {
            println("Exception: ${e.message}")
        }
    }
}