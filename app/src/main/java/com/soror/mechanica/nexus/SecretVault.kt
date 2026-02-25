package com.soror.mechanica.nexus

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SecretVault(context: Context) {
  private val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

  private val prefs = EncryptedSharedPreferences.create(
    context,
    "secret_vault",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
  )

  fun getOpenRouterKey(): String? = prefs.getString("openrouter_key", null)
  fun setOpenRouterKey(key: String) { prefs.edit().putString("openrouter_key", key.trim()).apply() }
}
