package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.FFChatGreen

@Composable
fun FFChatAvatar(
  photoUrl: String?,
  name: String,
  size: Dp = 48.dp,
  isOnline: Boolean? = null,
  modifier: Modifier = Modifier
) {
  Box(modifier = modifier.size(size)) {
    if (!photoUrl.isNullOrBlank()) {
      AsyncImage(
        model = photoUrl,
        contentDescription = "$name's avatar",
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .matchParentSize()
          .clip(CircleShape)
          .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), CircleShape)
      )
    } else {
      Box(
        modifier = Modifier
          .matchParentSize()
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Person,
          contentDescription = name,
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(size * 0.6f)
        )
      }
    }

    if (isOnline == true) {
      val badgeSize = (size * 0.28f).coerceAtLeast(10.dp)
      Box(
        modifier = Modifier
          .size(badgeSize)
          .align(Alignment.BottomEnd)
          .clip(CircleShape)
          .background(FFChatGreen)
          .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
      )
    }
  }
}
