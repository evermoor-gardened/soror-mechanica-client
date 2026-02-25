package com.soror.mechanica

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val app = application as SororApp
    setContent {
      MaterialTheme {
        Surface { AppRoot(app) }
      }
    }
  }
}
