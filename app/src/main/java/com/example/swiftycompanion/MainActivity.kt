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
    private var recentList: ArrayList<String> = ArrayList()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val login: Button = findViewById(R.id.login)

        val value = intent.extras
        if (value != null) {
            token = value.getString("token").toString()
            user = value.getString("user").toString()
            value.getStringArrayList("recent")?.let { recentList.addAll(it) }
            val err: LinearLayout = findViewById(R.id.error_message)
            val access = value.getString("access").toString()
            Log.i("test", access)
            if (access == "false") {
                err.visibility = View.VISIBLE
                login.text = "login"
                token = ""
            } else {
                err.visibility = View.GONE
            }
        }
        if (token.isNotEmpty())
            login.text = "connect"

        val settings: ImageButton = findViewById(R.id.settings)
        val erase : Button = findViewById(R.id.erase)
        //val userEntry : EditText = findViewById(R.id.user)
        settings.setOnClickListener {
            if (!isVisible){
                //userEntry.visibility = View.VISIBLE
                erase.visibility = View.VISIBLE
                isVisible = true
            }
            else{
                //if (userEntry.text.isNotEmpty())
                 //   user = userEntry.text.isNotEmpty().toString()
                erase.visibility = View.GONE
                //userEntry.visibility = View.GONE
                isVisible = false
            }
        }
        erase.setOnClickListener{
            token = ""
            login.text = "login"
        }
        login.setOnClickListener {
            if (token.isEmpty()) {
                // connect to 0Auth2 42 API
                val openURL = Intent(Intent.ACTION_VIEW)
                openURL.data = Uri.parse(BuildConfig.domain)
                //val search = Intent(Intent.ACTION_VIEW)
                //search.data = openURL.data
                //search.putExtra("recent", recentList)
                startActivity(openURL)
            }
            else {
                val connect = Intent(this, ProfileActivity::class.java)
                connect.putExtra("token", token)
                connect.putExtra("user", user)
                if (recentList.isNotEmpty())
                    connect.putExtra("recent", recentList)
                startActivity(connect)
            }
        }

        }

    }