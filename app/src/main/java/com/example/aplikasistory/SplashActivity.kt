package com.example.aplikasistory

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        val token = sessionManager.getToken()


        if (!token.isNullOrBlank()) {

            startActivity(Intent(this, MainActivity::class.java))
        } else {

            startActivity(Intent(this, WelcomeActivity::class.java))
        }
        finish()
    }
}
