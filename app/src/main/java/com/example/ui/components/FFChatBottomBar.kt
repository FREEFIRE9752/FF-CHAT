package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.FFChatCyan

import androidx.compose.ui.unit.dp

enum class MainTab {
  CHAT,
  GROUP,
  PROFILE
}

@Composable
fun FFChatBottomBar(
  currentTab: MainTab,
  onTabSelected: (MainTab) -> Unit,
  language: String,
  modifier: Modifier = Modifier
) {
  val chatLabel = if (language == "hi") "चैट" else "Chat"
  val groupLabel = if (language == "hi") "ग्रुप" else "Group"
  val profileLabel = if (language == "hi") "प्रोफ़ाइल" else "Profile"

  NavigationBar(
    modifier = modifier.testTag("main_bottom_nav"),
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp
  ) {
    NavigationBarItem(
      selected = currentTab == MainTab.CHAT,
      onClick = { onTabSelected(MainTab.CHAT) },
      icon = {
        Icon(
          imageVector = if (currentTab == MainTab.CHAT) Icons.Filled.ChatBubble else Icons.Outlined.ChatBubbleOutline,
          contentDescription = chatLabel
        )
      },
      label = { Text(chatLabel, fontWeight = if (currentTab == MainTab.CHAT) FontWeight.Bold else FontWeight.Normal) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.Black,
        selectedTextColor = FFChatCyan,
        indicatorColor = FFChatCyan,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
      ),
      modifier = Modifier.testTag("nav_tab_chat")
    )

    NavigationBarItem(
      selected = currentTab == MainTab.GROUP,
      onClick = { onTabSelected(MainTab.GROUP) },
      icon = {
        Icon(
          imageVector = if (currentTab == MainTab.GROUP) Icons.Filled.Groups else Icons.Outlined.Groups,
          contentDescription = groupLabel
        )
      },
      label = { Text(groupLabel, fontWeight = if (currentTab == MainTab.GROUP) FontWeight.Bold else FontWeight.Normal) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.Black,
        selectedTextColor = FFChatCyan,
        indicatorColor = FFChatCyan,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
      ),
      modifier = Modifier.testTag("nav_tab_group")
    )

    NavigationBarItem(
      selected = currentTab == MainTab.PROFILE,
      onClick = { onTabSelected(MainTab.PROFILE) },
      icon = {
        Icon(
          imageVector = if (currentTab == MainTab.PROFILE) Icons.Filled.Person else Icons.Outlined.PersonOutline,
          contentDescription = profileLabel
        )
      },
      label = { Text(profileLabel, fontWeight = if (currentTab == MainTab.PROFILE) FontWeight.Bold else FontWeight.Normal) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Color.Black,
        selectedTextColor = FFChatCyan,
        indicatorColor = FFChatCyan,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
      ),
      modifier = Modifier.testTag("nav_tab_profile")
    )
  }
}
