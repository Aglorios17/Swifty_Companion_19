package com.example.swiftycompanion

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

class SearchUser : AppCompatActivity() {
    private lateinit var recent: ListView
    private lateinit var listAdapter: ArrayAdapter<String>
    private lateinit var recentList: ArrayList<String>
    private lateinit var searchView: SearchView
    private lateinit var token : String
    private lateinit var user : String


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.search_user)
        // initializing list and adding data to list
        recentList = ArrayList()

        val value = intent.extras

        if (value != null) {
            token = value.getString("token").toString()
            user = value.getString("user").toString()
            value.getStringArrayList("recent")?.let { recentList.addAll(it) }
            //Log.i("Get Token", token)
        }

        val logout: Button = findViewById(R.id.logout)
        logout.setOnClickListener {
            val logoutI = Intent(this@SearchUser, MainActivity::class.java)
            logoutI.putExtra("token", token)
            logoutI.putExtra("user", user)
            startActivity(logoutI)
            finish()
        }
        val back: Button = findViewById(R.id.back)
        back.setOnClickListener {
            val backI = Intent(this@SearchUser, ProfileActivity::class.java)
            backI.putExtra("token", token)
            backI.putExtra("user", user)
            backI.putExtra("recent", recentList)
            startActivity(backI)
            finish()
        }
        searchView = findViewById(R.id.searchView)
        val searchButton : Button = findViewById(R.id.user_search)
        searchButton.setOnClickListener{
            val input = searchView.query.toString()
            CoroutineScope(Dispatchers.Main).launch {
                val obj = withContext(Dispatchers.Default) { apiRequest("/v2/users/$input", 1) }
                if (obj.length() != 0)
                {
                    val backI = Intent(this@SearchUser, ProfileActivity::class.java)
                    backI.putExtra("token", token)
                    backI.putExtra("user", input)
                    if (!recentList.contains(input))
                        recentList.add(input)
                    backI.putExtra("recent", recentList)
                    startActivity(backI)
                    finish()
                }
                else{
                    searchView.background = getDrawable(R.drawable.bg_red_rounded)
                }
            }
        }





        ////// SEARCHBAR
        recent = findViewById(R.id.recent)
        // initializing list adapter and setting layout
        // for each list view item and adding array list to it.
        listAdapter = ArrayAdapter<String>(
            this,
            R.layout.textview_costum, R.id.textSpecial,
            recentList
        )

        // on below line setting list
        // adapter to our list view.
        recent.adapter = listAdapter

        // on below line we are adding on query
        // listener for our search view.
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // on below line we are checking
                // if query exist or not.
                if (recentList.contains(query)) {
                    // if query exist within list we
                    // are filtering our list adapter.
                    listAdapter.filter.filter(query)
                } else {
                    // if query is not present we are displaying
                    // a toast message as no  data found..
                    Toast.makeText(this@SearchUser, "", Toast.LENGTH_LONG)
                        .show()
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // if query text is change in that case we
                // are filtering our adapter with
                // new text on below line.
                listAdapter.filter.filter(newText)
                return false
            }
        })
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
}