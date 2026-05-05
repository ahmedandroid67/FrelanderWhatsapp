package com.ahmed.clientflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ahmed.clientflow.ui.ClientFlowApp
import com.ahmed.clientflow.ui.theme.ClientFlowTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        MainViewModel.Factory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClientFlowTheme {
                ClientFlowApp(viewModel = viewModel)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.lockApp()
    }
}
