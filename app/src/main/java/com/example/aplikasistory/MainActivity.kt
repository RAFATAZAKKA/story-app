package com.example.aplikasistory

import android.content.Intent
import android.os.Bundle
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
import com.example.aplikasistory.map.MapsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private val viewModel: StoryViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_main)


        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        val recyclerView = findViewById<RecyclerView>(R.id.rv_stories)
        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        val adapter = StoryAdapter { story, sharedView ->
            val intent = Intent(this, StoryDetailActivity::class.java).apply {
                putExtra("story_detail", story)
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, sharedView, "photo_transition"
            )
            startActivity(intent, options.toBundle())
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.stories.observe(this) { result ->
            when (result) {
                is Result.Loading -> swipeRefreshLayout.isRefreshing = true
                is Result.Success -> {
                    swipeRefreshLayout.isRefreshing = false
                    adapter.submitList(result.data)
                }
                is Result.Error -> {
                    swipeRefreshLayout.isRefreshing = false
                    Toast.makeText(this, "Error: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        swipeRefreshLayout.setOnRefreshListener { viewModel.fetchStories() }
        viewModel.fetchStories()

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
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
            R.id.menu_map -> {
                // Intent untuk berpindah ke MapsActivity
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



    private fun logout() {
        val sessionManager = SessionManager(this)
        sessionManager.clearSession()
        viewModel.logout()
        Toast.makeText(this, "Logout berhasil", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

}

