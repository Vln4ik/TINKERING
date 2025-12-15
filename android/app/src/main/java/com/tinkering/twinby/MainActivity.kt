package com.tinkering.twinby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tinkering.twinby.ui.theme.TwinbyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TwinbyTheme {
                TwinbyApp()
            }
        }
    }
}


