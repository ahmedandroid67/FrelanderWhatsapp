package com.ahmed.clientflow

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.ahmed.clientflow.notification.NotificationHelper
import com.ahmed.clientflow.notification.ReminderWorker
import com.ahmed.clientflow.ui.ClientFlowApp
import com.ahmed.clientflow.ui.theme.ClientFlowTheme
import java.util.concurrent.TimeUnit

class MainActivity : FragmentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize notifications
        NotificationHelper.createChannels(this)
        scheduleReminderWorker()
        
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            ClientFlowTheme(
                darkThemeMode = uiState.appState.darkThemeMode,
                appTheme = uiState.appState.theme
            ) {
                ClientFlowApp(viewModel = viewModel)
            }
        }
    }
    
    private fun scheduleReminderWorker() {
        val remindersRequest = PeriodicWorkRequestBuilder<ReminderWorker>(6, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().build())
            .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            remindersRequest
        )
    }

    override fun onStop() {
        super.onStop()
        viewModel.lockApp()
    }
}
