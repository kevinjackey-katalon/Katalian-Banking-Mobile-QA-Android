package com.katalon.katalianbanking

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.katalon.katalianbanking.navigation.KatalianNavGraph
import com.katalon.katalianbanking.ui.theme.KatalianBankingTheme
import com.katalon.katalianbanking.ui.theme.Slate950

@OptIn(ExperimentalComposeUiApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KatalianBankingTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Slate950)
                        // Expose every Modifier.testTag as the view's resource-id so Appium /
                        // UiAutomator2 / Katalon can locate Compose elements by resource-id.
                        .semantics { testTagsAsResourceId = true }
                ) {
                    KatalianNavGraph()
                }
            }
        }
    }
}
