package com.example.repository

import com.example.model.ChatMessage
import com.example.model.ChatSummary
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

object ChatRepository {
  private val scope = CoroutineScope(Dispatchers.Default + Job())

  // Store messages by chatId (e.g. userId for direct chat or groupId for groups)
  private val messagesMap = mutableMapOf<String, MutableStateFlow<List<ChatMessage>>>()

  // Track typing status by chatId -> Set of typing userIds
  private val typingMap = mutableMapOf<String, MutableStateFlow<Set<String>>>()

  // Conversations list for the Chat Screen
  private val _conversations = MutableStateFlow<List<ChatSummary>>(emptyList())
  val conversations: StateFlow<List<ChatSummary>> = _conversations.asStateFlow()

  init {
    seedInitialChats()
  }

  private fun seedInitialChats() {
    val rohit = UserRepository.getUserById("user_rohit")!!
    val priya = UserRepository.getUserById("user_priya")!!
    val ananya = UserRepository.getUserById("user_ananya")!!

    val rohitMessages = listOf(
      ChatMessage(
        id = "m1",
        chatId = "user_rohit",
        senderId = "user_rohit",
        senderName = rohit.name,
        senderUsername = rohit.cleanUsername,
        text = "Hey! Have you tried the 1 GB media transfer on FF CHAT?",
        type = MessageType.TEXT,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 30,
        status = MessageStatus.READ
      ),
      ChatMessage(
        id = "m2",
        chatId = "user_rohit",
        senderId = "current_user_me",
        senderName = "Ganesh",
        senderUsername = "@ganesh_m",
        text = "Yes, chunked resumable upload handles massive 4K videos smoothly!",
        type = MessageType.TEXT,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 25,
        status = MessageStatus.READ
      ),
      ChatMessage(
        id = "m3",
        chatId = "user_rohit",
        senderId = "user_rohit",
        senderName = rohit.name,
        senderUsername = rohit.cleanUsername,
        text = "Here is the release APK build for testing:",
        type = MessageType.FILE,
        fileName = "ffchat-v2.0-release.apk",
        fileSize = 48500000L, // ~48.5 MB
        mimeType = "application/vnd.android.package-archive",
        timestamp = System.currentTimeMillis() - 1000 * 60 * 10,
        status = MessageStatus.READ
      )
    )
    messagesMap["user_rohit"] = MutableStateFlow(rohitMessages)

    val priyaMessages = listOf(
      ChatMessage(
        id = "p1",
        chatId = "user_priya",
        senderId = "user_priya",
        senderName = priya.name,
        senderUsername = priya.cleanUsername,
        text = "Loved the new group roles feature! Leader and Elder permissions look solid.",
        type = MessageType.TEXT,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
        status = MessageStatus.READ
      )
    )
    messagesMap["user_priya"] = MutableStateFlow(priyaMessages)

    val ananyaMessages = listOf(
      ChatMessage(
        id = "a1",
        chatId = "user_ananya",
        senderId = "user_ananya",
        senderName = ananya.name,
        senderUsername = ananya.cleanUsername,
        text = "Can we test the WebRTC screen sharing on Android?",
        type = MessageType.TEXT,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 5,
        status = MessageStatus.DELIVERED
      )
    )
    messagesMap["user_ananya"] = MutableStateFlow(ananyaMessages)

    refreshConversations()
  }

  fun getMessagesFlow(chatId: String): StateFlow<List<ChatMessage>> {
    return messagesMap.getOrPut(chatId) {
      MutableStateFlow(emptyList())
    }.asStateFlow()
  }

  fun getTypingFlow(chatId: String): StateFlow<Set<String>> {
    return typingMap.getOrPut(chatId) {
      MutableStateFlow(emptySet())
    }.asStateFlow()
  }

  fun setTyping(chatId: String, userId: String, isTyping: Boolean) {
    val flow = typingMap.getOrPut(chatId) { MutableStateFlow(emptySet()) }
    val current = flow.value.toMutableSet()
    if (isTyping) {
      current.add(userId)
    } else {
      current.remove(userId)
    }
    flow.value = current
  }

  fun sendMessage(
    chatId: String,
    sender: User,
    text: String,
    type: MessageType = MessageType.TEXT,
    mediaUrl: String? = null,
    fileName: String? = null,
    fileSize: Long? = null,
    mimeType: String? = null,
    replyTo: ChatMessage? = null
  ) {
    val msgId = "msg_${UUID.randomUUID()}"
    val newMsg = ChatMessage(
      id = msgId,
      chatId = chatId,
      senderId = sender.uid,
      senderName = sender.name,
      senderUsername = sender.cleanUsername,
      text = text,
      type = type,
      mediaUrl = mediaUrl,
      fileName = fileName,
      fileSize = fileSize,
      mimeType = mimeType,
      status = MessageStatus.SENDING,
      replyToId = replyTo?.id,
      replyToSenderName = replyTo?.senderName,
      replyToText = replyTo?.text ?: replyTo?.fileName,
      timestamp = System.currentTimeMillis()
    )

    val flow = messagesMap.getOrPut(chatId) { MutableStateFlow(emptyList()) }
    flow.value = flow.value + newMsg
    refreshConversations()

    // Simulate network delivery & read receipts
    scope.launch {
      delay(400)
      updateMessageStatus(chatId, msgId, MessageStatus.SENT)
      delay(800)
      updateMessageStatus(chatId, msgId, MessageStatus.DELIVERED)
      delay(1200)
      updateMessageStatus(chatId, msgId, MessageStatus.READ)
    }
  }

  fun updateMessageUploadProgress(chatId: String, messageId: String, progress: Float?, downloadUrl: String? = null) {
    val flow = messagesMap[chatId] ?: return
    flow.value = flow.value.map { msg ->
      if (msg.id == messageId) {
        msg.copy(
          uploadProgress = progress,
          mediaUrl = downloadUrl ?: msg.mediaUrl,
          status = if (progress == null || progress >= 1.0f) MessageStatus.SENT else MessageStatus.SENDING
        )
      } else {
        msg
      }
    }
  }

  private fun updateMessageStatus(chatId: String, messageId: String, status: MessageStatus) {
    val flow = messagesMap[chatId] ?: return
    flow.value = flow.value.map {
      if (it.id == messageId) it.copy(status = status) else it
    }
    refreshConversations()
  }

  fun refreshConversations() {
    val list = mutableListOf<ChatSummary>()
    messagesMap.forEach { (chatId, flow) ->
      val msgs = flow.value
      val lastMsg = msgs.lastOrNull()
      val targetUser = UserRepository.getUserById(chatId)
      if (targetUser != null && lastMsg != null) {
        list.add(
          ChatSummary(
            chatId = chatId,
            title = targetUser.name,
            subtitle = when (lastMsg.type) {
              MessageType.TEXT -> lastMsg.text
              MessageType.IMAGE -> "📷 Photo"
              MessageType.VIDEO -> "🎥 Video"
              MessageType.FILE -> "📎 File: ${lastMsg.fileName}"
              MessageType.SYSTEM -> lastMsg.text
            },
            avatarUrl = targetUser.photoUrl,
            isGroup = false,
            unreadCount = 0,
            lastMessageTime = lastMsg.timestamp,
            isOnline = targetUser.isOnline,
            targetUser = targetUser,
            lastMessageStatus = lastMsg.status
          )
        )
      }
    }
    _conversations.value = list.sortedByDescending { it.lastMessageTime }
  }

  fun clearChat(chatId: String) {
    messagesMap[chatId]?.value = emptyList()
    refreshConversations()
  }
}
