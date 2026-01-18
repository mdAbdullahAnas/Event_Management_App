package com.misty.eventmanagement.actor.manager.menu

import android.app.Activity
import android.content.Intent
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.misty.eventmanagement.MainActivity
import com.misty.eventmanagement.R
import com.misty.eventmanagement.actor.manager.ReportsActivity
import com.misty.eventmanagement.actor.manager.SettingsActivity

class ManagerMenuHandler(private val activity: Activity) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun handleMenuClick(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.menu_dashboard -> {
                Toast.makeText(activity, "Already on Dashboard", Toast.LENGTH_SHORT).show()
                true
            }

            R.id.menu_reports -> {
                // Start Reports Activity
                val intent = Intent(activity, ReportsActivity::class.java)
                activity.startActivity(intent)
                true
            }


            R.id.menu_settings -> {


                // Start SettingsActivity when Settings is clicked
                val intent = Intent(activity, SettingsActivity::class.java)
                activity.startActivity(intent)
                true
            }

            R.id.menu_logout -> {
                logout()
                true
            }

            else -> false
        }
    }

    private fun logout() {
        auth.signOut()

        val intent = Intent(activity, MainActivity::class.java)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        )

        activity.startActivity(intent)
        activity.finish()

        Toast.makeText(activity, "Logged out successfully", Toast.LENGTH_SHORT).show()
    }
}
