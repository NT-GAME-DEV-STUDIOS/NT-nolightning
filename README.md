# NT-noblitz ⚡ - Minecraft Plugin

[🇩🇪 Zur deutschen Version wechseln](README_DE.md) | [🇬🇧 English Version](README.md)

A modern, high-performance, and fully configurable Paper / Purpur / Spigot plugin (Minecraft 1.8.8 – 1.21.4+) for controlling and preventing lightning strikes, thunderstorms, and all their side effects.

---

## 🚀 Features

- **Complete Lightning Strike Prevention**:
  - Automatically cancels natural lightning strikes during thunderstorms.
  - Granular cause filtering (`WEATHER`, `TRIDENT`, `COMMAND`, `CUSTOM`).
  - **Visual-Only Mode**: Optionally converts lightning strikes into harmless visual effects (no fire, no block breaking, no entity damage, no mob transformations).
- **Comprehensive Side Effect Protections**:
  - **Fire Protection**: Stops lightning from igniting blocks (`BlockIgniteEvent`).
  - **Damage Protection**: Shields players, peaceful animals, and monsters from lightning damage.
  - **Item Protection**: Prevents dropped items and item frames from being vaporized.
  - **Mob Transformation Guard**:
    - Prevents Charged Creepers (`CreeperPowerEvent`).
    - Prevents Villagers from transforming into Witches (`EntityTransformEvent`).
    - Prevents Pigs from becoming Zombified Piglins (`PigZapEvent`).
    - Prevents Mooshroom color swapping.
- **Weather Management**:
  - Automatically suppresses thunderstorms (`ThunderChangeEvent`).
  - Optional: Keep weather permanently clear (`always-clear-weather`).
- **World Management**:
  - Modes: `ALL` (protect all worlds), `WHITELIST` (only specified worlds), `BLACKLIST` (all except specified).
  - Runtime toggle per world without server restarts.
- **Bilingual & Multi-Language Support**:
  - Configurable language in `config.yml`: `language: en` or `language: de`.
  - In-game language switching: `/noblitz lang <de|en>`.
- **Live Statistics**:
  - Tracks blocked strikes, prevented fires, and protected mobs in real time.
- **Universal Cross-Version Support**:
  - Compiled with Java 8 bytecode (`major version: 52`). Compatible with Minecraft 1.8.8 through 1.21.4+ on Spigot, Paper, and Purpur.

---

## 📜 Commands & Permissions

| Command | Permission | Description |
| :--- | :--- | :--- |
| `/noblitz help` | `noblitz.use` | Displays plugin command overview. |
| `/noblitz status` | `noblitz.status` | Displays protection status and strike statistics. |
| `/noblitz reload` | `noblitz.reload` | Reloads `config.yml`. |
| `/noblitz lang <de\|en>` | `noblitz.reload` | Changes plugin language live. |
| `/noblitz toggle [world]` | `noblitz.toggle` | Toggles lightning protection for the world. |
| `/noblitz strike <player> [visual]` | `noblitz.strike` | Summons lightning on a player (optionally visual only). |
| `/noblitz clearthunder [world]` | `noblitz.clearthunder` | Immediately clears thunderstorms and rain. |
| `/noblitz resetstats` | `noblitz.resetstats` | Resets blocked strike and fire statistics. |

**Admin Permission**: `noblitz.admin` (granted to OPs by default).  
**Bypass Permission**: `noblitz.bypass` (allows players with Channeling tridents to strike even when protected, if configured).

---

## ⚙️ Configuration (`config.yml`)

```yaml
enabled: true

# Language: "en" (English) or "de" (German)
# Can also be switched live via /noblitz lang <de|en>
language: en

debug: false

strike-rules:
  cancel-all-lightning: true
  cancel-causes:
    weather: true      # Block natural weather lightning
    trident: false     # Allow Channeling tridents
    command: false     # Allow /summon lightning_bolt
    custom: false      # Lightning triggered by other plugins
  visual-only: false   # Turn lightning into pure visual effects

protection:
  prevent-fire: true
  prevent-entity-damage: true
  prevent-item-destruction: true
  prevent-mob-transformations: true
  mute-sound: false

weather:
  prevent-thunderstorms: false
  always-clear-weather: false

worlds:
  mode: ALL # ALL, WHITELIST, or BLACKLIST
  list:
    - world
    - world_nether
    - world_the_end
```

---

## 📦 Building

Built using Gradle (targeting Java 8 bytecode):
```bash
.\gradlew.bat jar
```
The compiled JAR will be located at:
- `build/libs/NoBlitz-1.0.0.jar`
