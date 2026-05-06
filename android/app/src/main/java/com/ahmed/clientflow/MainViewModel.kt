package com.ahmed.clientflow

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ahmed.clientflow.data.AppRepository
import com.ahmed.clientflow.data.AppLanguage
import com.ahmed.clientflow.data.AppState
import com.ahmed.clientflow.data.AuthState
import com.ahmed.clientflow.data.Booking
import com.ahmed.clientflow.data.Client
import com.ahmed.clientflow.data.ClientNote
import com.ahmed.clientflow.data.ClientStatus
import com.ahmed.clientflow.data.DarkThemeMode
import com.ahmed.clientflow.data.Expense
import com.ahmed.clientflow.data.ExpenseCategory
import com.ahmed.clientflow.data.Invoice
import com.ahmed.clientflow.data.MessageLog
import com.ahmed.clientflow.data.MessageTemplate
import com.ahmed.clientflow.data.Payment
import com.ahmed.clientflow.data.PaymentStatus
import com.ahmed.clientflow.data.RecurrenceType
import com.ahmed.clientflow.data.Service
import com.ahmed.clientflow.data.computePaymentStatus
import com.ahmed.clientflow.data.randomId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UiState(
    val appState: AppState = AppState(),
    val authState: AuthState = AuthState.Setup,
    val pinError: Boolean = false,
    val exportPayload: String? = null
)

class MainViewModel(private val repository: AppRepository) : ViewModel() {
    private val pinError = MutableStateFlow(false)
    private val exportPayload = MutableStateFlow<String?>(null)

    val deviceId = MutableStateFlow("")

    init {
        viewModelScope.launch {
            deviceId.value = repository.getDeviceId()
        }
    }

    val uiState: StateFlow<UiState> = combine(
        repository.appState,
        repository.authState,
        pinError,
        exportPayload
    ) { appState, authState, pinErrorState, export ->
        UiState(
            appState = appState,
            authState = authState,
            pinError = pinErrorState,
            exportPayload = export
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun setupPin(pin: String) = viewModelScope.launch {
        repository.setPin(pin)
        pinError.value = false
    }

    fun unlock(pin: String) = viewModelScope.launch {
        pinError.value = !repository.unlock(pin)
    }

    fun clearPin() = viewModelScope.launch {
        repository.clearPin()
    }

    fun unlockByBiometric() = viewModelScope.launch {
        repository.unlockByBiometric()
    }

    fun lockApp() = viewModelScope.launch {
        repository.lock()
    }

    fun enableBiometric() = viewModelScope.launch {
        repository.updateState { it.copy(biometricEnabled = true) }
    }

    fun disableBiometric() = viewModelScope.launch {
        repository.updateState { it.copy(biometricEnabled = false) }
    }

    fun activatePro(code: String, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        onDone(repository.activatePro(code))
    }

    fun dismissExport() {
        exportPayload.value = null
    }

    fun setLanguage(language: AppLanguage) = viewModelScope.launch {
        repository.updateState { it.copy(language = language) }
    }

    fun exportData() {
        exportPayload.value = uiState.value.appState.toString()
    }

    fun saveClient(
        existingId: String?,
        name: String,
        phone: String,
        serviceType: String,
        notes: String,
        status: ClientStatus
    ) = viewModelScope.launch {
        repository.updateState { state ->
            if (existingId == null) {
                state.copy(
                    clients = state.clients + Client(
                        name = name.trim(),
                        phone = phone.trim(),
                        serviceType = serviceType.trim(),
                        notes = notes.trim(),
                        status = status
                    )
                )
            } else {
                state.copy(
                    clients = state.clients.map {
                        if (it.id == existingId) {
                            it.copy(
                                name = name.trim(),
                                phone = phone.trim(),
                                serviceType = serviceType.trim(),
                                notes = notes.trim(),
                                status = status
                            )
                        } else it
                    }
                )
            }
        }
    }

    fun addClientNote(clientId: String, content: String) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(clientNotes = state.clientNotes + ClientNote(clientId = clientId, content = content.trim()))
        }
    }

    fun deleteClientNote(noteId: String) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(clientNotes = state.clientNotes.filterNot { it.id == noteId })
        }
    }

    fun deleteClient(clientId: String) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(
                clients = state.clients.filterNot { it.id == clientId },
                bookings = state.bookings.filterNot { it.clientId == clientId },
                payments = state.payments.filterNot { it.clientId == clientId },
                invoices = state.invoices.filterNot { it.clientId == clientId }
            )
        }
    }

    fun updateClientStatus(clientId: String, status: ClientStatus) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(
                clients = state.clients.map { if (it.id == clientId) it.copy(status = status) else it }
            )
        }
    }

    fun saveBooking(
        bookingId: String?,
        clientId: String,
        date: String,
        time: String,
        location: String,
        notes: String,
        recurrence: RecurrenceType,
        recurrenceUntil: String
    ) = viewModelScope.launch {
        repository.updateState { state ->
            val booking = Booking(
                id = bookingId ?: Booking(clientId = clientId).id,
                clientId = clientId,
                date = date.trim(),
                time = time.trim(),
                location = location.trim(),
                notes = notes.trim(),
                recurrence = recurrence,
                recurrenceUntil = recurrenceUntil.trim()
            )
            if (bookingId == null) {
                state.copy(bookings = state.bookings + booking)
            } else {
                state.copy(bookings = state.bookings.map { if (it.id == bookingId) booking else it })
            }
        }
    }

    fun deleteBooking(bookingId: String) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(bookings = state.bookings.filterNot { it.id == bookingId })
        }
    }

    fun savePayment(clientId: String, total: Double, paid: Double, dueDate: String) = viewModelScope.launch {
        repository.updateState { state ->
            val status = computePaymentStatus(total, paid)
            val existing = state.payments.find { it.clientId == clientId }
            val next = Payment(
                id = existing?.id ?: Payment(clientId = clientId).id,
                clientId = clientId,
                totalAmount = total,
                paidAmount = paid,
                dueDate = dueDate.trim(),
                status = status
            )
            if (existing == null) {
                state.copy(payments = state.payments + next)
            } else {
                state.copy(payments = state.payments.map { if (it.clientId == clientId) next else it })
            }
        }
    }

    fun generateInvoice(clientId: String, amount: Double, description: String) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(invoices = state.invoices + Invoice(clientId = clientId, amount = amount, description = description))
        }
    }

    fun saveTemplate(existingId: String?, name: String, content: String, emoji: String) = viewModelScope.launch {
        repository.updateState { state ->
            val current = state.templates.find { it.id == existingId }
            val template = MessageTemplate(
                id = existingId ?: MessageTemplate(name = name, content = content, emoji = emoji).id,
                name = name.trim(),
                content = content.trim(),
                emoji = emoji,
                isDefault = current?.isDefault ?: false
            )
            if (existingId == null) {
                state.copy(templates = state.templates + template)
            } else {
                state.copy(templates = state.templates.map { if (it.id == existingId) template else it })
            }
        }
    }

    fun deleteTemplate(templateId: String) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(templates = state.templates.filterNot { it.id == templateId || it.isDefault && it.id == templateId })
        }
    }

    fun logMessage(clientId: String, templateId: String?, content: String, phone: String) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(messageLogs = state.messageLogs + MessageLog(
                clientId = clientId,
                templateId = templateId,
                content = content,
                phone = phone
            ))
        }
    }

    fun deleteMessageLog(logId: String) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(messageLogs = state.messageLogs.filterNot { it.id == logId })
        }
    }

    fun saveExpense(
        existingId: String?,
        clientId: String?,
        category: ExpenseCategory,
        amount: Double,
        description: String,
        date: Long
    ) = viewModelScope.launch {
        repository.updateState { state ->
            val expense = Expense(
                id = existingId ?: Expense().id,
                clientId = clientId,
                category = category,
                amount = amount,
                description = description.trim(),
                date = date
            )
            if (existingId == null) {
                state.copy(expenses = state.expenses + expense)
            } else {
                state.copy(expenses = state.expenses.map { if (it.id == existingId) expense else it })
            }
        }
    }

    fun deleteExpense(expenseId: String) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(expenses = state.expenses.filterNot { it.id == expenseId })
        }
    }

    fun saveService(existingId: String?, name: String, price: Double, emoji: String) = viewModelScope.launch {
        repository.updateState { state ->
            val service = Service(id = existingId ?: randomId(), name = name.trim(), defaultPrice = price, emoji = emoji)
            if (existingId == null) {
                state.copy(services = state.services + service)
            } else {
                state.copy(services = state.services.map { if (it.id == existingId) service else it })
            }
        }
    }

    fun deleteService(serviceId: String) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(services = state.services.filterNot { it.id == serviceId })
        }
    }

    fun canAddClient(currentCount: Int): Boolean {
        val state = uiState.value.appState
        return state.isPro || currentCount < state.freeClientLimit
    }

    fun importClients(imported: List<Client>) = viewModelScope.launch {
        repository.updateState { state ->
            state.copy(clients = state.clients + imported)
        }
    }

    fun updateLastBackupTime(timestamp: Long) = viewModelScope.launch {
        repository.updateState { it.copy(lastBackupTime = timestamp) }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(AppRepository(context)) as T
        }
    }
}
