package com.example.roomauditkit

import android.content.Context
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object AuditExporter {

    suspend fun exportLogsToJson(context: Context, logs: List<AuditLog>): File = withContext(Dispatchers.IO) {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val jsonString = gson.toJson(logs)
        
        val file = File(context.filesDir, "audit_logs.json")
        file.writeText(jsonString)
        file
    }

    suspend fun exportLogsToCsv(context: Context, logs: List<AuditLog>): File = withContext(Dispatchers.IO) {
        val file = File(context.filesDir, "audit_logs.csv")
        file.bufferedWriter().use { writer ->
            // Write CSV Header
            writer.write("id,action,entityName,itemId,oldValue,newValue,timestamp\n")
            
            // Write Rows
            logs.forEach { log ->
                val id = log.id
                val action = log.action.name
                val entityName = escapeCsv(log.entityName)
                val itemId = escapeCsv(log.itemId)
                val oldValue = escapeCsv(log.oldValue ?: "")
                val newValue = escapeCsv(log.newValue ?: "")
                val timestamp = log.timestamp
                
                writer.write("$id,$action,$entityName,$itemId,$oldValue,$newValue,$timestamp\n")
            }
        }
        file
    }

    private fun escapeCsv(value: String): String {
        var escaped = value
        if (escaped.contains("\"")) {
            escaped = escaped.replace("\"", "\"\"")
        }
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            escaped = "\"$escaped\""
        }
        return escaped
    }
}
