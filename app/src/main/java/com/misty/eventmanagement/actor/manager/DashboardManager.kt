package com.misty.eventmanagement.actor.manager

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.misty.eventmanagement.R
import com.misty.eventmanagement.actor.manager.menu.ManagerMenuHandler
import com.google.android.material.appbar.MaterialToolbar
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

data class Event(
    var id: String = "",
    var title: String = "",
    var description: String = "",
    var date: String = "", // stored as "dd/MM/yyyy"
    var imageUrls: List<String> = listOf()
)

class DashboardManager : AppCompatActivity() {

    private lateinit var menuHandler: ManagerMenuHandler
    private val db = FirebaseFirestore.getInstance()
    private lateinit var eventContainer: LinearLayout
    private lateinit var searchLayout: TextInputLayout
    private var eventList = mutableListOf<Event>()

    // Soft pastel colors for cards
    private val cardColors = listOf(
        "#FFEBEE", "#E3F2FD", "#E8F5E9", "#FFF3E0", "#F3E5F5", "#E0F7FA"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_manager)

        // Views
        eventContainer = findViewById(R.id.eventContainer)
        searchLayout = findViewById(R.id.searchLayout)
        val searchInput = searchLayout.editText
        val fabAddEvent: FloatingActionButton = findViewById(R.id.fabAddEvent)
        fabAddEvent.setOnClickListener { showAddEventDialog() }

        // Toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarManager)
        setSupportActionBar(toolbar)

        // Menu handler
        menuHandler = ManagerMenuHandler(this)

        // Load events from Firestore
        loadEvents()

        // Search functionality
        searchLayout.setEndIconOnClickListener {
            val query = searchInput?.text.toString()
            if (query.isBlank()) {
                displayEvents(eventList)
            } else {
                val filtered = eventList.filter {
                    it.title.contains(query, ignoreCase = true) ||
                            it.description.contains(query, ignoreCase = true) ||
                            formatDateWithDay(it.date).contains(query, ignoreCase = true)
                }
                displayEvents(filtered)
            }
        }
    }

    // Inflate menu
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_manager_dashboard, menu)
        return true
    }

    // Handle menu clicks
    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        return menuHandler.handleMenuClick(item) || super.onOptionsItemSelected(item)
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

    /** Display events in UI */
    private fun displayEvents(list: List<Event>) {
        eventContainer.removeAllViews()
        for (event in list) {
            val view = layoutInflater.inflate(R.layout.event_item, null)
            val cardView = view as androidx.cardview.widget.CardView
            val tvTitle = view.findViewById<TextView>(R.id.tvEventTitle)
            val tvDesc = view.findViewById<TextView>(R.id.tvEventDescription)
            val tvDate = view.findViewById<TextView>(R.id.tvEventDate)
            val btnEdit = view.findViewById<Button>(R.id.btnEditEvent)
            val btnDelete = view.findViewById<Button>(R.id.btnDeleteEvent)
            val btnSeeMore = view.findViewById<Button>(R.id.btnSeeMore)
            val imageContainer = view.findViewById<LinearLayout>(R.id.imageContainer)

            tvTitle.text = event.title
            tvDesc.text = event.description
            tvDate.text = formatDateWithDay(event.date)

            // Random pastel color
            val randomColor = cardColors[Random.nextInt(cardColors.size)]
            cardView.setCardBackgroundColor(android.graphics.Color.parseColor(randomColor))

            // Images
            imageContainer.removeAllViews()
            event.imageUrls.forEach { url ->
                val img = ImageView(this)
                val params = LinearLayout.LayoutParams(300, 300)
                params.setMargins(8, 0, 8, 0)
                img.layoutParams = params
                img.scaleType = ImageView.ScaleType.CENTER_CROP
                // Glide.with(this).load(url).into(img)
                imageContainer.addView(img)
            }

            // See More toggle
            btnSeeMore.setOnClickListener {
                if (tvDesc.maxLines == 2) {
                    tvDesc.maxLines = Int.MAX_VALUE
                    btnSeeMore.text = "See Less"
                } else {
                    tvDesc.maxLines = 2
                    btnSeeMore.text = "See More"
                }
            }

            // Edit/Delete
            btnEdit.setOnClickListener { showAddEventDialog(event) }
            btnDelete.setOnClickListener { deleteEvent(event) }

            eventContainer.addView(view)
        }
    }

    /** Add/Edit Event Dialog */
    private fun showAddEventDialog(editingEvent: Event? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_event, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.etEventTitle)
        val descInput = dialogView.findViewById<EditText>(R.id.etEventDescription)
        val dateInput = dialogView.findViewById<EditText>(R.id.etEventDate)
        val btnPickDate = dialogView.findViewById<ImageButton>(R.id.btnPickDate)

        if (editingEvent != null) {
            titleInput.setText(editingEvent.title)
            descInput.setText(editingEvent.description)
            dateInput.setText(editingEvent.date)
        }

        btnPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    dateInput.setText(String.format("%02d/%02d/%04d", day, month + 1, year))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle(if (editingEvent == null) "Add New Event" else "Edit Event")
            .setView(dialogView)
            .setPositiveButton(if (editingEvent == null) "Add" else "Update") { dialog, _ ->
                saveEvent(titleInput.text.toString(), descInput.text.toString(), dateInput.text.toString(), emptyList(), editingEvent)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    /** Save event to Firestore */
    private fun saveEvent(title: String, description: String, date: String, imageUrls: List<String>, editingEvent: Event?) {
        val event = Event(title = title, description = description, date = date, imageUrls = imageUrls)
        if (editingEvent == null) {
            db.collection("events").add(event).addOnSuccessListener {
                Toast.makeText(this, "Event Added", Toast.LENGTH_SHORT).show()
                loadEvents()
            }
        } else {
            db.collection("events").document(editingEvent.id).set(event).addOnSuccessListener {
                Toast.makeText(this, "Event Updated", Toast.LENGTH_SHORT).show()
                loadEvents()
            }
        }
    }

    /** Delete event */
    private fun deleteEvent(event: Event) {
        db.collection("events").document(event.id).delete().addOnSuccessListener {
            Toast.makeText(this, "Event Deleted", Toast.LENGTH_SHORT).show()
            loadEvents()
        }
    }

    /** Format date */
    private fun formatDateWithDay(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = sdf.parse(dateStr)
            val displayFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
            val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
            "${displayFormat.format(date!!)}\nDay: ${dayFormat.format(date)}"
        } catch (e: Exception) {
            dateStr
        }
    }
}
