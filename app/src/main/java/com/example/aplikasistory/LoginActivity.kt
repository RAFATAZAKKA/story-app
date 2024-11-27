package com.example.aplikasistory

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.aplikasistory.data.AuthViewModel

import com.example.aplikasistory.data.Injection
import com.example.aplikasistory.data.Result
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailField = findViewById<EditText>(R.id.ed_login_email)
        val passwordField = findViewById<EditText>(R.id.ed_login_password)
        val loginButton = findViewById<Button>(R.id.btn_login)

        // Observasi hasil login dari ViewModel
        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    loginButton.isEnabled = false
                }
                is Result.Success -> {
                    Toast.makeText(this, "Login Berhasil", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this, "Error: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                    Log.e("LoginActivity", "Login Error: ${result.exception.message}", result.exception)
                }
            }
            loginButton.isEnabled = true
        }

        // Handle klik tombol login
        loginButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }
    }
}