# ⚔ LegendarySMP Plugin

**Paper 1.20.1 – 1.20.4** · Java 17 · Maven

A complete Legendary Weapons SMP system with 5 unique weapons, one-of-a-kind crafting, and a Dragon Egg PvP objective.

---

## 🔨 Build

```bash
mvn clean package
# Output: target/LegendarySMP-1.0.0.jar
# Drop the jar into your server's /plugins folder
```

Requires Java 17+ and a Paper server (not Spigot — uses Paper-specific APIs).

---

## ⚔ Weapons

| Weapon | CMD | Ability |
|---|---|---|
| **Soulreaper** | 1001 | Kill → Regen II 5s · Crit → +10% dmg |
| **Twin Kings** | 1002 | Hit → Rage stack · 5 stacks → +50% next hit |
| **Frostfang** | 1003 | Every 3rd crit → Freeze target 3s |
| **Titanbreaker** | 1004 | Instant shield break · Charged hit → knockback · +1 reach |
| **Eclipse Blade** | 1005 | RClick → Dash 6 blocks · Next hit → +30% dmg (20s CD) |

All weapons:
- Are **unbreakable**
- Have **unique crafting recipes**
- Can only be crafted **once per world**
- **Always drop on death** (bypasses keepInventory)

---

## 🐉 Dragon Egg

- Gives the holder **Speed I** while in inventory
- **Always drops on death**
- Spawn it with `/legendary egg <player>`
- Owner identity: **private** (not broadcast)

---

## 📋 Crafting Recipes

```
SOULREAPER       TWIN KINGS       FROSTFANG
N D N            G D G            I B I
N D N            G G G            I D I
N E N            N B N            I S I

N = Netherite    G = Gold Ingot   I = Ice
D = Crying Obs   D = Diamond      B = Blue Ice
E = Ender Pearl  B = Blaze Rod    D = Diamond Sword
                 N = Netherite    S = Packed Ice

TITANBREAKER     ECLIPSE BLADE
N A N            E P E
N O N            P D P
N A N            E P E

N = Netherite    E = End Crystal
A = Anvil        P = Purpur Block
O = Obsidian     D = Netherite Sword
```

---

## 🛠 Admin Commands

| Command | Description |
|---|---|
| `/legendary give <player> <id>` | Give a legendary weapon |
| `/legendary egg <player>` | Give the Dragon Egg |
| `/legendary status` | Show which weapons exist |
| `/legendary reset <id\|all>` | Allow re-crafting |
| `/legendary reload` | Reload config.yml |

**Weapon IDs:** `soulreaper`, `twin_kings`, `frostfang`, `titanbreaker`, `eclipse_blade`

---

## ⚙ Config

Edit `plugins/LegendarySMP/config.yml`:

```yaml
allow-recrafting: false      # Set true to allow duplicates (not recommended)
dragon-egg:
  speed-buff: true
  broadcast-interval: 0     # 0 = disabled; N = broadcast holder every N minutes
weapons:
  soulreaper:
    crit-bonus: 0.10        # 10% extra crit damage
  eclipse-blade:
    dash-cooldown-ms: 20000 # 20 seconds
  # ... see config.yml for all values
```

---

## 📦 Resource Pack

See `resource_pack/RESOURCE_PACK_GUIDE.md`.

Custom model data IDs: 1001–1005 on `iron_hoe` and `iron_sword`.  
Design models in **Blockbench** (free): https://www.blockbench.net/

---

## 📁 File Structure

```
src/main/java/com/legendarysmp/
├── LegendarySMP.java               ← Main plugin class
├── WeaponType.java                 ← Weapon enum (id, CMD, lore, material)
├── WeaponManager.java              ← Item creation + recipe registration
├── DragonEggManager.java           ← Egg item + Speed effect task
├── PlayerData.java                 ← Per-player ability state
├── PlayerDataManager.java          ← Manages PlayerData instances
├── listeners/
│   ├── WeaponAbilityListener.java  ← All 5 weapon combat abilities
│   ├── DeathListener.java          ← Force-drops legendaries on death
│   └── CraftingListener.java       ← One-craft-per-world enforcement
└── commands/
    └── LegendaryCommand.java       ← /legendary admin command
```
