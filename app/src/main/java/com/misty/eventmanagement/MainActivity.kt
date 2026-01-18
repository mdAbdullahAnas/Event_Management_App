package com.misty.eventmanagement

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.misty.eventmanagement.actor.admin.Dashboard
import com.misty.eventmanagement.actor.guest.DashboardGuest
import com.misty.eventmanagement.actor.manager.DashboardManager
import com.misty.eventmanagement.actor.student.DashboardStudent
import com.misty.eventmanagement.auth.Login
import com.misty.eventmanagement.auth.RegisterActivity

class MainActivity : AppCompatActivity() {

    private lateinit var login: Login

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etEmail = findViewById<EditText>(R.id.textEmailAddress)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnGuest = findViewById<Button>(R.id.btnGuest)
        val btnRegister = findViewById<Button>(R.id.btnRegister)  // Register button

        login = Login()

        // 🔑 Login
        btnLogin.setOnClickListener {
            login.loginUser(
                etEmail.text.toString().trim(),
                etPassword.text.toString().trim()
            ) { success, msg, role ->

                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

                if (success) {
                    when (role) {
                        "Admin" -> startActivity(Intent(this, Dashboard::class.java))
                        "EventManager" -> startActivity(Intent(this, DashboardManager::class.java))
                        "Student" -> startActivity(Intent(this, DashboardStudent::class.java))
                    }
                }
            }
        }

        // 🔓 Guest
        btnGuest.setOnClickListener {
            startActivity(Intent(this, DashboardGuest::class.java))
        }

        // 📝 Register
        btnRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
