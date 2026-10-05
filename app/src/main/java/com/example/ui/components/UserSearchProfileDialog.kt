package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.User
import com.example.ui.theme.FFChatCyan
import com.example.ui.theme.FFChatGreen

@Composable
fun UserSearchProfileDialog(
  user: User,
  onDismiss: () -> Unit,
  onOpenDirectChat: (User) -> Unit,
  onVoiceCall: (User) -> Unit,
  onVideoCall: (User) -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("user_search_profile_dialog"),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close")
          }
        }

        FFChatAvatar(
          photoUrl = user.photoUrl,
          name = user.name,
          size = 96.dp,
          isOnline = user.isOnline
        )

        Spacer(Modifier.height(16.dp))

        Text(
          text = user.name,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = user.cleanUsername,
          style = MaterialTheme.typography.titleMedium,
          color = FFChatCyan,
          fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(if (user.isOnline) FFChatGreen else Color.Gray)
          )
          Spacer(Modifier.width(6.dp))
          Text(
            text = if (user.isOnline) "Online" else "Offline",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Spacer(Modifier.height(16.dp))

        Text(
          text = user.bio,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.height(24.dp))

        // Direct Message Main Action
        Button(
          onClick = {
            onDismiss()
            onOpenDirectChat(user)
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("direct_message_button"),
          colors = ButtonDefaults.buttonColors(containerColor = FFChatCyan)
        ) {
          Icon(Icons.Default.Chat, contentDescription = null, tint = Color.Black)
          Spacer(Modifier.width(8.dp))
          Text("Message", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(12.dp))

        // Calling buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          FilledTonalButton(
            onClick = {
              onDismiss()
              onVoiceCall(user)
            },
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("profile_voice_call_button")
          ) {
            Icon(Icons.Default.Call, contentDescription = "Voice Call", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Voice")
          }

          FilledTonalButton(
            onClick = {
              onDismiss()
              onVideoCall(user)
            },
            modifier = Modifier
              .weight(1f)
              .height(44.dp)
              .testTag("profile_video_call_button")
          ) {
            Icon(Icons.Default.Videocam, contentDescription = "Video Call", modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Video")
          }
        }
      }
    }
  }
}
