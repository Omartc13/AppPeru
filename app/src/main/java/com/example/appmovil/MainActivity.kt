package com.example.appmovil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.appmovil.data.AppDatabase
import com.example.appmovil.utils.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    lateinit var usernameInput : EditText
    lateinit var passwordInput : EditText
    lateinit var login_button : Button
    lateinit var register_link : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        usernameInput = findViewById(R.id.username_input)
        passwordInput = findViewById(R.id.password_input)
        login_button = findViewById(R.id.login_button)
        register_link = findViewById(R.id.register_link)

        val db = AppDatabase.getDatabase(this)

        register_link.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        login_button.setOnClickListener{
            val email = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor ingrese correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // We used email as username in RegisterActivity, so check both or just email
                // Using getUsuarioByEmail is safer since the input hint says "Email"
                val user = db.appDao().getUsuarioByEmail(email)

                if (user != null && user.passwordHash == password) {
                    Log.i("Login", "Success for user: ${user.username}")
                    
                    // Guardar sesión
                    SessionManager.saveUserSession(this@MainActivity, user.id_usuario)

                    val intent = Intent(this@MainActivity, MapaActivity::class.java)
                    startActivity(intent)
                    finish() // Close login activity so back button doesn't return here
                } else {
                    Toast.makeText(this@MainActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}