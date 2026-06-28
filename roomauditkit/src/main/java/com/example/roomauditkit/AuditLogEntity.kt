package com.example.roomauditkit

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val action: String,
    val entityName: String,
    val itemId: String,
    val oldValue: String?,
    val newValue: String?,
    val timestamp: Long
) {
    fun toAuditLog(): AuditLog {
        return AuditLog(
            id = id,
            action = AuditAction.valueOf(action),
            entityName = entityName,
            itemId = itemId,
            oldValue = oldValue,
            newValue = newValue,
            timestamp = timestamp
        )
    }

    companion object {
        fun fromAuditLog(log: AuditLog): AuditLogEntity {
            return AuditLogEntity(
                id = log.id,
                action = log.action.name,
                entityName = log.entityName,
                itemId = log.itemId,
                oldValue = log.oldValue,
                newValue = log.newValue,
                timestamp = log.timestamp
            )
        }
    }
}
