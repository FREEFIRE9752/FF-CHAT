package com.example.ui.groups

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.example.model.Group
import com.example.model.GroupRole
import com.example.repository.GroupRepository
import com.example.repository.UserRepository
import com.example.ui.components.CreateGroupDialog
import com.example.ui.components.FFChatAvatar
import com.example.ui.components.JoinGroupDialog
import com.example.ui.theme.FFChatCyan
import com.example.ui.theme.FFChatGreen
import com.example.ui.theme.FFChatOrange

@Composable
fun GroupListScreen(
  onOpenGroupChat: (Group) -> Unit,
  onJoinActiveCall: (Group) -> Unit,
  modifier: Modifier = Modifier
) {
  val groups by GroupRepository.groups.collectAsState()
  val currentUser = UserRepository.currentUser.collectAsState().value ?: return

  var showCreateDialog by remember { mutableStateOf(false) }
  var showJoinDialog by remember { mutableStateOf(false) }

  Scaffold(
    modifier = modifier.testTag("group_list_screen"),
    floatingActionButton = {
      ExtendedFloatingActionButton(
        onClick = { showCreateDialog = true },
        icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black) },
        text = { Text("Create Group", color = Color.Black, fontWeight = FontWeight.Bold) },
        containerColor = FFChatCyan,
        modifier = Modifier.testTag("create_group_fab")
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Header Quick Actions Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Your Groups (${groups.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        OutlinedButton(
          onClick = { showJoinDialog = true },
          shape = RoundedCornerShape(20.dp),
          modifier = Modifier.testTag("join_with_link_button")
        ) {
          Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Join with Code")
        }
      }

      if (groups.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.Group,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
              modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
              text = "No groups yet",
              style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
              text = "Create a group to become Leader 👑 and assign up to 10 Elders 🛡️, or join with an invite code.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(groups, key = { it.id }) { group ->
            GroupCardItem(
              group = group,
              currentUserId = currentUser.uid,
              onClick = { onOpenGroupChat(group) },
              onJoinActiveCall = { onJoinActiveCall(group) }
            )
          }
        }
      }
    }

    if (showCreateDialog) {
      CreateGroupDialog(
        onDismiss = { showCreateDialog = false },
        onCreateGroup = { name, bio, photoUrl ->
          val newGroup = GroupRepository.createGroup(name, bio, photoUrl, currentUser)
          onOpenGroupChat(newGroup)
        }
      )
    }

    if (showJoinDialog) {
      JoinGroupDialog(
        onDismiss = { showJoinDialog = false },
        onJoinCode = { code ->
          val (success, _) = GroupRepository.joinGroupByInviteCode(code, currentUser)
          if (success) {
            val joinedGroup = groups.find { it.inviteCode.equals(code.trim(), ignoreCase = true) }
            if (joinedGroup != null) onOpenGroupChat(joinedGroup)
          }
        }
      )
    }
  }
}

@Composable
private fun GroupCardItem(
  group: Group,
  currentUserId: String,
  onClick: () -> Unit,
  onJoinActiveCall: () -> Unit
) {
  val role = group.getUserRole(currentUserId)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick)
      .testTag("group_item_${group.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        FFChatAvatar(
          photoUrl = group.photoUrl,
          name = group.name,
          size = 52.dp
        )

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = group.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            // Role badge
            RoleBadge(role)
          }

          Spacer(Modifier.height(4.dp))

          Text(
            text = group.bio.ifBlank { "Group conversation" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )

          Spacer(Modifier.height(4.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "${group.memberIds.size} members",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = " • ",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "${group.elderIds.size}/10 Elders",
              style = MaterialTheme.typography.labelSmall,
              color = FFChatCyan
            )
          }
        }
      }

      // If active call is ongoing, display Join Call banner!
      if (!group.activeCallId.isNullOrBlank()) {
        Spacer(Modifier.height(10.dp))
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = FFChatGreen.copy(alpha = 0.15f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(10.dp)
                  .clip(CircleShape)
                  .background(FFChatGreen)
              )
              Spacer(Modifier.width(8.dp))
              Text(
                text = "Active Group Call in progress",
                style = MaterialTheme.typography.labelMedium,
                color = FFChatGreen,
                fontWeight = FontWeight.Bold
              )
            }

            Button(
              onClick = onJoinActiveCall,
              colors = ButtonDefaults.buttonColors(containerColor = FFChatGreen),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier.testTag("join_active_call_button")
            ) {
              Icon(Icons.Default.PhoneInTalk, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(4.dp))
              Text("Join Call", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
            }
          }
        }
      }
    }
  }
}

@Composable
fun RoleBadge(role: GroupRole) {
  val (label, bgColor, textColor) = when (role) {
    GroupRole.LEADER -> Triple("👑 Leader", FFChatOrange.copy(alpha = 0.2f), FFChatOrange)
    GroupRole.ELDER -> Triple("🛡️ Elder", FFChatCyan.copy(alpha = 0.2f), FFChatCyan)
    GroupRole.MEMBER -> Triple("Member", Color.Gray.copy(alpha = 0.15f), Color.LightGray)
  }

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 2.dp)
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = textColor,
      fontWeight = FontWeight.Bold
    )
  }
}
