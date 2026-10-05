package com.example.model

enum class GroupRole {
  LEADER,
  ELDER,
  MEMBER
}

data class Group(
  val id: String = "",
  val name: String = "",
  val photoUrl: String = "",
  val bio: String = "",
  val createdBy: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val inviteCode: String = "",
  val leaderId: String = "",
  val elderIds: List<String> = emptyList(), // Maximum 10 elders enforced!
  val memberIds: List<String> = emptyList(),
  val activeCallId: String? = null
) {
  fun getUserRole(userId: String): GroupRole {
    return when {
      userId == leaderId -> GroupRole.LEADER
      elderIds.contains(userId) -> GroupRole.ELDER
      memberIds.contains(userId) -> GroupRole.MEMBER
      else -> GroupRole.MEMBER
    }
  }

  fun canStartCall(userId: String): Boolean {
    val role = getUserRole(userId)
    return role == GroupRole.LEADER || role == GroupRole.ELDER
  }

  fun canManageMembers(userId: String): Boolean {
    val role = getUserRole(userId)
    return role == GroupRole.LEADER || role == GroupRole.ELDER
  }

  fun canPromoteElder(userId: String): Boolean {
    return userId == leaderId
  }

  fun canAddMoreElders(): Boolean {
    return elderIds.size < 10
  }
}
