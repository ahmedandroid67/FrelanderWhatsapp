package com.ahmed.clientflow.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ahmed.clientflow.MainActivity

object NotificationHelper {
    const val CHANNEL_BOOKINGS = "booking_reminders"
    const val CHANNEL_PAYMENTS = "payment_reminders"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val bookingChannel = NotificationChannel(
                CHANNEL_BOOKINGS,
                "Booking Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming bookings"
            }

            val paymentChannel = NotificationChannel(
                CHANNEL_PAYMENTS,
                "Payment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for overdue payments"
            }

            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannels(listOf(bookingChannel, paymentChannel))
        }
    }

    fun showBookingReminder(context: Context, clientName: String, date: String, time: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_BOOKINGS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Upcoming Booking")
            .setContentText("$clientName - $date at $time")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }

    fun showPaymentOverdue(context: Context, clientName: String, amount: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_PAYMENTS)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Payment Overdue")
            .setContentText("$clientName owes $$amount")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
    }
}
