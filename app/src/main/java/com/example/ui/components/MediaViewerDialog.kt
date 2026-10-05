package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.ChatMessage
import com.example.model.MessageType
import com.example.ui.theme.FFChatCyan

@Composable
fun MediaViewerDialog(
  message: ChatMessage,
  onDismiss: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .testTag("media_viewer_dialog"),
      color = Color.Black.copy(alpha = 0.95f)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        // Top Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .align(Alignment.TopCenter),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = message.senderName,
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = message.senderUsername,
              style = MaterialTheme.typography.bodySmall,
              color = FFChatCyan
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.2f))
              .testTag("close_media_viewer")
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        // Center Content
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 80.dp),
          contentAlignment = Alignment.Center
        ) {
          when (message.type) {
            MessageType.IMAGE -> {
              AsyncImage(
                model = message.mediaUrl ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=1080",
                contentDescription = "HD Photo Preview",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              )
            }
            MessageType.VIDEO -> {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .height(340.dp)
                  .padding(16.dp)
                  .clip(RoundedCornerShape(16.dp))
                  .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
              ) {
                AsyncImage(
                  model = message.mediaUrl ?: "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=800",
                  contentDescription = "HD Video Thumbnail",
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.matchParentSize()
                )
                Box(
                  modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                )
                Box(
                  modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(FFChatCyan),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play HD Video",
                    tint = Color.Black,
                    modifier = Modifier.size(44.dp)
                  )
                }
              }
            }
            MessageType.FILE -> {
              Card(
                modifier = Modifier
                  .fillMaxWidth(0.85f)
                  .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp)
              ) {
                Column(
                  modifier = Modifier.padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Box(
                    modifier = Modifier
                      .size(80.dp)
                      .clip(CircleShape)
                      .background(FFChatCyan.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(
                      Icons.Default.InsertDriveFile,
                      contentDescription = "File",
                      tint = FFChatCyan,
                      modifier = Modifier.size(40.dp)
                    )
                  }
                  Spacer(Modifier.height(16.dp))
                  Text(
                    text = message.fileName ?: "Document",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Spacer(Modifier.height(8.dp))
                  Text(
                    text = message.formattedFileSize,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                  )
                  Spacer(Modifier.height(20.dp))
                  Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = FFChatCyan),
                    modifier = Modifier.testTag("download_file_button")
                  ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("Download File", color = Color.Black, fontWeight = FontWeight.Bold)
                  }
                }
              }
            }
            else -> {}
          }
        }
      }
    }
  }
}
