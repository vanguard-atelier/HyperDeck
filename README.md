# 💿 HyperDeck

> **Zero-Algo, Backlog-First Media Vault & HyperOS Video Player for Android & Tablets.**  
> Attempt 3. Built for the Redmi Pad Pro and modern phones. Powered by Jetpack Compose & Miuix aesthetics.

---

## ⚡ The Lore
- **Attempt 1 (`Soytube`):** Handed a barebones video player by AI with no extractor. Backlog remained untouched.
- **Attempt 2 (`HyperTube`):** Forked LibreTube, tried to remodel the UI into HyperOS, broke the backend plumbing, scrapped.
- **Attempt 3 (`HyperDeck`):** Clean architecture. Decoupled Invidious/Piped stream manifest API + Media3 ExoPlayer with adaptive DASH/HLS, resolution switching, multi-subtitles, and a physical "CD Rack / Vault" library interface.

---

## ✨ Features

- 💿 **The Vault (CD Rack):** No algorithmic home feeds. You "burn" YouTube links into your personal vault. Each item is a permanent disc on your shelf.
- 📐 **Adaptive Tablet & Phone UI:**
  - **Tablets (Redmi Pad Pro):** Dual-pane navigation rail with docked player and side-by-side queue.
  - **Phones:** Single-column layout with HyperOS bottom navigation bar.
- 🎛️ **HyperOS Aesthetics:** Deep black backgrounds (`#0D0D11`), signature HyperOS blue accents, squircle borders, and glassy overlays.
- 🎬 **Power-User Player (Media3 / ExoPlayer):**
  - **Resolution Switching:** 144p up to 1080p / 4K.
  - **Subtitles & Captions:** Full `.vtt` multi-language subtitle selector.
  - **Audio Tracks:** Separate audio streams & background playback support.
  - **Aspect Ratio & Timeline:** Custom gesture-friendly seekbars with 10s jump controls.
- 🔗 **Zero-Algo Search:** Searches only when you ask it to. No recommended brainrot.
- 🚀 **Share Sheet Target:** Share any YouTube link directly to HyperDeck to burn it instantly into your deck.

---

## 🛠️ Tech Stack

- **UI:** Jetpack Compose + [Miuix](https://github.com/Compose-Miuix-UI/Miuix) HyperOS Design System
- **Player:** AndroidX Media3 (ExoPlayer + DASH + HLS + UI)
- **Engine:** Invidious REST API with multi-instance automatic fallback
- **Network & JSON:** OkHttp + Kotlinx Serialization
- **Image Loading:** Coil Compose
- **Target SDK:** Android 15 (API 35) / Minimum SDK: Android 8.0 (API 26)

---

## 📦 Automated Builds (CI/CD)

Every push to `main` triggers a GitHub Actions workflow that compiles the latest debug APK and attaches it to the workflow artifacts.

1. Go to the **Actions** tab on GitHub.
2. Select the latest **Build HyperDeck APK** run.
3. Download the `HyperDeck-debug-apk` artifact directly on your phone and install!
