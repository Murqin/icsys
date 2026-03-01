# I Can Show Your Speed

🤖 *Built with Claude AI*

A lightweight, client-side speed HUD mod for Minecraft.

Track your movement speed in real time with a fully customizable overlay — choose which metrics to display, add progress bars, adjust smoothing, and position the HUD anywhere on screen.

## 🎯 What It Does

Displays your real-time movement speed as a compact HUD overlay with three metrics:

- **Speed** — Total 3D movement speed (blocks/second)
- **H-Speed** — Horizontal speed only (useful for sprinting/elytra)
- **V-Speed** — Vertical speed with ↑↓ direction (useful for falling/flying)

Each metric can be shown as text, a color-coded progress bar, or both.

## ✨ Highlights

- **Fully Customizable** — Toggle each metric and bar individually, adjust opacity, and choose color themes
- **Drag & Drop** — Position the HUD anywhere with mouse dragging and pixel-perfect arrow key nudging
- **Smart Snapping** — HUD snaps to screen edges and center with visual guides
- **Adjustable Smoothing** — Cycle between 6 smoothing presets (1t to 60t) for raw or smooth readings
- **Color-Coded** — Speed values change color: white → yellow → red as you move faster
- **Bar-Only Mode** — Minimal bars with tiny labels (S, H, V↑/V↓) when text is off
- **Zero Overhead** — No mixins, client-side only, uses Fabric API events

## ⌨️ Controls

| Key | Action |
|---|---|
| `H` | Toggle HUD on/off |
| `K` | Open config screen |

### Config Screen

| Key | Action |
|---|---|
| Drag | Move HUD position |
| Arrow keys / `W A S D` | Nudge 1px (`Ctrl`: 10px) |
| `1` `2` `3` | Toggle Speed / H-Speed / V-Speed |
| `Ctrl+1` `Ctrl+2` `Ctrl+3` | Toggle progress bars |
| `R` | Cycle smoothing (1→5→10→20→40→60 ticks) |
| `O` / `Ctrl+O` | Cycle background Opacity |
| `T` | Cycle Color Theme |
| `ESC` | Save & close |

## 📦 Requirements

- Minecraft 1.21.11
- Fabric Loader ≥ 0.18.4
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
- [Mod Menu](https://modrinth.com/mod/modmenu) (optional — adds config button in mod list)

## 📝 License

[MIT](https://github.com/Murqin/icsys/blob/main/LICENSE)
