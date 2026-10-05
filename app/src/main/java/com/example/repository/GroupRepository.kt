package com.example.repository

import com.example.model.ChatMessage
import com.example.model.Group
import com.example.model.GroupRole
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object GroupRepository {

  private val _groups = MutableStateFlow<List<Group>>(emptyList())
  val groups: StateFlow<List<Group>> = _groups.asStateFlow()

  // Group messages stored by groupId
  private val groupMessagesMap = mutableMapOf<String, MutableStateFlow<List<ChatMessage>>>()

  init {
    seedInitialGroups()
  }

  private fun seedInitialGroups() {
    val myUid = UserRepository.currentUser.value?.uid ?: "current_user_me"
    val defaultGroup = Group(
      id = "group_developers",
      name = "FF Devs & Architects 🚀",
      photoUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150",
      bio = "Official guild for FF CHAT developers, high-scale architecture & WebRTC",
      createdBy = myUid,
      createdAt = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7,
      inviteCode = "FFDEV2026",
      leaderId = myUid,
      elderIds = listOf("user_rohit", "user_priya"), // 2 elders out of 10 max
      memberIds = listOf(myUid, "user_rohit", "user_priya", "user_arav", "user_ananya")
    )

    _groups.value = listOf(defaultGroup)

    val initialMsgs = listOf(
      ChatMessage(
        id = "gm1",
        chatId = "group_developers",
        senderId = myUid,
        senderName = "Ganesh Maurya",
        senderUsername = "@ganesh_m",
        text = "Welcome to the FF CHAT team group! Check out group roles & call features.",
        type = MessageType.TEXT,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 3,
        status = MessageStatus.READ
      ),
      ChatMessage(
        id = "gm2",
        chatId = "group_developers",
        senderId = "user_rohit",
        senderName = "Rohit Sharma",
        senderUsername = "@rohit_ff",
        text = "Leader and Elder permissions are working seamlessly. Ready for group calls!",
        type = MessageType.TEXT,
        timestamp = System.currentTimeMillis() - 1000 * 60 * 60 * 2,
        status = MessageStatus.READ
      )
    )
    groupMessagesMap["group_developers"] = MutableStateFlow(initialMsgs)
  }

  fun getGroupById(groupId: String): Group? {
    return _groups.value.find { it.id == groupId }
  }

  fun getGroupMessagesFlow(groupId: String): StateFlow<List<ChatMessage>> {
    return groupMessagesMap.getOrPut(groupId) {
      MutableStateFlow(emptyList())
    }.asStateFlow()
  }

  fun createGroup(name: String, bio: String, photoUrl: String, creator: User): Group {
    val groupId = "group_${UUID.randomUUID()}"
    val inviteCode = "FF" + UUID.randomUUID().toString().take(6).uppercase()
    val group = Group(
      id = groupId,
      name = name.trim(),
      bio = bio.trim(),
      photoUrl = if (photoUrl.isNotBlank()) photoUrl else "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=150",
      createdBy = creator.uid,
      createdAt = System.currentTimeMillis(),
      inviteCode = inviteCode,
      leaderId = creator.uid, // Creator is automatically Leader
      elderIds = emptyList(),
      memberIds = listOf(creator.uid)
    )

    _groups.value = _groups.value + group
    groupMessagesMap[groupId] = MutableStateFlow(
      listOf(
        ChatMessage(
          id = "sys_${System.currentTimeMillis()}",
          chatId = groupId,
          senderId = "system",
          senderName = "System",
          senderUsername = "@ffchat",
          text = "${creator.name} created the group \"${group.name}\" as Leader 👑",
          type = MessageType.SYSTEM,
          status = MessageStatus.READ
        )
      )
    )
    return group
  }

  fun joinGroupByInviteCode(inviteCode: String, user: User): Pair<Boolean, String> {
    val cleanCode = inviteCode.trim().uppercase()
    val targetGroup = _groups.value.find { it.inviteCode.equals(cleanCode, ignoreCase = true) }
      ?: return Pair(false, "Invalid invite code. Please check and try again.")

    if (targetGroup.memberIds.contains(user.uid)) {
      return Pair(true, "You are already a member of ${targetGroup.name}")
    }

    val updatedGroup = targetGroup.copy(
      memberIds = targetGroup.memberIds + user.uid
    )
    _groups.value = _groups.value.map { if (it.id == updatedGroup.id) updatedGroup else it }

    // System announcement message
    sendGroupMessage(
      groupId = targetGroup.id,
      sender = user,
      text = "${user.name} joined via invite link! 👋",
      type = MessageType.SYSTEM
    )
    return Pair(true, "Successfully joined ${targetGroup.name}")
  }

  /**
   * Leader can promote up to 10 Elders.
   */
  fun promoteToElder(groupId: String, targetUserId: String, currentUserId: String): Pair<Boolean, String> {
    val group = getGroupById(groupId) ?: return Pair(false, "Group not found")
    if (group.leaderId != currentUserId) {
      return Pair(false, "Only the Group Leader can promote Elders")
    }
    if (group.elderIds.size >= 10) {
      return Pair(false, "Maximum 10 Elders allowed per group")
    }
    if (group.elderIds.contains(targetUserId)) {
      return Pair(false, "User is already an Elder")
    }

    val updatedGroup = group.copy(
      elderIds = group.elderIds + targetUserId
    )
    _groups.value = _groups.value.map { if (it.id == groupId) updatedGroup else it }

    val targetUser = UserRepository.getUserById(targetUserId)
    sendGroupMessage(
      groupId = groupId,
      sender = UserRepository.currentUser.value!!,
      text = "${targetUser?.name ?: "Member"} was promoted to Elder 🛡️ (Total: ${updatedGroup.elderIds.size}/10)",
      type = MessageType.SYSTEM
    )
    return Pair(true, "Promoted to Elder")
  }

  fun demoteElder(groupId: String, targetUserId: String, currentUserId: String): Pair<Boolean, String> {
    val group = getGroupById(groupId) ?: return Pair(false, "Group not found")
    if (group.leaderId != currentUserId) {
      return Pair(false, "Only the Group Leader can demote Elders")
    }

    val updatedGroup = group.copy(
      elderIds = group.elderIds.filter { it != targetUserId }
    )
    _groups.value = _groups.value.map { if (it.id == groupId) updatedGroup else it }

    val targetUser = UserRepository.getUserById(targetUserId)
    sendGroupMessage(
      groupId = groupId,
      sender = UserRepository.currentUser.value!!,
      text = "${targetUser?.name ?: "Elder"} was demoted to Member",
      type = MessageType.SYSTEM
    )
    return Pair(true, "Demoted to Member")
  }

  fun removeMember(groupId: String, targetUserId: String, currentUserId: String): Pair<Boolean, String> {
    val group = getGroupById(groupId) ?: return Pair(false, "Group not found")
    val currentUserRole = group.getUserRole(currentUserId)
    val targetUserRole = group.getUserRole(targetUserId)

    // Leader can kick anyone except self; Elder can kick normal members only
    if (currentUserRole == GroupRole.MEMBER) {
      return Pair(false, "Members do not have permission to kick")
    }
    if (currentUserRole == GroupRole.ELDER && targetUserRole != GroupRole.MEMBER) {
      return Pair(false, "Elders can only remove regular members")
    }
    if (targetUserId == group.leaderId) {
      return Pair(false, "Cannot remove the Group Leader")
    }

    val updatedGroup = group.copy(
      memberIds = group.memberIds.filter { it != targetUserId },
      elderIds = group.elderIds.filter { it != targetUserId }
    )
    _groups.value = _groups.value.map { if (it.id == groupId) updatedGroup else it }

    val targetUser = UserRepository.getUserById(targetUserId)
    sendGroupMessage(
      groupId = groupId,
      sender = UserRepository.currentUser.value!!,
      text = "${targetUser?.name ?: "User"} was removed from the group",
      type = MessageType.SYSTEM
    )
    return Pair(true, "Member removed successfully")
  }

  fun updateGroupInfo(groupId: String, name: String, bio: String, photoUrl: String, currentUserId: String): Boolean {
    val group = getGroupById(groupId) ?: return false
    if (group.leaderId != currentUserId) return false

    val updatedGroup = group.copy(
      name = name.trim(),
      bio = bio.trim(),
      photoUrl = if (photoUrl.isNotBlank()) photoUrl else group.photoUrl
    )
    _groups.value = _groups.value.map { if (it.id == groupId) updatedGroup else it }
    return true
  }

  fun setActiveCall(groupId: String, callId: String?) {
    val group = getGroupById(groupId) ?: return
    val updated = group.copy(activeCallId = callId)
    _groups.value = _groups.value.map { if (it.id == groupId) updated else it }
  }

  fun sendGroupMessage(
    groupId: String,
    sender: User,
    text: String,
    type: MessageType = MessageType.TEXT,
    mediaUrl: String? = null,
    fileName: String? = null,
    fileSize: Long? = null,
    mimeType: String? = null,
    replyTo: ChatMessage? = null
  ) {
    val newMsg = ChatMessage(
      id = "gmsg_${UUID.randomUUID()}",
      chatId = groupId,
      senderId = sender.uid,
      senderName = sender.name,
      senderUsername = sender.cleanUsername,
      text = text,
      type = type,
      mediaUrl = mediaUrl,
      fileName = fileName,
      fileSize = fileSize,
      mimeType = mimeType,
      status = MessageStatus.READ,
      replyToId = replyTo?.id,
      replyToSenderName = replyTo?.senderName,
      replyToText = replyTo?.text ?: replyTo?.fileName,
      timestamp = System.currentTimeMillis()
    )

    val flow = groupMessagesMap.getOrPut(groupId) { MutableStateFlow(emptyList()) }
    flow.value = flow.value + newMsg
  }
}
