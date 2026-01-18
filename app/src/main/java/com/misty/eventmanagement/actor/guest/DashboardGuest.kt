package com.misty.eventmanagement.actor.guest

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.misty.eventmanagement.R
import kotlin.random.Random

data class EventGuest(
    var id: String = "",
    var title: String = "",
    var description: String = ""
)

class DashboardGuest : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var eventContainer: LinearLayout
    private lateinit var searchLayout: TextInputLayout
    private var eventList = mutableListOf<EventGuest>()
    private val colors = listOf(
        "#FFEBEE", "#E3F2FD", "#E8F5E9", "#FFF3E0", "#F3E5F5", "#E0F7FA"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_guest)

        eventContainer = findViewById(R.id.eventContainerGuest)
        searchLayout = findViewById(R.id.searchLayout)
        val searchInput = searchLayout.editText

        loadEvents()

        // Search button click
        searchLayout.setEndIconOnClickListener {
            val query = searchInput?.text.toString()
            if (query.isBlank()) {
                Toast.makeText(this, "Enter text to search", Toast.LENGTH_SHORT).show()
                displayEvents(eventList)
            } else {
                filterEvents(query)
            }
        }
    }

    private fun loadEvents() {
        eventContainer.removeAllViews()
        db.collection("events").get().addOnSuccessListener { result ->
            eventList.clear()
            for (doc in result) {
                val event = doc.toObject(EventGuest::class.java)
                event.id = doc.id
                eventList.add(event)
            }
            displayEvents(eventList)
        }
    }

    private fun displayEvents(list: List<EventGuest>) {
        eventContainer.removeAllViews()
        for (event in list) {
            val view = LayoutInflater.from(this).inflate(R.layout.event_item_guest, null)
            val tvTitle = view.findViewById<TextView>(R.id.tvEventTitleGuest)
            val tvDesc = view.findViewById<TextView>(R.id.tvEventDescriptionGuest)
            val btnSeeMore = view.findViewById<TextView>(R.id.btnSeeMoreGuest)

            tvTitle.text = event.title
            tvDesc.text = event.description

            // See More / See Less toggle
            btnSeeMore.setOnClickListener {
                if (tvDesc.maxLines == 2) {
                    tvDesc.maxLines = Int.MAX_VALUE
                    btnSeeMore.text = "See Less"
                } else {
                    tvDesc.maxLines = 2
                    btnSeeMore.text = "See More"
                }
            }

            // Random pastel background
            val colorHex = colors[Random.nextInt(colors.size)]
            view.setBackgroundColor(Color.parseColor(colorHex))

            // Margin dynamically
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            view.layoutParams = params

            eventContainer.addView(view)
        }
    }

    private fun filterEvents(query: String) {
        val filtered = eventList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
        displayEvents(filtered)
    }
}
