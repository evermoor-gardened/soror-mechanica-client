package com.soror.mechanica

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.soror.mechanica.ui.DevScreen
import com.soror.mechanica.ui.DiscordPlaceholderScreen
import com.soror.mechanica.ui.RoomsScreen

@Composable
fun AppRoot(app: SororApp) {
  val nav = rememberNavController()
  var tab by remember { mutableStateOf("rooms") }

  Scaffold(
    bottomBar = {
      NavigationBar {
        NavigationBarItem(
          selected = tab == "rooms",
          onClick = { tab = "rooms"; nav.navigate("rooms") },
          icon = { Icon(Icons.Default.Chat, contentDescription = "Rooms") },
          label = { Text("Rooms") }
        )
        NavigationBarItem(
          selected = tab == "discord",
          onClick = { tab = "discord"; nav.navigate("discord") },
          icon = { Icon(Icons.Default.Build, contentDescription = "Discord") },
          label = { Text("Discord") }
        )
        NavigationBarItem(
          selected = tab == "dev",
          onClick = { tab = "dev"; nav.navigate("dev") },
          icon = { Icon(Icons.Default.Tune, contentDescription = "Dev") },
          label = { Text("Dev") }
        )
      }
    }
  ) { padding ->
    NavHost(navController = nav, startDestination = "rooms", modifier = Modifier.padding(padding)) {
      composable("rooms") { RoomsScreen(app.services) }
      composable("discord") { DiscordPlaceholderScreen() }
      composable("dev") { DevScreen(app.services) }
    }
  }
}
