package com.example.model

enum class MessageType {
  TEXT,
  IMAGE,
  VIDEO,
  FILE,
  SYSTEM
}

enum class MessageStatus {
  SENDING,
  SENT,
  DELIVERED,
  READ
}

data class ChatMessage(
  val id: String = "",
  val chatId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val senderUsername: String = "",
  val text: String = "",
  val type: MessageType = MessageType.TEXT,
  val mediaUrl: String? = null,
  val fileName: String? = null,
  val fileSize: Long? = null, // In bytes, up to 1 GB (1,073,741,824 bytes)
  val mimeType: String? = null,
  val uploadProgress: Float? = null, // null when not uploading, 0.0f..1.0f when in progress
  val status: MessageStatus = MessageStatus.SENT,
  val replyToId: String? = null,
  val replyToSenderName: String? = null,
  val replyToText: String? = null,
  val timestamp: Long = System.currentTimeMillis()
) {
  val formattedFileSize: String
    get() {
      val bytes = fileSize ?: return ""
      val kb = bytes / 1024.0
      val mb = kb / 1024.0
      val gb = mb / 1024.0
      return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.0f KB", kb)
        else -> "$bytes B"
      }
    }
}
