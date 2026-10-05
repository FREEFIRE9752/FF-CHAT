package com.example.repository

import com.example.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object UserRepository {

  // Seeded directory of searchable users for real-time username search
  private val allRegisteredUsers = mutableListOf(
    User(
      uid = "user_rohit",
      name = "Rohit Sharma",
      username = "@rohit_ff",
      photoUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
      bio = "Tech enthusiast & Android developer 🚀",
      isOnline = true,
      lastSeen = System.currentTimeMillis()
    ),
    User(
      uid = "user_priya",
      name = "Priya Patel",
      username = "@priya_design",
      photoUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150",
      bio = "UI/UX Designer | Coffee Lover ☕",
      isOnline = true,
      lastSeen = System.currentTimeMillis() - 1000 * 60 * 5
    ),
    User(
      uid = "user_arav",
      name = "Aarav Mehta",
      username = "@aarav_gamer",
      photoUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=150",
      bio = "Competitive gamer & tech reviewer 🎮",
      isOnline = false,
      lastSeen = System.currentTimeMillis() - 1000 * 60 * 45
    ),
    User(
      uid = "user_ananya",
      name = "Ananya Singh",
      username = "@ananya_s",
      photoUrl = "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=150",
      bio = "Photographer & travel blogger 📸",
      isOnline = true,
      lastSeen = System.currentTimeMillis()
    ),
    User(
      uid = "user_vikram",
      name = "Vikram Reddy",
      username = "@vikram_dev",
      photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
      bio = "Open-source contributor & cloud architect",
      isOnline = false,
      lastSeen = System.currentTimeMillis() - 1000 * 60 * 120
    )
  )

  // Initial logged in user
  private val _currentUser = MutableStateFlow<User?>(
    User(
      uid = "current_user_me",
      name = "Ganesh Maurya",
      username = "@ganesh_m",
      photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150",
      bio = "Building high-performance Android apps with Jetpack Compose ⚡",
      isOnline = true
    )
  )
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  private val _needsProfileSetup = MutableStateFlow(false)
  val needsProfileSetup: StateFlow<Boolean> = _needsProfileSetup.asStateFlow()

  fun isUsernameTaken(username: String, excludeUid: String? = null): Boolean {
    val clean = if (username.startsWith("@")) username.lowercase() else "@${username.lowercase()}"
    return allRegisteredUsers.any { it.cleanUsername == clean && it.uid != excludeUid }
  }

  fun validateUsername(username: String): String? {
    val trimmed = username.trim()
    val clean = if (trimmed.startsWith("@")) trimmed.substring(1) else trimmed
    if (clean.length < 3) return "Username must be at least 3 characters"
    if (clean.length > 25) return "Username cannot exceed 25 characters"
    val regex = Regex("^[a-zA-Z0-9_]+$")
    if (!regex.matches(clean)) return "Only letters, numbers, and underscores allowed"
    if (isUsernameTaken(clean, _currentUser.value?.uid)) return "This @username is already taken"
    return null
  }

  fun searchByUsername(query: String): List<User> {
    val cleanQuery = query.trim().removePrefix("@").lowercase()
    if (cleanQuery.isEmpty()) return emptyList()
    val myUid = _currentUser.value?.uid
    return allRegisteredUsers.filter { user ->
      user.uid != myUid && (
        user.cleanUsername.removePrefix("@").contains(cleanQuery) ||
          user.name.lowercase().contains(cleanQuery)
      )
    }
  }

  fun getUserById(uid: String): User? {
    if (uid == _currentUser.value?.uid) return _currentUser.value
    return allRegisteredUsers.find { it.uid == uid }
  }

  fun updateProfile(name: String, username: String, bio: String, photoUrl: String): Boolean {
    val error = validateUsername(username)
    if (error != null) return false

    val clean = if (username.startsWith("@")) username.lowercase() else "@${username.lowercase()}"
    val current = _currentUser.value ?: return false
    val updated = current.copy(
      name = name.trim(),
      username = clean,
      bio = bio.trim(),
      photoUrl = photoUrl
    )
    _currentUser.value = updated

    // Update in directory
    val index = allRegisteredUsers.indexOfFirst { it.uid == current.uid }
    if (index != -1) {
      allRegisteredUsers[index] = updated
    } else {
      allRegisteredUsers.add(updated)
    }
    _needsProfileSetup.value = false
    return true
  }

  fun loginWithGoogle(accountName: String, accountEmail: String, photoUrl: String?) {
    // Generates or loads user
    val existing = allRegisteredUsers.find { it.name.equals(accountName, ignoreCase = true) }
    if (existing != null) {
      _currentUser.value = existing
      _needsProfileSetup.value = false
    } else {
      val generatedUsername = "@${accountName.lowercase().replace(" ", "_")}"
      val newUser = User(
        uid = "uid_${System.currentTimeMillis()}",
        name = accountName,
        username = generatedUsername,
        photoUrl = photoUrl ?: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150",
        bio = "Hey there! I am using FF CHAT."
      )
      _currentUser.value = newUser
      _needsProfileSetup.value = true // First login triggers Profile Setup!
    }
  }

  fun logout() {
    _currentUser.value = null
  }

  fun blockUser(targetUserId: String) {
    val current = _currentUser.value ?: return
    val updatedList = current.blockedUserIds.toMutableList()
    if (!updatedList.contains(targetUserId)) {
      updatedList.add(targetUserId)
      _currentUser.value = current.copy(blockedUserIds = updatedList)
    }
  }

  fun unblockUser(targetUserId: String) {
    val current = _currentUser.value ?: return
    val updatedList = current.blockedUserIds.filter { it != targetUserId }
    _currentUser.value = current.copy(blockedUserIds = updatedList)
  }

  fun deleteAccount() {
    val current = _currentUser.value ?: return
    allRegisteredUsers.removeAll { it.uid == current.uid }
    _currentUser.value = null
  }

  // Profile Photos and Videos Uploads
  private val _myProfileMedia = MutableStateFlow<List<com.example.model.ProfileMedia>>(
    listOf(
      com.example.model.ProfileMedia(
        id = "med_1",
        userId = "current_user_me",
        url = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
        isVideo = false,
        title = "AI & Architecture Setup 💻"
      ),
      com.example.model.ProfileMedia(
        id = "med_2",
        userId = "current_user_me",
        url = "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=800",
        isVideo = true,
        title = "4K WebRTC Live Demo 🎥"
      ),
      com.example.model.ProfileMedia(
        id = "med_3",
        userId = "current_user_me",
        url = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
        isVideo = false,
        title = "Travel Memories 🌊"
      )
    )
  )
  val myProfileMedia: StateFlow<List<com.example.model.ProfileMedia>> = _myProfileMedia.asStateFlow()

  fun uploadProfileMedia(url: String, isVideo: Boolean, title: String) {
    val newMedia = com.example.model.ProfileMedia(
      id = "med_${System.currentTimeMillis()}",
      userId = _currentUser.value?.uid ?: "current_user_me",
      url = url,
      isVideo = isVideo,
      title = title.ifBlank { if (isVideo) "Video Post" else "Photo Post" }
    )
    _myProfileMedia.value = listOf(newMedia) + _myProfileMedia.value
  }

  fun removeProfileMedia(mediaId: String) {
    _myProfileMedia.value = _myProfileMedia.value.filter { it.id != mediaId }
  }
}
