package com.example.roomauditkit

data class AuditLog(
    val id: Long = 0,
    val action: AuditAction,
    val entityName: String,
    val itemId: String,
    val oldValue: String? = null,
    val newValue: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
