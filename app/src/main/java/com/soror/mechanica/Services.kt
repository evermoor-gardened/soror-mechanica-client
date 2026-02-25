package com.soror.mechanica

import android.content.Context
import com.soror.mechanica.archivum.Archivum
import com.soror.mechanica.archivum.ArchivumDb
import com.soror.mechanica.nexus.SecretVault
import com.soror.mechanica.pons.PonsMux
import com.soror.mechanica.status.StatusStore

data class Services(
  val archivum: Archivum,
  val status: StatusStore,
  val vault: SecretVault,
  val pons: PonsMux,
) {
  companion object {
    fun create(context: Context): Services {
      val db = ArchivumDb.build(context)
      val archivum = Archivum(db)
      val status = StatusStore(archivum)
      val vault = SecretVault(context)
      val pons = PonsMux(vault)
      return Services(archivum, status, vault, pons)
    }
  }
}
