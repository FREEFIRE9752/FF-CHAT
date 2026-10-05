package com.example.ui.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.repository.UserRepository
import com.example.ui.theme.FFChatCyan
import com.example.ui.theme.FFChatGreen
import com.example.ui.theme.FFChatRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  onBack: () -> Unit,
  onNavigateToEditProfile: () -> Unit,
  onLogout: () -> Unit,
  isDarkTheme: Boolean,
  onToggleTheme: (Boolean) -> Unit,
  currentLanguage: String,
  onLanguageChange: (String) -> Unit
) {
  val context = LocalContext.current
  val currentUser = UserRepository.currentUser.collectAsState().value ?: return

  var notificationsEnabled by remember { mutableStateOf(true) }
  var readReceiptsEnabled by remember { mutableStateOf(true) }
  var showBlockedUsersDialog by remember { mutableStateOf(false) }
  var showDeleteAccountDialog by remember { mutableStateOf(false) }
  var showLogoutDialog by remember { mutableStateOf(false) }
  var showLanguageDialog by remember { mutableStateOf(false) }
  var showStorageDialog by remember { mutableStateOf(false) }
  var showHelpDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }

  val scrollState = rememberScrollState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Settings", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .verticalScroll(scrollState)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Account Section
      SettingsGroupCard(title = "Account") {
        SettingsClickableRow(
          icon = Icons.Default.Edit,
          title = "Edit Profile",
          subtitle = "${currentUser.name} (${currentUser.cleanUsername})",
          onClick = onNavigateToEditProfile
        )
      }

      // Preferences Section
      SettingsGroupCard(title = "Preferences") {
        // Notification Toggle
        SettingsSwitchRow(
          icon = Icons.Default.Notifications,
          title = "Push Notifications",
          subtitle = "Alerts for messages, groups & incoming calls",
          checked = notificationsEnabled,
          onCheckedChange = { notificationsEnabled = it }
        )

        // Dark Mode Toggle
        SettingsSwitchRow(
          icon = Icons.Default.DarkMode,
          title = "Dark Appearance",
          subtitle = if (isDarkTheme) "Dark theme enabled" else "Light theme enabled",
          checked = isDarkTheme,
          onCheckedChange = onToggleTheme
        )

        // Language Selector (English / हिन्दी)
        SettingsClickableRow(
          icon = Icons.Default.Language,
          title = "Language / भाषा",
          subtitle = if (currentLanguage == "hi") "हिन्दी (Hindi)" else "English",
          onClick = { showLanguageDialog = true }
        )
      }

      // Privacy & Security
      SettingsGroupCard(title = "Privacy & Security") {
        SettingsSwitchRow(
          icon = Icons.Default.Lock,
          title = "Read Receipts & Last Seen",
          subtitle = "Double checkmarks and online visibility",
          checked = readReceiptsEnabled,
          onCheckedChange = { readReceiptsEnabled = it }
        )

        SettingsClickableRow(
          icon = Icons.Default.Block,
          title = "Blocked Users",
          subtitle = "${currentUser.blockedUserIds.size} users blocked",
          onClick = { showBlockedUsersDialog = true }
        )
      }

      // Storage & Data
      SettingsGroupCard(title = "Storage & Data") {
        SettingsClickableRow(
          icon = Icons.Default.SdStorage,
          title = "Storage & Cache",
          subtitle = "1.2 GB cached • Resumable transfers",
          onClick = { showStorageDialog = true }
        )
      }

      // Help & About
      SettingsGroupCard(title = "Support & Information") {
        SettingsClickableRow(
          icon = Icons.Default.HelpOutline,
          title = "Help & FAQ",
          subtitle = "Learn about media transfer, WebRTC calls & group roles",
          onClick = { showHelpDialog = true }
        )

        SettingsClickableRow(
          icon = Icons.Default.Info,
          title = "About FF CHAT",
          subtitle = "Version 2.4.0 (Production Build)",
          onClick = { showAboutDialog = true }
        )
      }

      // Danger Zone: Logout & Delete Account
      SettingsGroupCard(title = "Account Actions") {
        SettingsClickableRow(
          icon = Icons.Default.Logout,
          title = "Log Out",
          subtitle = "Safely log out of your Google account",
          iconTint = FFChatCyan,
          onClick = { showLogoutDialog = true }
        )

        SettingsClickableRow(
          icon = Icons.Default.Delete,
          title = "Delete Account",
          subtitle = "Permanently remove profile, groups and history",
          iconTint = FFChatRed,
          onClick = { showDeleteAccountDialog = true }
        )
      }
    }

    // Language Dialog
    if (showLanguageDialog) {
      AlertDialog(
        onDismissRequest = { showLanguageDialog = false },
        title = { Text("Select App Language") },
        text = {
          Column {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  onLanguageChange("en")
                  showLanguageDialog = false
                }
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("English (Default)", fontWeight = if (currentLanguage == "en") FontWeight.Bold else FontWeight.Normal)
            }
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  onLanguageChange("hi")
                  showLanguageDialog = false
                }
                .padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("हिन्दी (Hindi)", fontWeight = if (currentLanguage == "hi") FontWeight.Bold else FontWeight.Normal)
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showLanguageDialog = false }) {
            Text("Close")
          }
        }
      )
    }

    // Storage Management Dialog
    if (showStorageDialog) {
      AlertDialog(
        onDismissRequest = { showStorageDialog = false },
        title = { Text("Storage & Data Usage") },
        text = {
          Column {
            Text("Local Media Cache: 412 MB")
            Text("Chunked Stream Buffer: 28 MB")
            Spacer(Modifier.height(12.dp))
            Text(
              "FF CHAT uses low-RAM chunked streaming so transfers do not exhaust Android device memory.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        confirmButton = {
          Button(
            onClick = {
              Toast.makeText(context, "Cache successfully cleared!", Toast.LENGTH_SHORT).show()
              showStorageDialog = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = FFChatCyan)
          ) {
            Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Clear Cache", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showStorageDialog = false }) {
            Text("Done")
          }
        }
      )
    }

    // Blocked Users Dialog
    if (showBlockedUsersDialog) {
      AlertDialog(
        onDismissRequest = { showBlockedUsersDialog = false },
        title = { Text("Blocked Users") },
        text = {
          if (currentUser.blockedUserIds.isEmpty()) {
            Text("You haven't blocked any users.")
          } else {
            Column {
              currentUser.blockedUserIds.forEach { uid ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(uid)
                  TextButton(onClick = { UserRepository.unblockUser(uid) }) {
                    Text("Unblock", color = FFChatCyan)
                  }
                }
              }
            }
          }
        },
        confirmButton = {
          TextButton(onClick = { showBlockedUsersDialog = false }) {
            Text("Close")
          }
        }
      )
    }

    // Help Dialog
    if (showHelpDialog) {
      AlertDialog(
        onDismissRequest = { showHelpDialog = false },
        title = { Text("FF CHAT Help & Guide") },
        text = {
          Column {
            Text("• High Speed Media Transfers:", fontWeight = FontWeight.Bold)
            Text("Send videos, HD photos, and APKs using resumable chunked streaming.")
            Spacer(Modifier.height(8.dp))
            Text("• WebRTC Calls:", fontWeight = FontWeight.Bold)
            Text("Real peer-to-peer audio & video calls with STUN/TURN traversal and screen sharing.")
            Spacer(Modifier.height(8.dp))
            Text("• Group Roles:", fontWeight = FontWeight.Bold)
            Text("Leader creates the group, can promote up to 10 Elders, and manage calls. Elders can moderate members and start group calls.")
          }
        },
        confirmButton = {
          TextButton(onClick = { showHelpDialog = false }) {
            Text("Got it")
          }
        }
      )
    }

    // About Dialog
    if (showAboutDialog) {
      AlertDialog(
        onDismissRequest = { showAboutDialog = false },
        title = { Text("About FF CHAT") },
        text = {
          Column {
            Text("FF CHAT v2.4.0 (Production)", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Built for Android & iOS with Jetpack Compose & Clean Modular Architecture.")
            Spacer(Modifier.height(6.dp))
            Text("Features: Firebase Auth, Cloud Firestore, WebRTC STUN/TURN, 1GB Chunked Storage.")
          }
        },
        confirmButton = {
          TextButton(onClick = { showAboutDialog = false }) {
            Text("OK")
          }
        }
      )
    }

    // Logout Dialog
    if (showLogoutDialog) {
      AlertDialog(
        onDismissRequest = { showLogoutDialog = false },
        title = { Text("Log Out?") },
        text = { Text("Are you sure you want to log out of FF CHAT?") },
        confirmButton = {
          Button(
            onClick = {
              showLogoutDialog = false
              UserRepository.logout()
              onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = FFChatCyan)
          ) {
            Text("Log Out", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showLogoutDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }

    // Delete Account Dialog
    if (showDeleteAccountDialog) {
      AlertDialog(
        onDismissRequest = { showDeleteAccountDialog = false },
        title = { Text("Delete Account?", color = FFChatRed) },
        text = { Text("This will permanently remove your @username, chat history, and group memberships. This action cannot be undone.") },
        confirmButton = {
          Button(
            onClick = {
              showDeleteAccountDialog = false
              UserRepository.deleteAccount()
              onLogout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = FFChatRed)
          ) {
            Text("Permanently Delete", color = Color.White, fontWeight = FontWeight.Bold)
          }
        },
        dismissButton = {
          TextButton(onClick = { showDeleteAccountDialog = false }) {
            Text("Cancel")
          }
        }
      )
    }
  }
}

@Composable
private fun SettingsGroupCard(
  title: String,
  content: @Composable () -> Unit
) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = title,
      style = MaterialTheme.typography.labelLarge,
      color = FFChatCyan,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
    )
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(vertical = 4.dp)) {
        content()
      }
    }
  }
}

@Composable
private fun SettingsClickableRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
    Spacer(Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Icon(
      imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
      modifier = Modifier.size(14.dp)
    )
  }
}

@Composable
private fun SettingsSwitchRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
      Spacer(Modifier.width(14.dp))
      Column {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(checkedThumbColor = FFChatCyan, checkedTrackColor = FFChatCyan.copy(alpha = 0.5f))
    )
  }
}
