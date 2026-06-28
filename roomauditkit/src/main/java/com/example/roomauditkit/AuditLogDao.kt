package com.example.roomauditkit

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insertLog(log: AuditLogEntity)

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<AuditLogEntity>

    @Query("SELECT * FROM audit_logs ORDER BY timestamp ASC")
    suspend fun getAllLogsOldestFirst(): List<AuditLogEntity>

    @Query("SELECT * FROM audit_logs WHERE action = :action ORDER BY timestamp DESC")
    suspend fun getLogsByAction(action: String): List<AuditLogEntity>

    @Query("SELECT * FROM audit_logs WHERE action = :action ORDER BY timestamp ASC")
    suspend fun getLogsByActionOldestFirst(action: String): List<AuditLogEntity>

    @Query("SELECT * FROM audit_logs WHERE entityName = :entityName ORDER BY timestamp DESC")
    suspend fun getLogsByEntity(entityName: String): List<AuditLogEntity>

    @Query("DELETE FROM audit_logs")
    suspend fun clearLogs()
}
