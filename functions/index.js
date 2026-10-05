const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

/**
 * 1. Direct Message Push Notification Trigger
 */
exports.onDirectMessageCreated = functions.firestore
  .document("chats/{chatId}/messages/{messageId}")
  .onCreate(async (snap, context) => {
    const message = snap.data();
    const chatId = context.params.chatId;

    const chatDoc = await db.collection("chats").doc(chatId).get();
    if (!chatDoc.exists) return null;

    const participants = chatDoc.data().participants || [];
    const recipientUid = participants.find((uid) => uid !== message.senderId);
    if (!recipientUid) return null;

    // Fetch recipient's FCM tokens
    const userDoc = await db.collection("users").doc(recipientUid).get();
    if (!userDoc.exists) return null;

    const fcmToken = userDoc.data().fcmToken;
    if (!fcmToken) return null;

    const payload = {
      notification: {
        title: message.senderName || "FF CHAT Message",
        body: message.type === "TEXT" ? message.text : `Sent a ${message.type.toLowerCase()}`,
      },
      data: {
        chatId: chatId,
        senderId: message.senderId,
        type: message.type,
      },
    };

    return admin.messaging().sendToDevice(fcmToken, payload);
  });

/**
 * 2. Group Message Notification Dispatcher
 */
exports.onGroupMessageCreated = functions.firestore
  .document("groups/{groupId}/messages/{messageId}")
  .onCreate(async (snap, context) => {
    const message = snap.data();
    const groupId = context.params.groupId;

    if (message.type === "SYSTEM") return null;

    const groupDoc = await db.collection("groups").doc(groupId).get();
    if (!groupDoc.exists) return null;

    const group = groupDoc.data();
    const recipientUids = (group.memberIds || []).filter((uid) => uid !== message.senderId);

    if (recipientUids.length === 0) return null;

    // Batch fetch FCM tokens
    const userSnapshots = await Promise.all(
      recipientUids.map((uid) => db.collection("users").doc(uid).get())
    );

    const tokens = [];
    userSnapshots.forEach((doc) => {
      if (doc.exists && doc.data().fcmToken) {
        tokens.push(doc.data().fcmToken);
      }
    });

    if (tokens.length === 0) return null;

    const payload = {
      notification: {
        title: `${group.name} • ${message.senderName}`,
        body: message.type === "TEXT" ? message.text : `Sent a ${message.type.toLowerCase()}`,
      },
      data: {
        groupId: groupId,
        type: message.type,
      },
    };

    return admin.messaging().sendToDevice(tokens, payload);
  });

/**
 * 3. Group Role & Maximum 10 Elders Server-Side Validation
 */
exports.validateGroupRoles = functions.firestore
  .document("groups/{groupId}")
  .onWrite(async (change, context) => {
    if (!change.after.exists) return null;

    const afterData = change.after.data();
    const elderIds = afterData.elderIds || [];

    // Strictly enforce maximum 10 Elders
    if (elderIds.length > 10) {
      console.error(`Group ${context.params.groupId} exceeded elder limit: ${elderIds.length}`);
      // Revert elder list to previous state or slice first 10
      const trimmedElders = elderIds.slice(0, 10);
      return change.after.ref.update({ elderIds: trimmedElders });
    }
    return null;
  });

/**
 * 4. High-Priority WebRTC Incoming Call Notification
 */
exports.onCallInitiated = functions.firestore
  .document("calls/{callId}")
  .onCreate(async (snap, context) => {
    const callData = snap.data();
    const callId = context.params.callId;

    if (callData.isGroup) {
      // Group call alert to members
      const groupDoc = await db.collection("groups").doc(callData.targetId).get();
      if (!groupDoc.exists) return null;

      const memberIds = (groupDoc.data().memberIds || []).filter((uid) => uid !== callData.callerId);
      const userSnaps = await Promise.all(
        memberIds.map((uid) => db.collection("users").doc(uid).get())
      );

      const tokens = [];
      userSnaps.forEach((doc) => {
        if (doc.exists && doc.data().fcmToken) {
          tokens.push(doc.data().fcmToken);
        }
      });

      if (tokens.length === 0) return null;

      return admin.messaging().sendToDevice(tokens, {
        data: {
          type: "INCOMING_CALL",
          callId: callId,
          callType: callData.callType,
          callerName: callData.callerName,
          isGroup: "true",
          groupName: groupDoc.data().name,
        },
      });
    } else {
      // Direct call alert
      const targetUserDoc = await db.collection("users").doc(callData.targetId).get();
      if (!targetUserDoc.exists || !targetUserDoc.data().fcmToken) return null;

      return admin.messaging().sendToDevice(targetUserDoc.data().fcmToken, {
        data: {
          type: "INCOMING_CALL",
          callId: callId,
          callType: callData.callType,
          callerName: callData.callerName,
          callerAvatar: callData.callerAvatar || "",
        },
        priority: "high",
      });
    }
  });

/**
 * 5. WebRTC Stale Signaling Cleanup
 */
exports.cleanupStaleCalls = functions.pubsub
  .schedule("every 30 minutes")
  .onRun(async () => {
    const twoHoursAgo = Date.now() - 2 * 60 * 60 * 1000;
    const staleCalls = await db.collection("calls")
      .where("startedAt", "<", twoHoursAgo)
      .get();

    const batch = db.batch();
    staleCalls.forEach((doc) => {
      batch.delete(doc.ref);
    });

    return batch.commit();
  });
