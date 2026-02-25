package com.soror.mechanica.archivum

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class Archivum(private val db: ArchivumDb) {
  private val dao = db.dao()

  fun roomsFlow(): Flow<List<RoomEntity>> = dao.roomsFlow()
  fun tasksFlow(roomId: String): Flow<List<TaskEntity>> = dao.tasksFlow(roomId)
  fun notesFlow(roomId: String): Flow<List<NoteEntity>> = dao.notesFlow(roomId)
  fun templatesFlow(roomId: String): Flow<List<TemplateEntity>> = dao.templatesFlow(roomId)
  fun messagesFlow(roomId: String): Flow<List<MessageEntity>> = dao.messagesFlow(roomId)

  suspend fun createRoom(title: String): RoomEntity {
    val now = System.currentTimeMillis()
    val room = RoomEntity(
      id = UUID.randomUUID().toString(),
      title = title,
      lifecycle = RoomLifecycle.ACTIVE,
      createdAt = now,
      updatedAt = now
    )
    dao.upsertRoom(room)
    val template = TemplateEntity(
      id = UUID.randomUUID().toString(),
      roomId = room.id,
      name = "Default",
      content = "",
      isActive = true,
      createdAt = now,
      updatedAt = now
    )
    dao.upsertTemplate(template)
    return room
  }

  suspend fun upsertRoom(room: RoomEntity) = dao.upsertRoom(room.copy(updatedAt = System.currentTimeMillis()))
  suspend fun upsertTask(task: TaskEntity) = dao.upsertTask(task.copy(updatedAt = System.currentTimeMillis()))
  suspend fun upsertNote(note: NoteEntity) = dao.upsertNote(note.copy(updatedAt = System.currentTimeMillis()))
  suspend fun upsertTemplate(t: TemplateEntity) = dao.upsertTemplate(t.copy(updatedAt = System.currentTimeMillis()))
  suspend fun setActiveTemplate(roomId: String, templateId: String) = dao.setActiveTemplate(roomId, templateId)
  suspend fun getActiveTemplate(roomId: String) = dao.getActiveTemplate(roomId)

  suspend fun insertMessage(roomId: String, role: String, content: String) {
    dao.insertMessage(
      MessageEntity(
        id = UUID.randomUUID().toString(),
        roomId = roomId,
        role = role,
        content = content,
        createdAt = System.currentTimeMillis()
      )
    )
  }
}
