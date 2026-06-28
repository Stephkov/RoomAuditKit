package com.example.sample

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.roomauditkit.AuditAction
import com.example.roomauditkit.AuditLog
import com.example.roomauditkit.AuditLogger
import com.example.roomauditkit.RoomAuditKit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogsActivity : AppCompatActivity() {

    private val logs = mutableListOf<AuditLog>()
    private lateinit var adapter: LogAdapter
    private lateinit var auditLogger: AuditLogger
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy  HH:mm:ss", Locale.getDefault())

    private var selectedAction: AuditAction? = null  // null = All
    private var newestFirst = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        auditLogger = RoomAuditKit.with(this).setDatabaseName("room_audit_db").build()

        val listViewLogs = findViewById<ListView>(R.id.listViewLogs)
        val btnClearLogs = findViewById<Button>(R.id.btnClearLogs)
        val btnExportJson = findViewById<Button>(R.id.btnExportJson)
        val btnExportCsv = findViewById<Button>(R.id.btnExportCsv)
        val spinnerAction = findViewById<Spinner>(R.id.spinnerAction)
        val spinnerSort = findViewById<Spinner>(R.id.spinnerSort)

        adapter = LogAdapter()
        listViewLogs.adapter = adapter

        // Set up Action filter spinner
        val actionOptions = listOf("All", "INSERT", "UPDATE", "DELETE")
        val actionAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, actionOptions)
        actionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerAction.adapter = actionAdapter
        spinnerAction.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                selectedAction = when (pos) {
                    1 -> AuditAction.INSERT
                    2 -> AuditAction.UPDATE
                    3 -> AuditAction.DELETE
                    else -> null
                }
                loadLogs()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Set up Sort spinner
        val sortOptions = listOf("Newest First", "Oldest First")
        val sortAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sortOptions)
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSort.adapter = sortAdapter
        spinnerSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                newestFirst = (pos == 0)
                loadLogs()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnClearLogs.setOnClickListener {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    auditLogger.clearLogs()
                }
                loadLogs()
                Toast.makeText(this@LogsActivity, "Logs cleared", Toast.LENGTH_SHORT).show()
            }
        }

        btnExportJson.setOnClickListener {
            lifecycleScope.launch {
                val file = auditLogger.exportLogsToJson(this@LogsActivity)
                Toast.makeText(
                    this@LogsActivity,
                    "JSON saved to:\n${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        btnExportCsv.setOnClickListener {
            lifecycleScope.launch {
                val file = auditLogger.exportLogsToCsv(this@LogsActivity)
                Toast.makeText(
                    this@LogsActivity,
                    "CSV saved to:\n${file.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        loadLogs()
    }

    private fun loadLogs() {
        lifecycleScope.launch {
            val fetchedLogs = withContext(Dispatchers.IO) {
                val action = selectedAction
                if (action != null) {
                    auditLogger.getLogsByActionSorted(action, newestFirst)
                } else {
                    if (newestFirst) {
                        auditLogger.getLogsSortedNewestFirst()
                    } else {
                        auditLogger.getLogsSortedOldestFirst()
                    }
                }
            }
            logs.clear()
            logs.addAll(fetchedLogs)
            adapter.notifyDataSetChanged()
        }
    }

    inner class LogAdapter : BaseAdapter() {
        override fun getCount(): Int = logs.size
        override fun getItem(position: Int): Any = logs[position]
        override fun getItemId(position: Int): Long = logs[position].id

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_log, parent, false)
            val log = logs[position]

            val tvActionChip = view.findViewById<TextView>(R.id.tvActionChip)
            val tvEntityId = view.findViewById<TextView>(R.id.tvEntityId)
            val tvTimestamp = view.findViewById<TextView>(R.id.tvTimestamp)
            val tvOldValue = view.findViewById<TextView>(R.id.tvOldValue)
            val tvNewValue = view.findViewById<TextView>(R.id.tvNewValue)
            val layoutOldValue = view.findViewById<LinearLayout>(R.id.layoutOldValue)
            val layoutNewValue = view.findViewById<LinearLayout>(R.id.layoutNewValue)

            // Set action chip text and color
            tvActionChip.text = log.action.name
            val chipBackground: Drawable? = when (log.action) {
                AuditAction.INSERT -> ContextCompat.getDrawable(this@LogsActivity, R.drawable.bg_chip_insert)
                AuditAction.UPDATE -> ContextCompat.getDrawable(this@LogsActivity, R.drawable.bg_chip_update)
                AuditAction.DELETE -> ContextCompat.getDrawable(this@LogsActivity, R.drawable.bg_chip_delete)
            }
            tvActionChip.background = chipBackground

            tvEntityId.text = "${log.entityName}  ·  ID: ${log.itemId}"
            tvTimestamp.text = dateFormat.format(Date(log.timestamp))

            // Show or hide Old Value
            if (log.oldValue != null) {
                layoutOldValue.visibility = View.VISIBLE
                tvOldValue.text = log.oldValue
            } else {
                layoutOldValue.visibility = View.GONE
            }

            // Show or hide New Value
            if (log.newValue != null) {
                layoutNewValue.visibility = View.VISIBLE
                tvNewValue.text = log.newValue
            } else {
                layoutNewValue.visibility = View.GONE
            }

            return view
        }
    }
}
