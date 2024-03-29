package me.chancehalo.musicfeed

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.tabs.TabItem
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: AlbumListAdapter

    private lateinit var gridLayoutManager: GridLayoutManager

    private val albumList = ArrayList<Album>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        gridLayoutManager = GridLayoutManager(this, 2)
        recyclerView.layoutManager = gridLayoutManager
        adapter = AlbumListAdapter(albumList)
        recyclerView.adapter = adapter
        fetchMusicData("Top Albums")

        tabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                Log.i("dev", "reselected")
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Log.i("dev", "unselected")
                val length = albumList.size - 1
                for (_i in 0..length) {
                    albumList.removeAt(0)
                    adapter.notifyItemRemoved(0)
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val t = tab?.text
                Log.i("dev", t.toString())
                fetchMusicData(t.toString())
            }

        })
    }

    private fun fetchMusicData(type: String) {

        lateinit var feedType: String

        // Add better error handling?
        when (type) {
            "Coming Soon" -> feedType = "coming-soon"

            "New Releases" -> feedType = "new-releases"

            "Top Albums" -> feedType = "top-albums"

            "Top Songs" -> feedType = "top-songs"
        }


        val url =
            "https://rss.itunes.apple.com/api/v1/us/apple-music/$feedType/all/25/explicit.json"

        val client = OkHttpClient()

        val request = Request.Builder().url(url).build()

        val length = albumList.size - 1

//        for (_i in 0..length) {
//            val album = albumList[0]
//            albumList.remove(album)
//            adapter.notifyItemRemoved(0)
//        }

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                val json = JSONObject(body)
                val feed = json.getJSONObject("feed")
                val results = feed.getJSONArray("results")

                for (i in 0 until results.length()) {
                    val album = results.getJSONObject(i)
                    addToList(Album(album))
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.i("dev", "it failed")
                // Add better error handling?
                e.printStackTrace()
            }
        })
    }

    private fun addToList(album: Album) {
        runOnUiThread {
            albumList.add(album)
            adapter.notifyItemInserted(albumList.size - 1)
        }
    }

}