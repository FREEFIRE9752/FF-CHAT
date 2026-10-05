package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ChatMessage
import com.example.model.Group
import com.example.model.GroupRole
import com.example.model.MessageType
import com.example.repository.GroupRepository
import com.example.repository.UserRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun testAppNameResource() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FF CHAT", appName)
  }

  @Test
  fun testUsernameValidation() {
    // Valid username
    assertNull(UserRepository.validateUsername("@alex_99"))

    // Short username
    assertNotNull(UserRepository.validateUsername("@ab"))

    // Disallowed characters
    assertNotNull(UserRepository.validateUsername("@alex!#"))
  }

  @Test
  fun testSearchByUsername() {
    val results = UserRepository.searchByUsername("rohit")
    assertTrue(results.isNotEmpty())
    assertEquals("@rohit_ff", results.first().cleanUsername)
  }

  @Test
  fun testGroupRolesAndMax10Elders() {
    val group = Group(
      id = "grp_test",
      name = "Test Group",
      leaderId = "leader_user",
      elderIds = (1..10).map { "elder_$it" }, // Exactly 10 elders
      memberIds = listOf("leader_user", "member_user") + (1..10).map { "elder_$it" }
    )

    assertEquals(GroupRole.LEADER, group.getUserRole("leader_user"))
    assertEquals(GroupRole.ELDER, group.getUserRole("elder_1"))
    assertEquals(GroupRole.MEMBER, group.getUserRole("member_user"))

    assertTrue(group.canStartCall("leader_user"))
    assertTrue(group.canStartCall("elder_1"))
    assertFalse(group.canStartCall("member_user"))

    // Cannot add more than 10 elders
    assertFalse(group.canAddMoreElders())
  }

  @Test
  fun test1GBFileFormatting() {
    val message = ChatMessage(
      id = "m1",
      chatId = "c1",
      senderId = "u1",
      type = MessageType.FILE,
      fileName = "video_4k.mp4",
      fileSize = 1024L * 1024L * 1024L // 1 GB
    )
    assertEquals("1.00 GB", message.formattedFileSize)
  }

  @Test
  fun testProfileMediaUpload() {
    val initialCount = UserRepository.myProfileMedia.value.size
    UserRepository.uploadProfileMedia(
      url = "https://example.com/test.jpg",
      isVideo = false,
      title = "New Sunset"
    )
    val afterUpload = UserRepository.myProfileMedia.value
    assertEquals(initialCount + 1, afterUpload.size)
    assertEquals("New Sunset", afterUpload.first().title)

    // Removal
    UserRepository.removeProfileMedia(afterUpload.first().id)
    assertEquals(initialCount, UserRepository.myProfileMedia.value.size)
  }
}
