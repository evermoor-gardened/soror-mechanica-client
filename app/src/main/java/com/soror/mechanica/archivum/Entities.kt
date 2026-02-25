package com.soror.mechanica.archivum

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RoomLifecycle { ACTIVE, BACKGROUND, ABSENT }
enum class EngineTarget { WEB, API, HYBRID }
enum class TaskStatus { OPEN, RUNNING, DONE, ARCHIVED }

@Entity(tableName = "rooms")
data class RoomEntity(
  @PrimaryKey val id: String,
  val title: String,
  val lifecycle: RoomLifecycle,
  val activeTemplateId: String? = null,
  val engineTarget: EngineTarget = EngineTarget.WEB,
  val createdAt: Long,
  val updatedAt: Long,
)

@Entity(tableName = "tasks")
data class TaskEntity(
  @PrimaryKey val id: String,
  val roomId: String,
  val title: String,
  val body: String? = null,
  val status: TaskStatus,
  val needsReview: Boolean = false,
  val orderIndex: Int = 0,
  val createdAt: Long,
  val updatedAt: Long,
)

@Entity(tableName = "notes")
data class NoteEntity(
  @PrimaryKey val id: String,
  val roomId: String,
  val content: String,
  val pinned: Boolean = false,
  val orderIndex: Int = 0,
  val createdAt: Long,
  val updatedAt: Long,
)

@Entity(tableName = "templates")
data class TemplateEntity(
  @PrimaryKey val id: String,
  val roomId: String,
  val name: String,
  val content: String,
  val isActive: Boolean = false,
  val createdAt: Long,
  val updatedAt: Long,
)

@Entity(tableName = "messages")
data class MessageEntity(
  @PrimaryKey val id: String,
  val roomId: String,
  val role: String,
  val content: String,
  val createdAt: Long,
)
