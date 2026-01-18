package com.misty.eventmanagement.actor.manager

import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.text.BaseColor
import com.itextpdf.text.Document
import com.itextpdf.text.Font
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfPCell
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import com.misty.eventmanagement.R
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

data class EventReport(
    val title: String,
    val interested: Int,
    val notInterested: Int,
    val going: Int
)

class ReportsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var container: LinearLayout
    private lateinit var btnExportCSV: Button
    private lateinit var btnExportPDF: Button
    private val reportList = mutableListOf<EventReport>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        container = findViewById(R.id.reportsContainer)
        btnExportCSV = findViewById(R.id.btnExportCSV)
        btnExportPDF = findViewById(R.id.btnExportPDF)

        loadReports()

        btnExportCSV.setOnClickListener { exportCSV() }
        btnExportPDF.setOnClickListener { exportPDF() }
    }

    private fun loadReports() {
        container.removeAllViews()
        reportList.clear()

        db.collection("events").get().addOnSuccessListener { events ->
            for (eventDoc in events) {
                val title = eventDoc.getString("title") ?: "No Title"

                eventDoc.reference.collection("responses").get()
                    .addOnSuccessListener { resDocs ->

                        var interested = 0
                        var notInterested = 0
                        var going = 0

                        for (doc in resDocs) {
                            when (doc.getString("response")) {
                                "Interested" -> interested++
                                "Not Interested" -> notInterested++
                                "Going" -> going++
                            }
                        }

                        val total = interested + notInterested + going
                        val percentInterested = if (total > 0) interested * 100 / total else 0
                        val percentNotInterested = if (total > 0) notInterested * 100 / total else 0
                        val percentGoing = if (total > 0) going * 100 / total else 0

                        reportList.add(EventReport(title, interested, notInterested, going))

                        // -------- Card --------
                        val cardView = CardView(this)
                        cardView.radius = 12f
                        cardView.cardElevation = 6f
                        cardView.setCardBackgroundColor(Color.WHITE)

                        val params = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        params.setMargins(0, 0, 0, 16)
                        cardView.layoutParams = params

                        val cardLayout = LinearLayout(this)
                        cardLayout.orientation = LinearLayout.VERTICAL
                        cardLayout.setPadding(24, 24, 24, 24)
                        cardView.addView(cardLayout)

                        // Title
                        val tvTitle = TextView(this)
                        tvTitle.text = title
                        tvTitle.textSize = 18f
                        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD)
                        cardLayout.addView(tvTitle)

                        // Total response
                        val tvTotal = TextView(this)
                        tvTotal.text = "Total Responses: $total"
                        tvTotal.setPadding(0, 8, 0, 8)
                        cardLayout.addView(tvTotal)

                        // -------- Progress Bars --------
                        fun addBar(label: String, count: Int, percent: Int, color: Int) {
                            val tvLabel = TextView(this)
                            tvLabel.text = "$label: ($count) $percent%"
                            tvLabel.setPadding(0, 8, 0, 4)
                            cardLayout.addView(tvLabel)

                            val bar = ProgressBar(
                                this,
                                null,
                                android.R.attr.progressBarStyleHorizontal
                            )
                            bar.max = 100
                            bar.progress = percent
                            bar.progressDrawable.setColorFilter(
                                color,
                                android.graphics.PorterDuff.Mode.SRC_IN
                            )
                            bar.layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                40
                            )
                            cardLayout.addView(bar)
                        }

                        addBar("Interested", interested, percentInterested, Color.parseColor("#4CAF50"))
                        addBar("Not Interested", notInterested, percentNotInterested, Color.parseColor("#F44336"))
                        addBar("Going", going, percentGoing, Color.parseColor("#2196F3"))

                        container.addView(cardView)
                    }
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to load reports", Toast.LENGTH_SHORT).show()
        }
    }

    // ---------------- CSV Export ----------------
    private fun exportCSV() {
        if (reportList.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(dir, "event_report.csv")

            val writer = FileWriter(file)
            writer.append("Event Title,Interested,Not Interested,Going,Total\n")
            for (r in reportList) {
                val total = r.interested + r.notInterested + r.going
                writer.append("${r.title},${r.interested},${r.notInterested},${r.going},$total\n")
            }
            writer.close()
            Toast.makeText(this, "CSV exported", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    // ---------------- PDF Export ----------------
    private fun exportPDF() {
        if (reportList.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(dir, "event_report.pdf")

            val document = Document()
            PdfWriter.getInstance(document, FileOutputStream(file))
            document.open()

            document.add(
                Paragraph(
                    "Event Reports\n\n",
                    Font(Font.FontFamily.HELVETICA, 18f, Font.BOLD)
                )
            )

            val table = PdfPTable(5)
            table.widthPercentage = 100f

            listOf("Event", "Interested", "Not Interested", "Going", "Total").forEach {
                val cell = PdfPCell(Phrase(it))
                cell.backgroundColor = BaseColor.LIGHT_GRAY
                table.addCell(cell)
            }

            for (r in reportList) {
                val total = r.interested + r.notInterested + r.going
                table.addCell(r.title)
                table.addCell(r.interested.toString())
                table.addCell(r.notInterested.toString())
                table.addCell(r.going.toString())
                table.addCell(total.toString())
            }

            document.add(table)
            document.close()
            Toast.makeText(this, "PDF exported", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }
}
