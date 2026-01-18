package com.misty.eventmanagement.auth

import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun loginUser(
        email: String,
        password: String,
        callback: (Boolean, String, String?) -> Unit
    ) {

        // 🔹 Hardcoded Admin
        if (email == "admin@gmail.com") {
            if (password != "1234567890") {
                callback(false, "Incorrect password for Admin", null)
                return
            }
            callback(true, "Admin login successful", "Admin")
            return
        }

        // 🔹 Validation
        if (email.isEmpty() && password.isEmpty()) {
            callback(false, "Email and Password are required", null)
            return
        }

        if (email.isEmpty()) {
            callback(false, "Email is required", null)
            return
        }

        if (password.isEmpty()) {
            callback(false, "Password is required", null)
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback(false, "Invalid email format", null)
            return
        }

        if (password.length < 6) {
            callback(false, "Password must be at least 6 characters", null)
            return
        }

        // 🔥 Firebase Login
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { res ->
                val uid = res.user!!.uid

                db.collection("users").document(uid).get()
                    .addOnSuccessListener { doc ->
                        if (!doc.exists()) {
                            callback(false, "User data not found", null)
                            return@addOnSuccessListener
                        }

                        val role = doc.getString("role")
                        val status = doc.getString("status")

                        if (status != "active") {
                            callback(false, "Account blocked", null)
                            return@addOnSuccessListener
                        }

                        callback(true, "Login successful", role)
                    }
            }
            .addOnFailureListener {
                callback(false, it.message ?: "Login failed", null)
            }
    }
}
