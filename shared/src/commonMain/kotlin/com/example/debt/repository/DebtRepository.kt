package com.example.debt.repository

import com.example.debt.models.Debt
import com.example.debt.models.LogEntry
import com.example.debt.models.Payment
import com.example.debt.utils.getLocalSettings
import com.example.debt.utils.SettingsKeys
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.DocumentSnapshot
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.random.Random

class DebtRepository {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val settings = getLocalSettings()

    private fun getTargetUid(currentUserUid: String?): String? {
        val linkedId = settings.getString(SettingsKeys.LINKED_SHOP_ID)?.takeIf { it.isNotBlank() }
        return linkedId ?: currentUserUid ?: auth.currentUser?.uid
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDebts(): Flow<List<Debt>> {
        return auth.authStateChanged.flatMapLatest { user ->
            val targetUid = getTargetUid(user?.uid)
            if (targetUid != null) {
                firestore.collection("users").document(targetUid).collection("debts")
                    .snapshots
                    .map { snapshot ->
                        snapshot.documents.filter { it.exists }.map { doc ->
                            doc.toDebtSafe()
                        }
                    }
                    .catch { e ->
                        println("Error in getDebts snapshot listener for $targetUid: ${e.message}")
                    }
            } else {
                flowOf(emptyList())
            }
        }.catch { e ->
            println("Error in getDebts auth stream: ${e.message}")
            emit(emptyList())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAuditLogs(): Flow<List<LogEntry>> {
        return auth.authStateChanged.flatMapLatest { user ->
            val targetUid = getTargetUid(user?.uid)
            if (targetUid != null) {
                firestore.collection("users").document(targetUid).collection("audit_logs")
                    .snapshots
                    .map { snapshot ->
                        snapshot.documents.filter { it.exists }.map { doc ->
                            doc.toLogEntrySafe()
                        }.sortedByDescending { it.timestamp }
                    }
                    .catch { e ->
                        println("Error in getAuditLogs snapshot listener for $targetUid: ${e.message}")
                    }
            } else {
                flowOf(emptyList())
            }
        }.catch { e ->
            println("Error in getAuditLogs auth stream: ${e.message}")
            emit(emptyList())
        }
    }

    suspend fun saveDebt(debt: Debt, isUpdate: Boolean = false) {
        try {
            val targetUid = getTargetUid(null) ?: return
            firestore.collection("users").document(targetUid).collection("debts").document(debt.id).set(debt)
            logAction(
                actionType = if (isUpdate) "UPDATE_DEBT" else "ADD_DEBT",
                description = "${if (isUpdate) "Updated" else "Added"} debt for ${debt.customerName}: KES ${debt.totalAmount}"
            )
        } catch (e: Exception) {
            println("Error saving debt: ${e.message}")
        }
    }

    suspend fun deleteDebt(debt: Debt) {
        try {
            val targetUid = getTargetUid(null) ?: return
            firestore.collection("users").document(targetUid).collection("debts").document(debt.id).delete()
            logAction(
                actionType = "DELETE_DEBT",
                description = "Deleted debt for ${debt.customerName}: KES ${debt.totalAmount}"
            )
        } catch (e: Exception) {
            println("Error deleting debt: ${e.message}")
        }
    }

    suspend fun logAction(actionType: String, description: String) {
        try {
            val targetUid = getTargetUid(null) ?: return
            val userEmail = auth.currentUser?.email ?: "Unknown User"
            val logEntry = LogEntry(
                id = "${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(1000, 9999)}",
                timestamp = Clock.System.now(),
                actionType = actionType,
                description = description,
                actorEmail = userEmail
            )
            firestore.collection("users").document(targetUid).collection("audit_logs").document(logEntry.id).set(logEntry)
        } catch (e: Exception) {
            println("Error logging action: ${e.message}")
        }
    }

    fun getShopName(): String {
        return settings.getString(SettingsKeys.SHOP_NAME) ?: "Thawne Shop"
    }
}

private fun DocumentSnapshot.toDebtSafe(): Debt {
    if (!exists) {
        return Debt(
            id = id,
            customerName = "Unknown Customer",
            customerID = "",
            phoneNumber = "",
            product = "Item",
            totalAmount = 0.0,
            dateTaken = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            dueDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date,
            createdAt = Clock.System.now()
        )
    }
    // Try standard kotlinx.serialization decoding first
    try {
        val debt: Debt = this.data()
        return if (debt.id.isBlank()) debt.copy(id = this.id) else debt
    } catch (e: Exception) {
        println("Direct deserialization failed for debt doc $id: ${e.message}. Attempting resilient fallback parsing...")
    }

    // Resilient fallback parsing if fields/types vary in Firestore
    val docId = this.id
    val customerName = runCatching { get<String>("customerName") }.getOrDefault("Customer (${docId.take(4)})")
    val customerID = runCatching { get<String>("customerID") }.getOrDefault("")
    val phoneNumber = runCatching { get<String>("phoneNumber") }.getOrDefault("")
    val product = runCatching { get<String>("product") }.getOrDefault("General Item")

    val totalAmount = runCatching { get<Double>("totalAmount") }.getOrElse {
        runCatching { get<Long>("totalAmount").toDouble() }.getOrElse {
            runCatching { get<Int>("totalAmount").toDouble() }.getOrElse {
                runCatching { get<String>("totalAmount").toDoubleOrNull() ?: 0.0 }.getOrDefault(0.0)
            }
        }
    }

    val payments = runCatching { get<List<Payment>>("payments") }.getOrDefault(emptyList())

    val dateTakenStr = runCatching { get<String>("dateTaken") }.getOrNull()
    val dateTaken = dateTakenStr?.let {
        runCatching { LocalDate.parse(it) }.getOrNull()
    } ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    val dueDateStr = runCatching { get<String>("dueDate") }.getOrNull()
    val dueDate = dueDateStr?.let {
        runCatching { LocalDate.parse(it) }.getOrNull()
    } ?: dateTaken

    val notes = runCatching { get<String>("notes") }.getOrDefault("")

    val createdAt = runCatching {
        val tsStr = get<String>("createdAt")
        Instant.parse(tsStr)
    }.getOrElse {
        runCatching {
            val epochMillis = get<Long>("createdAt")
            Instant.fromEpochMilliseconds(epochMillis)
        }.getOrDefault(Clock.System.now())
    }

    return Debt(
        id = docId,
        customerName = customerName,
        customerID = customerID,
        phoneNumber = phoneNumber,
        product = product,
        totalAmount = totalAmount,
        payments = payments,
        dateTaken = dateTaken,
        dueDate = dueDate,
        notes = notes,
        createdAt = createdAt
    )
}

private fun DocumentSnapshot.toLogEntrySafe(): LogEntry {
    if (!exists) {
        return LogEntry(
            id = id,
            timestamp = Clock.System.now(),
            actionType = "UNKNOWN",
            description = "",
            actorEmail = "Unknown"
        )
    }
    try {
        val entry: LogEntry = this.data()
        return if (entry.id.isBlank()) entry.copy(id = this.id) else entry
    } catch (e: Exception) {
        println("Direct deserialization failed for log doc $id: ${e.message}. Attempting resilient fallback parsing...")
    }

    val docId = this.id
    val actionType = runCatching { get<String>("actionType") }.getOrDefault("UNKNOWN")
    val description = runCatching { get<String>("description") }.getOrDefault("")
    val actorEmail = runCatching { get<String>("actorEmail") }.getOrDefault("Unknown")

    val timestamp = runCatching {
        val tsStr = get<String>("timestamp")
        Instant.parse(tsStr)
    }.getOrElse {
        runCatching {
            val epochMillis = get<Long>("timestamp")
            Instant.fromEpochMilliseconds(epochMillis)
        }.getOrDefault(Clock.System.now())
    }

    return LogEntry(
        id = docId,
        timestamp = timestamp,
        actionType = actionType,
        description = description,
        actorEmail = actorEmail
    )
}
