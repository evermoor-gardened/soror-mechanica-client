package com.soror.mechanica.pons

import com.soror.mechanica.nexus.SecretVault
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class TokenDelta(val text: String)

class OpenRouterClient(private val vault: SecretVault) {
  private val client = OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build()

  fun streamChat(model: String, userText: String): Flow<TokenDelta> = callbackFlow {
    val key = vault.getOpenRouterKey()
    if (key.isNullOrBlank()) {
      trySend(TokenDelta("[No OpenRouter key set in Dev tab]"))
      close()
      return@callbackFlow
    }

    val payload = JSONObject().apply {
      put("model", model)
      put("stream", true)
      put("messages", JSONArray().apply {
        put(JSONObject().apply {
          put("role", "user")
          put("content", userText)
        })
      })
    }

    val req = Request.Builder()
      .url("https://openrouter.ai/api/v1/chat/completions")
      .addHeader("Authorization", "Bearer $key")
      .addHeader("Content-Type", "application/json")
      .post(payload.toString().toRequestBody("application/json".toMediaType()))
      .build()

    val factory = EventSources.createFactory(client)
    val es = factory.newEventSource(req, object : EventSourceListener() {
      override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
        if (data == "[DONE]") { close(); return }
        try {
          val json = JSONObject(data)
          val choices = json.optJSONArray("choices") ?: return
          val delta = choices.optJSONObject(0)?.optJSONObject("delta") ?: return
          val content = delta.optString("content", "")
          if (content.isNotEmpty()) trySend(TokenDelta(content))
        } catch (_: Throwable) { }
      }
      override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
        trySend(TokenDelta("\n[Stream error] " + (t?.message ?: "unknown")))
        close()
      }
    })

    awaitClose { es.cancel() }
  }
}
