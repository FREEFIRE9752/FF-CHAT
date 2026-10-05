package com.example.ui.groups

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Group
import com.example.model.GroupRole
import com.example.model.User
import com.example.repository.GroupRepository
import com.example.repository.UserRepository
import com.example.ui.components.FFChatAvatar
import com.example.ui.components.UserSearchProfileDialog
import com.example.ui.theme.FFChatCyan
import com.example.ui.theme.FFChatOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupInfoScreen(
  group: Group,
  onBack: () -> Unit,
  onOpenDirectChat: (User) -> Unit
) {
  val context = LocalContext.current
  val currentUser = UserRepository.currentUser.collectAsState().value ?: return
  val allGroups by GroupRepository.groups.collectAsState()
  val currentGroup = allGroups.find { it.id == group.id } ?: group

  val myRole = currentGroup.getUserRole(currentUser.uid)
  val isLeader = myRole == GroupRole.LEADER
  val isElder = myRole == GroupRole.ELDER

  var showEditInfoDialog by remember { mutableStateOf(false) }
  var selectedMemberForProfile by remember { mutableStateOf<User?>(null) }
  var actionFeedbackMessage by remember { mutableStateOf<String?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Group Information", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (isLeader) {
            IconButton(
              onClick = { showEditInfoDialog = true },
              modifier = Modifier.testTag("edit_group_info_button")
            ) {
              Icon(Icons.Default.Edit, contentDescription = "Edit Group", tint = FFChatCyan)
            }
          }
        }
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Group Hero Header
      item {
        Card(
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            FFChatAvatar(
              photoUrl = currentGroup.photoUrl,
              name = currentGroup.name,
              size = 90.dp
            )
            Spacer(Modifier.height(14.dp))
            Text(
              text = currentGroup.name,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            RoleBadge(myRole)

            Spacer(Modifier.height(10.dp))
            Text(
              text = currentGroup.bio.ifBlank { "No bio provided" },
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Share Invite Link Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "Invite Link & Code",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Code: ${currentGroup.inviteCode}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = FFChatCyan,
                  fontWeight = FontWeight.SemiBold
                )
              }

              Row {
                IconButton(
                  onClick = {
                    val link = "ffchat://join?code=${currentGroup.inviteCode}"
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("FF CHAT Invite", link))
                    Toast.makeText(context, "Invite link copied to clipboard!", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.testTag("copy_invite_link_button")
                ) {
                  Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = FFChatCyan)
                }

                IconButton(
                  onClick = {
                    val link = "ffchat://join?code=${currentGroup.inviteCode}"
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("FF CHAT Invite", link))
                    Toast.makeText(context, "Share link: $link", Toast.LENGTH_SHORT).show()
                  },
                  modifier = Modifier.testTag("share_invite_link_button")
                ) {
                  Icon(Icons.Default.Share, contentDescription = "Share")
                }
              }
            }

            Spacer(Modifier.height(6.dp))
            Text(
              text = "Anyone with this code can join this group.",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Elders Hierarchy Summary
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = "Group Hierarchy & Permissions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Elders assigned: ${currentGroup.elderIds.size} / 10 maximum",
                style = MaterialTheme.typography.bodySmall,
                color = FFChatCyan
              )
            }
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(FFChatCyan.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Shield, contentDescription = null, tint = FFChatCyan, modifier = Modifier.size(20.dp))
            }
          }
        }
      }

      // Member List Header
      item {
        Text(
          text = "Members (${currentGroup.memberIds.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
      }

      // Members List
      items(currentGroup.memberIds) { memberId ->
        val memberUser = UserRepository.getUserById(memberId)
        val memberRole = currentGroup.getUserRole(memberId)

        MemberRowItem(
          user = memberUser ?: User(uid = memberId, name = "Member", username = "@user"),
          role = memberRole,
          isSelf = memberId == currentUser.uid,
          canManage = currentGroup.canManageMembers(currentUser.uid),
          isLeader = isLeader,
          onTapMember = {
            if (memberUser != null && memberId != currentUser.uid) {
              selectedMemberForProfile = memberUser
            }
          },
          onPromoteElder = {
            val (ok, msg) = GroupRepository.promoteToElder(currentGroup.id, memberId, currentUser.uid)
            actionFeedbackMessage = msg
          },
          onDemoteElder = {
            val (ok, msg) = GroupRepository.demoteElder(currentGroup.id, memberId, currentUser.uid)
            actionFeedbackMessage = msg
          },
          onKickMember = {
            val (ok, msg) = GroupRepository.removeMember(currentGroup.id, memberId, currentUser.uid)
            actionFeedbackMessage = msg
          }
        )
      }
    }

    // Feedback alert
    actionFeedbackMessage?.let { msg ->
      AlertDialog(
        onDismissRequest = { actionFeedbackMessage = null },
        title = { Text("Group Management") },
        text = { Text(msg) },
        confirmButton = {
          Button(onClick = { actionFeedbackMessage = null }) {
            Text("OK")
          }
        }
      )
    }

    // Edit Group Info Dialog (Leader only)
    if (showEditInfoDialog) {
      EditGroupInfoDialog(
        currentName = currentGroup.name,
        currentBio = currentGroup.bio,
        currentPhoto = currentGroup.photoUrl,
        onDismiss = { showEditInfoDialog = false },
        onSave = { name, bio, photo ->
          GroupRepository.updateGroupInfo(currentGroup.id, name, bio, photo, currentUser.uid)
          showEditInfoDialog = false
        }
      )
    }

    // Member profile dialog
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
  }
}

@Composable
private fun MemberRowItem(
  user: User,
  role: GroupRole,
  isSelf: Boolean,
  canManage: Boolean,
  isLeader: Boolean,
  onTapMember: () -> Unit,
  onPromoteElder: () -> Unit,
  onDemoteElder: () -> Unit,
  onKickMember: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onTapMember)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      FFChatAvatar(
        photoUrl = user.photoUrl,
        name = user.name,
        size = 46.dp,
        isOnline = user.isOnline
      )

      Spacer(Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = if (isSelf) "${user.name} (You)" else user.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(Modifier.width(6.dp))
          RoleBadge(role)
        }
        Text(
          text = user.cleanUsername,
          style = MaterialTheme.typography.labelSmall,
          color = FFChatCyan
        )
      }

      // Role management action controls
      if (!isSelf) {
        Row {
          // Leader can promote to Elder or demote Elder
          if (isLeader) {
            if (role == GroupRole.MEMBER) {
              IconButton(
                onClick = onPromoteElder,
                modifier = Modifier.testTag("promote_elder_${user.uid}")
              ) {
                Icon(Icons.Default.Security, contentDescription = "Promote to Elder", tint = FFChatCyan)
              }
            } else if (role == GroupRole.ELDER) {
              IconButton(
                onClick = onDemoteElder,
                modifier = Modifier.testTag("demote_elder_${user.uid}")
              ) {
                Icon(Icons.Default.Shield, contentDescription = "Demote to Member", tint = FFChatOrange)
              }
            }
          }

          // Leader can kick any member/elder; Elder can kick members
          if ((isLeader && role != GroupRole.LEADER) || (!isLeader && canManage && role == GroupRole.MEMBER)) {
            IconButton(
              onClick = onKickMember,
              modifier = Modifier.testTag("kick_member_${user.uid}")
            ) {
              Icon(Icons.Default.PersonRemove, contentDescription = "Remove Member", tint = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun EditGroupInfoDialog(
  currentName: String,
  currentBio: String,
  currentPhoto: String,
  onDismiss: () -> Unit,
  onSave: (String, String, String) -> Unit
) {
  var name by remember { mutableStateOf(currentName) }
  var bio by remember { mutableStateOf(currentBio) }
  var photo by remember { mutableStateOf(currentPhoto) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Group Info") },
    text = {
      Column {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Group Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
          value = bio,
          onValueChange = { bio = it },
          label = { Text("Group Bio") },
          maxLines = 3,
          modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
          value = photo,
          onValueChange = { photo = it },
          label = { Text("Group Photo URL") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(onClick = { onSave(name, bio, photo) }) {
        Text("Save Changes")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
