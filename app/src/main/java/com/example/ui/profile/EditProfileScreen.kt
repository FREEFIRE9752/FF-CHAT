package com.example.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.repository.UserRepository
import com.example.ui.components.FFChatAvatar
import com.example.ui.theme.FFChatCyan
import com.example.ui.theme.FFChatGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
  onBack: () -> Unit
) {
  val currentUser = UserRepository.currentUser.collectAsState().value ?: return

  var name by remember { mutableStateOf(currentUser.name) }
  var username by remember { mutableStateOf(currentUser.cleanUsername) }
  var bio by remember { mutableStateOf(currentUser.bio) }
  var photoUrl by remember { mutableStateOf(currentUser.photoUrl) }
  var usernameError by remember { mutableStateOf<String?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          Button(
            onClick = {
              val error = UserRepository.validateUsername(username)
              if (error != null) {
                usernameError = error
              } else {
                val ok = UserRepository.updateProfile(name, username, bio, photoUrl)
                if (ok) onBack() else usernameError = "Username is already taken"
              }
            },
            enabled = name.isNotBlank() && username.length >= 3 && usernameError == null,
            colors = ButtonDefaults.buttonColors(containerColor = FFChatCyan),
            modifier = Modifier
              .padding(end = 8.dp)
              .testTag("save_profile_button")
          ) {
            Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(contentAlignment = Alignment.BottomEnd) {
        FFChatAvatar(
          photoUrl = photoUrl,
          name = name,
          size = 100.dp
        )
        IconButton(
          onClick = {
            val presets = listOf(
              "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
              "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
              "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
              "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150"
            )
            photoUrl = presets.filter { it != photoUrl }.random()
          },
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(FFChatCyan)
        ) {
          Icon(Icons.Default.PhotoCamera, contentDescription = "Pick Photo", tint = Color.Black, modifier = Modifier.size(18.dp))
        }
      }

      Spacer(Modifier.height(24.dp))

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display Name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = FFChatCyan) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(Modifier.height(14.dp))

          OutlinedTextField(
            value = username,
            onValueChange = { input ->
              val formatted = if (input.startsWith("@")) input else "@$input"
              username = formatted
              usernameError = UserRepository.validateUsername(formatted)
            },
            label = { Text("Unique @Username") },
            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null, tint = FFChatCyan) },
            trailingIcon = {
              if (usernameError == null && username.length >= 3) {
                Icon(Icons.Default.Check, contentDescription = null, tint = FFChatGreen)
              }
            },
            isError = usernameError != null,
            supportingText = {
              if (usernameError != null) Text(usernameError ?: "", color = MaterialTheme.colorScheme.error)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(Modifier.height(14.dp))

          OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("Bio") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
          )

          Spacer(Modifier.height(14.dp))

          OutlinedTextField(
            value = photoUrl,
            onValueChange = { photoUrl = it },
            label = { Text("Photo URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    }
  }
}
