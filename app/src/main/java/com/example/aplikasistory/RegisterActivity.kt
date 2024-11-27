package com.example.aplikasistory

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.aplikasistory.data.Result
import com.example.aplikasistory.data.AuthViewModel
import com.example.aplikasistory.data.Injection

class RegisterActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(Injection.provideRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val nameField = findViewById<EditText>(R.id.ed_register_name)
        val emailField = findViewById<EditText>(R.id.ed_register_email)
        val passwordField = findViewById<EditText>(R.id.ed_register_password)
        val registerButton = findViewById<Button>(R.id.btn_register)

        viewModel.registerResult.observe(this) { result ->
            when (result) {
                is Result.Loading -> registerButton.isEnabled = false
                is Result.Success -> {
                    Toast.makeText(this, result.data.message ?: "Register Berhasil", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is Result.Error -> {
                    Toast.makeText(this, "Error: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
            registerButton.isEnabled = true
        }

        passwordField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                passwordField.error = if (s.isNullOrEmpty() || s.length < 8) {
                    "Password tidak boleh kurang dari 8 karakter"
                } else null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        registerButton.setOnClickListener {
            val name = nameField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailField.error = "Email tidak valid"
                return@setOnClickListener
            }

            viewModel.register(name, email, password)
        }
    }
}
