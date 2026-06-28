# RoomAuditKit

RoomAuditKit is a lightweight Android/Kotlin library for recording local audit history of data operations in Android apps.

The library is designed for apps that use local data and want to keep a separate history of what changed, when it changed, and what the previous and new values were.

RoomAuditKit stores INSERT, UPDATE, and DELETE events in a dedicated Room database and exposes simple APIs for writing, reading, filtering, sorting, clearing, and exporting audit logs.

It can answer questions such as:

- Which data item was inserted?
- Which data item was updated?
- What was the old value before the update?
- What is the new value after the update?
- Which data item was deleted?
- When did the operation happen?
- Which logs belong to a specific entity?
- Which logs belong to a specific action type?
- How can I export the logs for debugging or QA?

The library works fully locally and does not require a server, Firebase, authentication, or an internet connection.
## Features

- Works locally inside the Android app
- Uses Room SQL for persistent audit storage
- Records `INSERT`,`UPDATE`, and `DELETE` operations
- Stores the entity/table name
- Stores the changed item ID
- Stores the old and new value as a JSON string
- Supports reading all logs
- Supports filtering logs by action type
- Supports sorting logs newest-first or oldest-first
- Supports clearing all audit logs
- Supports exporting logs to JSON or CSV
- Includes a fluent AuditLogBuilder
- Includes a sample product management app
- Keeps audit logging separate from your main app database


## Implementation

### Local module

RoomAuditKit can be used as a local Gradle module. Add the library module to your project and include it from `settings.gradle.kts`:

```kotlin
include(":roomauditkit")
```

Then add the module dependency to your app module:

```kotlin
dependencies {
    implementation(project(":roomauditkit"))
}
```

### JitPack

The `roomauditkit` module can be prepared for Maven publishing with JitPack after the project is pushed to GitHub and a release or tag is created.

Add JitPack to `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}
```

Add the dependency:

```kotlin
dependencies {
    implementation("com.github.StephKov:roomauditkit:1.0.0")
}
```

## Quick Start

Create an `AuditLogger` once from an Android `Context`:

```kotlin
import com.example.roomauditkit.RoomAuditKit

val auditLogger = RoomAuditKit.create(context)
```

Log changes from the places where your app creates, updates, or deletes data:

```kotlin
auditLogger.logInsert(
    entityName = "Product",
    itemId = product.id,
    newValueJson = gson.toJson(product)
)
```

Read logs from a coroutine:

```kotlin
lifecycleScope.launch {
    val logs = auditLogger.getLogsSortedNewestFirst()
}
```

## Usage Examples

### INSERT

Use `logInsert` when a new entity is created.

```kotlin
auditLogger.logInsert(
    entityName = "Product",
    itemId = product.id,
    newValueJson = gson.toJson(product)
)
```

### UPDATE

Use `logUpdate` when an existing entity changes. Pass both the old value and the new value.

```kotlin
auditLogger.logUpdate(
    entityName = "Product",
    itemId = updatedProduct.id,
    oldValueJson = gson.toJson(oldProduct),
    newValueJson = gson.toJson(updatedProduct)
)
```

### DELETE

Use `logDelete` before or when an entity is removed.

```kotlin
auditLogger.logDelete(
    entityName = "Product",
    itemId = product.id,
    oldValueJson = gson.toJson(product)
)
```

## Builder API

Use `RoomAuditKit.with(context)` when you want to configure the internal audit database name.

```kotlin
val auditLogger = RoomAuditKit.with(context)
    .setDatabaseName("room_audit_db")
    .build()
```

For custom log creation, use `AuditLogBuilder` and pass the result to `auditLogger.log(...)`.

```kotlin
import com.example.roomauditkit.AuditAction
import com.example.roomauditkit.AuditLogBuilder

auditLogger.log(
    AuditLogBuilder()
        .action(AuditAction.UPDATE)
        .entityName("Product")
        .itemId(updatedProduct.id)
        .oldValueJson(gson.toJson(oldProduct))
        .newValueJson(gson.toJson(updatedProduct))
        .build()
)
```

## Reading Logs

All read APIs are suspend functions.

```kotlin
lifecycleScope.launch {
    val allLogs = auditLogger.getAllLogs()
    val newestFirst = auditLogger.getLogsSortedNewestFirst()
    val oldestFirst = auditLogger.getLogsSortedOldestFirst()
}
```

Filter by action:

```kotlin
lifecycleScope.launch {
    val inserts = auditLogger.getLogsByAction(AuditAction.INSERT)
    val updatesNewestFirst = auditLogger.getLogsByActionSorted(
        action = AuditAction.UPDATE,
        newestFirst = true
    )
}
```

Filter by entity:

```kotlin
lifecycleScope.launch {
    val productLogs = auditLogger.getLogsByEntity("Product")
}
```

Clear all logs:

```kotlin
lifecycleScope.launch {
    auditLogger.clearLogs()
}
```

## Export JSON/CSV

Export APIs live on `AuditLogger` and export all current logs.

```kotlin
lifecycleScope.launch {
    val jsonFile = auditLogger.exportLogsToJson(context)
    val csvFile = auditLogger.exportLogsToCsv(context)
}
```

The JSON export writes a pretty-printed array of `AuditLog` objects:

```json
[
  {
    "id": 1,
    "action": "INSERT",
    "entityName": "Product",
    "itemId": "product-1",
    "newValue": "{\"id\":\"product-1\",\"name\":\"Desk\",\"price\":149.99}",
    "timestamp": 1719413280000
  }
]
```

The CSV export writes the following columns:

```text
id,action,entityName,itemId,oldValue,newValue,timestamp
```

Both files are written under the app's internal files directory using the current implementation.

## Sample App

The `sample` module demonstrates RoomAuditKit in a small product-management app.

The main screen shows products and lets you:

- Add a product, logged as `INSERT`.
- Edit a product, logged as `UPDATE`.
- Delete a product, logged as `DELETE`.
- Open the audit log viewer.

The logs screen shows stored audit events with:

- Action filtering for All, `INSERT`, `UPDATE`, and `DELETE`.
- Sort order switching between newest-first and oldest-first.
- Old and new JSON values when available.
- JSON and CSV export through the public `AuditLogger` API.

Run the sample from Android Studio by selecting the `sample` configuration.

## Screenshots

<p align="center">
  <img src="screenshots/Screenshot%202026-06-27%20163630.png" width="230" alt="alt1" />
  <img src="screenshots/Screenshot%202026-06-27%20163641.png" width="230" alt="alt2" />
  <img src="screenshots/Screenshot%202026-06-27%20163703.png" width="230" alt="alt3" />
</p>

<p align="center">
  <img src="screenshots/Screenshot%202026-06-27%20163715.png" width="230" alt="alt4" />
  <img src="screenshots/Screenshot%202026-06-27%20163731.png" width="230" alt="alt5" />
  <img src="screenshots/Screenshot%202026-06-27%20163740.png" width="230" alt="alt6" />
</p>

<p align="center">
  <img src="screenshots/Screenshot%202026-06-27%20163750.png" width="230" alt="alt7" />
</p>
## API Overview

| Type | API | Description |
|---|---|---|
| `RoomAuditKit` | `create(context)` | Creates an `AuditLogger` with the default database name. |
| `RoomAuditKit` | `with(context)` | Starts the builder flow. |
| `RoomAuditKit.Builder` | `setDatabaseName(name)` | Sets the Room database name used for audit storage. |
| `RoomAuditKit.Builder` | `build()` | Creates or returns the singleton `AuditLogger`. |
| `AuditLogger` | `logInsert(entityName, itemId, newValueJson)` | Logs an insert event. |
| `AuditLogger` | `logUpdate(entityName, itemId, oldValueJson, newValueJson)` | Logs an update event. |
| `AuditLogger` | `logDelete(entityName, itemId, oldValueJson)` | Logs a delete event. |
| `AuditLogger` | `log(auditLog)` | Logs a custom `AuditLog`. |
| `AuditLogger` | `getAllLogs()` | Reads all logs. |
| `AuditLogger` | `getLogsSortedNewestFirst()` | Reads logs newest-first. |
| `AuditLogger` | `getLogsSortedOldestFirst()` | Reads logs oldest-first. |
| `AuditLogger` | `getLogsByAction(action)` | Reads logs for a specific action. |
| `AuditLogger` | `getLogsByActionSorted(action, newestFirst)` | Reads logs for an action with explicit sort order. |
| `AuditLogger` | `getLogsByEntity(entityName)` | Reads logs for a specific entity name. |
| `AuditLogger` | `exportLogsToJson(context)` | Exports all current logs to `audit_logs.json`. |
| `AuditLogger` | `exportLogsToCsv(context)` | Exports all current logs to `audit_logs.csv`. |
| `AuditLogger` | `clearLogs()` | Deletes all stored audit logs. |
| `AuditLogBuilder` | `action(action)` | Sets the action on a custom log. |
| `AuditLogBuilder` | `entityName(entityName)` | Sets the tracked entity name. |
| `AuditLogBuilder` | `itemId(itemId)` | Sets the tracked item ID. |
| `AuditLogBuilder` | `oldValueJson(oldValue)` | Sets the previous JSON value. |
| `AuditLogBuilder` | `newValueJson(newValue)` | Sets the next JSON value. |
| `AuditLogBuilder` | `build()` | Builds an `AuditLog`. |

## Requirements

- Min SDK: 26
- Kotlin
- AndroidX Room
- Kotlin Coroutines
- Gson
- Android app context for initialization and export

## License

RoomAuditKit is released under the MIT License.

```text
MIT License

Copyright (c) 2026 RoomAuditKit

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
