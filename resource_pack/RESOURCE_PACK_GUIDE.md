# LegendarySMP Resource Pack Guide

## Folder Structure

```
LegendaryPack/
├── pack.mcmeta
├── pack.png                        ← your pack icon
└── assets/
    └── minecraft/
        ├── models/
        │   └── item/
        │       ├── iron_hoe.json   ← overrides for SOULREAPER + TITANBREAKER
        │       ├── iron_sword.json ← overrides for TWIN KINGS, FROSTFANG, ECLIPSE BLADE
        │       └── custom/
        │           ├── soulreaper.json
        │           ├── twin_kings.json
        │           ├── frostfang.json
        │           ├── titanbreaker.json
        │           └── eclipse_blade.json
        └── textures/
            └── item/
                └── custom/
                    ├── soulreaper.png
                    ├── twin_kings.png
                    ├── frostfang.png
                    ├── titanbreaker.png
                    └── eclipse_blade.png
```

---

## pack.mcmeta

```json
{
  "pack": {
    "pack_format": 34,
    "description": "LegendarySMP Weapon Models"
  }
}
```

Pack format 34 = Minecraft 1.21 / 1.21.1.

---

## iron_hoe.json  (holds Soulreaper CMD:1001, Titanbreaker CMD:1004)

```json
{
  "parent": "item/handheld",
  "textures": {
    "layer0": "item/iron_hoe"
  },
  "overrides": [
    { "predicate": { "custom_model_data": 1001 }, "model": "item/custom/soulreaper" },
    { "predicate": { "custom_model_data": 1004 }, "model": "item/custom/titanbreaker" }
  ]
}
```

## iron_sword.json  (holds Twin Kings CMD:1002, Frostfang CMD:1003, Eclipse Blade CMD:1005)

```json
{
  "parent": "item/handheld",
  "textures": {
    "layer0": "item/iron_sword"
  },
  "overrides": [
    { "predicate": { "custom_model_data": 1002 }, "model": "item/custom/twin_kings" },
    { "predicate": { "custom_model_data": 1003 }, "model": "item/custom/frostfang" },
    { "predicate": { "custom_model_data": 1005 }, "model": "item/custom/eclipse_blade" }
  ]
}
```

---

## Example custom model  (assets/minecraft/models/item/custom/soulreaper.json)

For a flat 2D texture (simplest approach — replace with a 3D Blockbench model later):

```json
{
  "parent": "item/handheld",
  "textures": {
    "layer0": "item/custom/soulreaper"
  }
}
```

For a 3D Blockbench model export, replace the "parent" with your exported model file.

---

## Custom Model Data Reference

| Weapon         | Base Item   | CMD  |
|----------------|-------------|------|
| Soulreaper     | IRON_HOE    | 1001 |
| Twin Kings     | IRON_SWORD  | 1002 |
| Frostfang      | IRON_SWORD  | 1003 |
| Titanbreaker   | IRON_HOE    | 1004 |
| Eclipse Blade  | IRON_SWORD  | 1005 |

---

## Tools for 3D Models

- **Blockbench** (free): https://www.blockbench.net/
  - Create → Java Block/Item Model
  - Export → Export Block/Item Model → place in `models/item/custom/`
- Recommended: make a 3D sword/scythe shape in Blockbench for each weapon.

---

## Applying the Resource Pack

1. Zip the `LegendaryPack/` folder (contents, not the folder itself).
2. Host the zip online (e.g. MC Pack hosting, GitHub releases, your server host's file manager).
3. In `server.properties`:
   ```
   resource-pack=https://your-url/LegendaryPack.zip
   resource-pack-sha1=<sha1 of the zip>
   ```
4. Players will be prompted to download on join.
