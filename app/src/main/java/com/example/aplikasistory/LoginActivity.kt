package com.example.aplikasistory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.aplikasistory.data.AuthViewModel
import com.example.aplikasistory.data.Injection
import com.example.aplikasistory.data.Result


class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var progressBar: ProgressBar
    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        progressBar = findViewById(R.id.progressBar)
        val emailField = findViewById<EditText>(R.id.ed_login_email)
        val passwordField = findViewById<EditText>(R.id.ed_login_password)
        val loginButton = findViewById<Button>(R.id.btn_login)

        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    loginButton.isEnabled = false
                }
                is Result.Success -> {
                    progressBar.visibility = View.GONE

                    result.data.loginResult?.token?.let { token ->
                        sessionManager.saveLogin(token)
                    }

                    Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is Result.Error -> {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Login Error: ${result.exception.message}", result.exception)
                }
            }
            loginButton.isEnabled = true
        }

        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            passwordField.error = if (password.isNullOrEmpty() || password.length < 8) {
                "Password tidak boleh kurang dari 8 karakter"
            } else null

            if (passwordField.error != null) return@setOnClickListener

            viewModel.login(email, password)
        }
    }

    private fun fadeInViews(vararg views: View) {
        views.forEach { view ->
            view.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }
    }
}


