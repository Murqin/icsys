# I Can Show Your Speed

🤖 *Built with Claude AI*

A lightweight, client-side Fabric mod for Minecraft 1.21.11 that displays real-time movement speed as a customizable HUD overlay.

## Demo

[![ICSYS Demo](https://img.youtube.com/vi/IFEV1uXG_fc/hqdefault.jpg)](https://youtu.be/IFEV1uXG_fc)

## Features

- **3 Speed Metrics** — 3D Speed, Horizontal Speed (H-Speed), Vertical Speed (V-Speed) with ↑↓ direction indicators
- **Per-Metric Progress Bars** — Optional color-coded bars under each metric, individually toggleable
- **Color-Coded Values** — White (< 5 b/s), Yellow (5–15 b/s), Red (> 15 b/s)
- **Adjustable Smoothing** — Cycle through 1 / 5 / 10 / 20 / 40 / 60 tick averaging windows
- **Draggable HUD** — Full drag & drop positioning with snap-to-edge/center guides
- **Pixel-Perfect Nudging** — Arrow keys for 1px, Ctrl+Arrows for 10px adjustments
- **Customizable Looks** — Change background opacity and color themes
- **Center-Aligned Text** — Clean, centered layout inside the HUD box
- **Bar-Only Mode** — Show just bars with S / H / V↑↓ labels, no text
- **Mod Menu Integration** — Access config screen from Mod Menu (optional)
- **Persistent Config** — All settings saved to `config/icsys-speed-hud.json`
- **Auto-Hide** — HUD hidden when F3 debug screen is active
- **Multi-Language** — English & Turkish

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | ≥ 0.18.4 |
| Fabric API | Latest for 1.21.11 |
| Fabric Language Kotlin | ≥ 1.13.9 |
| Mod Menu | Optional |

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
3. Download the latest `.jar` from [Releases](https://github.com/Murqin/icsys/releases) or [Modrinth](https://modrinth.com/mod/icsys)
4. Place all `.jar` files into your `.minecraft/mods` folder
5. Launch Minecraft with the Fabric profile

## Controls

### In-Game

| Key | Action |
|---|---|
| `H` | Toggle HUD visibility |
| `K` | Open config screen |

### Config Screen

| Key | Action |
|---|---|
| Drag / `W A S D` | Move HUD |
| `← → ↑ ↓` / `W A S D` | Nudge 1px |
| `Ctrl` + Arrows/WASD | Nudge 10px |
| `1` / `2` / `3` | Toggle Speed / H-Speed / V-Speed text |
| `Ctrl+1` / `Ctrl+2` / `Ctrl+3` | Toggle Speed / H-Speed / V-Speed bar |
| `R` | Cycle smoothing window (1→5→10→20→40→60 ticks) |
| `O` / `Ctrl+O` | Cycle background Opacity |
| `T` | Cycle Color Theme |
| `ESC` | Save & exit |

## Building from Source

```bash
git clone https://github.com/Murqin/icsys.git
cd icsys
./gradlew build
```

Output JAR will be in `build/libs/`.

## License

[MIT](LICENSE)
