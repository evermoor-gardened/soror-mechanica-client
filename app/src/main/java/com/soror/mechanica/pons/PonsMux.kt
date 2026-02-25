package com.soror.mechanica.pons

import com.soror.mechanica.nexus.SecretVault

class PonsMux(vault: SecretVault) {
  val openRouter = OpenRouterClient(vault)
}
