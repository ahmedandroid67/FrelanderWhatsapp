package com.ahmed.clientflow.data

import kotlinx.serialization.Serializable
import java.security.MessageDigest
import java.util.UUID

@Serializable
data class Client(
    val id: String = randomId(),
    val name: String,
    val phone: String,
    val serviceType: String = "",
    val notes: String = "",
    val status: ClientStatus = ClientStatus.Lead,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Booking(
    val id: String = randomId(),
    val clientId: String,
    val date: String = "",
    val time: String = "",
    val location: String = "",
    val notes: String = ""
)

@Serializable
data class Payment(
    val id: String = randomId(),
    val clientId: String,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val dueDate: String = "",
    val status: PaymentStatus = PaymentStatus.Unpaid
)

@Serializable
data class Invoice(
    val id: String = randomId(),
    val clientId: String,
    val amount: Double,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class MessageTemplate(
    val id: String = randomId(),
    val name: String,
    val content: String,
    val emoji: String,
    val isDefault: Boolean = false
)

@Serializable
data class AppState(
    val clients: List<Client> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
    val templates: List<MessageTemplate> = defaultTemplates(),
    val isPro: Boolean = false,
    val freeClientLimit: Int = 1,
    val language: AppLanguage = AppLanguage.English
)

@Serializable
enum class ClientStatus { Lead, Quoted, Booked, Completed, Paid }

@Serializable
enum class PaymentStatus { Unpaid, Partial, Paid }

@Serializable
enum class AppLanguage { English, French, Arabic }

fun computePaymentStatus(total: Double, paid: Double): PaymentStatus = when {
    paid <= 0 -> PaymentStatus.Unpaid
    paid >= total -> PaymentStatus.Paid
    else -> PaymentStatus.Partial
}

fun defaultTemplates(): List<MessageTemplate> = listOf(
    MessageTemplate(
        id = "tpl_payment_reminder",
        name = "Payment Reminder",
        content = "Hi {name}, reminder: payment of {amount} is overdue. Please let me know when you can settle it. Thank you!",
        emoji = "\uD83D\uDCB3",
        isDefault = true
    ),
    MessageTemplate(
        id = "tpl_booking_confirm",
        name = "Booking Confirmation",
        content = "Hi {name}, your booking for {service} is confirmed for {date}. Looking forward to it!",
        emoji = "\uD83D\uDCC5",
        isDefault = true
    ),
    MessageTemplate(
        id = "tpl_followup",
        name = "Follow-up",
        content = "Hi {name}, checking in. Hope everything went well. Reach out anytime.",
        emoji = "\uD83D\uDC4B",
        isDefault = true
    )
)

fun randomId(): String = UUID.randomUUID().toString()

fun hashPin(pin: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
}
