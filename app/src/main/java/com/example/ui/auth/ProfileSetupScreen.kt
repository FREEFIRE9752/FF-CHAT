package com.example.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import com.example.repository.UserRepository
import com.example.ui.components.FFChatAvatar
import com.example.ui.theme.FFChatCyan
import com.example.ui.theme.FFChatGreen

@Composable
fun ProfileSetupScreen(
  initialName: String,
  initialEmail: String,
  onSetupComplete: () -> Unit
) {
  var name by remember { mutableStateOf(initialName) }
  var username by remember {
    val seed = initialName.lowercase().replace(" ", "_")
    mutableStateOf(if (seed.isNotBlank()) "@$seed" else "@user")
  }
  var bio by remember { mutableStateOf("Hey there! I am using FF CHAT.") }
  var photoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150") }
  var usernameError by remember { mutableStateOf<String?>(null) }

  val scrollState = rememberScrollState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("profile_setup_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState)
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(Modifier.height(20.dp))

      Text(
        text = "Set Up Your Profile",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(Modifier.height(8.dp))

      Text(
        text = "Choose your display name and unique @username for discovery and messaging.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(Modifier.height(28.dp))

      // Avatar Picker Preview
      Box(contentAlignment = Alignment.BottomEnd) {
        FFChatAvatar(
          photoUrl = photoUrl,
          name = name.ifBlank { "User" },
          size = 110.dp
        )
        IconButton(
          onClick = {
            // Preset avatar switch or URL prompt
            val presets = listOf(
              "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
              "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
              "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
              "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150"
            )
            photoUrl = presets.filter { it != photoUrl }.random()
          },
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(FFChatCyan)
            .testTag("change_avatar_button")
        ) {
          Icon(Icons.Default.PhotoCamera, contentDescription = "Change Avatar", tint = Color.Black, modifier = Modifier.size(18.dp))
        }
      }

      Spacer(Modifier.height(24.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          // Profile Name
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Profile Name") },
            leadingIcon = {
              Icon(Icons.Default.Person, contentDescription = null, tint = FFChatCyan)
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_name_input")
          )

          Spacer(Modifier.height(16.dp))

          // Unique @Username
          OutlinedTextField(
            value = username,
            onValueChange = { input ->
              val formatted = if (input.startsWith("@")) input else "@$input"
              username = formatted
              usernameError = UserRepository.validateUsername(formatted)
            },
            label = { Text("Unique @Username") },
            leadingIcon = {
              Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = FFChatCyan)
            },
            trailingIcon = {
              if (usernameError == null && username.length >= 3) {
                Icon(Icons.Default.Check, contentDescription = "Available", tint = FFChatGreen)
              }
            },
            isError = usernameError != null,
            supportingText = {
              if (usernameError != null) {
                Text(usernameError ?: "", color = MaterialTheme.colorScheme.error)
              } else {
                Text("Searchable across FF CHAT by other users", color = FFChatGreen)
              }
            },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_username_input")
          )

          Spacer(Modifier.height(16.dp))

          // Bio
          OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            maxLines = 3,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_bio_input")
          )

          Spacer(Modifier.height(16.dp))

          // Photo URL
          OutlinedTextField(
            value = photoUrl,
            onValueChange = { photoUrl = it },
            label = { Text("Profile Photo URL") },
            singleLine = true,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("profile_photo_url_input")
          )
        }
      }

      Spacer(Modifier.height(28.dp))

      Button(
        onClick = {
          val error = UserRepository.validateUsername(username)
          if (error != null) {
            usernameError = error
          } else {
            val success = UserRepository.updateProfile(name, username, bio, photoUrl)
            if (success) {
              onSetupComplete()
            } else {
              usernameError = "Failed to update profile. Check username uniqueness."
            }
          }
        },
        enabled = name.isNotBlank() && username.length >= 3 && usernameError == null,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("complete_profile_button"),
        colors = ButtonDefaults.buttonColors(containerColor = FFChatCyan),
        shape = RoundedCornerShape(12.dp)
      ) {
        Text("Complete Profile & Start Chatting", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    }
  }
}
