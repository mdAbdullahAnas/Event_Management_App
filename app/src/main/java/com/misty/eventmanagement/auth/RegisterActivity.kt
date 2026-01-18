package com.misty.eventmanagement.auth

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.misty.eventmanagement.R

class RegisterActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etInstitution = findViewById<EditText>(R.id.etInstitution)
        val spRole = findViewById<Spinner>(R.id.spRole)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        spRole.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Student", "EventManager")
        )

        btnRegister.setOnClickListener {

            auth.createUserWithEmailAndPassword(
                etEmail.text.toString(),
                etPassword.text.toString()
            ).addOnSuccessListener {

                val uid = it.user!!.uid

                val userData = hashMapOf(
                    "uid" to uid,
                    "email" to etEmail.text.toString(),
                    "phone" to etPhone.text.toString(),
                    "institution" to etInstitution.text.toString(),
                    "role" to spRole.selectedItem.toString(),
                    "status" to "active",
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users").document(uid).set(userData)

                Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
