package com.example.debt.repository

import com.example.debt.models.LinkedDevice
import com.example.debt.utils.SettingsKeys
import com.example.debt.utils.getLocalSettings
import com.example.debt.utils.getOrCreateDeviceId
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

object LinkingService {
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth
    private val settings = getLocalSettings()

    suspend fun generateLinkCode(): String {
        val ownerUid = auth.currentUser?.uid ?: throw IllegalStateException("Must be logged in to generate code")
        val code = (100000..999999).random().toString()
        
        val linkData = mapOf(
            "ownerUid" to ownerUid,
            "ownerEmail" to auth.currentUser?.email,
            "createdAt" to Clock.System.now().toEpochMilliseconds()
        )
        
        firestore.collection("link_codes").document(code).set(linkData)
        return code
    }

    suspend fun linkDevice(code: String, deviceName: String = "Secondary Phone"): Boolean {
        val doc = firestore.collection("link_codes").document(code).get()
        if (doc.exists) {
            val ownerUid = doc.get<String>("ownerUid")
            val ownerEmail = doc.get<String>("ownerEmail")
            
            val deviceId = getOrCreateDeviceId(settings)
            val finalDeviceName = deviceName.ifBlank { "Secondary Device" }
            
            val linkedDevice = LinkedDevice(
                deviceId = deviceId,
                deviceName = finalDeviceName,
                linkedAt = Clock.System.now()
            )

            // Register device in owner's linked_devices collection
            firestore.collection("users").document(ownerUid)
                .collection("linked_devices").document(deviceId).set(linkedDevice)

            settings.saveString(SettingsKeys.LINKED_SHOP_ID, ownerUid)
            settings.saveString(SettingsKeys.OWNER_EMAIL, ownerEmail)
            settings.saveString(SettingsKeys.DEVICE_NAME, finalDeviceName)
            return true
        }
        return false
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getLinkedDevices(): Flow<List<LinkedDevice>> {
        return auth.authStateChanged.flatMapLatest { user ->
            val ownerUid = user?.uid ?: return@flatMapLatest flowOf(emptyList())
            firestore.collection("users").document(ownerUid).collection("linked_devices")
                .snapshots
                .map { snapshot ->
                    snapshot.documents.filter { it.exists }.mapNotNull { doc ->
                        doc.toLinkedDeviceSafe()
                    }.sortedByDescending { it.linkedAt }
                }
                .catch { e ->
                    println("Error in getLinkedDevices: ${e.message}")
                    emit(emptyList())
                }
        }.catch { emit(emptyList()) }
    }

    suspend fun revokeDevice(deviceId: String) {
        val ownerUid = auth.currentUser?.uid ?: return
        try {
            firestore.collection("users").document(ownerUid).collection("linked_devices").document(deviceId).delete()
        } catch (e: Exception) {
            println("Error revoking device $deviceId: ${e.message}")
        }
    }

    fun getLinkedShopId(): String? {
        return settings.getString(SettingsKeys.LINKED_SHOP_ID)?.takeIf { it.isNotBlank() }
    }

    fun unlink() {
        settings.clear()
    }
}

private fun DocumentSnapshot.toLinkedDeviceSafe(): LinkedDevice? {
    if (!exists) return null
    try {
        val device: LinkedDevice = this.data()
        return if (device.deviceId.isBlank()) device.copy(deviceId = this.id) else device
    } catch (_: Exception) {
        val docId = this.id
        val name = runCatching { get<String>("deviceName") }.getOrDefault("Secondary Device")
        val linkedAtStr = runCatching { get<String>("linkedAt") }.getOrNull()
        val linkedAt = linkedAtStr?.let { runCatching { Instant.parse(it) }.getOrNull() } ?: Clock.System.now()
        return LinkedDevice(docId, name, linkedAt)
    }
}
