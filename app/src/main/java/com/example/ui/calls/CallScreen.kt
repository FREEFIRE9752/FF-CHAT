package com.example.ui.calls

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.ScreenShare
import androidx.compose.material.icons.filled.StopScreenShare
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CallSession
import com.example.model.CallStatus
import com.example.model.CallType
import com.example.repository.CallRepository
import com.example.service.WebRtcSignalingService
import com.example.ui.components.FFChatAvatar
import com.example.ui.theme.FFChatCyan
import com.example.ui.theme.FFChatGreen
import com.example.ui.theme.FFChatRed

@Composable
fun CallScreen(
  session: CallSession,
  onEndCall: () -> Unit
) {
  val webrtcState by WebRtcSignalingService.callState.collectAsState()

  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  val durationFormatted = remember(session.durationSeconds) {
    val m = session.durationSeconds / 60
    val s = session.durationSeconds % 60
    String.format("%02d:%02d", m, s)
  }

  Surface(
    modifier = Modifier
      .fillMaxSize()
      .testTag("webrtc_call_screen"),
    color = Color(0xFF070B14)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Main View: Video Stream or Audio Avatar
      if (session.callType == CallType.VIDEO && !webrtcState.isVideoMuted) {
        // Remote Video Canvas
        Box(modifier = Modifier.fillMaxSize()) {
          AsyncImage(
            model = if (session.isScreenSharing) {
              "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=1080" // Screen share data stream preview
            } else {
              session.targetAvatar.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=1080" }
            },
            contentDescription = "Remote Stream",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )

          // Dark overlay for legibility
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color.Black.copy(alpha = 0.6f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.8f)
                  )
                )
              )
          )

          // Screen share badge
          if (session.isScreenSharing) {
            Card(
              colors = CardDefaults.cardColors(containerColor = FFChatCyan.copy(alpha = 0.25f)),
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.ScreenShare, contentDescription = null, tint = FFChatCyan, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Screen Sharing Active (Android MediaProjection / WebRTC)", color = FFChatCyan, style = MaterialTheme.typography.labelSmall)
              }
            }
          }

          // Local Picture-in-Picture (PiP) video preview
          Card(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(top = 70.dp, end = 16.dp)
              .size(width = 110.dp, height = 150.dp)
              .border(2.dp, FFChatCyan, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
          ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
              AsyncImage(
                model = session.callerAvatar.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400" },
                contentDescription = "Local Video Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
              Text(
                text = if (webrtcState.isFrontCamera) "Front" else "Back",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                modifier = Modifier
                  .background(Color.Black.copy(alpha = 0.5f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }
      } else {
        // Voice Call View: Animated waves & Avatar
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .size(170.dp)
              .scale(if (session.status == CallStatus.CONNECTED) pulseScale else 1f)
              .clip(CircleShape)
              .background(FFChatCyan.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            FFChatAvatar(
              photoUrl = session.targetAvatar,
              name = session.targetName,
              size = 130.dp
            )
          }

          Spacer(Modifier.height(24.dp))

          Text(
            text = session.targetName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )

          Spacer(Modifier.height(6.dp))

          Text(
            text = when (session.status) {
              CallStatus.CONNECTING -> "Connecting WebRTC Peer Connection…"
              CallStatus.RINGING -> "Ringing…"
              CallStatus.CONNECTED -> "In Call • $durationFormatted"
              CallStatus.ENDED -> "Call Ended"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if (session.status == CallStatus.CONNECTED) FFChatGreen else Color.LightGray
          )

          if (session.status == CallStatus.CONNECTED) {
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.GraphicEq, contentDescription = null, tint = FFChatCyan, modifier = Modifier.size(20.dp))
              Spacer(Modifier.width(6.dp))
              Text("WebRTC Encrypted Audio Stream", style = MaterialTheme.typography.labelSmall, color = FFChatCyan)
            }
          }
        }
      }

      // Top WebRTC STUN/TURN & Network Telemetry Bar
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 28.dp, start = 16.dp, end = 16.dp)
          .align(Alignment.TopCenter),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.65f)),
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (session.status == CallStatus.CONNECTED) FFChatGreen else Color.Yellow)
            )
            Spacer(Modifier.width(6.dp))
            Text(
              text = if (session.isGroup) "Group Call (${session.participantUids.size} in room)" else "1-on-1 Secure Call",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.SemiBold
            )
          }

          Text(
            text = "${webrtcState.latencyMs}ms • ${webrtcState.bitrateKbps} kbps • ICE: ${webrtcState.iceGatheringState}",
            style = MaterialTheme.typography.labelSmall,
            color = FFChatCyan,
            fontSize = 10.sp
          )
        }
      }

      // Bottom Call Control Actions
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
          .align(Alignment.BottomCenter),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D33).copy(alpha = 0.95f))
      ) {
        Column(
          modifier = Modifier.padding(vertical = 18.dp, horizontal = 12.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Action buttons row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Mute Button
            IconButton(
              onClick = { CallRepository.toggleMute() },
              modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (session.isMuted) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                .testTag("call_mute_button")
            ) {
              Icon(
                imageVector = if (session.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute",
                tint = if (session.isMuted) FFChatRed else Color.White
              )
            }

            // Camera Toggle Button
            IconButton(
              onClick = { CallRepository.toggleCamera() },
              modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (!session.isCameraOn) Color.White.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                .testTag("call_camera_button")
            ) {
              Icon(
                imageVector = if (session.isCameraOn) Icons.Default.Videocam else Icons.Default.VideocamOff,
                contentDescription = "Camera",
                tint = if (!session.isCameraOn) FFChatRed else Color.White
              )
            }

            // Switch Camera Button
            if (session.callType == CallType.VIDEO) {
              IconButton(
                onClick = { CallRepository.switchCamera() },
                modifier = Modifier
                  .size(52.dp)
                  .clip(CircleShape)
                  .background(Color.White.copy(alpha = 0.1f))
                  .testTag("call_switch_camera_button")
              ) {
                Icon(
                  imageVector = Icons.Default.Cameraswitch,
                  contentDescription = "Switch Camera",
                  tint = Color.White
                )
              }
            }

            // Speaker Button
            IconButton(
              onClick = { CallRepository.toggleSpeaker() },
              modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (session.isSpeakerOn) FFChatCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                .testTag("call_speaker_button")
            ) {
              Icon(
                imageVector = if (session.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                contentDescription = "Speaker",
                tint = if (session.isSpeakerOn) FFChatCyan else Color.White
              )
            }

            // Screen Sharing Button
            IconButton(
              onClick = { CallRepository.toggleScreenSharing() },
              modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(if (session.isScreenSharing) FFChatCyan else Color.White.copy(alpha = 0.1f))
                .testTag("call_screen_share_button")
            ) {
              Icon(
                imageVector = if (session.isScreenSharing) Icons.Default.StopScreenShare else Icons.Default.ScreenShare,
                contentDescription = "Screen Share",
                tint = if (session.isScreenSharing) Color.Black else Color.White
              )
            }

            // End Call Button
            IconButton(
              onClick = {
                CallRepository.endCall()
                onEndCall()
              },
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(FFChatRed)
                .testTag("end_call_button")
            ) {
              Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "End Call",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
              )
            }
          }
        }
      }
    }
  }
}
