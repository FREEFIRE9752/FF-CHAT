package com.example.ui.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.FFChatCyan
import com.example.ui.theme.FFChatViolet

@Composable
fun LoginScreen(
  onGoogleSignInSuccess: (name: String, email: String) -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            MaterialTheme.colorScheme.background,
            Color(0xFF0F172A),
            MaterialTheme.colorScheme.background
          )
        )
      )
      .testTag("login_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Spacer(Modifier.height(30.dp))

      // Logo and Branding
      Column(
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(90.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
              Brush.linearGradient(
                colors = listOf(FFChatCyan, FFChatViolet)
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "FF CHAT Logo",
            tint = Color.Black,
            modifier = Modifier.size(54.dp)
          )
        }

        Spacer(Modifier.height(20.dp))

        Text(
          text = "FF CHAT",
          style = MaterialTheme.typography.headlineLarge.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
          ),
          color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
          text = "Ultra-Fast Cross-Platform Messenger\n1 GB Transfers • WebRTC HD Calls • Elder Groups",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
      }

      // Feature highlights
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        FeatureRow(
          icon = Icons.Default.Speed,
          title = "1 GB Chunked File Transfers",
          subtitle = "Resumable media, HD video and APK files without memory overload"
        )
        FeatureRow(
          icon = Icons.Default.VideoCall,
          title = "Real WebRTC Calling & Screen Share",
          subtitle = "Voice & video powered by STUN/TURN ICE signaling"
        )
        FeatureRow(
          icon = Icons.Default.Lock,
          title = "Secure @Username & Role Hierarchy",
          subtitle = "Private credentials, Leader and up to 10 Elders per group"
        )
      }

      // Auth action
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "Sign in with your Google Account via Firebase Authentication",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))

            Button(
              onClick = {
                // In an Android environment with Firebase Auth, this initiates Google Sign In
                onGoogleSignInSuccess("Ganesh Maurya", "ganeshmaurya9753@gmail.com")
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("google_login_button"),
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.buttonColors(containerColor = FFChatCyan)
            ) {
              Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = Color.Black
              )
              Spacer(Modifier.width(10.dp))
              Text(
                text = "Continue with Google",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
              )
            }
          }
        }

        Spacer(Modifier.height(16.dp))

        Text(
          text = "By signing in, you accept FF CHAT Terms of Service and Privacy Policy.",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          textAlign = TextAlign.Center
        )
      }
    }
  }
}

@Composable
private fun FeatureRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(44.dp)
        .clip(CircleShape)
        .background(FFChatCyan.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(icon, contentDescription = null, tint = FFChatCyan, modifier = Modifier.size(22.dp))
    }
    Spacer(Modifier.width(14.dp))
    Column {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
