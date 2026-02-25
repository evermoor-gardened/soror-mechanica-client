package com.soror.mechanica.archivum

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ArchivumDao {
  @Query("SELECT * FROM rooms ORDER BY updatedAt DESC")
  fun roomsFlow(): Flow<List<RoomEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertRoom(room: RoomEntity)

  @Query("SELECT * FROM tasks WHERE roomId = :roomId AND status != 'ARCHIVED' ORDER BY updatedAt DESC")
  fun tasksFlow(roomId: String): Flow<List<TaskEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertTask(task: TaskEntity)

  @Query("SELECT * FROM notes WHERE roomId = :roomId ORDER BY updatedAt DESC")
  fun notesFlow(roomId: String): Flow<List<NoteEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertNote(note: NoteEntity)

  @Query("SELECT * FROM templates WHERE roomId = :roomId ORDER BY updatedAt DESC")
  fun templatesFlow(roomId: String): Flow<List<TemplateEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertTemplate(t: TemplateEntity)

  @Query("UPDATE templates SET isActive = CASE WHEN id = :templateId THEN 1 ELSE 0 END WHERE roomId = :roomId")
  suspend fun setActiveTemplate(roomId: String, templateId: String)

  @Query("SELECT * FROM templates WHERE roomId = :roomId AND isActive = 1 LIMIT 1")
  suspend fun getActiveTemplate(roomId: String): TemplateEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(msg: MessageEntity)

  @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY createdAt ASC")
  fun messagesFlow(roomId: String): Flow<List<MessageEntity>>
}
