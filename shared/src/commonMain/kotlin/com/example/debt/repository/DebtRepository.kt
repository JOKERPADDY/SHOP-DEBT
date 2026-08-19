package com.example.debt.repository

import com.example.debt.models.Debt
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DebtRepository {
    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    private val debtsCollection get() = auth.currentUser?.let { 
        firestore.collection("users").document(it.uid).collection("debts")
    }

    fun getDebts(): Flow<List<Debt>> {
        return debtsCollection?.snapshots?.map { snapshot ->
            snapshot.documents.map { it.data() }
        } ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    suspend fun saveDebt(debt: Debt) {
        debtsCollection?.document(debt.id)?.set(debt)
    }

    suspend fun deleteDebt(debtId: String) {
        debtsCollection?.document(debtId)?.delete()
    }
}
