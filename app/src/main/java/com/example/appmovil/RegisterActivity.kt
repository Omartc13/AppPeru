package com.example.appmovil

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appmovil.data.AppDatabase
import com.example.appmovil.data.Usuario
import com.example.appmovil.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Date

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val usernameInput = findViewById<EditText>(R.id.register_username) // Using 'Nombre completo' field as username/name for simplicity or I should add a separate username field? The layout has "Nombre completo" ID register_username. I will treat it as the display name and generate a username or just use email as login.
        // Wait, the layout IDs are: register_username (hint "Nombre completo"), register_email, register_password. 
        // The DB has 'username' (unique) and 'nombre'. 
        // I will use the email as the username for login purposes, or split the "Nombre completo" to get a username. 
        // Better: Use email as the unique identifier for login in this simple flow, but the DB requires a unique 'username'.
        // I'll just use the email as the username for now to avoid errors, or ask the user. 
        // Let's map: register_username -> nombre. register_email -> email AND username. 
        
        val emailInput = findViewById<EditText>(R.id.register_email)
        val passwordInput = findViewById<EditText>(R.id.register_password)
        val repeatPasswordInput = findViewById<EditText>(R.id.register_password_repeat)
        val registerButton = findViewById<Button>(R.id.register_button)
        val loginLink = findViewById<TextView>(R.id.login_link)

        val db = AppDatabase.getDatabase(this)

        loginLink.setOnClickListener {
            finish() // Regresa al Login
        }

        registerButton.setOnClickListener {
            val nombre = usernameInput.text.toString().trim()
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()
            val repeatPassword = repeatPasswordInput.text.toString().trim()

            if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
                Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != repeatPassword) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Database Operation
            lifecycleScope.launch {
                val existingUser = db.appDao().getUsuarioByEmail(email)
                if (existingUser != null) {
                    Toast.makeText(this@RegisterActivity, "El correo ya está registrado", Toast.LENGTH_SHORT).show()
                } else {
                    // Create new user
                    // Using email as username to ensure uniqueness for now, since UI doesn't have separate username field
                    val newUser = Usuario(
                        username = email, 
                        email = email,
                        passwordHash = password, // In a real app, hash this!
                        nombre = nombre,
                        fotoPerfilUrl = null,
                        fechaRegistro = Date(),
                        privado = false
                    )
                    
                    try {
                        val newId = db.appDao().insertUsuario(newUser)
                        
                        // Guardar sesión automáticamente
                        SessionManager.saveUserSession(this@RegisterActivity, newId.toInt())

                        Toast.makeText(this@RegisterActivity, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show()
                        
                        // Go to Main App (Mapa)
                        val intent = Intent(this@RegisterActivity, MapaActivity::class.java)
                        // Clear stack so user can't go back to register
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } catch (e: Exception) {
                         Toast.makeText(this@RegisterActivity, "Error al registrar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}