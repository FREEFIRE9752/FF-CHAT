package com.example.ui.chat

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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.model.MessageStatus
import com.example.model.MessageType
import com.example.model.User
import com.example.repository.ChatRepository
import com.example.repository.UserRepository
import com.example.service.StorageService
import com.example.service.UploadProgress
import com.example.ui.components.FFChatAvatar
import com.example.ui.components.FileUploadProgressCard
import com.example.ui.components.MediaViewerDialog
import com.example.ui.theme.DarkBubbleReceived
import com.example.ui.theme.DarkBubbleSent
import com.example.ui.theme.FFChatCyan
import com.example.ui.theme.FFChatGreen
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectChatScreen(
  targetUser: User,
  onBack: () -> Unit,
  onStartVoiceCall: () -> Unit,
  onStartVideoCall: () -> Unit,
  onViewProfile: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val currentUser = UserRepository.currentUser.collectAsState().value ?: return

  val messages by ChatRepository.getMessagesFlow(targetUser.uid).collectAsState()
  val typingUsers by ChatRepository.getTypingFlow(targetUser.uid).collectAsState()
  val isOtherTyping = typingUsers.contains(targetUser.uid)

  var textInput by remember { mutableStateOf("") }
  var replyingTo by remember { mutableStateOf<ChatMessage?>(null) }
  var selectedMediaForViewer by remember { mutableStateOf<ChatMessage?>(null) }
  var showAttachmentSheet by remember { mutableStateOf(false) }
  var showMoreMenu by remember { mutableStateOf(false) }

  // Upload simulation state for 1 GB files
  var activeUploadProgress by remember { mutableStateOf<UploadProgress?>(null) }
  var activeUploadingFileName by remember { mutableStateOf("") }
  var uploadJob by remember { mutableStateOf<Job?>(null) }

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
            modifier = Modifier.clickable { onViewProfile() }
          ) {
            FFChatAvatar(
              photoUrl = targetUser.photoUrl,
              name = targetUser.name,
              size = 42.dp,
              isOnline = targetUser.isOnline
            )
            Spacer(Modifier.width(10.dp))
            Column {
              Text(
                text = targetUser.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = if (isOtherTyping) "typing…" else if (targetUser.isOnline) "Online" else "Last seen recently",
                style = MaterialTheme.typography.labelSmall,
                color = if (isOtherTyping) FFChatCyan else if (targetUser.isOnline) FFChatGreen else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = onStartVoiceCall, modifier = Modifier.testTag("chat_voice_call_button")) {
            Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = FFChatCyan)
          }
          IconButton(onClick = onStartVideoCall, modifier = Modifier.testTag("chat_video_call_button")) {
            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = FFChatCyan)
          }
          Box {
            IconButton(onClick = { showMoreMenu = true }, modifier = Modifier.testTag("chat_more_button")) {
              Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
            DropdownMenu(
              expanded = showMoreMenu,
              onDismissRequest = { showMoreMenu = false }
            ) {
              DropdownMenuItem(
                text = { Text("View Profile") },
                onClick = {
                  showMoreMenu = false
                  onViewProfile()
                }
              )
              DropdownMenuItem(
                text = { Text("Clear Chat") },
                onClick = {
                  showMoreMenu = false
                  ChatRepository.clearChat(targetUser.uid)
                }
              )
              DropdownMenuItem(
                text = { Text("Block ${targetUser.cleanUsername}") },
                onClick = {
                  showMoreMenu = false
                  UserRepository.blockUser(targetUser.uid)
                  onBack()
                }
              )
            }
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(8.dp)
      ) {
        // Upload progress card if a 1 GB file is currently transmitting
        activeUploadProgress?.let { progress ->
          FileUploadProgressCard(
            fileName = activeUploadingFileName,
            progressFraction = progress.progressFraction,
            bytesTransferred = progress.bytesTransferred,
            totalBytes = progress.totalBytes,
            onCancel = {
              uploadJob?.cancel()
              activeUploadProgress = null
            }
          )
        }

        // Reply preview banner
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

        // Attachments Picker Sheet
        AnimatedVisibility(visible = showAttachmentSheet) {
          AttachmentPickerPanel(
            onSendPhoto = {
              showAttachmentSheet = false
              simulateChunkedMediaUpload(
                context = context,
                fileName = "photo_hd_${System.currentTimeMillis()}.jpg",
                fileSize = 18_500_000L, // 18.5 MB HD photo
                mimeType = "image/jpeg",
                type = MessageType.IMAGE,
                targetUid = targetUser.uid,
                currentUser = currentUser,
                onProgress = { p, f ->
                  activeUploadProgress = p
                  activeUploadingFileName = f
                }
              )
            },
            onSendVideo = {
              showAttachmentSheet = false
              simulateChunkedMediaUpload(
                context = context,
                fileName = "cinematic_4k_${System.currentTimeMillis()}.mp4",
                fileSize = 750_000_000L, // 750 MB HD Video (under 1GB)
                mimeType = "video/mp4",
                type = MessageType.VIDEO,
                targetUid = targetUser.uid,
                currentUser = currentUser,
                onProgress = { p, f ->
                  activeUploadProgress = p
                  activeUploadingFileName = f
                }
              )
            },
            onSendFile = {
              showAttachmentSheet = false
              simulateChunkedMediaUpload(
                context = context,
                fileName = "project_resources_full.apk",
                fileSize = 450_000_000L, // 450 MB APK
                mimeType = "application/vnd.android.package-archive",
                type = MessageType.FILE,
                targetUid = targetUser.uid,
                currentUser = currentUser,
                onProgress = { p, f ->
                  activeUploadProgress = p
                  activeUploadingFileName = f
                }
              )
            }
          )
        }

        // Message Input Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { showAttachmentSheet = !showAttachmentSheet },
            modifier = Modifier.testTag("attach_file_button")
          ) {
            Icon(Icons.Default.AttachFile, contentDescription = "Attach media", tint = FFChatCyan)
          }

          OutlinedTextField(
            value = textInput,
            onValueChange = {
              textInput = it
              coroutineScope.launch {
                ChatRepository.setTyping(targetUser.uid, currentUser.uid, it.isNotEmpty())
              }
            },
            placeholder = { Text("Message…") },
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
              .testTag("chat_message_input")
          )

          Spacer(Modifier.width(6.dp))

          IconButton(
            onClick = {
              if (textInput.trim().isNotEmpty()) {
                val sendingText = textInput.trim()
                val currentReply = replyingTo
                textInput = ""
                replyingTo = null
                ChatRepository.setTyping(targetUser.uid, currentUser.uid, false)

                ChatRepository.sendMessage(
                  chatId = targetUser.uid,
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
              .testTag("send_message_button")
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
        .padding(paddingValues)
        .testTag("chat_messages_list"),
      contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(messages, key = { it.id }) { message ->
        ChatMessageBubble(
          message = message,
          isMe = message.senderId == currentUser.uid,
          onReply = { replyingTo = message },
          onMediaClick = { selectedMediaForViewer = message }
        )
      }

      if (isOtherTyping) {
        item {
          TypingBubble(targetUser.name)
        }
      }
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
private fun AttachmentPickerPanel(
  onSendPhoto: () -> Unit,
  onSendVideo: () -> Unit,
  onSendFile: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 10.dp),
    horizontalArrangement = Arrangement.SpaceEvenly
  ) {
    AttachmentActionItem(
      icon = Icons.Default.Image,
      label = "HD Photo",
      color = Color(0xFF38BDF8),
      onClick = onSendPhoto
    )
    AttachmentActionItem(
      icon = Icons.Default.VideoLibrary,
      label = "HD Video",
      color = Color(0xFFA855F7),
      onClick = onSendVideo
    )
    AttachmentActionItem(
      icon = Icons.Default.InsertDriveFile,
      label = "File / APK",
      color = Color(0xFF34D399),
      onClick = onSendFile
    )
  }
}

@Composable
private fun AttachmentActionItem(
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
        .size(52.dp)
        .clip(CircleShape)
        .background(color.copy(alpha = 0.2f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(28.dp))
    }
    Spacer(Modifier.height(6.dp))
    Text(label, style = MaterialTheme.typography.labelSmall)
  }
}

@Composable
private fun ChatMessageBubble(
  message: ChatMessage,
  isMe: Boolean,
  onReply: () -> Unit,
  onMediaClick: () -> Unit
) {
  val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
  ) {
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
      modifier = Modifier
        .widthIn(max = 300.dp)
        .testTag("chat_bubble_${message.id}")
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        // Reply snippet preview
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
                  text = message.replyToSenderName ?: "User",
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

        // Media content
        when (message.type) {
          MessageType.IMAGE -> {
            AsyncImage(
              model = message.mediaUrl ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600",
              contentDescription = "Photo",
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
                contentDescription = "Video Thumbnail",
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
                  text = message.formattedFileSize,
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.LightGray
                )
              }
            }
            Spacer(Modifier.height(4.dp))
          }
          else -> {}
        }

        // Text body
        if (message.text.isNotBlank()) {
          Text(
            text = message.text,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
          )
          Spacer(Modifier.height(4.dp))
        }

        // Footer: Timestamp + Status + Reply Action
        Row(
          modifier = Modifier.align(Alignment.End),
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onReply,
            modifier = Modifier.size(18.dp)
          ) {
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
            when (message.status) {
              MessageStatus.SENDING -> {
                Text("•", color = Color.LightGray, fontSize = 12.sp)
              }
              MessageStatus.SENT -> {
                Icon(Icons.Default.Done, contentDescription = "Sent", tint = Color.LightGray, modifier = Modifier.size(14.dp))
              }
              MessageStatus.DELIVERED -> {
                Icon(Icons.Default.DoneAll, contentDescription = "Delivered", tint = Color.LightGray, modifier = Modifier.size(14.dp))
              }
              MessageStatus.READ -> {
                Icon(Icons.Default.DoneAll, contentDescription = "Read", tint = FFChatCyan, modifier = Modifier.size(14.dp))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun TypingBubble(senderName: String) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = DarkBubbleReceived),
    modifier = Modifier.padding(vertical = 4.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "$senderName is typing…",
        style = MaterialTheme.typography.labelMedium,
        color = FFChatCyan
      )
    }
  }
}

private fun simulateChunkedMediaUpload(
  context: android.content.Context,
  fileName: String,
  fileSize: Long,
  mimeType: String,
  type: MessageType,
  targetUid: String,
  currentUser: User,
  onProgress: (UploadProgress?, String) -> Unit
) {
  kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default).launch {
    val totalSteps = 8
    val stepSize = fileSize / totalSteps

    for (step in 1..totalSteps) {
      delay(200)
      val transferred = step * stepSize
      val fraction = step.toFloat() / totalSteps
      onProgress(
        UploadProgress(
          bytesTransferred = transferred,
          totalBytes = fileSize,
          progressFraction = fraction,
          isCompleted = step == totalSteps
        ),
        fileName
      )
    }

    delay(200)
    onProgress(null, "")

    ChatRepository.sendMessage(
      chatId = targetUid,
      sender = currentUser,
      text = if (type == MessageType.FILE) "Attached: $fileName" else "",
      type = type,
      mediaUrl = if (type == MessageType.IMAGE) "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800" else "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=800",
      fileName = fileName,
      fileSize = fileSize,
      mimeType = mimeType
    )
  }
}
