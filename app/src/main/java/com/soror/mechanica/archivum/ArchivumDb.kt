package com.soror.mechanica.archivum

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [RoomEntity::class, TaskEntity::class, NoteEntity::class, TemplateEntity::class, MessageEntity::class],
  version = 1,
  exportSchema = false
)
abstract class ArchivumDb : RoomDatabase() {
  abstract fun dao(): ArchivumDao

  companion object {
    fun build(context: Context): ArchivumDb =
      Room.databaseBuilder(context, ArchivumDb::class.java, "archivum.db")
        .fallbackToDestructiveMigration()
        .build()
  }
}
