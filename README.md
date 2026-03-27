# ⌂ SetHomePlugin

**Version:** 1.0
**Author:** Maximilian Hirtzbruch
**Minecraft:** 1.21 – 1.21.x
**API:** Bukkit · Spigot · Paper · Folia · Purpur

---

## ✨ Features

| Feature | Details |

|---|---|

| 🌍 **11 Languages** | DE, EN, FR, ES, IT, PL, NL, PT, RU, ZH, JA |

| 🏠 **GUI Menu** | Paginated, Left Click = Teleport, Right Click = Delete |

| ✅ **Delete Confirmation** | Custom Confirm GUI before deletion |

| 🔑 **LuckPerms** | Rank-based home limits (fully configurable) |

| ⏱️ **Teleport Delay** | Configurable countdown, cancels on movement |

| 🎆 **Particles & Sounds** | Fully toggleable |

| 💾 **YAML Storage** | Per-player files, MySQL preparation |

| 🔄 **Hot Reload** | `/sethomereload` without server restart |

---

## 📦 Installation

1. Place the JAR file in the `plugins/` folder

2. Start the server — `config.yml` will be created automatically

3. Set the language in `config.yml`: `general.language: de`

---

## 🎮 Commands

| Command | Description |

|---|---|

| `/sethome <name>` | Set home at current location |

| `/home <name>` | Teleport to a home |

`/delhome <name>` | Delete a home |

`/homes` | Show all homes in the GUI |

`/sethomereload` | Reload configuration |

---

## 🔐 Permissions

| Permission | Description | Default |

|---|---|---|

`sethome.use` | All basic commands | `true` |

`sethome.admin` | Reload + Bypass | `op` |

`sethome.homes.unlimited` | Unlimited homes | `op` |

`sethome.homes.<n>` | Max. n homes (1, 2, 3, 5, 10…) | `false` |


` ... ---

## ⚙️ Configuration (Excerpt)

```yaml
general:

language: "de" # Language: de, en, fr, es, it, pl, nl, pt, ru, zh, ja

default-max-homes: 3 # Default limit without LuckPerms rank

teleport-delay: 3 # Seconds until teleport (0 = instant)

cancel-on-move: true # Cancel teleport on movement

rank-homes: # LuckPerms rank → Number of homes

owner: 999

admin: 50

vip: 10

default: 3

```

---

## 🖥️ GUI Overview

```
┌────────────────────────────────────────────┐
│ ⌂ House 1 ⌂ Base ⌂ Farm ░ ░ ░ ░ │ ← Homes (Left click = TP)
│ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ │
│ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ │
│ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ ░ │
│ ◀ Back ░ ░ ░ ░ Close ░ ▶ │ ← Navigation
└─────────────────────────────────────────────┘

Right-click opens Confirmation GUI:
┌────────────────────────────────────────────┐
│ ░ ░ ░ ░ ✔ Delete ⌂ Farm ✘ Cancel ░ │
└──────────────────────────────────────────┘
```

---

## 🗂️ File Structure

```
plugins/SetHomePlugin/
├── config.yml ← Main Configuration
└── homes/

├── <uuid>.yml ← Home Data per Player

└── ...

```

---

## 🔧 Build

```bash
`mvn clean package`
`# → target/SetHomePlugin-1.0.jar```

**Requirements:** Java 21, Maven 3.8+

---

## 📝 Changelog

### v1.0

- Initial release
- 11 languages, GUI, LuckPerms, particles, sounds
