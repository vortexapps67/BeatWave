<div align="center">

<img src="https://private-user-images.githubusercontent.com/267700146/677091356-94db79e3-95bf-4a47-8133-ef2e785227fb.png?jwt=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJnaXRodWIuY29tIiwiYXVkIjoicmF3LmdpdGh1YnVzZXJjb250ZW50LmNvbSIsImtleSI6ImtleTUiLCJleHAiOjE3OTU2MzgxOTUsIm5iZiI6MTc5NTYzNzg5NSwicGF0aCI6Ii8yNjc3MDAxNDYvNjc3MDkxMzU2LTk0ZGI3OWUzLTk1YmYtNGE0Ny04MTMzLWVmMmU3ODUyMjdmYi5wbmc_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ3JlZGVudGlhbD1BS0lBVkNPRFlMU0E1M1BRSzRaQSUyRjIwMjYxMTI1JTJGdXMtZWFzdC0xJTJGczMlMkZhd3M0X3JlcXVlc3QmWC1BbXotRGF0ZT0yMDI2MTEyNVQyMDE4MTVaJlgtQW16LUV4cGlyZXM9MzAwJlgtQW16LVNpZ25hdHVyZT1hNjIxN2VkMDFlNDdjODY5OTEwOGJiNDk5ZjBjY2JkYjcwZDQ5MTIyYWM0Y2M1MGYzMTkzZDQ0MDE0YTAwZTE4JlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCZyZXNwb25zZS1jb250ZW50LXR5cGU9aW1hZ2UlMkZwbmcifQ.neFFPhVx3LtEDAjTI54SW0ZTZBKlIq49_qurmMfo-yc" alt="BeatWave Logo" width="132" />

# BEATWAVE

**Open-source Liquid Glass music player for Android**

[![Download Latest Release](https://img.shields.io/badge/⬇️%20DOWNLOAD%20Latest%20Build-4CAF50?style=for-the-badge&logo=android&logoColor=white)](https://github.com/beatlabs790/BeatWave/releases/latest)
[![Previous Tag v5.0.0](https://img.shields.io/badge/Tag-v6.1.1-388E3C?style=for-the-badge)](https://github.com/beatlabs790/BeatWave/releases/tag/v6.1.1)

[![Downloads](https://img.shields.io/github/downloads/beatlabs790/BeatWave/total?style=for-the-badge&color=blue)](https://github.com/beatlabs790/BeatWave/releases)
[![License](https://img.shields.io/github/license/beatlabs790/BeatWave?style=for-the-badge)](LICENSE)
[![Discord](https://img.shields.io/badge/Discord-Join%20Us-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/spbuDTePSR)
[![Website](https://img.shields.io/badge/BeatWave-Website-8A2BE2?style=for-the-badge&logo=googlechrome&logoColor=white)](https://support-akshansh.vercel.app)

> **Android 8.0+** required • **UPI:** `akshanshsinha67@axl` • **Website:** [support-akshansh.vercel.app](https://support-akshansh.vercel.app)

</div>

---

## 🎵 About BeatWave

**BeatWave** is a free, open-source music player for Android that streams from YouTube Music, built with **Jetpack Compose** on a **Media3** ExoPlayer core. The UI is a custom **Liquid Glass** design system — frosted, refractive surfaces, iOS-style bouncy scrolling, and progressive blur chrome — instead of stock Material widgets.

BeatWave started as a fork of [CONVX](https://github.com/cosmictaserdev-creator/Convx) and also builds on components from [vivi-music](https://github.com/vivizzz007/vivi-music) (see [Credits](#-credits) below).

---

## ✨ Features

### Liquid Glass UI
* **Real backdrop blur:** frosted glass chrome (nav bar, floating buttons, sheets) that actually samples and refracts the content behind it, not a flat translucent color.
* **iOS-style motion:** bouncy rubber-band overscroll, blurred page transitions, springy nav puck.
* **Material You:** adaptive colors pulled from the currently playing artwork.

### Streaming
* **Full YT Music catalog:** ad-free streaming and background playback with full notification/lock-screen controls.
* **Offline downloads:** cache tracks locally with smart storage management.
* **Lossless/high-quality audio** and a built-in equalizer.

### Lyrics & Social
* **Synced, karaoke-style lyrics** with word-by-word highlighting.
* **Discord Rich Presence:** show what you're listening to on your profile.
* **Listen Together:** sync playback with friends in real time.

### Privacy & Updates
* **Zero telemetry:** no trackers, no analytics, fully local library and preferences.
* **Built-in updater:** in-app update checks and changelogs, no third-party store required.

---

## 🚗 Android Auto Setup

If BeatWave doesn't appear in Android Auto:

1. Open **Android Auto** on your phone.
2. Tap the **hamburger menu** (three lines) and go to **Settings**.
3. Scroll to the bottom and tap the **version number** multiple times to enable Developer Settings.
4. Tap the **three dots menu** (`⋮`) at the top-right.
5. Select **Developer settings**.
6. Enable **Unknown sources**.
7. Restart Android Auto and connect to your car.

---

## 🏗️ Architecture

Quick map for contributors — see [CONTRIBUTING.md](CONTRIBUTING.md) for the full guide:

* **UI:** Jetpack Compose, MVVM (`ui/screens` + `viewmodels`), navigated via `ui/screens/NavigationBuilder.kt`.
* **Liquid Glass:** `ui/component/GlassEffect.kt` exposes `Modifier.liquidGlass(...)`, built on a vendored, source-included copy of [Kyant0/backdrop](https://github.com/Kyant0/backdrop) under `ui/component/backdrop/`. A `Backdrop` (usually a `rememberLayerBackdrop()` attached via `Modifier.layerBackdrop(...)` to some subtree) captures real pixels; any surface holding a reference to that same backdrop can sample, blur, and refract it through `drawBackdrop(...)`. The floating nav bar, circular back/share buttons, and sheets are all just glass surfaces sampling a nearby backdrop this way.
* **Playback:** Media3 `ExoPlayer` service in `playback/MusicService.kt`.
* **Data:** Room database (`db/`) for the local library, DataStore for preferences (`utils/DataStore.kt`, `constants/PreferenceKeys.kt`).
* **YouTube Music access:** the `innertube` module — an unofficial InnerTube API client, kept separate from the app module.
* **Updater:** `vivimusic/updater/` checks GitHub Releases for new versions and handles in-app APK download/install (FOSS/GMS build flavors behave slightly differently — see `BuildConfig.CAST_AVAILABLE`).

---

## 🤝 Contributing

Contributions are welcome — bug reports, feature requests, translations, and code. Start with [CONTRIBUTING.md](CONTRIBUTING.md) for the project layout, build setup, and PR checklist. Short version:

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

## 🛡️ Privacy & Data Collection

At **BeatWave**, your privacy is our top priority. We believe that your music and data belong exclusively to you:

* **Zero Data Collection:** we do **not** collect, store, or share any of your personal information, usage habits, or listening history.
* **100% Local:** all your settings, downloaded tracks, and offline caches are stored securely on your device.
* **No Tracking:** no hidden trackers, analytics, or background services monitoring your activity.

---

## 📜 Disclaimer & License

This project and its contents are **not affiliated with, funded, authorized, endorsed by, or in any way associated with** YouTube, Google LLC, or any of their affiliates and subsidiaries.

Any trademark, service mark, trade name, or other intellectual property rights used in this project are owned by their respective owners.

**BeatWave** is an independent project created for educational and personal use purposes.

This project is licensed under the terms specified in the [LICENSE](LICENSE) file (GPL-3.0).

---

## 💬 Community & Support

Connect with other listeners, suggest features, report bugs, and stay updated on releases:

* **Discord:** [Join Community](https://discord.gg/spbuDTePSR)
* **Links:** [Report Bugs](https://github.com/beatlabs790/BeatWave/issues) • [Discussions](https://github.com/beatlabs790/BeatWave/discussions) • [Releases](https://github.com/beatlabs790/BeatWave/releases)

---

## 🙏 Credits

BeatWave is developed and maintained by [Vortex Apps](https://github.com/vortexapps67). See [support-akshansh.vercel.app](https://support-akshansh.vercel.app) for support.

### Built On
* **[vivi-music](https://github.com/vivizzz007/vivi-music)** by **Vividh P Ashokan** — the project BeatWave was forked from.
* The **Apple Music Player V17** full-screen player style (`Settings → Player Theme`) is ported from [vivi-music](https://github.com/vivizzz007/vivi-music)'s Apple Music player UI, GPL-3.0.

### Foundational Projects
* **[Kyant0/backdrop](https://github.com/Kyant0/backdrop)** — the real-time backdrop blur/refraction library the Liquid Glass UI is built on.
* **[Better Lyrics](https://github.com/better-lyrics/better-lyrics)** and **[SimpMusic](https://github.com/maxrave-dev/SimpMusic)** — synced lyrics.
* **[YouLyPlus](https://github.com/ibratabian17/YouLyPlus)** — in-app lyrics styling.
* **[Monochrome](https://github.com/monochrome-music/monochrome)** — the animated visualizer canvas.

<div align="center">

The open-source community for tools, libraries, and APIs that make this project possible.

**Made with ❤️ for music lovers everywhere**

⭐ *Star this repo if you enjoy BeatWave!*

</div>
