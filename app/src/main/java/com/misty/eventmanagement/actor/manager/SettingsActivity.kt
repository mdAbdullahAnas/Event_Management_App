package com.misty.eventmanagement.actor.manager

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.misty.eventmanagement.R

class SettingsActivity : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val switchNotifications = findViewById<Switch>(R.id.switchNotifications)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)

        // 🔔 Notifications switch
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Notifications ${if (isChecked) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
        }

        // 🔑 Change password
        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrent = dialogView.findViewById<android.widget.EditText>(R.id.etCurrentPassword)
        val etNew = dialogView.findViewById<android.widget.EditText>(R.id.etNewPassword)
        val etConfirm = dialogView.findViewById<android.widget.EditText>(R.id.etConfirmPassword)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val current = etCurrent.text.toString().trim()
                val newPass = etNew.text.toString().trim()
                val confirmPass = etConfirm.text.toString().trim()

                // Validation
                if (current.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPass.length < 6) {
                    Toast.makeText(this, "New password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPass != confirmPass) {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // 🔄 Reauthenticate and update password
                val user = auth.currentUser
                if (user != null && user.email != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, current)
                    user.reauthenticate(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                user.updatePassword(newPass)
                                    .addOnCompleteListener { updateTask ->
                                        if (updateTask.isSuccessful) {
                                            Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(this, "Failed: ${updateTask.exception?.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(this, "Current password incorrect", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .create()

        dialog.show()
    }
}
