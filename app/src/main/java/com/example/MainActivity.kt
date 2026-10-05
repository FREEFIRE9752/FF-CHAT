package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.model.CallSession
import com.example.model.CallType
import com.example.model.Group
import com.example.model.User
import com.example.repository.CallRepository
import com.example.repository.GroupRepository
import com.example.repository.UserRepository
import com.example.service.NotificationService
import com.example.ui.auth.LoginScreen
import com.example.ui.auth.ProfileSetupScreen
import com.example.ui.calls.CallScreen
import com.example.ui.chat.ChatListScreen
import com.example.ui.chat.DirectChatScreen
import com.example.ui.components.FFChatBottomBar
import com.example.ui.components.FFChatMainHeader
import com.example.ui.components.MainTab
import com.example.ui.groups.GroupChatScreen
import com.example.ui.groups.GroupInfoScreen
import com.example.ui.groups.GroupListScreen
import com.example.ui.profile.EditProfileScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.FFChatTheme

sealed class AppScreen {
  object Main : AppScreen()
  data class DirectChat(val targetUser: User) : AppScreen()
  data class GroupChat(val group: Group) : AppScreen()
  data class GroupInfo(val group: Group) : AppScreen()
  object EditProfile : AppScreen()
  object Settings : AppScreen()
}

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    NotificationService.initChannels(this)

    // Check deep links (e.g. ffchat://join?code=XYZ)
    val inviteCode = intent?.data?.getQueryParameter("code")

    setContent {
      val systemDark = isSystemInDarkTheme()
      var isDarkTheme by remember { mutableStateOf(systemDark) }
      var currentLanguage by remember { mutableStateOf("en") }

      val currentUser by UserRepository.currentUser.collectAsState()
      val needsProfileSetup by UserRepository.needsProfileSetup.collectAsState()
      val activeCallSession by CallRepository.activeCall.collectAsState()

      var currentTab by remember { mutableStateOf(MainTab.CHAT) }
      var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.Main) }

      // Handle deep link invite code on startup
      LaunchedEffect(inviteCode) {
        if (!inviteCode.isNullOrBlank() && currentUser != null) {
          GroupRepository.joinGroupByInviteCode(inviteCode, currentUser!!)
        }
      }

      FFChatTheme(darkTheme = isDarkTheme) {
        // If WebRTC call is active, show the fullscreen CallScreen
        if (activeCallSession != null) {
          CallScreen(
            session = activeCallSession!!,
            onEndCall = {
              CallRepository.endCall()
            }
          )
        } else if (currentUser == null) {
          // Logged out: show Login
          LoginScreen(
            onGoogleSignInSuccess = { name, email ->
              UserRepository.loginWithGoogle(name, email, null)
            }
          )
        } else if (needsProfileSetup) {
          // First login profile setup
          ProfileSetupScreen(
            initialName = currentUser?.name ?: "",
            initialEmail = "",
            onSetupComplete = {
              currentScreen = AppScreen.Main
            }
          )
        } else {
          // Main authenticated application flow
          when (val screen = currentScreen) {
            is AppScreen.Main -> {
              Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                  val title = when (currentTab) {
                    MainTab.CHAT -> if (currentLanguage == "hi") "FF चैट" else "FF CHAT"
                    MainTab.GROUP -> if (currentLanguage == "hi") "ग्रुप्स" else "Groups"
                    MainTab.PROFILE -> if (currentLanguage == "hi") "मेरी प्रोफ़ाइल" else "My Profile"
                  }
                  FFChatMainHeader(
                    title = title,
                    onOpenSettings = { currentScreen = AppScreen.Settings }
                  )
                },
                bottomBar = {
                  FFChatBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    language = currentLanguage
                  )
                }
              ) { paddingValues ->
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                ) {
                  when (currentTab) {
                    MainTab.CHAT -> {
                      ChatListScreen(
                        onOpenDirectChat = { user ->
                          currentScreen = AppScreen.DirectChat(user)
                        },
                        onStartVoiceCall = { user ->
                          CallRepository.startDirectCall(currentUser!!, user, CallType.VOICE)
                        },
                        onStartVideoCall = { user ->
                          CallRepository.startDirectCall(currentUser!!, user, CallType.VIDEO)
                        }
                      )
                    }
                    MainTab.GROUP -> {
                      GroupListScreen(
                        onOpenGroupChat = { group ->
                          currentScreen = AppScreen.GroupChat(group)
                        },
                        onJoinActiveCall = { group ->
                          CallRepository.joinGroupCall(currentUser!!, group.id)
                        }
                      )
                    }
                    MainTab.PROFILE -> {
                      ProfileScreen(
                        onNavigateToEditProfile = { currentScreen = AppScreen.EditProfile },
                        onNavigateToSettings = { currentScreen = AppScreen.Settings }
                      )
                    }
                  }
                }
              }
            }

            is AppScreen.DirectChat -> {
              BackHandler {
                currentScreen = AppScreen.Main
              }
              DirectChatScreen(
                targetUser = screen.targetUser,
                onBack = { currentScreen = AppScreen.Main },
                onStartVoiceCall = {
                  CallRepository.startDirectCall(currentUser!!, screen.targetUser, CallType.VOICE)
                },
                onStartVideoCall = {
                  CallRepository.startDirectCall(currentUser!!, screen.targetUser, CallType.VIDEO)
                },
                onViewProfile = {
                  // Direct view or profile info
                }
              )
            }

            is AppScreen.GroupChat -> {
              BackHandler {
                currentScreen = AppScreen.Main
              }
              GroupChatScreen(
                group = screen.group,
                onBack = { currentScreen = AppScreen.Main },
                onOpenGroupInfo = { grp ->
                  currentScreen = AppScreen.GroupInfo(grp)
                },
                onOpenDirectChat = { user ->
                  currentScreen = AppScreen.DirectChat(user)
                },
                onStartGroupCall = { grp, isVideo ->
                  CallRepository.startGroupCall(currentUser!!, grp.id, if (isVideo) CallType.VIDEO else CallType.VOICE)
                },
                onJoinActiveCall = { grp ->
                  CallRepository.joinGroupCall(currentUser!!, grp.id)
                }
              )
            }

            is AppScreen.GroupInfo -> {
              BackHandler {
                currentScreen = AppScreen.GroupChat(screen.group)
              }
              GroupInfoScreen(
                group = screen.group,
                onBack = { currentScreen = AppScreen.GroupChat(screen.group) },
                onOpenDirectChat = { user ->
                  currentScreen = AppScreen.DirectChat(user)
                }
              )
            }

            is AppScreen.EditProfile -> {
              BackHandler {
                currentScreen = AppScreen.Main
              }
              EditProfileScreen(
                onBack = { currentScreen = AppScreen.Main }
              )
            }

            is AppScreen.Settings -> {
              BackHandler {
                currentScreen = AppScreen.Main
              }
              SettingsScreen(
                onBack = { currentScreen = AppScreen.Main },
                onNavigateToEditProfile = { currentScreen = AppScreen.EditProfile },
                onLogout = {
                  currentScreen = AppScreen.Main
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = { isDarkTheme = it },
                currentLanguage = currentLanguage,
                onLanguageChange = { currentLanguage = it }
              )
            }
          }
        }
      }
    }
  }
}
