package com.example.model

data class ProfileMedia(
  val id: String,
  val userId: String,
  val url: String,
  val isVideo: Boolean,
  val title: String,
  val timestamp: Long = System.currentTimeMillis()
)
