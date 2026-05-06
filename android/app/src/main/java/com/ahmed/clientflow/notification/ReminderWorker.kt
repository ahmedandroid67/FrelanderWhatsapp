package com.ahmed.clientflow.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ahmed.clientflow.data.AppRepository
import com.ahmed.clientflow.data.PaymentStatus
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = AppRepository(context)

    override suspend fun doWork(): Result {
        try {
            val state = repository.appState.first()
            val currentTime = System.currentTimeMillis()
            val calendar = Calendar.getInstance()

            // Check bookings within 24 hours
            state.bookings.forEach { booking ->
                if (booking.date.isNotBlank() && booking.time.isNotBlank()) {
                    try {
                        val bookingDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                            .parse("${booking.date} ${booking.time}")
                        
                        if (bookingDate != null) {
                            val timeDiff = bookingDate.time - currentTime
                            // Notify if booking is within 24 hours and not past
                            if (timeDiff in 0..TimeUnit.HOURS.toMillis(24)) {
                                val client = state.clients.find { it.id == booking.clientId }
                                if (client != null) {
                                    NotificationHelper.showBookingReminder(
                                        applicationContext,
                                        client.name,
                                        booking.date,
                                        booking.time
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Skip invalid date formats
                    }
                }
            }

            // Check overdue payments
            state.payments.forEach { payment ->
                if (payment.status != PaymentStatus.Paid && payment.dueDate.isNotBlank()) {
                    try {
                        val dueDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(payment.dueDate)
                        if (dueDate != null && dueDate.time < currentTime) {
                            // Payment is overdue
                            val client = state.clients.find { it.id == payment.clientId }
                            if (client != null) {
                                val balance = payment.totalAmount - payment.paidAmount
                                if (balance > 0) {
                                    NotificationHelper.showPaymentOverdue(
                                        applicationContext,
                                        client.name,
                                        balance
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Skip invalid date formats
                    }
                }
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }
}
