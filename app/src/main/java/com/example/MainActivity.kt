package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.readvault.ReadVaultApp
import com.example.readvault.ui.theme.ReadVaultTheme
import com.example.readvault.ui.viewmodel.ReadVaultViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ReadVaultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            ReadVaultTheme {
                ReadVaultApp(viewModel = viewModel)
            }
        }
    }
}
