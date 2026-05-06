package com.ahmed.clientflow.ui.theme

import androidx.compose.ui.graphics.Color

object AppTokens {
    object Semantic {
        val success = green600
        val successContainer = green50
        val successText = green700
        val warning = amber500
        val warningContainer = Color(0xFFFFFBEB)
        val warningText = amber600
        val error = red400
        val errorContainer = red50
        val errorText = red600
        val info = blue500
        val infoContainer = blue50
    }

    object Pipeline {
        val lead = blue500
        val quoted = purple400
        val booked = amber400
        val completed = teal400
        val paidUp = green600
        val leadBg = blue50
        val quotedBg = Color(0xFFF5F3FF)
        val bookedBg = Color(0xFFFFFBEB)
        val completedBg = Color(0xFFF0FDFA)
        val paidUpBg = green50
    }

    object BookingDots {
        val one = green600
        val two = amber500
        val threePlus = red400
    }

    object Calendar {
        val todayBorder = purple400
        val weekdayAccent = purple400
        val hasBooking = blue500
        val selectedBg = blue500
        val surfaceVariant = Color(0x66E2E8F0)
    }

    object Surface {
        val overdueCard = surfacePink
        val gradientStart = Color(0xFF2563EB)
        val gradientEnd = Color(0xFF7C3AED)
    }
}
