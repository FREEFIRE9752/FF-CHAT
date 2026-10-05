package com.example.ui.profile

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.ProfileMedia
import com.example.repository.UserRepository
import com.example.ui.components.FFChatAvatar
import com.example.ui.theme.FFChatCyan
import com.example.ui.theme.FFChatViolet

@Composable
fun ProfileScreen(
  onNavigateToEditProfile: () -> Unit,
  onNavigateToSettings: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val currentUser = UserRepository.currentUser.collectAsState().value ?: return
  val profileMediaList by UserRepository.myProfileMedia.collectAsState()

  var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Photos, 2: Videos
  var showUploadDialog by remember { mutableStateOf(false) }
  var uploadIsVideo by remember { mutableStateOf(false) }
  var previewMediaItem by remember { mutableStateOf<ProfileMedia?>(null) }

  val filteredMedia = remember(profileMediaList, selectedTab) {
    when (selectedTab) {
      1 -> profileMediaList.filter { !it.isVideo }
      2 -> profileMediaList.filter { it.isVideo }
      else -> profileMediaList
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .testTag("profile_screen")
  ) {
    // Top Bar Actions
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.End
    ) {
      IconButton(
        onClick = onNavigateToSettings,
        modifier = Modifier.testTag("open_settings_button")
      ) {
        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
      }
    }

    // Profile Details Card
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Profile Avatar
      FFChatAvatar(
        photoUrl = currentUser.photoUrl,
        name = currentUser.name,
        size = 96.dp,
        isOnline = true
      )

      Spacer(Modifier.height(12.dp))

      Text(
        text = currentUser.name,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(Modifier.height(4.dp))

      // Username with quick copy
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
          .padding(horizontal = 12.dp, vertical = 4.dp)
      ) {
        Text(
          text = currentUser.cleanUsername,
          style = MaterialTheme.typography.titleMedium,
          color = FFChatCyan,
          fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.width(6.dp))
        IconButton(
          onClick = {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Username", currentUser.cleanUsername))
            Toast.makeText(context, "${currentUser.cleanUsername} copied to clipboard", Toast.LENGTH_SHORT).show()
          },
          modifier = Modifier.size(24.dp)
        ) {
          Icon(
            imageVector = Icons.Default.ContentCopy,
            contentDescription = "Copy username",
            tint = FFChatCyan,
            modifier = Modifier.size(16.dp)
          )
        }
      }

      Spacer(Modifier.height(8.dp))

      Text(
        text = currentUser.bio,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(Modifier.height(16.dp))

      // Buttons Row: Edit Profile & Media Upload Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = onNavigateToEditProfile,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .height(42.dp)
            .testTag("edit_profile_button")
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Edit Profile")
        }

        Button(
          onClick = {
            uploadIsVideo = false
            showUploadDialog = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = FFChatCyan),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .height(42.dp)
            .testTag("upload_photo_button")
        ) {
          Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Photo", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = {
            uploadIsVideo = true
            showUploadDialog = true
          },
          colors = ButtonDefaults.buttonColors(containerColor = FFChatViolet),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .weight(1f)
            .height(42.dp)
            .testTag("upload_video_button")
        ) {
          Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text("Video", color = Color.White, fontWeight = FontWeight.Bold)
        }
      }
    }

    Spacer(Modifier.height(16.dp))

    // Media Filter Tabs (All, Photos, Videos)
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.background,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = FFChatCyan
        )
      }
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("All (${profileMediaList.size})") }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Photos (${profileMediaList.count { !it.isVideo }})") }
      )
      Tab(
        selected = selectedTab == 2,
        onClick = { selectedTab = 2 },
        text = { Text("Videos (${profileMediaList.count { it.isVideo }})") }
      )
    }

    // Photos & Videos Grid
    if (filteredMedia.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = if (selectedTab == 2) Icons.Default.Videocam else Icons.Default.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(56.dp)
          )
          Spacer(Modifier.height(12.dp))
          Text(
            text = if (selectedTab == 2) "No videos uploaded yet" else "No photos uploaded yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(Modifier.height(6.dp))
          Text(
            text = "Tap Photo or Video above to share moments on your profile!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }
      }
    } else {
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .fillMaxSize()
          .padding(12.dp)
          .testTag("profile_media_grid"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filteredMedia, key = { it.id }) { item ->
          ProfileMediaCard(
            item = item,
            onClick = { previewMediaItem = item },
            onDelete = { UserRepository.removeProfileMedia(item.id) }
          )
        }
      }
    }

    // Upload Media Dialog
    if (showUploadDialog) {
      UploadMediaDialog(
        isVideo = uploadIsVideo,
        onDismiss = { showUploadDialog = false },
        onUpload = { url, isVid, title ->
          UserRepository.uploadProfileMedia(url, isVid, title)
          showUploadDialog = false
          Toast.makeText(context, "${if (isVid) "Video" else "Photo"} uploaded to your profile!", Toast.LENGTH_SHORT).show()
        }
      )
    }

    // Media Preview Dialog
    previewMediaItem?.let { media ->
      ProfileMediaViewerDialog(
        media = media,
        onDismiss = { previewMediaItem = null },
        onDelete = {
          UserRepository.removeProfileMedia(media.id)
          previewMediaItem = null
        }
      )
    }
  }
}

@Composable
private fun ProfileMediaCard(
  item: ProfileMedia,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .aspectRatio(1f)
      .clip(RoundedCornerShape(14.dp))
      .clickable(onClick = onClick)
      .testTag("media_card_${item.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      AsyncImage(
        model = item.url,
        contentDescription = item.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier.matchParentSize()
      )

      // Video play indicator
      if (item.isVideo) {
        Box(
          modifier = Modifier
            .matchParentSize()
            .background(Color.Black.copy(alpha = 0.25f)),
          contentAlignment = Alignment.Center
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(FFChatCyan.copy(alpha = 0.85f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.Black, modifier = Modifier.size(24.dp))
          }
        }
      }

      // Title banner at bottom
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.BottomCenter)
          .background(Color.Black.copy(alpha = 0.6f))
          .padding(horizontal = 8.dp, vertical = 6.dp)
      ) {
        Text(
          text = item.title,
          style = MaterialTheme.typography.labelSmall,
          color = Color.White,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
private fun UploadMediaDialog(
  isVideo: Boolean,
  onDismiss: () -> Unit,
  onUpload: (url: String, isVideo: Boolean, title: String) -> Unit
) {
  val presetPhotos = listOf(
    "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800",
    "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
    "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800",
    "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800"
  )

  val presetVideos = listOf(
    "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?w=800",
    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800"
  )

  var selectedUrl by remember { mutableStateOf(if (isVideo) presetVideos.first() else presetPhotos.first()) }
  var title by remember { mutableStateOf("") }
  var isVideoSelected by remember { mutableStateOf(isVideo) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (isVideoSelected) "Upload Profile Video 🎥" else "Upload Profile Photo 📸",
        fontWeight = FontWeight.Bold
      )
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        Text(
          text = "Select from presets or paste a custom media URL to add to your profile gallery.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))

        // Toggle Type
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = {
              isVideoSelected = false
              selectedUrl = presetPhotos.first()
            },
            modifier = Modifier.weight(1f),
            colors = if (!isVideoSelected) ButtonDefaults.outlinedButtonColors(containerColor = FFChatCyan.copy(alpha = 0.2f)) else ButtonDefaults.outlinedButtonColors()
          ) {
            Text("Photo", color = if (!isVideoSelected) FFChatCyan else MaterialTheme.colorScheme.onSurface)
          }

          OutlinedButton(
            onClick = {
              isVideoSelected = true
              selectedUrl = presetVideos.first()
            },
            modifier = Modifier.weight(1f),
            colors = if (isVideoSelected) ButtonDefaults.outlinedButtonColors(containerColor = FFChatViolet.copy(alpha = 0.2f)) else ButtonDefaults.outlinedButtonColors()
          ) {
            Text("Video", color = if (isVideoSelected) FFChatViolet else MaterialTheme.colorScheme.onSurface)
          }
        }

        Spacer(Modifier.height(12.dp))

        // Preset Thumbnails Selector
        Text("Quick Presets:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val list = if (isVideoSelected) presetVideos else presetPhotos
          list.forEach { url ->
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable { selectedUrl = url }
            ) {
              AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
              )
              if (selectedUrl == url) {
                Box(
                  modifier = Modifier
                    .matchParentSize()
                    .background(FFChatCyan.copy(alpha = 0.4f))
                )
              }
            }
          }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Caption / Title") },
          placeholder = { Text(if (isVideoSelected) "My new video post" else "Sunset vibes") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("upload_media_title_input")
        )

        Spacer(Modifier.height(10.dp))

        OutlinedTextField(
          value = selectedUrl,
          onValueChange = { selectedUrl = it },
          label = { Text("Media URL") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("upload_media_url_input")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (selectedUrl.isNotBlank()) {
            onUpload(selectedUrl.trim(), isVideoSelected, title.trim())
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = FFChatCyan),
        modifier = Modifier.testTag("confirm_upload_button")
      ) {
        Text("Upload", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}

@Composable
private fun ProfileMediaViewerDialog(
  media: ProfileMedia,
  onDismiss: () -> Unit,
  onDelete: () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxSize()
        .testTag("profile_media_viewer"),
      color = Color.Black.copy(alpha = 0.95f)
    ) {
      Box(modifier = Modifier.fillMaxSize()) {
        // Top Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .align(Alignment.TopCenter),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = media.title,
              style = MaterialTheme.typography.titleMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = if (media.isVideo) "Video Post" else "Photo Post",
              style = MaterialTheme.typography.labelSmall,
              color = FFChatCyan
            )
          }

          Row {
            IconButton(
              onClick = onDelete,
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .testTag("delete_media_button")
            ) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
            }
            Spacer(Modifier.width(8.dp))
            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
          }
        }

        // Center View
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 70.dp),
          contentAlignment = Alignment.Center
        ) {
          if (media.isVideo) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp)),
              contentAlignment = Alignment.Center
            ) {
              AsyncImage(
                model = media.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
              )
              Box(
                modifier = Modifier
                  .size(68.dp)
                  .clip(CircleShape)
                  .background(FFChatCyan),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(40.dp))
              }
            }
          } else {
            AsyncImage(
              model = media.url,
              contentDescription = media.title,
              contentScale = ContentScale.Fit,
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            )
          }
        }
      }
    }
  }
}
