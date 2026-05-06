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
    val notes: String = "",
    val recurrence: RecurrenceType = RecurrenceType.None,
    val recurrenceUntil: String = ""
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
data class Expense(
    val id: String = randomId(),
    val clientId: String? = null,
    val category: ExpenseCategory = ExpenseCategory.Other,
    val amount: Double = 0.0,
    val description: String = "",
    val date: Long = System.currentTimeMillis()
)

@Serializable
enum class ExpenseCategory { Travel, Supplies, Software, Marketing, Other }

@Serializable
data class MessageTemplate(
    val id: String = randomId(),
    val name: String,
    val content: String,
    val emoji: String,
    val isDefault: Boolean = false
)

@Serializable
data class ClientNote(
    val id: String = randomId(),
    val clientId: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class MessageLog(
    val id: String = randomId(),
    val clientId: String,
    val templateId: String?,
    val content: String,
    val phone: String,
    val sentAt: Long = System.currentTimeMillis()
)

@Serializable
data class Service(
    val id: String = randomId(),
    val name: String,
    val defaultPrice: Double = 0.0,
    val emoji: String = "⚙️"
)

fun defaultServices(): List<Service> = listOf(
    Service(id = "svc_web", name = "Web Development", defaultPrice = 500.0, emoji = "🌐"),
    Service(id = "svc_design", name = "Graphic Design", defaultPrice = 200.0, emoji = "🎨"),
    Service(id = "svc_photo", name = "Photography", defaultPrice = 300.0, emoji = "📷"),
    Service(id = "svc_consult", name = "Consulting", defaultPrice = 150.0, emoji = "💼"),
    Service(id = "svc_writing", name = "Writing & Translation", defaultPrice = 100.0, emoji = "✍️"),
    Service(id = "svc_video", name = "Video Editing", defaultPrice = 250.0, emoji = "🎬"),
    Service(id = "svc_marketing", name = "Digital Marketing", defaultPrice = 400.0, emoji = "📱"),
    Service(id = "svc_tutoring", name = "Tutoring", defaultPrice = 50.0, emoji = "📚"),
    Service(id = "svc_mobile", name = "Mobile App Development", defaultPrice = 800.0, emoji = "📲"),
    Service(id = "svc_seo", name = "SEO Services", defaultPrice = 350.0, emoji = "🔍")
)

@Serializable
data class AppState(
    val clients: List<Client> = emptyList(),
    val bookings: List<Booking> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val invoices: List<Invoice> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val templates: List<MessageTemplate> = defaultTemplates(),
    val clientNotes: List<ClientNote> = emptyList(),
    val messageLogs: List<MessageLog> = emptyList(),
    val services: List<Service> = defaultServices(),
    val isPro: Boolean = false,
    val biometricEnabled: Boolean = false,
    val freeClientLimit: Int = 1,
    val language: AppLanguage = AppLanguage.English,
    val darkThemeMode: DarkThemeMode = DarkThemeMode.System,
    val currencySymbol: String = "$",
    val currencyCode: String = "USD",
    val lastBackupTime: Long? = null
)

@Serializable
enum class ClientStatus { Lead, Quoted, Booked, Completed, Paid }

@Serializable
enum class PaymentStatus { Unpaid, Partial, Paid }

@Serializable
enum class AppLanguage { English, French, Arabic }

@Serializable
enum class RecurrenceType { None, Daily, Weekly, Monthly }

@Serializable
enum class DarkThemeMode { System, Light, Dark }

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
