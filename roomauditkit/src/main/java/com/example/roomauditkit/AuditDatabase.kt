package com.example.roomauditkit

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AuditLogEntity::class], version = 1, exportSchema = false)
abstract class AuditDatabase : RoomDatabase() {
    abstract fun auditLogDao(): AuditLogDao
}
