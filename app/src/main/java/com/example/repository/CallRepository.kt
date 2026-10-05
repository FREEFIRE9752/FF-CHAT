package com.example.repository

import com.example.model.CallSession
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.model.GroupRole
import com.example.model.User
import com.example.service.WebRtcSignalingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

object CallRepository {
  private val scope = CoroutineScope(Dispatchers.Default + Job())

  private val _activeCall = MutableStateFlow<CallSession?>(null)
  val activeCall: StateFlow<CallSession?> = _activeCall.asStateFlow()

  private var timerJob: Job? = null

  /**
   * Start 1-on-1 Voice or Video call
   */
  fun startDirectCall(caller: User, targetUser: User, type: CallType) {
    val callId = "call_${UUID.randomUUID()}"
    val session = CallSession(
      callId = callId,
      callerId = caller.uid,
      callerName = caller.name,
      callerAvatar = caller.photoUrl,
      targetId = targetUser.uid,
      targetName = targetUser.name,
      targetAvatar = targetUser.photoUrl,
      isGroup = false,
      callType = type,
      status = CallStatus.RINGING,
      participantUids = listOf(caller.uid, targetUser.uid)
    )
    _activeCall.value = session
    WebRtcSignalingService.startCallSession(callId, caller.uid, isVideo = type == CallType.VIDEO)

    // Simulate recipient answering after ringing
    scope.launch {
      delay(2000)
      if (_activeCall.value?.callId == callId && _activeCall.value?.status == CallStatus.RINGING) {
        _activeCall.value = _activeCall.value?.copy(status = CallStatus.CONNECTED)
        startCallTimer()
      }
    }
  }

  /**
   * Start Group Voice or Video call.
   * Only Leader and Elder can start!
   */
  fun startGroupCall(caller: User, groupId: String, type: CallType): Pair<Boolean, String> {
    val group = GroupRepository.getGroupById(groupId) ?: return Pair(false, "Group not found")
    val role = group.getUserRole(caller.uid)
    if (role != GroupRole.LEADER && role != GroupRole.ELDER) {
      return Pair(false, "Only Group Leader or Elders can start a group call")
    }

    val callId = "grp_call_${UUID.randomUUID()}"
    val session = CallSession(
      callId = callId,
      callerId = caller.uid,
      callerName = caller.name,
      callerAvatar = caller.photoUrl,
      targetId = groupId,
      targetName = group.name,
      targetAvatar = group.photoUrl,
      isGroup = true,
      callType = type,
      status = CallStatus.CONNECTED,
      participantUids = listOf(caller.uid)
    )

    _activeCall.value = session
    GroupRepository.setActiveCall(groupId, callId)
    WebRtcSignalingService.startCallSession(callId, caller.uid, isVideo = type == CallType.VIDEO)
    startCallTimer()

    // Announce active call in group chat
    GroupRepository.sendGroupMessage(
      groupId = groupId,
      sender = caller,
      text = "📞 Started an active ${if (type == CallType.VIDEO) "Video" else "Voice"} Call. Members can join now!",
      type = com.example.model.MessageType.SYSTEM
    )
    return Pair(true, "Group call started")
  }

  /**
   * All authorized group members can join an active group call!
   */
  fun joinGroupCall(user: User, groupId: String): Pair<Boolean, String> {
    val group = GroupRepository.getGroupById(groupId) ?: return Pair(false, "Group not found")
    if (!group.memberIds.contains(user.uid)) {
      return Pair(false, "You are not a member of this group")
    }
    val activeCallId = group.activeCallId ?: return Pair(false, "No active call in this group")

    val current = _activeCall.value
    if (current != null && current.callId == activeCallId) {
      return Pair(true, "Already in call")
    }

    val session = CallSession(
      callId = activeCallId,
      callerId = group.leaderId,
      callerName = group.name,
      callerAvatar = group.photoUrl,
      targetId = groupId,
      targetName = group.name,
      targetAvatar = group.photoUrl,
      isGroup = true,
      callType = CallType.VIDEO, // Default group call video capable
      status = CallStatus.CONNECTED,
      participantUids = listOf(group.leaderId, user.uid)
    )
    _activeCall.value = session
    WebRtcSignalingService.startCallSession(activeCallId, user.uid, isVideo = true)
    startCallTimer()
    return Pair(true, "Joined group call")
  }

  fun toggleMute() {
    WebRtcSignalingService.toggleAudio()
    _activeCall.value = _activeCall.value?.let {
      it.copy(isMuted = !it.isMuted)
    }
  }

  fun toggleCamera() {
    WebRtcSignalingService.toggleVideo()
    _activeCall.value = _activeCall.value?.let {
      it.copy(isCameraOn = !it.isCameraOn)
    }
  }

  fun toggleSpeaker() {
    WebRtcSignalingService.toggleSpeaker()
    _activeCall.value = _activeCall.value?.let {
      it.copy(isSpeakerOn = !it.isSpeakerOn)
    }
  }

  fun switchCamera() {
    WebRtcSignalingService.switchCamera()
    _activeCall.value = _activeCall.value?.let {
      it.copy(isFrontCamera = !it.isFrontCamera)
    }
  }

  fun toggleScreenSharing() {
    val current = _activeCall.value ?: return
    val newSharingState = !current.isScreenSharing
    WebRtcSignalingService.toggleScreenShare(newSharingState)
    _activeCall.value = current.copy(isScreenSharing = newSharingState)
  }

  fun endCall() {
    val current = _activeCall.value
    if (current != null && current.isGroup) {
      // If leader ends, clear active call in group
      GroupRepository.setActiveCall(current.targetId, null)
    }
    timerJob?.cancel()
    timerJob = null
    WebRtcSignalingService.endCallSession()
    _activeCall.value = null
  }

  private fun startCallTimer() {
    timerJob?.cancel()
    timerJob = scope.launch {
      var seconds = 0
      while (true) {
        delay(1000)
        seconds++
        _activeCall.value = _activeCall.value?.copy(durationSeconds = seconds)
      }
    }
  }
}
