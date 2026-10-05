# FF CHAT — Production Setup, Configuration & Deployment Guide

## 1. Overview
**FF CHAT** is a production-grade, high-performance messaging platform engineered with:
- **Authentication**: Firebase Authentication with Google Sign-In & unique `@username` registration.
- **Direct & Group Chat**: Real-time communication with sent, delivered, and read indicators, typing status, and replies.
- **1 GB Media Transfer**: Resumable, chunked streaming architecture for HD photos, 4K videos, and APK packages without excessive RAM consumption.
- **WebRTC Calling**: Real-time peer-to-peer Voice and Video calling with STUN/TURN ICE signaling and Android Screen Sharing (MediaProjection).
- **Group Role Hierarchy**: Group Creator becomes **Leader** 👑; can promote up to **10 Elders** 🛡️. Leader and Elders can start group calls; authorized members can join active calls.
- **Localization**: Full English and Hindi (हिन्दी) localization with in-app toggle.

---

## 2. Firebase & Google Sign-In Setup

### A. Firebase Project Creation
1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a project named `ffchat-prod`.
2. Enable **Authentication**:
   - Activate **Google Sign-In** provider.
   - Set support email.
3. Enable **Cloud Firestore** in production mode.
4. Enable **Firebase Cloud Storage** in production mode.
5. Enable **Cloud Messaging (FCM)**.

### B. Google Sign-In & SHA-1 Configuration (Android)
1. Generate your debug and release SHA-1 fingerprints:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
2. In Firebase Project Settings > General > Your Android apps:
   - Add Android App with Package Name: `com.aistudio.ffchat.kxmpzq`
   - Paste the SHA-1 and SHA-256 fingerprints.
   - Download `google-services.json` and place it in the `app/` folder.
3. Web Client ID:
   - Copy the OAuth 2.0 Web Client ID from Google Cloud Console > APIs & Services > Credentials.
   - Provide it to `FirebaseConfig.kt` or set via `.env` / `BuildConfig.GOOGLE_WEB_CLIENT_ID`.

---

## 3. Deploying Firestore & Storage Rules

Deploy the included security rules:
```bash
# Install Firebase CLI if not already installed
npm install -g firebase-tools

# Login to Firebase
firebase login

# Initialize project
firebase use --add ffchat-prod

# Deploy Firestore Security Rules
firebase deploy --only firestore:rules

# Deploy Cloud Storage Security Rules (Enforcing 1 GB hard ceiling)
firebase deploy --only storage
```

---

## 4. Deploying Cloud Functions

The functions in `/functions` automate push notifications, group elder limits (maximum 10), and WebRTC signaling cleanup.

```bash
cd functions
npm install
cd ..
firebase deploy --only functions
```

---

## 5. WebRTC STUN and TURN Server Provisioning

FF CHAT uses STUN for NAT traversal and TURN for symmetric NAT fallback.

### Coturn Setup on a Linux VM (Ubuntu/Debian)
```bash
sudo apt update && sudo apt install -y coturn

# Configure /etc/turnserver.conf:
listening-port=3478
tls-listening-port=5349
listening-ip=0.0.0.0
external-ip=YOUR_PUBLIC_IP
realm=turn.ffchat.net
user=ffchat_user:ffchat_turn_secure_token
lt-cred-mech
fingerprint
```
Restart coturn:
```bash
sudo systemctl restart coturn
```

Update your TURN credentials in `FirebaseConfig.kt`.

---

## 6. Android Build Instructions

### Prerequisites
- Android Studio Ladybug / Meerkat or later
- JDK 17 or JDK 21
- Android SDK 34 / 36

### Build Commands
```bash
# Run unit & local Robolectric tests
gradle :app:testDebugUnitTest

# Assemble Debug APK
gradle :app:assembleDebug

# Build Production Signed App Bundle (.aab)
gradle :app:bundleRelease
```
Outputs:
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release AAB: `app/build/outputs/bundle/release/app-release.aab`

---

## 7. iOS Build & Screen Sharing Setup

FF CHAT architecture is cross-platform compatible.

### A. iOS Prerequisites
- macOS with Xcode 15+
- CocoaPods (`gem install cocoapods`)

### B. GoogleService-Info.plist
1. Download `GoogleService-Info.plist` from Firebase Console.
2. Drag and drop into the Xcode project under the Runner root target.

### C. ReplayKit Broadcast Extension (For iOS Screen Sharing)
To support system-wide screen sharing on iOS:
1. In Xcode: File > New > Target > **Broadcast Upload Extension**.
2. Target Name: `FFChatScreenShareExtension`.
3. In `SampleHandler.swift`, process CMSampleBuffer frames and pipe them into WebRTC's `RTCVideoSource`:
   ```swift
   import ReplayKit
   import WebRTC

   class SampleHandler: RPBroadcastSampleHandler {
     override func processSampleBuffer(_ sampleBuffer: CMSampleBuffer, with sampleBufferType: RPSampleBufferType) {
       switch sampleBufferType {
       case .video:
         // Pipe frames to shared WebRTC video sink
         break
       default:
         break
       }
     }
   }
   ```
4. Set App Group in Capabilities: `group.com.ffchat.screenshare`.

### D. iOS Permissions (Info.plist)
Add to `ios/Runner/Info.plist`:
```xml
<key>NSCameraUsageDescription</key>
<string>FF CHAT needs camera access for HD video calling.</string>
<key>NSMicrophoneUsageDescription</key>
<string>FF CHAT needs microphone access for voice and video calling.</string>
<key>NSPhotoLibraryUsageDescription</key>
<string>FF CHAT needs photo access for sending HD photos up to 1GB.</string>
```

---

## 8. Security & Production Checklist
- [x] `@username` uniqueness validated server-side.
- [x] Google UID and user emails hidden from public profiles.
- [x] 1 GB maximum file size enforced in Storage rules.
- [x] Chunked resumable upload implemented to protect memory.
- [x] Group roles enforced: Leader, Elder (max 10), Member.
- [x] WebRTC calling signaling with STUN/TURN.
- [x] Offline support and clean MVVM repository structure.
