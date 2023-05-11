package com.example.swiftycompanion

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
import java.net.HttpURLConnection
import java.net.URL
class ProfileActivity : AppCompatActivity(){
    private lateinit var token : String
    private lateinit var actualUser : String
    private lateinit var recentList: ArrayList<String>

    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val searchUser = intent.extras
        if (searchUser != null)
        {
            token = searchUser.getString("token").toString()
            actualUser = searchUser.getString("user").toString()
            recentList = ArrayList()
            searchUser.getStringArrayList("recent")?.let { recentList.addAll(it) }
            Log.i(searchUser.getString("user").toString(), token)
            setupProfile(searchUser.getString("user").toString(), token, 1)
        }
        val uri = intent.data
        if (uri != null && uri.getQueryParameter("token").toString() != "") {
            val code : String = uri.getQueryParameter("code").toString()
            Log.i("CODE", code)
            if (code == "null") {
                val log = Intent(this, MainActivity::class.java)
                log.putExtra("access", "false")
                startActivity(log)
                finish()
            }
            else if (token == "null")
                requestAccessToken()
        }


        setContentView(R.layout.profile_layout)
        val logout: Button = findViewById(R.id.logout)
        logout.setOnClickListener {
            val log = Intent(this, MainActivity::class.java)
            log.putExtra("access", "true")
            log.putExtra("token", token)
            log.putExtra("user", "aglorios")
            startActivity(log)
            finish()
        }
        val search: Button = findViewById(R.id.search)
        search.setOnClickListener {
            val searchUserView = Intent(this@ProfileActivity, SearchUser::class.java)
            searchUserView.putExtra("token", token)
            searchUserView.putExtra("user", actualUser)
            searchUserView.putExtra("recent", recentList)
            startActivity(searchUserView)
        }
        val profile: Button = findViewById(R.id.my_profile)
        profile.setOnClickListener {
            setupProfile("aglorios", token, 1)
        }
        val show: LinearLayout = findViewById(R.id.data)
        val data: CardView = findViewById(R.id.more_info)
        val more: TextView = findViewById(R.id.more_text)
        data.setOnClickListener{
            if (more.text == "more") {
                more.text = "less"
                show.visibility = View.VISIBLE
            }
            else {
                more.text = "more"
                show.visibility = View.GONE
            }
        }
    }

    private fun requestAccessToken() {
        Log.i("NEW TOKEN", "REQUEST NEW TOKEN")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url("https://api.intra.42.fr/oauth/token")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(
                        FormBody.Builder()
                            .add("grant_type", "client_credentials")
                            .add("client_id", getString(R.string.com_auth0_uid))
                            .add("client_secret", getString(R.string.com_auth0_secret))
                            .build()
                    )
                    .build()
                val response = OkHttpClient().newCall(request).execute()
                val responseBody = response.peekBody(Long.MAX_VALUE).string()


                if (response.isSuccessful) {
                    val tok = JSONObject(responseBody).getString("access_token")
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
        actualUser = user
        Log.i("TOKEN", token)
        Log.i("user", user)
        CoroutineScope(Dispatchers.Main).launch {
            val obj = withContext(Dispatchers.Default) { apiRequest("/v2/users/$user", status) }
            if (obj.length() != 0) {
                // PSEUDO
                hideKeyboard()
                val pseudo: TextView = findViewById(R.id.pseudo)
                pseudo.text = obj.get("login").toString()
                // Personnal Data
                val dataList: ArrayList<Model> = ArrayList()
                dataList.add(Model("First name :", obj.get("first_name").toString()))
                dataList.add(Model("Last name :", obj.get("last_name").toString()))
                dataList.add(Model("Email :", obj.get("email").toString()))
                if (obj.get("phone").toString() != "hidden")
                    dataList.add(Model("Phone :", obj.get("phone").toString()))
                dataList.add(Model("Status :", obj.get("kind").toString()))
                val recyclerViewData : RecyclerView = findViewById(R.id.recycler_view_data)
                recyclerViewData.layoutManager = GridLayoutManager(this@ProfileActivity, 1, GridLayoutManager.VERTICAL, false)
                recyclerViewData.adapter= RecyclerViewAdapter(dataList)
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
                    val item = Model(name, xp.toString().take(5))
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
                //Log.i("Project", projectList.toString())
                val recyclerViewProject : RecyclerView = findViewById(R.id.project_recycler_view)
                recyclerViewProject.layoutManager = GridLayoutManager(this@ProfileActivity, 1, GridLayoutManager.VERTICAL, false)
                recyclerViewProject.adapter= RecyclerViewAdapter(projectList)
                /// Achievements
                ///v2/users/:user_id/events
                //val event = withContext(Dispatchers.Default) { apiRequest("/v2/users/$user/events", status)}
                //Log.i("Events", event.toString())
                // loading
                val myProfile : Button = findViewById(R.id.my_profile)
                if (user != "aglorios"){
                    myProfile.visibility = View.VISIBLE
                }
                else {
                    myProfile.visibility = View.INVISIBLE
                }
                val loading : LinearLayout = findViewById(R.id.loading)
                loading.visibility = View.GONE
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
                        JSONObject()
                    }
                    else {
                        JSONObject()
                    }
                }
            } catch (e: Exception) {
                JSONObject()
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

//    fun more_info(view: View) {}
}