package com.example.swiftycompanion

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.swiftycompanion.databinding.ActivityMainBinding
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var token: EditText
    private var isVisible = false
    private lateinit var binding: ActivityMainBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val settings: ImageButton = findViewById(R.id.settings)
        token = findViewById(R.id.token)
        settings.setOnClickListener {
            if (!isVisible){
            token.visibility = View.VISIBLE
            isVisible = true
            }
            else{
                token.visibility = View.GONE
                isVisible = false
                println(token.text)
            }
        }
        val login: Button = findViewById(R.id.login)
        login.setOnClickListener {
            // TODO : connect to 0Auth2 42 API
            val openURL = Intent(android.content.Intent.ACTION_VIEW)
            openURL.data = Uri.parse(getString(R.string.com_auth0_domain))
            //println(openURL.data)
            startActivity(openURL)
        }
        }

    }