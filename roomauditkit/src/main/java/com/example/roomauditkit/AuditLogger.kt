package com.example.roomauditkit

import android.content.Context
import java.io.File

/**
 * The main interface for logging audit actions to the database.
 */
interface AuditLogger {
    /**
     * Logs an insert action for the given entity.
     */
    fun logInsert(entityName: String, itemId: String, newValueJson: String)

    /**
     * Logs an update action for the given entity.
     */
    fun logUpdate(entityName: String, itemId: String, oldValueJson: String, newValueJson: String)

    /**
     * Logs a delete action for the given entity.
     */
    fun logDelete(entityName: String, itemId: String, oldValueJson: String)
    
    /**
     * Logs a custom [AuditLog] built via [AuditLogBuilder].
     */
    fun log(auditLog: AuditLog)

    /**
     * Retrieves all logs.
     */
    suspend fun getAllLogs(): List<AuditLog>

    /**
     * Retrieves all logs ordered by newest first.
     */
    suspend fun getLogsSortedNewestFirst(): List<AuditLog>

    /**
     * Retrieves all logs ordered by oldest first.
     */
    suspend fun getLogsSortedOldestFirst(): List<AuditLog>

    /**
     * Retrieves all logs matching the specific [action].
     */
    suspend fun getLogsByAction(action: AuditAction): List<AuditLog>

    /**
     * Retrieves all logs matching the specific [action] ordered by timestamp.
     */
    suspend fun getLogsByActionSorted(action: AuditAction, newestFirst: Boolean): List<AuditLog>

    /**
     * Retrieves all logs matching the specific [entityName].
     */
    suspend fun getLogsByEntity(entityName: String): List<AuditLog>

    /**
     * Exports all current logs to a JSON file.
     */
    suspend fun exportLogsToJson(context: Context): File

    /**
     * Exports all current logs to a CSV file.
     */
    suspend fun exportLogsToCsv(context: Context): File

    /**
     * Clears all logs from the database.
     */
    suspend fun clearLogs()
}
