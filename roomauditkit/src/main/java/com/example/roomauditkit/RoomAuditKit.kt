package com.example.roomauditkit

import android.content.Context
import androidx.room.Room

object RoomAuditKit {

    @Volatile
    private var loggerInstance: AuditLogger? = null

    /**
     * Builder class to customize RoomAuditKit initialization.
     */
    class Builder(private val context: Context) {
        private var databaseName: String = "room_audit_db"

        /**
         * Sets the database name used by Room to store audit logs.
         */
        fun setDatabaseName(name: String) = apply { this.databaseName = name }

        /**
         * Builds and returns the singleton [AuditLogger] instance.
         */
        fun build(): AuditLogger {
            return loggerInstance ?: synchronized(RoomAuditKit::class.java) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    AuditDatabase::class.java,
                    databaseName
                ).build()
                
                val newInstance = RoomAuditLogger(db.auditLogDao())
                loggerInstance = newInstance
                newInstance
            }
        }
    }

    /**
     * Starts the building process for the [AuditLogger].
     */
    fun with(context: Context): Builder {
        return Builder(context)
    }

    /**
     * A simple shorthand to create the [AuditLogger] with default settings.
     */
    fun create(context: Context): AuditLogger {
        return Builder(context).build()
    }
    
    // Kept for backward compatibility
    fun getLogger(context: Context): AuditLogger {
        return create(context)
    }
}
