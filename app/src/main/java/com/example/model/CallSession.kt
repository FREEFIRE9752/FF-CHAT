package com.example.model

enum class CallType {
  VOICE,
  VIDEO
}

enum class CallStatus {
  CONNECTING,
  RINGING,
  CONNECTED,
  ENDED
}

data class CallSession(
  val callId: String = "",
  val callerId: String = "",
  val callerName: String = "",
  val callerAvatar: String = "",
  val targetId: String = "", // Recipient userId or GroupId
  val targetName: String = "",
  val targetAvatar: String = "",
  val isGroup: Boolean = false,
  val callType: CallType = CallType.VOICE,
  val status: CallStatus = CallStatus.CONNECTING,
  val isMuted: Boolean = false,
  val isCameraOn: Boolean = true,
  val isSpeakerOn: Boolean = true,
  val isFrontCamera: Boolean = true,
  val isScreenSharing: Boolean = false,
  val participantUids: List<String> = emptyList(),
  val signalingIceCandidatesCount: Int = 4,
  val durationSeconds: Int = 0,
  val startedAt: Long = System.currentTimeMillis()
)
