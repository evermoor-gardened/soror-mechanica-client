package com.soror.mechanica.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.soror.mechanica.Services
import com.soror.mechanica.archivum.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun RoomsScreen(services: Services) {
  val scope = rememberCoroutineScope()
  val rooms by services.archivum.roomsFlow().collectAsState(initial = emptyList())
  var activeRoomId by remember { mutableStateOf<String?>(null) }
  var showScratchpad by remember { mutableStateOf(false) }
  var showWeb by remember { mutableStateOf(false) }

  LaunchedEffect(rooms) {
    if (activeRoomId == null) {
      val active = rooms.firstOrNull { it.lifecycle == RoomLifecycle.ACTIVE } ?: rooms.firstOrNull()
      activeRoomId = active?.id
    }
  }
  val activeRoom = rooms.firstOrNull { it.id == activeRoomId }

  Column(Modifier.fillMaxSize()) {
    TopAppBar(
      title = {
        if (activeRoom != null) InlineTitle(activeRoom.title) { newTitle ->
          scope.launch { services.archivum.upsertRoom(activeRoom.copy(title = newTitle)) }
        } else Text("Rooms")
      },
      actions = {
        IconButton(onClick = { showScratchpad = !showScratchpad }) {
          Icon(Icons.Default.Settings, contentDescription = "Scratchpad")
        }
        IconButton(onClick = { showWeb = !showWeb }) {
          Icon(Icons.Default.Web, contentDescription = "Web")
        }
      }
    )

    Row(Modifier.fillMaxSize()) {
      RoomList(
        rooms = rooms,
        activeRoomId = activeRoomId,
        onSelect = { r -> activeRoomId = r.id; scope.launch { services.status.setActiveRoom(r) } },
        onCreate = { scope.launch {
          val created = services.archivum.createRoom("Room ${'$'}{rooms.size + 1}")
          activeRoomId = created.id
          services.status.setActiveRoom(created)
        } },
        onArchive = { r -> scope.launch { services.archivum.upsertRoom(r.copy(lifecycle = RoomLifecycle.ABSENT)) } },
        onRestore = { r -> scope.launch { services.status.setActiveRoom(r.copy(lifecycle = RoomLifecycle.ACTIVE)); activeRoomId = r.id } },
        modifier = Modifier.width(180.dp).fillMaxHeight()
      )

      Box(Modifier.weight(1f).fillMaxHeight()) {
        if (showWeb) WebPane(url = "https://example.com") else TranscriptPane(services, activeRoom)
      }

      if (showScratchpad && activeRoom != null) {
        ScratchpadTray(services, activeRoom.id, Modifier.width(320.dp).fillMaxHeight())
      }
    }
  }
}

@Composable
private fun InlineTitle(text: String, onCommit: (String) -> Unit) {
  var editing by remember { mutableStateOf(false) }
  var value by remember(text) { mutableStateOf(text) }
  if (!editing) {
    TextButton(onClick = { editing = true }) { Text(text) }
  } else {
    Row(verticalAlignment = Alignment.CenterVertically) {
      BasicTextField(value = value, onValueChange = { value = it }, textStyle = TextStyle.Default, singleLine = true)
      Spacer(Modifier.width(8.dp))
      TextButton(onClick = { editing = false; onCommit(value) }) { Text("OK") }
    }
  }
}

@Composable
private fun RoomList(
  rooms: List<RoomEntity>,
  activeRoomId: String?,
  onSelect: (RoomEntity) -> Unit,
  onCreate: () -> Unit,
  onArchive: (RoomEntity) -> Unit,
  onRestore: (RoomEntity) -> Unit,
  modifier: Modifier = Modifier
) {
  val infinite = rememberInfiniteTransition(label = "pulse")
  val pulse by infinite.animateFloat(
    initialValue = 0.6f,
    targetValue = 1.0f,
    animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
    label = "pulseAnim"
  )

  Column(modifier.padding(8.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text("Rooms", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
      IconButton(onClick = onCreate) { Icon(Icons.Default.Add, contentDescription = "Add") }
    }
    Divider()
    LazyColumn {
      items(rooms) { room ->
        val isActive = room.id == activeRoomId && room.lifecycle == RoomLifecycle.ACTIVE
        val bg = when (room.lifecycle) {
          RoomLifecycle.ACTIVE -> MaterialTheme.colorScheme.primary.copy(alpha = if (isActive) pulse else 0.25f)
          RoomLifecycle.BACKGROUND -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f)
          RoomLifecycle.ABSENT -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
        }
        val height = if (room.lifecycle == RoomLifecycle.ABSENT) 34.dp else 44.dp

        Row(
          Modifier.fillMaxWidth().height(height).background(bg).padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(room.title, modifier = Modifier.weight(1f), maxLines = 1)
          if (room.lifecycle != RoomLifecycle.ABSENT) TextButton(onClick = { onArchive(room) }) { Text("—") }
          else TextButton(onClick = { onRestore(room) }) { Text("◻︎") }
        }
        Spacer(Modifier.height(6.dp))
      }
    }
  }
}

@Composable
private fun TranscriptPane(services: Services, room: RoomEntity?) {
  val scope = rememberCoroutineScope()
  var composer by remember { mutableStateOf("") }
  var streamingText by remember { mutableStateOf("") }
  var crtFlash by remember { mutableStateOf(false) }

  if (room == null) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Create/select a room.") }
    return
  }

  val messages by services.archivum.messagesFlow(room.id).collectAsState(initial = emptyList())

  Column(Modifier.fillMaxSize()) {
    LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(12.dp)) {
      items(messages) { m ->
        Text((if (m.role == "user") "You: " else "AI: ") + m.content)
        Spacer(Modifier.height(8.dp))
      }
      if (streamingText.isNotBlank()) item { Text("AI: " + streamingText) }
    }

    if (crtFlash) Box(Modifier.fillMaxWidth().height(4.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)))

    Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
      OutlinedTextField(
        value = composer,
        onValueChange = { composer = it },
        modifier = Modifier.weight(1f),
        placeholder = { Text("Type...") }
      )
      Spacer(Modifier.width(8.dp))
      Button(onClick = { // Project Start
        scope.launch { composer = services.archivum.getActiveTemplate(room.id)?.content ?: "" }
      }) { Text("Project Start") }

      Spacer(Modifier.width(8.dp))
      Button(onClick = {
        val prompt = composer.trim()
        if (prompt.isBlank()) return@Button
        scope.launch { services.archivum.insertMessage(room.id, "user", prompt) }
        composer = ""
        streamingText = ""

        when (room.engineTarget) {
          EngineTarget.API -> {
            scope.launch {
              services.pons.openRouter.streamChat("openai/gpt-4o-mini", prompt).collect { streamingText += it.text }
              if (streamingText.length > 2000) { crtFlash = true; delay(350); crtFlash = false }
              services.archivum.insertMessage(room.id, "assistant", streamingText)
              streamingText = ""
            }
          }
          else -> scope.launch {
            services.archivum.insertMessage(room.id, "assistant", "[WEB engine not automated yet. Toggle WebView and use the site.]")
          }
        }
      }) { Text("Invoke") }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebPane(url: String) {
  AndroidView(
    factory = { ctx ->
      WebView(ctx).apply {
        settings.javaScriptEnabled = true
        webViewClient = WebViewClient()
        loadUrl(url)
      }
    },
    modifier = Modifier.fillMaxSize()
  )
}
