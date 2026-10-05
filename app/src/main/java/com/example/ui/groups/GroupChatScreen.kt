package com.example.ui.groups

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.ChatMessage
import com.example.model.Group
import com.example.model.GroupRole
import com.example.model.MessageType
import com.example.model.User
import com.example.repository.GroupRepository
import com.example.repository.UserRepository
import com.example.ui.components.FFChatAvatar
import com.example.ui.components.MediaViewerDialog
import com.example.ui.components.UserSearchProfileDialog
import com.example.ui.theme.DarkBubbleReceived
import com.example.ui.theme.DarkBubbleSent
import com.example.ui.theme.FFChatCyan
import com.example.ui.theme.FFChatGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
  group: Group,
  onBack: () -> Unit,
  onOpenGroupInfo: (Group) -> Unit,
  onOpenDirectChat: (User) -> Unit,
  onStartGroupCall: (Group, isVideo: Boolean) -> Unit,
  onJoinActiveCall: (Group) -> Unit
) {
  val currentUser = UserRepository.currentUser.collectAsState().value ?: return
  val currentGroup = GroupRepository.groups.collectAsState().value.find { it.id == group.id } ?: group
  val messages by GroupRepository.getGroupMessagesFlow(group.id).collectAsState()

  val userRole = currentGroup.getUserRole(currentUser.uid)
  val canStartCall = currentGroup.canStartCall(currentUser.uid)

  var textInput by remember { mutableStateOf("") }
  var replyingTo by remember { mutableStateOf<ChatMessage?>(null) }
  var showAttachmentSheet by remember { mutableStateOf(false) }
  var selectedMediaForViewer by remember { mutableStateOf<ChatMessage?>(null) }
  var selectedMemberForProfile by remember { mutableStateOf<User?>(null) }
  var callPermissionError by remember { mutableStateOf<String?>(null) }

  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clickable { onOpenGroupInfo(currentGroup) }
              .testTag("group_header_tap")
          ) {
            FFChatAvatar(
              photoUrl = currentGroup.photoUrl,
              name = currentGroup.name,
              size = 42.dp
            )
            Spacer(Modifier.width(10.dp))
            Column {
              Text(
                text = currentGroup.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${currentGroup.memberIds.size} members • Tap for Info",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          // If active call is ongoing, authorized members can JOIN!
          if (!currentGroup.activeCallId.isNullOrBlank()) {
            IconButton(
              onClick = { onJoinActiveCall(currentGroup) },
              modifier = Modifier.testTag("group_join_call_action")
            ) {
              Icon(Icons.Default.PhoneInTalk, contentDescription = "Join Call", tint = FFChatGreen)
            }
          } else {
            // Voice Call start
            IconButton(
              onClick = {
                if (canStartCall) {
                  onStartGroupCall(currentGroup, false)
                } else {
                  callPermissionError = "Only Leader 👑 or Elders 🛡️ can start a group call. Members can join active calls."
                }
              },
              modifier = Modifier.testTag("group_voice_call_action")
            ) {
              Icon(Icons.Default.Call, contentDescription = "Start Voice Call", tint = if (canStartCall) FFChatCyan else Color.Gray)
            }
            // Video Call start
            IconButton(
              onClick = {
                if (canStartCall) {
                  onStartGroupCall(currentGroup, true)
                } else {
                  callPermissionError = "Only Leader 👑 or Elders 🛡️ can start a group call. Members can join active calls."
                }
              },
              modifier = Modifier.testTag("group_video_call_action")
            ) {
              Icon(Icons.Default.Videocam, contentDescription = "Start Video Call", tint = if (canStartCall) FFChatCyan else Color.Gray)
            }
          }

          IconButton(onClick = { onOpenGroupInfo(currentGroup) }) {
            Icon(Icons.Default.Info, contentDescription = "Group Info")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    bottomBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(8.dp)
      ) {
        // Reply banner
        replyingTo?.let { replyMsg ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .width(4.dp)
                    .height(28.dp)
                    .background(FFChatCyan, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Column {
                  Text(
                    text = "Replying to ${replyMsg.senderName}",
                    style = MaterialTheme.typography.labelSmall,
                    color = FFChatCyan,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = replyMsg.text.ifBlank { replyMsg.fileName ?: "Media" },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }
              }
              IconButton(onClick = { replyingTo = null }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
              }
            }
          }
        }

        // Attachments Panel
        AnimatedVisibility(visible = showAttachmentSheet) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            GroupAttachmentButton(
              icon = Icons.Default.Image,
              label = "HD Photo",
              color = Color(0xFF38BDF8),
              onClick = {
                showAttachmentSheet = false
                GroupRepository.sendGroupMessage(
                  groupId = currentGroup.id,
                  sender = currentUser,
                  text = "",
                  type = MessageType.IMAGE,
                  mediaUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800"
                )
              }
            )
            GroupAttachmentButton(
              icon = Icons.Default.VideoLibrary,
              label = "HD Video",
              color = Color(0xFFA855F7),
              onClick = {
                showAttachmentSheet = false
                GroupRepository.sendGroupMessage(
                  groupId = currentGroup.id,
                  sender = currentUser,
                  text = "",
                  type = MessageType.VIDEO,
                  mediaUrl = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=800"
                )
              }
            )
            GroupAttachmentButton(
              icon = Icons.Default.InsertDriveFile,
              label = "File / APK",
              color = Color(0xFF34D399),
              onClick = {
                showAttachmentSheet = false
                GroupRepository.sendGroupMessage(
                  groupId = currentGroup.id,
                  sender = currentUser,
                  text = "Shared package file",
                  type = MessageType.FILE,
                  fileName = "ffchat-architecture-v1.apk",
                  fileSize = 420_000_000L
                )
              }
            )
          }
        }

        // Input row
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = { showAttachmentSheet = !showAttachmentSheet }) {
            Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = FFChatCyan)
          }

          OutlinedTextField(
            value = textInput,
            onValueChange = { textInput = it },
            placeholder = { Text("Message ${currentGroup.name}…") },
            maxLines = 4,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
              focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
              focusedBorderColor = Color.Transparent,
              unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
              .weight(1f)
              .testTag("group_chat_input")
          )

          Spacer(Modifier.width(6.dp))

          IconButton(
            onClick = {
              if (textInput.trim().isNotEmpty()) {
                val sendingText = textInput.trim()
                val currentReply = replyingTo
                textInput = ""
                replyingTo = null

                GroupRepository.sendGroupMessage(
                  groupId = currentGroup.id,
                  sender = currentUser,
                  text = sendingText,
                  type = MessageType.TEXT,
                  replyTo = currentReply
                )
              }
            },
            enabled = textInput.trim().isNotEmpty(),
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(if (textInput.trim().isNotEmpty()) FFChatCyan else MaterialTheme.colorScheme.surfaceVariant)
              .testTag("group_send_message_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Send,
              contentDescription = "Send",
              tint = if (textInput.trim().isNotEmpty()) Color.Black else Color.Gray,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        if (msg.type == MessageType.SYSTEM) {
          SystemMessageBadge(msg.text)
        } else {
          val isMe = msg.senderId == currentUser.uid
          val senderUser = UserRepository.getUserById(msg.senderId)

          GroupMessageRow(
            message = msg,
            isMe = isMe,
            senderRole = currentGroup.getUserRole(msg.senderId),
            onMemberTap = {
              if (senderUser != null && !isMe) {
                selectedMemberForProfile = senderUser
              }
            },
            onReply = { replyingTo = msg },
            onMediaClick = { selectedMediaForViewer = msg }
          )
        }
      }
    }

    // Call permission alert if regular member tries to start call
    callPermissionError?.let { err ->
      AlertDialog(
        onDismissRequest = { callPermissionError = null },
        title = { Text("Call Permission") },
        text = { Text(err) },
        confirmButton = {
          Button(onClick = { callPermissionError = null }) {
            Text("Understood")
          }
        }
      )
    }

    // Member profile popup when tapping member name/avatar
    selectedMemberForProfile?.let { member ->
      UserSearchProfileDialog(
        user = member,
        onDismiss = { selectedMemberForProfile = null },
        onOpenDirectChat = {
          selectedMemberForProfile = null
          onOpenDirectChat(it)
        },
        onVoiceCall = {
          selectedMemberForProfile = null
          onOpenDirectChat(it)
        },
        onVideoCall = {
          selectedMemberForProfile = null
          onOpenDirectChat(it)
        }
      )
    }

    selectedMediaForViewer?.let { mediaMsg ->
      MediaViewerDialog(
        message = mediaMsg,
        onDismiss = { selectedMediaForViewer = null }
      )
    }
  }
}

@Composable
private fun GroupMessageRow(
  message: ChatMessage,
  isMe: Boolean,
  senderRole: GroupRole,
  onMemberTap: () -> Unit,
  onReply: () -> Unit,
  onMediaClick: () -> Unit
) {
  val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
  ) {
    if (!isMe) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clickable(onClick = onMemberTap)
          .padding(start = 4.dp, bottom = 2.dp)
          .testTag("tap_member_${message.senderId}")
      ) {
        Text(
          text = message.senderName,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = FFChatCyan
        )
        Spacer(Modifier.width(6.dp))
        when (senderRole) {
          GroupRole.LEADER -> Text("👑 Leader", color = Color(0xFFF59E0B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
          GroupRole.ELDER -> Text("🛡️ Elder", color = FFChatCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
          GroupRole.MEMBER -> {}
        }
      }
    }

    Card(
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isMe) 16.dp else 4.dp,
        bottomEnd = if (isMe) 4.dp else 16.dp
      ),
      colors = CardDefaults.cardColors(
        containerColor = if (isMe) DarkBubbleSent else DarkBubbleReceived
      ),
      modifier = Modifier.widthIn(max = 300.dp)
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        if (!message.replyToText.isNullOrBlank()) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.2f),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp)
          ) {
            Row(modifier = Modifier.padding(6.dp)) {
              Box(
                modifier = Modifier
                  .width(3.dp)
                  .height(24.dp)
                  .background(FFChatCyan, RoundedCornerShape(2.dp))
              )
              Spacer(Modifier.width(6.dp))
              Column {
                Text(
                  text = message.replyToSenderName ?: "Member",
                  style = MaterialTheme.typography.labelSmall,
                  color = FFChatCyan,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = message.replyToText,
                  style = MaterialTheme.typography.bodySmall,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  color = Color.LightGray
                )
              }
            }
          }
        }

        when (message.type) {
          MessageType.IMAGE -> {
            AsyncImage(
              model = message.mediaUrl ?: "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=600",
              contentDescription = "Group Photo",
              contentScale = ContentScale.Crop,
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable { onMediaClick() }
            )
            Spacer(Modifier.height(4.dp))
          }
          MessageType.VIDEO -> {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black)
                .clickable { onMediaClick() },
              contentAlignment = Alignment.Center
            ) {
              AsyncImage(
                model = message.mediaUrl ?: "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=600",
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
              )
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(CircleShape)
                  .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White)
              }
            }
            Spacer(Modifier.height(4.dp))
          }
          MessageType.FILE -> {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable { onMediaClick() }
                .padding(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = FFChatCyan)
              Spacer(Modifier.width(8.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = message.fileName ?: "File",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = message.formattedFileSize.ifBlank { "File" },
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.LightGray
                )
              }
            }
            Spacer(Modifier.height(4.dp))
          }
          else -> {}
        }

        if (message.text.isNotBlank()) {
          Text(
            text = message.text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
          )
          Spacer(Modifier.height(4.dp))
        }

        Row(
          modifier = Modifier.align(Alignment.End),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = onReply, modifier = Modifier.size(18.dp)) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Reply,
              contentDescription = "Reply",
              tint = Color.LightGray.copy(alpha = 0.6f),
              modifier = Modifier.size(14.dp)
            )
          }
          Spacer(Modifier.width(6.dp))
          Text(
            text = timeFormat.format(Date(message.timestamp)),
            style = MaterialTheme.typography.labelSmall,
            color = Color.LightGray.copy(alpha = 0.7f),
            fontSize = 10.sp
          )
          if (isMe) {
            Spacer(Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Default.DoneAll,
              contentDescription = "Delivered",
              tint = FFChatCyan,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SystemMessageBadge(text: String) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
      )
    }
  }
}

@Composable
private fun GroupAttachmentButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  color: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(8.dp)
  ) {
    Box(
      modifier = Modifier
        .size(48.dp)
        .clip(CircleShape)
        .background(color.copy(alpha = 0.2f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
    }
    Spacer(Modifier.height(4.dp))
    Text(label, style = MaterialTheme.typography.labelSmall)
  }
}
