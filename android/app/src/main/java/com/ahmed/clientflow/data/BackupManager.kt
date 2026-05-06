package com.ahmed.clientflow.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupManager(private val context: Context, private val repository: AppRepository) {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    data class BackupResult(
        val success: Boolean,
        val message: String,
        val bytesWritten: Int = 0
    )

    data class RestoreResult(
        val success: Boolean,
        val message: String,
        val clientsImported: Int = 0,
        val bookingsImported: Int = 0,
        val paymentsImported: Int = 0,
        val expensesImported: Int = 0,
        val invoicesImported: Int = 0
    )

    suspend fun exportToUri(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        try {
            val currentState = repository.appState.first()
            val backupData = BackupData(
                version = 1,
                exportedAt = System.currentTimeMillis(),
                appState = currentState
            )
            
            val jsonString = json.encodeToString(BackupData.serializer(), backupData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(jsonString)
                }
            } ?: return@withContext BackupResult(false, "Failed to open output stream")
            
            val bytes = jsonString.toByteArray().size
            BackupResult(true, "Backup saved successfully", bytes)
        } catch (e: Exception) {
            e.printStackTrace()
            BackupResult(false, "Export failed: ${e.message}")
        }
    }

    suspend fun importFromUri(uri: Uri, mergeStrategy: MergeStrategy = MergeStrategy.REPLACE): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext RestoreResult(false, "Failed to open input stream")

            val backupData = json.decodeFromString(BackupData.serializer(), jsonString)
            
            when (mergeStrategy) {
                MergeStrategy.REPLACE -> {
                    repository.updateState { backupData.appState }
                    RestoreResult(
                        success = true,
                        message = "Data restored successfully",
                        clientsImported = backupData.appState.clients.size,
                        bookingsImported = backupData.appState.bookings.size,
                        paymentsImported = backupData.appState.payments.size,
                        expensesImported = backupData.appState.expenses.size,
                        invoicesImported = backupData.appState.invoices.size
                    )
                }
                MergeStrategy.MERGE -> {
                    val currentState = repository.appState.first()
                    
                    // Merge by ID - newer data wins
                    val mergedClients = (currentState.clients + backupData.appState.clients)
                        .distinctBy { it.id }
                    val mergedBookings = (currentState.bookings + backupData.appState.bookings)
                        .distinctBy { it.id }
                    val mergedPayments = (currentState.payments + backupData.appState.payments)
                        .distinctBy { it.id }
                    val mergedExpenses = (currentState.expenses + backupData.appState.expenses)
                        .distinctBy { it.id }
                    val mergedInvoices = (currentState.invoices + backupData.appState.invoices)
                        .distinctBy { it.id }
                    val mergedTemplates = (currentState.templates + backupData.appState.templates)
                        .distinctBy { it.id }
                    
                    repository.updateState { 
                        backupData.appState.copy(
                            clients = mergedClients,
                            bookings = mergedBookings,
                            payments = mergedPayments,
                            expenses = mergedExpenses,
                            invoices = mergedInvoices,
                            templates = mergedTemplates
                        )
                    }
                    
                    RestoreResult(
                        success = true,
                        message = "Data merged successfully",
                        clientsImported = backupData.appState.clients.size,
                        bookingsImported = backupData.appState.bookings.size,
                        paymentsImported = backupData.appState.payments.size,
                        expensesImported = backupData.appState.expenses.size,
                        invoicesImported = backupData.appState.invoices.size
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            RestoreResult(false, "Import failed: ${e.message}")
        }
    }

    fun generateBackupFileName(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US)
        val date = dateFormat.format(Date())
        return "clientflow_backup_$date.json"
    }

    enum class MergeStrategy {
        REPLACE,    // Replace all current data with backup
        MERGE       // Merge backup data with current (deduplicate by ID)
    }

    @kotlinx.serialization.Serializable
    private data class BackupData(
        val version: Int,
        val exportedAt: Long,
        val appState: AppState
    )
}
