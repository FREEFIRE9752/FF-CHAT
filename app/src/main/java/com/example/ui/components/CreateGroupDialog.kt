package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FFChatCyan

@Composable
fun CreateGroupDialog(
  onDismiss: () -> Unit,
  onCreateGroup: (name: String, bio: String, photoUrl: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var bio by remember { mutableStateOf("") }
  var photoUrl by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(Icons.Default.GroupAdd, contentDescription = null, tint = FFChatCyan)
    },
    title = {
      Text("Create New Group", fontWeight = FontWeight.Bold)
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "As the creator, you will automatically be assigned as Leader 👑 with full elder and member management controls.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
          value = name,
          onValueChange = {
            name = it
            errorText = null
          },
          label = { Text("Group Name *") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("create_group_name_input"),
          isError = errorText != null
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
          value = bio,
          onValueChange = { bio = it },
          label = { Text("Group Bio") },
          maxLines = 3,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("create_group_bio_input")
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
          value = photoUrl,
          onValueChange = { photoUrl = it },
          label = { Text("Group Photo URL (optional)") },
          placeholder = { Text("https://...") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("create_group_photo_input")
        )

        if (errorText != null) {
          Spacer(Modifier.height(8.dp))
          Text(
            text = errorText ?: "",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.trim().isBlank()) {
            errorText = "Group name is required"
          } else {
            onCreateGroup(name.trim(), bio.trim(), photoUrl.trim())
            onDismiss()
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = FFChatCyan),
        modifier = Modifier.testTag("confirm_create_group_button")
      ) {
        Text("Create Group", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
    shape = RoundedCornerShape(20.dp)
  )
}
