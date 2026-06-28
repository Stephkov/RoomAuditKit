package com.example.roomauditkit

/**
 * A builder class for creating [AuditLog] instances.
 */
class AuditLogBuilder {
    private var action: AuditAction? = null
    private var entityName: String? = null
    private var itemId: String? = null
    private var oldValue: String? = null
    private var newValue: String? = null

    /**
     * Sets the action type for this log.
     */
    fun action(action: AuditAction) = apply { this.action = action }

    /**
     * Sets the entity name for this log.
     */
    fun entityName(entityName: String) = apply { this.entityName = entityName }

    /**
     * Sets the unique identifier of the item being tracked.
     */
    fun itemId(itemId: String) = apply { this.itemId = itemId }

    /**
     * Sets the old value (JSON string) before the action occurred.
     */
    fun oldValueJson(oldValue: String?) = apply { this.oldValue = oldValue }

    /**
     * Sets the new value (JSON string) after the action occurred.
     */
    fun newValueJson(newValue: String?) = apply { this.newValue = newValue }

    /**
     * Builds and returns the [AuditLog] instance.
     * @throws IllegalArgumentException if required fields (action, entityName, itemId) are missing.
     */
    fun build(): AuditLog {
        requireNotNull(action) { "AuditAction is required." }
        requireNotNull(entityName) { "Entity name is required." }
        requireNotNull(itemId) { "Item ID is required." }

        return AuditLog(
            action = action!!,
            entityName = entityName!!,
            itemId = itemId!!,
            oldValue = oldValue,
            newValue = newValue,
            timestamp = System.currentTimeMillis()
        )
    }
}
