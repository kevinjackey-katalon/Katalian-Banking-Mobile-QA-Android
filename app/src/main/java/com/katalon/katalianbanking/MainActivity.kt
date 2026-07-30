package com.katalon.katalianbanking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.katalon.katalianbanking.navigation.KatalianNavGraph
import com.katalon.katalianbanking.ui.theme.KatalianBankingTheme
import com.katalon.katalianbanking.ui.theme.Slate950

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KatalianBankingTheme {
                Surface(modifier = Modifier.fillMaxSize().background(Slate950)) {
                    KatalianNavGraph()
                }
            }
        }
    }
}
