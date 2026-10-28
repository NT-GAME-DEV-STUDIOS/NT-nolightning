# NoBlitz ⚡ - Minecraft Plugin

[🇬🇧 Switch to English Version](README.md) | [🇩🇪 Deutsche Version](README_DE.md)

Ein modernes, performantes und voll konfigurierbares Paper/Purpur/Spigot-Plugin (Minecraft 1.8.8 – 1.21.4+) zur Steuerung und Verhinderung von Blitzen, Gewitterstürmen und deren Nebeneffekten.

---

## 🚀 Features

- **Vollständige Blitz-Blockierung**:
  - Verhindert natürliche Blitze bei Gewitter.
  - Filter nach Ursache (`WEATHER`, `TRIDENT`, `COMMAND`, `CUSTOM`).
  - **Visual-Only-Modus**: Wandelt Blitze optional in rein visuelle Effekte um (kein Feuer, keine Zerstörung, kein Schaden).
- **Schutz vor Nebeneffekten**:
  - **Brandschutz**: Verhindert, dass Blitze Blöcke in Brand setzen (`BlockIgniteEvent`).
  - **Schadensschutz**: Schützt Spieler, friedliche Tiere und Monster vor Blitzschaden.
  - **Item-Schutz**: Verhindert, dass fallengelassene Items oder Item-Frames verbrannt werden.
  - **Mob-Transformationsschutz**:
    - Verhindert geladene Creeper (`CreeperPowerEvent`).
    - Verhindert Hexen-Transformation bei Dorfbewohnern (`EntityTransformEvent`).
    - Verhindert Zombified Piglins bei Schweinen (`PigZapEvent`).
    - Verhindert Pilzkuh-Farbwechsel.
- **Wetter-Management**:
  - Automatisches Abbrechen von Gewittern (`ThunderChangeEvent`).
  - Optional: Dauerhaft schönes Wetter (`always-clear-weather`).
- **Flexible Welten-Verwaltung**:
  - Modi: `ALL` (alle Welten), `WHITELIST` (nur gelistete Welten), `BLACKLIST` (alle außer gelistete Welten).
  - Runtime-Toggle pro Welt im laufenden Spielbetrieb ohne Server-Neustart.
- **Zweisprachige Unterstützung (i18n)**:
  - Konfigurierbar in `config.yml`: `language: de` oder `language: en`.
  - In-Game umschaltbar: `/noblitz lang <de|en>`.
- **Live-Statistiken**:
  - Zählt blockierte Blitze, verhinderte Brände und gerettete Mobs in Echtzeit.
- **Universelle Cross-Version-Kompatibilität**:
  - Kompiliert mit Java 8 Bytecode (`major version: 52`). Läuft auf Spigot, Paper und Purpur von 1.8.8 bis 1.21.4+.

---

## 📜 Befehle & Berechtigungen

| Befehl | Berechtigung | Beschreibung |
| :--- | :--- | :--- |
| `/noblitz help` | `noblitz.use` | Zeigt die Übersicht aller Befehle. |
| `/noblitz status` | `noblitz.status` | Zeigt den aktuellen Schutzstatus und Statistiken. |
| `/noblitz reload` | `noblitz.reload` | Lädt die `config.yml` neu. |
| `/noblitz lang <de\|en>` | `noblitz.reload` | Ändert die Sprache des Plugins live im Spiel. |
| `/noblitz toggle [welt]` | `noblitz.toggle` | Schaltet den Schutz für die angegebene (oder aktuelle) Welt um. |
| `/noblitz strike <spieler> [visual]` | `noblitz.strike` | Beschwört einen Blitz auf einen Spieler (optional nur visuell). |
| `/noblitz clearthunder [welt]` | `noblitz.clearthunder` | Beendet Gewitter und Sturm sofort. |
| `/noblitz resetstats` | `noblitz.resetstats` | Setzt den Zähler für blockierte Blitze/Brände zurück. |

**Admin-Recht**: `noblitz.admin` (besitzt standardmäßig alle Rechte).  
**Bypass-Recht**: `noblitz.bypass` (erlaubt Spielern mit Channeling-Dreizack Blitze trotz Schutz zu rufen, falls konfiguriert).

---

## ⚙️ Konfiguration (`config.yml`)

```yaml
enabled: true

# Sprache: "de" (Deutsch) oder "en" (Englisch)
# Kann auch im Spiel über /noblitz lang <de|en> gewechselt werden
language: de

debug: false

strike-rules:
  cancel-all-lightning: true
  cancel-causes:
    weather: true      # Blitze durch Gewitter blockieren
    trident: false     # Channeling-Dreizack erlauben
    command: false     # /summon lightning_bolt erlauben
    custom: false      # Blitze durch andere Plugins
  visual-only: false   # Blitze nur als Effekt anzeigen

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
  mode: ALL # ALL, WHITELIST oder BLACKLIST
  list:
    - world
    - world_nether
    - world_the_end
```

---

## 📦 Kompilierung

Das Plugin wird mit Gradle gebaut:
```bash
.\gradlew.bat jar
```
Die fertige JAR befindet sich im Projektordner:
- `build/libs/NoBlitz-1.0.0.jar`
