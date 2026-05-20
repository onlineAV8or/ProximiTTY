# ProximiTTY - Connect Phone and TV

Phone ↔ Android TV proximity discovery and remote-render demo built on **Google Nearby Connections**.

The phone discovers nearby TVs over BLE / Bluetooth / Wi-Fi, connects, measures round-trip latency, and can stream a JSON-described UI tree that the TV renders natively in Jetpack Compose.

## Modules

| Module | Type | Role |
|---|---|---|
| `shared/` | Android library | Payload schema, `UiNode` tree, permission list, suspending wrappers around `ConnectionsClient` |
| `phone-app/` | Android app | Discoverer. Compose UI with scan list, RTT display, JSON editor, preset templates |
| `tv-app/` | Android TV app | Advertiser foreground service + recursive Compose `UiNode` renderer |

## Architecture

```
Phone (discoverer)             TV (advertiser)
─────────────────              ─────────────────
Nearby.startDiscovery()  ───▶  Nearby.startAdvertising()
                               (foreground service)
requestConnection()      ───▶  onConnectionInitiated → accept
                               onConnectionResult(success)
sendPayload(RenderUi)    ───▶  TvState.remoteUi = node
                               TvScreen recomposes → RenderUi(node)
```

Strategy: `P2P_STAR` — one TV, many phones.
Payload: `Hello`, `Ping`, `Pong`, `RenderUi(UiNode)` — JSON over Nearby BYTES payload.

## Building

Requires JDK 17 and the Android SDK with API 35 platform installed.

```bash
./gradlew :phone-app:assembleDebug
./gradlew :tv-app:assembleDebug
```

Output APKs land in `phone-app/build/outputs/apk/debug/` and `tv-app/build/outputs/apk/debug/`.

## Installing

```bash
adb -s <phone-serial> install -r phone-app/build/outputs/apk/debug/phone-app-debug.apk
adb -s <tv-serial>    install -r tv-app/build/outputs/apk/debug/tv-app-debug.apk
```

On the phone, grant **Location** (coarse + fine) and **Nearby Devices** permissions on first launch.

## JSON UI schema

The phone can send a `UiNode` tree to be rendered on the TV. Supported types:

| Type | Fields |
|---|---|
| `text` | `text`, `size`, `color`, `bold`, `align` |
| `column` / `row` | `children`, `padding`, `spacing`, `alignment`, `background` |
| `card` | `child`, `padding`, `background`, `cornerRadius` |
| `box` | `children`, `padding`, `background`, `alignment` |
| `spacer` | `size` |
| `clear` | (resets TV to status screen) |

Example:
```json
{
  "type": "column",
  "padding": 48,
  "spacing": 16,
  "alignment": "center",
  "children": [
    { "type": "text", "text": "Hello TV!", "size": 64, "bold": true },
    { "type": "text", "text": "Sent from phone", "color": "#94A3B8" }
  ]
}
```

## Permissions

Nearby Connections needs ALL of these (and the device-wide Location toggle ON):

- `ACCESS_COARSE_LOCATION`, `ACCESS_FINE_LOCATION`
- `BLUETOOTH_SCAN`, `BLUETOOTH_ADVERTISE`, `BLUETOOTH_CONNECT` (API 31+)
- `BLUETOOTH`, `BLUETOOTH_ADMIN` (API ≤ 30)
- `NEARBY_WIFI_DEVICES` (API 33+)
- `ACCESS_WIFI_STATE`, `CHANGE_WIFI_STATE`

## License

MIT
