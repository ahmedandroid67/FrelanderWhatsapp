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
import com.ahmed.clientflow.data.ClientStatus
import com.ahmed.clientflow.data.Invoice
import com.ahmed.clientflow.data.MessageTemplate
import com.ahmed.clientflow.data.Payment
import com.ahmed.clientflow.data.PaymentStatus
import com.ahmed.clientflow.data.computePaymentStatus
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

    fun lockApp() = viewModelScope.launch {
        repository.lock()
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
        notes: String
    ) = viewModelScope.launch {
        repository.updateState { state ->
            val booking = Booking(
                id = bookingId ?: Booking(clientId = clientId).id,
                clientId = clientId,
                date = date.trim(),
                time = time.trim(),
                location = location.trim(),
                notes = notes.trim()
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

    fun canAddClient(currentCount: Int): Boolean {
        val state = uiState.value.appState
        return state.isPro || currentCount < state.freeClientLimit
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(AppRepository(context)) as T
        }
    }
}
