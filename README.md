# Prism Player

Prism Player is an Android offline music player built with Jetpack Compose. It focuses on a clean library experience, queue-first playback, a built-in equalizer with presets, and lightweight local analytics (recently added tracks, simple play history).

## Screenshot preview

- `docs/screenshots/01-home.jpeg` — Home screen with Quick Play / recommendations
- `docs/screenshots/02-library.jpeg` — Library tab (songs/albums/artists)
- `docs/screenshots/03-player.jpeg` — Full player (artwork, controls, queue access)
- `docs/screenshots/04-queue.jpeg` — Queue sheet with drag-to-reorder
- `docs/screenshots/05-equalizer.jpeg` — Equalizer screen (bands + presets)
- `docs/screenshots/06-edit-track.jpeg` — Track metadata editor (tags + artwork)

## Features

- Local library import based on user-selected folders (Music/Downloads by default)
- Playback powered by Media3 (ExoPlayer) with a `MediaSessionService` for background playback
- Now Playing experience:
  - Mini player and full player screen
  - Queue management (including drag-to-reorder)
  - Shuffle and repeat controls
- Equalizer:
  - Hardware equalizer via `android.media.audiofx.Equalizer`
  - Built-in presets (Flat, Bass, Vocal, Treble)
  - Save custom presets (persisted locally)
- Track metadata utilities:
  - Read/write tags using Jaudiotagger (title, artist, album, genre, year, track number, artwork)
  - Media scanner refresh after tag edits
- Basic library views:
  - Albums, artists, and album detail screens
  - Search across library entities
- Lightweight local stats:
  - Simple play history table
  - “Total listening hours” derived from play count (local only)

## Tech stack

- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Navigation: `androidx.navigation:navigation-compose`
- Playback: AndroidX Media3 (ExoPlayer, Session, UI)
- Persistence:
  - Room (`Song` + `PlayHistory`)
  - SharedPreferences for library folders and equalizer preferences
- Images: Coil (Compose)
- Permissions: Accompanist Permissions
- Queue reorder: `sh.calvin.reorderable:reorderable`
- Tag editing: `net.jthink:jaudiotagger`

