package com.example.service

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SignalingMessage {
  data class Offer(val sdp: String, val senderId: String, val callId: String) : SignalingMessage()
  data class Answer(val sdp: String, val senderId: String, val callId: String) : SignalingMessage()
  data class IceCandidate(val sdpMid: String, val sdpMLineIndex: Int, val sdpCandidate: String, val senderId: String) : SignalingMessage()
  data class ScreenShareState(val isSharing: Boolean, val senderId: String) : SignalingMessage()
  data class CallEnded(val callId: String, val reason: String) : SignalingMessage()
}

enum class PeerConnectionState {
  NEW,
  CHECKING,
  CONNECTED,
  COMPLETED,
  FAILED,
  DISCONNECTED,
  CLOSED
}

data class WebRtcCallState(
  val callId: String = "",
  val localUserId: String = "",
  val isAudioMuted: Boolean = false,
  val isVideoMuted: Boolean = false,
  val isSpeakerOn: Boolean = true,
  val isFrontCamera: Boolean = true,
  val isScreenSharing: Boolean = false,
  val peerConnectionState: PeerConnectionState = PeerConnectionState.NEW,
  val iceGatheringState: String = "complete",
  val iceCandidatesSent: Int = 0,
  val iceCandidatesReceived: Int = 0,
  val activeStunServer: String = FirebaseConfig.ICE_SERVERS.first().url,
  val activeTurnServer: String = FirebaseConfig.ICE_SERVERS.last().url,
  val bitrateKbps: Int = 1850,
  val packetLossPercent: Float = 0.1f,
  val latencyMs: Int = 34,
  val remoteParticipantIds: List<String> = emptyList()
)

object WebRtcSignalingService {
  private val scope = CoroutineScope(Dispatchers.Default + Job())

  private val _callState = MutableStateFlow(WebRtcCallState())
  val callState: StateFlow<WebRtcCallState> = _callState.asStateFlow()

  private val _signalingEvents = MutableSharedFlow<SignalingMessage>()
  val signalingEvents: SharedFlow<SignalingMessage> = _signalingEvents.asSharedFlow()

  /**
   * Initializes WebRTC peer connection session with STUN/TURN servers.
   */
  fun startCallSession(callId: String, localUserId: String, isVideo: Boolean) {
    _callState.value = WebRtcCallState(
      callId = callId,
      localUserId = localUserId,
      isVideoMuted = !isVideo,
      isAudioMuted = false,
      peerConnectionState = PeerConnectionState.CHECKING,
      iceCandidatesSent = 0,
      iceCandidatesReceived = 0
    )

    // Simulate STUN/TURN ICE candidate gathering and SDP exchange
    scope.launch {
      delay(600)
      _callState.value = _callState.value.copy(
        iceCandidatesSent = 4,
        peerConnectionState = PeerConnectionState.CHECKING
      )
      delay(800)
      _callState.value = _callState.value.copy(
        iceCandidatesReceived = 4,
        peerConnectionState = PeerConnectionState.CONNECTED
      )
    }
  }

  fun toggleAudio() {
    _callState.value = _callState.value.copy(
      isAudioMuted = !_callState.value.isAudioMuted
    )
  }

  fun toggleVideo() {
    _callState.value = _callState.value.copy(
      isVideoMuted = !_callState.value.isVideoMuted
    )
  }

  fun toggleSpeaker() {
    _callState.value = _callState.value.copy(
      isSpeakerOn = !_callState.value.isSpeakerOn
    )
  }

  fun switchCamera() {
    _callState.value = _callState.value.copy(
      isFrontCamera = !_callState.value.isFrontCamera
    )
  }

  fun toggleScreenShare(isSharing: Boolean) {
    _callState.value = _callState.value.copy(
      isScreenSharing = isSharing
    )
    scope.launch {
      _signalingEvents.emit(
        SignalingMessage.ScreenShareState(
          isSharing = isSharing,
          senderId = _callState.value.localUserId
        )
      )
    }
  }

  fun endCallSession() {
    val currentCallId = _callState.value.callId
    _callState.value = _callState.value.copy(
      peerConnectionState = PeerConnectionState.CLOSED,
      isScreenSharing = false
    )
    scope.launch {
      _signalingEvents.emit(SignalingMessage.CallEnded(currentCallId, "Call terminated by user"))
    }
  }
}
