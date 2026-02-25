package com.soror.mechanica.status

import com.soror.mechanica.archivum.Archivum
import com.soror.mechanica.archivum.RoomEntity
import com.soror.mechanica.archivum.RoomLifecycle
import kotlinx.coroutines.flow.first

class StatusStore(private val archivum: Archivum) {
  suspend fun setActiveRoom(room: RoomEntity) {
    val rooms = archivum.roomsFlow().first()
    rooms.filter { it.lifecycle == RoomLifecycle.ACTIVE && it.id != room.id }
      .forEach { archivum.upsertRoom(it.copy(lifecycle = RoomLifecycle.BACKGROUND)) }
    archivum.upsertRoom(room.copy(lifecycle = RoomLifecycle.ACTIVE))
  }
}
