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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatSummary
import com.example.model.MessageStatus
import com.example.model.User
import com.example.repository.ChatRepository
import com.example.repository.UserRepository
import com.example.ui.components.FFChatAvatar
import com.example.ui.components.UserSearchProfileDialog
import com.example.ui.theme.FFChatCyan
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatListScreen(
  onOpenDirectChat: (User) -> Unit,
  onStartVoiceCall: (User) -> Unit,
  onStartVideoCall: (User) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedSearchedUser by remember { mutableStateOf<User?>(null) }

  val conversations by ChatRepository.conversations.collectAsState()
  val searchResults = remember(searchQuery) {
    if (searchQuery.isNotBlank()) UserRepository.searchByUsername(searchQuery) else emptyList()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("chat_list_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Top compact search box: "Search @username"
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = {
            Text(
              text = "Search @username",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = FFChatCyan,
              modifier = Modifier.size(20.dp)
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(24.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            focusedBorderColor = FFChatCyan,
            unfocusedBorderColor = Color.Transparent
          ),
          modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .testTag("search_username_input")
        )
      }

      // Search results overlay if search query is active
      AnimatedVisibility(visible = searchQuery.isNotBlank()) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Search Results for \"$searchQuery\"",
              style = MaterialTheme.typography.labelMedium,
              color = FFChatCyan,
              fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            if (searchResults.isEmpty()) {
              Text(
                text = "No user found with this @username",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
              )
            } else {
              searchResults.forEach { user ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { selectedSearchedUser = user }
                    .padding(8.dp)
                    .testTag("search_result_item_${user.cleanUsername}"),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  FFChatAvatar(
                    photoUrl = user.photoUrl,
                    name = user.name,
                    size = 40.dp,
                    isOnline = user.isOnline
                  )
                  Spacer(Modifier.width(12.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = user.name,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = user.cleanUsername,
                      style = MaterialTheme.typography.bodySmall,
                      color = FFChatCyan
                    )
                  }
                  Text(
                    text = "Tap to view",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }
          }
        }
      }

      // Main Conversation List
      if (conversations.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.ChatBubbleOutline,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
              text = "No messages yet",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
              text = "Search a unique @username above to start messaging and sharing 1 GB files!",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
        ) {
          items(conversations, key = { it.chatId }) { summary ->
            ConversationRow(
              summary = summary,
              onClick = {
                val target = summary.targetUser ?: UserRepository.getUserById(summary.chatId)
                if (target != null) {
                  onOpenDirectChat(target)
                }
              }
            )
          }
        }
      }
    }

    // User profile modal when search result is tapped
    selectedSearchedUser?.let { user ->
      UserSearchProfileDialog(
        user = user,
        onDismiss = { selectedSearchedUser = null },
        onOpenDirectChat = {
          selectedSearchedUser = null
          searchQuery = ""
          onOpenDirectChat(it)
        },
        onVoiceCall = {
          selectedSearchedUser = null
          onStartVoiceCall(it)
        },
        onVideoCall = {
          selectedSearchedUser = null
          onStartVideoCall(it)
        }
      )
    }
  }
}

@Composable
private fun ConversationRow(
  summary: ChatSummary,
  onClick: () -> Unit
) {
  val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp, vertical = 10.dp)
      .testTag("conversation_item_${summary.chatId}"),
    verticalAlignment = Alignment.CenterVertically
  ) {
    FFChatAvatar(
      photoUrl = summary.avatarUrl,
      name = summary.title,
      size = 54.dp,
      isOnline = summary.isOnline
    )

    Spacer(Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = summary.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onBackground,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = timeFormat.format(Date(summary.lastMessageTime)),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!summary.isGroup) {
          when (summary.lastMessageStatus) {
            MessageStatus.READ -> {
              Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Read",
                tint = FFChatCyan,
                modifier = Modifier.size(16.dp)
              )
              Spacer(Modifier.width(4.dp))
            }
            MessageStatus.DELIVERED -> {
              Icon(
                imageVector = Icons.Default.DoneAll,
                contentDescription = "Delivered",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
              Spacer(Modifier.width(4.dp))
            }
            MessageStatus.SENT -> {
              Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "Sent",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
              )
              Spacer(Modifier.width(4.dp))
            }
            else -> {}
          }
        }

        Text(
          text = summary.subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f)
        )

        if (summary.unreadCount > 0) {
          Spacer(Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .size(20.dp)
              .clip(CircleShape)
              .background(FFChatCyan),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${summary.unreadCount}",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = Color.Black
            )
          }
        }
      }
    }
  }
}
