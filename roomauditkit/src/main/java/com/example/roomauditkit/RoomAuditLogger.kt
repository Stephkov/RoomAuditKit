package com.example.roomauditkit

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class RoomAuditLogger internal constructor(
    private val dao: AuditLogDao
) : AuditLogger {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun logInsert(entityName: String, itemId: String, newValueJson: String) {
        val log = AuditLog(
            action = AuditAction.INSERT,
            entityName = entityName,
            itemId = itemId,
            newValue = newValueJson
        )
        insertAsync(log)
    }

    override fun logUpdate(
        entityName: String,
        itemId: String,
        oldValueJson: String,
        newValueJson: String
    ) {
        val log = AuditLog(
            action = AuditAction.UPDATE,
            entityName = entityName,
            itemId = itemId,
            oldValue = oldValueJson,
            newValue = newValueJson
        )
        insertAsync(log)
    }

    override fun logDelete(entityName: String, itemId: String, oldValueJson: String) {
        val log = AuditLog(
            action = AuditAction.DELETE,
            entityName = entityName,
            itemId = itemId,
            oldValue = oldValueJson
        )
        insertAsync(log)
    }

    private fun insertAsync(log: AuditLog) {
        scope.launch {
            dao.insertLog(AuditLogEntity.fromAuditLog(log))
        }
    }

    override fun log(auditLog: AuditLog) {
        insertAsync(auditLog)
    }

    override suspend fun getAllLogs(): List<AuditLog> {
        return dao.getAllLogs().map { it.toAuditLog() }
    }

    override suspend fun getLogsSortedNewestFirst(): List<AuditLog> {
        return dao.getAllLogs().map { it.toAuditLog() }
    }

    override suspend fun getLogsSortedOldestFirst(): List<AuditLog> {
        return dao.getAllLogsOldestFirst().map { it.toAuditLog() }
    }

    override suspend fun getLogsByAction(action: AuditAction): List<AuditLog> {
        return dao.getLogsByAction(action.name).map { it.toAuditLog() }
    }

    override suspend fun getLogsByActionSorted(action: AuditAction, newestFirst: Boolean): List<AuditLog> {
        val entities = if (newestFirst) {
            dao.getLogsByAction(action.name)
        } else {
            dao.getLogsByActionOldestFirst(action.name)
        }
        return entities.map { it.toAuditLog() }
    }

    override suspend fun getLogsByEntity(entityName: String): List<AuditLog> {
        return dao.getLogsByEntity(entityName).map { it.toAuditLog() }
    }

    override suspend fun exportLogsToJson(context: Context): File {
        return AuditExporter.exportLogsToJson(context, getAllLogs())
    }

    override suspend fun exportLogsToCsv(context: Context): File {
        return AuditExporter.exportLogsToCsv(context, getAllLogs())
    }

    override suspend fun clearLogs() {
        dao.clearLogs()
    }
}
