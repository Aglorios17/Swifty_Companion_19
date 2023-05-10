package com.example.swiftycompanion

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class ProfileActivity : AppCompatActivity(){
    private lateinit var token : String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_layout)
        val logout: Button = findViewById(R.id.back)
        logout.setOnClickListener {
            token = ""
            val log = Intent(this, MainActivity::class.java)
            startActivity(log)
        }

        val uri = intent.data
        if (uri != null) {
            val code : String = uri.getQueryParameter("code").toString()
            Log.i("CODE", code)
            requestAccessToken()
            //Log.d("ACCESS_TOKEN", token)
            //setupProfile()
        }
        val search: Button = findViewById(R.id.search)
        val user: EditText = findViewById(R.id.user_search)
        search.setOnClickListener {
            setupProfile(user.text.toString(), token, 1)
        }
    }

    private fun requestAccessToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("https://api.intra.42.fr/oauth/token")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(
                        FormBody.Builder()
                            .add("grant_type", "client_credentials")
                            .add("client_id", getString(R.string.com_auth0_client_id))
                            .add("client_secret", getString(R.string.secret))
                            .build()
                    )
                    .build()
                val response = OkHttpClient().newCall(request).execute()
                val responseBody = response.peekBody(Long.MAX_VALUE).string()

                if (response.isSuccessful) {
                    val tok = JSONObject(responseBody).getString("access_token")
                    Log.i("TOK", tok)
                    withContext(Dispatchers.Main)
                    {
                        setupProfile("aglorios", tok, 0)
                    }
                } else {
                    Log.e("Request ACCESS_TOKEN", "Failed to get access token")
                }
            } catch (e: Exception) {
                Log.e("Request ACCESS_TOKEN", "Error: ${e.message}")
            }
        }
    }

    private fun setupProfile(user: String, tok : String, status : Int){
        token = tok
        Log.i("TOKEN", token)
        Log.i("user", user)
        CoroutineScope(Dispatchers.Main).launch {
            val myTextView: TextView = findViewById(R.id.user_search)
            val obj = withContext(Dispatchers.Default) { apiRequest("/v2/users/$user", status) }
            if (obj.length() != 0) {
                // PSEUDO
                myTextView.setBackgroundColor(Color.TRANSPARENT)
                hideKeyboard()
                val pseudo: TextView = findViewById(R.id.pseudo)
                pseudo.text = obj.get("login").toString()
                // PICTURE
                val image = obj.getJSONObject("image").getJSONObject("versions").get("medium")
                val profilePicture: CircleImageView = findViewById(R.id.profile_picture)
                Picasso.get().load(image.toString()).into(profilePicture)
                // LEVEL
                val level = obj.getJSONArray("cursus_users").getJSONObject(1).get("level").toString()
                val ll : TextView = findViewById(R.id.level)
                ll.text = level
                val afterDecimal = level.split(".")[1].toIntOrNull()
                if (afterDecimal != null) {
                    val progressBar : ProgressBar = findViewById(R.id.progressBar)
                    if (afterDecimal < 10){
                        progressBar.progress = afterDecimal * 10
                    }
                    else{
                        progressBar.progress = afterDecimal
                    }
                }
                // CAMPUS
                val campus : TextView = findViewById(R.id.campus_name)
                campus.text = obj.getJSONArray("campus").getJSONObject(0).get("name").toString()
                //println(obj.getJSONArray("campus").getJSONObject(0).get("name").toString())
                //Wallet
                val money : TextView = findViewById(R.id.money)
                money.text = obj.get("wallet").toString()
                //Since
                val since : TextView = findViewById(R.id.date)
                since.text = obj.get("pool_year").toString()
                //Status
                //var status : TextView = findViewById(R.id.status)
                //status.text = obj.get("kind").toString()
                //Skills
                val skillsArray = obj.getJSONArray("cursus_users").getJSONObject(1).getJSONArray("skills")
                val skillsList: ArrayList<Model> = ArrayList()
                for (i in 0 until skillsArray.length()) {
                    val name = skillsArray.getJSONObject(i).getString("name")
                    val xp = skillsArray.getJSONObject(i).getDouble("level")
                    val item = Model(name, xp.toString())
                    skillsList.add(item)
                }
                //Log.i("Skills", skillsList.toString())
                val recyclerView : RecyclerView = findViewById(R.id.recycler_view)
                recyclerView.layoutManager = GridLayoutManager(this@ProfileActivity, 1, GridLayoutManager.VERTICAL, false)
                recyclerView.adapter= RecyclerViewAdapter(skillsList)
                /// Project
                val projectArray = obj.getJSONArray("projects_users")
                val projectList: ArrayList<Model> = ArrayList()
                for (i in 0 until projectArray.length()) {
                    val name = projectArray.getJSONObject(i).getJSONObject("project").getString("name")
                    val finalMark = projectArray.getJSONObject(i).getString("final_mark")
                    if (finalMark != "null"){
                        val item = Model(name, finalMark)
                        projectList.add(item)
                    }
                }
                Log.i("Project", projectList.toString())
                val recyclerViewProject : RecyclerView = findViewById(R.id.project_recycler_view)
                recyclerViewProject.layoutManager = GridLayoutManager(this@ProfileActivity, 1, GridLayoutManager.VERTICAL, false)
                recyclerViewProject.adapter= RecyclerViewAdapter(projectList)
            }
            else{
                myTextView.setBackgroundColor(Color.argb(128, 255, 0, 0))
            }
        }
    }
    private suspend fun apiRequest(route: String, status: Int) : JSONObject
    {
        return withContext(Dispatchers.IO) {
            val url = URL("https://api.intra.42.fr$route")
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.setRequestProperty("Authorization", "Bearer $token")
                connection.requestMethod = "GET"
                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    Log.i("Response message:", response)
                    JSONObject(response)
                } else {
                    // Handle errors
                    if (status == 0){
                        throw IOException("HTTP error code: ${connection.responseCode}")
                    }
                    else {
                        JSONObject()
                    }
                }
            } catch (e: Exception) {
                throw IOException("Error while getting response from $route: ${e.message}")
            } finally {
                connection.disconnect()
            }
        }
    }
    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}