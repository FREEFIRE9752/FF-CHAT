package com.example.model

data class User(
  val uid: String = "",
  val name: String = "",
  val username: String = "", // e.g. "@alex_hunter"
  val photoUrl: String = "",
  val bio: String = "Hey there! I am using FF CHAT.",
  val isOnline: Boolean = true,
  val lastSeen: Long = System.currentTimeMillis(),
  val createdAt: Long = System.currentTimeMillis(),
  val blockedUserIds: List<String> = emptyList()
) {
  val cleanUsername: String
    get() = if (username.startsWith("@")) username.lowercase() else "@${username.lowercase()}"
}
