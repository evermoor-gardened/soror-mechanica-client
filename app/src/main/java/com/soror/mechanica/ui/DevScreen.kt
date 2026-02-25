package com.soror.mechanica.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.soror.mechanica.Services
import com.soror.mechanica.archivum.EngineTarget
import kotlinx.coroutines.launch

@Composable
fun DevScreen(services: Services) {
  val scope = rememberCoroutineScope()
  val rooms by services.archivum.roomsFlow().collectAsState(initial = emptyList())
  val active = rooms.firstOrNull { it.lifecycle.name == "ACTIVE" } ?: rooms.firstOrNull()
  var key by remember { mutableStateOf(services.vault.getOpenRouterKey() ?: "") }

  Column(Modifier.fillMaxSize().padding(16.dp)) {
    Text("Dev", style = MaterialTheme.typography.headlineSmall)
    Spacer(Modifier.height(12.dp))

    Text("OpenRouter API Key")
    OutlinedTextField(value = key, onValueChange = { key = it }, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(8.dp))
    Button(onClick = { services.vault.setOpenRouterKey(key) }) { Text("Save") }

    Spacer(Modifier.height(20.dp))
    Text("Active Room Engine Target")
    if (active == null) {
      Text("No rooms yet.")
    } else {
      var sel by remember(active.id) { mutableStateOf(active.engineTarget) }
      Row {
        FilterChip(selected = sel == EngineTarget.WEB, onClick = {
          sel = EngineTarget.WEB; scope.launch { services.archivum.upsertRoom(active.copy(engineTarget = sel)) }
        }, label = { Text("WEB") })
        Spacer(Modifier.width(8.dp))
        FilterChip(selected = sel == EngineTarget.API, onClick = {
          sel = EngineTarget.API; scope.launch { services.archivum.upsertRoom(active.copy(engineTarget = sel)) }
        }, label = { Text("API") })
        Spacer(Modifier.width(8.dp))
        FilterChip(selected = sel == EngineTarget.HYBRID, onClick = {
          sel = EngineTarget.HYBRID; scope.launch { services.archivum.upsertRoom(active.copy(engineTarget = sel)) }
        }, label = { Text("HYBRID") })
      }
    }
  }
}
