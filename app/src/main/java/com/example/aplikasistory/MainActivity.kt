package com.example.aplikasistory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.aplikasistory.add_story.AddStoryActivity
import com.example.aplikasistory.data.Injection
import com.example.aplikasistory.data.Result
import com.example.aplikasistory.data.StoryAdapter
import com.example.aplikasistory.data.response.ListStoryItem
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private val viewModel: StoryViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var adapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // RecyclerView dan SwipeRefresh
        recyclerView = findViewById(R.id.rv_stories)
        swipeRefreshLayout = findViewById(R.id.swipe_refresh)

        val adapter = StoryAdapter { story, sharedView ->
            val intent = Intent(this, StoryDetailActivity::class.java).apply {
                putExtra("story_detail", story) // Parcelable untuk mengirim data
            }

            // Buat shared element transition
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                sharedView, // View yang akan digunakan untuk shared element
                "photo_transition" // Nama transisi sesuai styles.xml
            )

            startActivity(intent, options.toBundle())
        }


        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Observasi data cerita
        viewModel.stories.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    swipeRefreshLayout.isRefreshing = true
                }
                is Result.Success -> {
                    swipeRefreshLayout.isRefreshing = false
                    if (result.data.isNullOrEmpty()) {
                        Toast.makeText(this, "Tidak ada data untuk ditampilkan", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("MainActivity", "Data: ${result.data}")
                        adapter.submitList(result.data) // Pastikan adapter mendukung submitList.
                    }
                }
                is Result.Error -> {
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(
                        this,
                        "Error: ${result.exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Refresh data saat swipe
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchStories()
        }

        // Ambil cerita saat halaman dimuat
        viewModel.fetchStories()

        val fab: FloatingActionButton = findViewById(R.id.fab) // Pastikan ID sesuai dengan FloatingActionButton di XML
        fab.setOnClickListener {
            val intent = Intent(this, AddStoryActivity::class.java)
            startActivity(intent)
        }
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun logout() {
        viewModel.logout()
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}
