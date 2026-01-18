package com.misty.eventmanagement.actor.student

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.misty.eventmanagement.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

data class Event(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var date: String = ""  // stored as "dd/MM/yyyy"
)

class DashboardStudent : AppCompatActivity() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var eventContainer: LinearLayout
    private var eventList = mutableListOf<Event>()

    private val colors = listOf(
        "#FFEBEE", "#E3F2FD", "#E8F5E9", "#FFF3E0", "#F3E5F5", "#E0F7FA"
    ) // soft pastel colors

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_student)

        eventContainer = findViewById(R.id.eventContainerStudent)
        val searchLayout = findViewById<TextInputLayout>(R.id.searchLayout)
        val searchInput = searchLayout.editText

        loadEvents()

        // Search triggered by icon click
        searchLayout.setEndIconOnClickListener {
            val query = searchInput?.text.toString()
            if (query.isBlank()) {
                displayEvents(eventList) // show all if empty
            } else {
                filterEvents(query)
            }
        }
    }

    /** Load all events from Firestore */
    private fun loadEvents() {
        eventContainer.removeAllViews()
        db.collection("events").get().addOnSuccessListener { result ->
            eventList.clear()
            for (doc in result) {
                val event = doc.toObject(Event::class.java)
                event.id = doc.id
                eventList.add(event)
            }
            displayEvents(eventList)
        }
    }

    /** Display events in UI with RadioButtons */
    private fun displayEvents(list: List<Event>) {
        eventContainer.removeAllViews()
        val currentUserUid = auth.currentUser?.uid

        for (event in list) {
            val view = LayoutInflater.from(this).inflate(R.layout.event_item_student, null)

            val tvTitle = view.findViewById<TextView>(R.id.tvEventTitleStudent)
            val tvDesc = view.findViewById<TextView>(R.id.tvEventDescriptionStudent)
            val tvDate = view.findViewById<TextView>(R.id.tvEventDateStudent)
            val btnSeeMore = view.findViewById<TextView>(R.id.btnSeeMore)
            val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroupResponse)
            val btnSubmit = view.findViewById<Button>(R.id.btnSubmitResponse)

            tvTitle.text = event.title
            tvDesc.text = event.description
            tvDate.text = formatDateWithDay(event.date)

            // See More / See Less
            btnSeeMore.setOnClickListener {
                if (tvDesc.maxLines == 3) {
                    tvDesc.maxLines = Int.MAX_VALUE
                    btnSeeMore.text = "See Less"
                } else {
                    tvDesc.maxLines = 3
                    btnSeeMore.text = "See More"
                }
            }

            // Random soft pastel background
            val colorHex = colors[Random.nextInt(colors.size)]
            view.setBackgroundColor(Color.parseColor(colorHex))

            // Load previous response if exists
            if (currentUserUid != null) {
                db.collection("events").document(event.id)
                    .collection("responses").document(currentUserUid)
                    .get().addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            when (doc.getString("response")) {
                                "Interested" -> radioGroup.check(R.id.rbInterested)
                                "Not Interested" -> radioGroup.check(R.id.rbNotInterested)
                                "Going" -> radioGroup.check(R.id.rbGoing)
                            }
                        }
                    }
            }

            // Submit response
            btnSubmit.setOnClickListener {
                val selectedId = radioGroup.checkedRadioButtonId
                val response = when (selectedId) {
                    R.id.rbInterested -> "Interested"
                    R.id.rbNotInterested -> "Not Interested"
                    R.id.rbGoing -> "Going"
                    else -> null
                }

                if (response == null) {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                    // temporarily make button red
                    btnSubmit.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
                    btnSubmit.postDelayed({
                        btnSubmit.setBackgroundColor(resources.getColor(R.color.green_500))
                    }, 1000)
                    return@setOnClickListener
                }

                if (currentUserUid != null) {
                    db.collection("events").document(event.id)
                        .collection("responses").document(currentUserUid)
                        .set(mapOf("response" to response))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Response submitted", Toast.LENGTH_SHORT).show()
                            // make button green temporarily
                            btnSubmit.setBackgroundColor(resources.getColor(android.R.color.holo_green_light))
                            btnSubmit.postDelayed({
                                btnSubmit.setBackgroundColor(resources.getColor(R.color.green_500))
                            }, 1000)
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to submit response", Toast.LENGTH_SHORT).show()
                            // make button red temporarily
                            btnSubmit.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
                            btnSubmit.postDelayed({
                                btnSubmit.setBackgroundColor(resources.getColor(R.color.green_500))
                            }, 1000)
                        }
                }
            }

            // Add margin dynamically
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            view.layoutParams = params

            eventContainer.addView(view)
        }
    }

    /** Filter events by search query */
    private fun filterEvents(query: String) {
        val filtered = eventList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
        displayEvents(filtered)
    }

    /** Format date as "20 January 2026\nDay: Tuesday" */
    private fun formatDateWithDay(dateStr: String): String {
        return try {
            val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateStr) ?: return dateStr
            val outputDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val outputDay = SimpleDateFormat("EEEE", Locale.getDefault())
            "${outputDate.format(date)}\nDay: ${outputDay.format(date)}"
        } catch (e: Exception) {
            dateStr
        }
    }
}
