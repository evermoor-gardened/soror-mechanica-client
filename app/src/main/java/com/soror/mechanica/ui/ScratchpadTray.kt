package com.soror.mechanica.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.soror.mechanica.Services
import com.soror.mechanica.archivum.*
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun ScratchpadTray(services: Services, roomId: String, modifier: Modifier = Modifier) {
  val scope = rememberCoroutineScope()
  var tab by remember { mutableStateOf(0) }

  val tasks by services.archivum.tasksFlow(roomId).collectAsState(initial = emptyList())
  val notes by services.archivum.notesFlow(roomId).collectAsState(initial = emptyList())
  val templates by services.archivum.templatesFlow(roomId).collectAsState(initial = emptyList())

  Column(modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)).padding(10.dp)) {
    TabRow(selectedTabIndex = tab) {
      Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("TASKS") })
      Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("NOTES") })
      Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("TEMPLATES") })
    }

    Spacer(Modifier.height(10.dp))

    when (tab) {
      0 -> {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Tasks", style = MaterialTheme.typography.titleSmall)
          TextButton(onClick = {
            scope.launch {
              val now = System.currentTimeMillis()
              services.archivum.upsertTask(TaskEntity(
                id = UUID.randomUUID().toString(),
                roomId = roomId,
                title = "Task",
                status = TaskStatus.OPEN,
                createdAt = now,
                updatedAt = now
              ))
            }
          }) { Text("+") }
        }
        LazyColumn {
          items(tasks) { t ->
            val glyph = when (t.status) { TaskStatus.RUNNING -> "■"; else -> "□" }
            val txt = if (t.status == TaskStatus.DONE) "~~${'$'}{t.title}~~" else t.title
            Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
              Text((if (t.needsReview) "▣ " else "") + glyph + " " + txt)
              Row {
                TextButton(onClick = {
                  scope.launch { services.archivum.upsertTask(t.copy(needsReview = !t.needsReview)) }
                }) { Text("R") }
                TextButton(onClick = {
                  scope.launch {
                    val ns = if (t.status == TaskStatus.DONE) TaskStatus.OPEN else TaskStatus.DONE
                    services.archivum.upsertTask(t.copy(status = ns))
                  }
                }) { Text("✓") }
              }
            }
            Divider()
          }
        }
      }
      1 -> {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Notes", style = MaterialTheme.typography.titleSmall)
          TextButton(onClick = {
            scope.launch {
              val now = System.currentTimeMillis()
              services.archivum.upsertNote(NoteEntity(
                id = UUID.randomUUID().toString(),
                roomId = roomId,
                content = "",
                pinned = false,
                createdAt = now,
                updatedAt = now
              ))
            }
          }) { Text("+") }
        }
        LazyColumn {
          items(notes) { n ->
            val highlight = if (n.pinned) {
              Modifier.background(
                Brush.horizontalGradient(
                  listOf(
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f),
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
                    MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                  )
                )
              )
            } else Modifier

            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp).then(highlight).padding(6.dp)) {
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (n.pinned) "PINNED" else "NOTE", style = MaterialTheme.typography.labelSmall)
                TextButton(onClick = { scope.launch { services.archivum.upsertNote(n.copy(pinned = !n.pinned)) } }) {
                  Text(if (n.pinned) "Unpin" else "Pin")
                }
              }
              var v by remember(n.id) { mutableStateOf(n.content) }
              BasicTextField(
                value = v,
                onValueChange = { v = it; scope.launch { services.archivum.upsertNote(n.copy(content = it)) } },
                textStyle = TextStyle.Default,
                modifier = Modifier.fillMaxWidth().heightIn(min = 60.dp)
              )
            }
            Divider()
          }
        }
      }
      2 -> {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
          Text("Templates", style = MaterialTheme.typography.titleSmall)
          TextButton(onClick = {
            scope.launch {
              val now = System.currentTimeMillis()
              val id = UUID.randomUUID().toString()
              services.archivum.upsertTemplate(TemplateEntity(
                id = id, roomId = roomId, name = "Template", content = "", isActive = templates.isEmpty(),
                createdAt = now, updatedAt = now
              ))
              if (templates.isEmpty()) services.archivum.setActiveTemplate(roomId, id)
            }
          }) { Text("+") }
        }
        LazyColumn {
          items(templates) { t ->
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
              Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text((if (t.isActive) "● " else "○ ") + t.name)
                TextButton(onClick = { scope.launch { services.archivum.setActiveTemplate(roomId, t.id) } }) { Text("Use") }
              }
              var v by remember(t.id) { mutableStateOf(t.content) }
              BasicTextField(
                value = v,
                onValueChange = { v = it; scope.launch { services.archivum.upsertTemplate(t.copy(content = it)) } },
                textStyle = TextStyle.Default,
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp)
                  .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.25f)).padding(6.dp)
              )
            }
            Divider()
          }
        }
      }
    }
  }
}
