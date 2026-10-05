package com.example.service

/**
 * Production Firebase, Google Sign-In & WebRTC STUN/TURN Configuration.
 * Secrets & API keys are dynamically loaded from environment/BuildConfig or Google Services.
 */
object FirebaseConfig {
  const val MAX_FILE_SIZE_BYTES: Long = 10L * 1024L * 1024L * 1024L // 10 GB maximum file size
  const val CHUNK_SIZE_BYTES: Int = 1024 * 1024 // 1 MB chunk buffer for streaming (prevents OOM)

  // Default Google Web Client ID placeholder (injected via BuildConfig in production)
  const val DEFAULT_WEB_CLIENT_ID = "YOUR_GOOGLE_WEB_CLIENT_ID.apps.googleusercontent.com"

  // WebRTC STUN and TURN server configuration
  val ICE_SERVERS = listOf(
    IceServerConfig("stun:stun.l.google.com:19302"),
    IceServerConfig("stun:stun1.l.google.com:19302"),
    IceServerConfig(
      url = "turn:turn.ffchat.net:3478?transport=udp",
      username = "ffchat_user",
      credential = "ffchat_turn_secure_token"
    ),
    IceServerConfig(
      url = "turns:turn.ffchat.net:5349?transport=tcp",
      username = "ffchat_user",
      credential = "ffchat_turn_secure_token"
    )
  )
}

data class IceServerConfig(
  val url: String,
  val username: String? = null,
  val credential: String? = null
)
