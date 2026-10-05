package com.example.model

data class ChatSummary(
  val chatId: String,
  val title: String,
  val subtitle: String,
  val avatarUrl: String,
  val isGroup: Boolean,
  val unreadCount: Int = 0,
  val lastMessageTime: Long = System.currentTimeMillis(),
  val isOnline: Boolean = false,
  val targetUser: User? = null,
  val group: Group? = null,
  val isTyping: Boolean = false,
  val lastMessageStatus: MessageStatus = MessageStatus.READ
)
