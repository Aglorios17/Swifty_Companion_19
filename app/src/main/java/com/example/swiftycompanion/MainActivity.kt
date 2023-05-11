package com.example.swiftycompanion

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var isVisible = false
    private var token: String = ""
    private var user: String = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val login: Button = findViewById(R.id.login)

        val value = intent.extras
        if (value != null) {
            token = value.getString("token").toString()
            user = value.getString("user").toString()
            val err: LinearLayout = findViewById(R.id.error_message)
            val access = value.getString("access").toString()
            Log.i("test", access)
            if (access == "false") {
                Log.i("test", access)
                err.visibility = View.VISIBLE
            } else {
                err.visibility = View.GONE
            }
        }
        if (token.isNotEmpty())
            login.text = "connect"

        val settings: ImageButton = findViewById(R.id.settings)
        val erase : Button = findViewById(R.id.erase)
        settings.setOnClickListener {
            if (!isVisible){
            erase.visibility = View.VISIBLE
            isVisible = true
            }
            else{
                erase.visibility = View.GONE
                isVisible = false
            }
        }
        erase.setOnClickListener{
            token = ""
            user = ""
            login.text = "login"
        }
        login.setOnClickListener {
            if (token.isEmpty()) {
                // connect to 0Auth2 42 API
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data = Uri.parse(getString(R.string.com_auth0_domain))
                startActivity(openURL)
            }
            else {
                val connect = Intent(this, ProfileActivity::class.java)
                connect.putExtra("token", token)
                connect.putExtra("user", user)
                startActivity(connect)
            }
        }

        }

    }