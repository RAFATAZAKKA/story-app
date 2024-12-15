package com.example.aplikasistory

import android.os.Bundle
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.aplikasistory.data.response.ListStoryItem

class StoryDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_detail)

        supportPostponeEnterTransition()
        val story = intent.getParcelableExtra<ListStoryItem>("story_detail") ?: return
        val imageView = findViewById<ImageView>(R.id.ivStory)

        Glide.with(this)
            .load(story.photoUrl)
            .into(findViewById(R.id.ivStory))

        findViewById<TextView>(R.id.tvTitle).text = story.name
        findViewById<TextView>(R.id.tvDescription).text = story.description
        findViewById<TextView>(R.id.tvDate).text = story.createdAt

        imageView.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    imageView.viewTreeObserver.removeOnPreDrawListener(this)
                    supportStartPostponedEnterTransition()
                    return true
                }
            }
        )
    }
}
