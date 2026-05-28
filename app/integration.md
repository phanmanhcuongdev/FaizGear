# Faiz Gear Android Integration

Tai lieu nay la contract de app Android/Kotlin mo phong Faiz Gear goi `faiz-api`.

## Base URL

Server Flask lang nghe tren IP Tailscale cua may chay API.

```text
Base URL: http://100.64.0.6:5555
Endpoint: GET /faiz?code=<code>
```

App Android chi can gui code. App khong giu Proxmox token va khong goi thang Proxmox.

```text
Android/Kotlin app -> faiz-api 100.64.0.6 -> Proxmox API 100.64.0.5 -> node pve-01
```

## Code Mapping

| Code | Ten tren app | Tac dung |
| --- | --- | --- |
| `103` | Status Check | Kiem tra trang thai tat ca VM/LXC tren node `pve-01` |
| `555` | Wake Proxmox | Chi gui Wake-on-LAN de bat Proxmox |
| `111` | Faiz Start | Start VM `208` va LXC `999` tren `pve-01` |
| `106` | Batch Start | Start VM `205`, `206`, `207`, `210`, `211`, `219`, `221`, `222` |
| `999` | Managed Shutdown | Shutdown mem cac VM/LXC server dang quan ly tren `pve-01` |
| `888` | Guest Shutdown | Giong `999` ve backend, nhung action rieng la `guest_shutdown` |
| `000` | Proxmox Shutdown | Shutdown host Proxmox `pve-01`, can `confirm=pve-01` |

`000` la lenh nguy hiem nhat. App nen bat xac nhan rieng truoc khi gui code nay.

## Managed Targets

Nhung code start/shutdown guest se quan ly danh sach nay:

```text
VM: 205, 206, 207, 208, 210, 211, 219, 221, 222
LXC: 999
Node: pve-01
Proxmox API: 100.64.0.5:8006
```

## Endpoint

Tat ca request dung method `GET`:

```http
GET http://100.64.0.6:5555/faiz?code=<code>
```

Server tra header nay cho moi response de tranh cache/preload lenh nguy hiem:

```http
Cache-Control: no-store
```

## 103 - Status Check

```http
GET /faiz?code=103
```

Response thanh cong:

```json
{
  "status": "complete",
  "code": "103",
  "action": "status_check",
  "ui": {
    "title": "SCAN",
    "subtitle": "STATUS OK"
  },
  "message": "Status complete!",
  "node": "pve-01",
  "summary": {
    "ok": 12,
    "failed": 0,
    "total": 12
  },
  "targets": [
    {
      "type": "qemu",
      "id": "208",
      "name": "workstation",
      "state": "running",
      "ok": true
    },
    {
      "type": "lxc",
      "id": "999",
      "name": "faiz-lxc",
      "state": "stopped",
      "ok": true
    }
  ]
}
```

`103` tra ve tat ca QEMU VM va LXC tren node `pve-01`, khong chi managed targets.

Gia tri `state` cua tung target thuong la:

```text
running
stopped
unknown
```

## 555 - Wake Proxmox

```http
GET /faiz?code=555
```

Tac dung:

- gui Wake-on-LAN magic packet de bat Proxmox/workstation host
- khong start VM/LXC
- tra JSON nhanh, khong cho app cho Proxmox boot xong

Response thanh cong:

```json
{
  "status": "complete",
  "code": "555",
  "action": "henshin",
  "ui": {
    "title": "WAKE",
    "subtitle": "SENT"
  },
  "message": "Wake-on-LAN sent, Proxmox API not ready",
  "node": "pve-01",
  "summary": {
    "ok": 1,
    "failed": 0,
    "total": 1
  },
  "targets": [],
  "detail": "Proxmox API 100.64.0.5:8006 is not reachable yet"
}
```

Neu Proxmox API da reachable, response van la `complete`:

```json
{
  "status": "complete",
  "code": "555",
  "action": "henshin",
  "ui": {
    "title": "WAKE",
    "subtitle": "PROXMOX"
  },
  "message": "Wake-on-LAN sent, Proxmox API is reachable",
  "node": "pve-01",
  "summary": {
    "ok": 1,
    "failed": 0,
    "total": 1
  },
  "targets": []
}
```

HTTP status la `200` neu server da gui duoc WOL.

## 111 - Faiz Start

```http
GET /faiz?code=111
```

Tac dung:

- start VM `208` neu chua running
- start LXC `999` neu chua running

Response thanh cong:

```json
{
  "status": "complete",
  "code": "111",
  "action": "faiz_start",
  "ui": {
    "title": "COMPLETE",
    "subtitle": "FAIZ START"
  },
  "message": "Faiz targets start requested",
  "node": "pve-01",
  "summary": {
    "ok": 2,
    "failed": 0,
    "total": 2
  },
  "targets": [
    {
      "type": "qemu",
      "id": "208",
      "name": "workstation",
      "state": "stopped",
      "result": "start_requested",
      "ok": true
    },
    {
      "type": "lxc",
      "id": "999",
      "name": "faiz-lxc",
      "state": "running",
      "result": "already_running",
      "ok": true
    }
  ]
}
```

## 106 - Batch Start

```http
GET /faiz?code=106
```

Tac dung:

- start VM `205`
- start VM `206`
- start VM `207`
- start VM `210`
- start VM `211`
- start VM `219`
- start VM `221`
- start VM `222`

Response thanh cong:

```json
{
  "status": "complete",
  "code": "106",
  "action": "batch_start",
  "ui": {
    "title": "BURST",
    "subtitle": "BATCH START"
  },
  "message": "Batch start requested",
  "node": "pve-01",
  "summary": {
    "ok": 8,
    "failed": 0,
    "total": 8
  },
  "targets": [
    {
      "type": "qemu",
      "id": "205",
      "name": "qemu-205",
      "state": "stopped",
      "result": "start_requested",
      "ok": true
    }
  ]
}
```

Response partial:

```json
{
  "status": "partial",
  "code": "106",
  "action": "batch_start",
  "ui": {
    "title": "PARTIAL",
    "subtitle": "6/8 STARTED"
  },
  "message": "Batch start requested",
  "node": "pve-01",
  "summary": {
    "ok": 6,
    "failed": 2,
    "total": 8
  },
  "error_code": "E_PROXMOX_ERROR",
  "targets": []
}
```

## 999 - Managed Shutdown

```http
GET /faiz?code=999
```

Tac dung:

- shutdown mem cac VM/LXC trong managed targets
- khong shutdown host Proxmox `pve-01`

Response thanh cong:

```json
{
  "status": "complete",
  "code": "999",
  "action": "managed_shutdown",
  "ui": {
    "title": "COMPLETE",
    "subtitle": "GUEST OFF"
  },
  "message": "Shutdown requested!",
  "node": "pve-01",
  "summary": {
    "ok": 10,
    "failed": 0,
    "total": 10
  },
  "targets": [
    {
      "type": "qemu",
      "id": "208",
      "name": "workstation",
      "state": "running",
      "result": "shutdown_requested",
      "ok": true
    },
    {
      "type": "lxc",
      "id": "999",
      "name": "faiz-lxc",
      "state": "stopped",
      "result": "already_stopped",
      "ok": true
    }
  ]
}
```

## 888 - Guest Shutdown

```http
GET /faiz?code=888
```

Tac dung hien tai giong `999`: shutdown mem cac VM/LXC trong managed targets.

Response thanh cong:

```json
{
  "status": "complete",
  "code": "888",
  "action": "guest_shutdown",
  "ui": {
    "title": "COMPLETE",
    "subtitle": "GUEST OFF"
  },
  "message": "Shutdown requested!",
  "node": "pve-01",
  "summary": {
    "ok": 10,
    "failed": 0,
    "total": 10
  },
  "targets": [
    {
      "type": "qemu",
      "id": "208",
      "name": "workstation",
      "state": "running",
      "result": "shutdown_requested",
      "ok": true
    }
  ]
}
```

## 000 - Proxmox Shutdown

```http
GET /faiz?code=000
```

Tac dung:

- shutdown host Proxmox `pve-01`
- khong chi shutdown VM/LXC
- bat buoc gui them `confirm=pve-01`

Neu thieu confirm:

```json
{
  "status": "error",
  "code": "000",
  "action": "proxmox_shutdown",
  "error_code": "E_CONFIRM_REQUIRED",
  "ui": {
    "title": "CONFIRM",
    "subtitle": "NODE OFF"
  },
  "message": "Confirmation required for Proxmox host shutdown",
  "node": "pve-01",
  "targets": []
}
```

HTTP status:

```text
403 Forbidden
```

Request shutdown that:

```http
GET /faiz?code=000&confirm=pve-01
```

Response thanh cong:

```json
{
  "status": "complete",
  "code": "000",
  "action": "proxmox_shutdown",
  "ui": {
    "title": "NODE OFF",
    "subtitle": "PROXMOX"
  },
  "message": "Proxmox shutdown requested!",
  "node": "pve-01",
  "targets": []
}
```

## Result Values

Khi start/shutdown VM/LXC, moi target co `result`:

```text
start_requested
already_running
shutdown_requested
already_stopped
error
skipped
```

Target status check dung `state` va khong co `result`.

Target action start/shutdown dung `state` la trang thai truoc khi gui action, va co them `result`.

`targets` luon co voi VM/LXC actions. Voi `000`, server tra `targets: []`.

Khi mot target loi trong batch:

```json
{
  "type": "qemu",
  "id": "205",
  "name": "qemu-205",
  "state": "unknown",
  "result": "error",
  "ok": false,
  "error_code": "E_PROXMOX_ERROR",
  "message": "Proxmox API error"
}
```

## Summary

Moi action nhieu target co `summary`:

```json
{
  "summary": {
    "ok": 7,
    "failed": 1,
    "total": 8
  }
}
```

App nen parse theo thu tu:

```text
status -> error_code -> ui.title/ui.subtitle -> summary -> targets
```

## Error Responses

Code sai:

```json
{
  "status": "error",
  "code": "abc",
  "action": "unknown",
  "error_code": "E_BAD_CODE",
  "ui": {
    "title": "DENIED",
    "subtitle": "BAD CODE"
  },
  "message": "Exceed Charge!"
}
```

HTTP status:

```text
400 Bad Request
```

Loi Proxmox:

```json
{
  "status": "error",
  "code": "106",
  "action": "batch_start",
  "error_code": "E_PROXMOX_ERROR",
  "ui": {
    "title": "FAILED",
    "subtitle": "PROXMOX ERR"
  },
  "message": "Proxmox error!",
  "detail": "..."
}
```

HTTP status:

```text
502 Bad Gateway
```

Rieng code `555` khong start VM/LXC nua. Loi rieng cua `555` la Wake-on-LAN failed:

```json
{
  "status": "error",
  "code": "555",
  "action": "henshin",
  "error_code": "E_WOL_FAILED",
  "ui": {
    "title": "WOL FAIL",
    "subtitle": "WAKE LOST"
  },
  "message": "Wake-on-LAN failed",
  "detail": "..."
}
```

## Error Codes

App nen uu tien doc `status`, `error_code`, `ui.title`, `ui.subtitle`.

| Error code | Y nghia | UI goi y |
| --- | --- | --- |
| `E_BAD_CODE` | Code khong nam trong mapping | `DENIED / BAD CODE` |
| `E_MISSING_CODE` | Thieu `?code=` | `NO CODE / ENTER CODE` |
| `E_CONFIRM_REQUIRED` | Lenh `000` thieu confirm | `CONFIRM / NODE OFF` |
| `E_PROXMOX_UNREACHABLE` | Khong ket noi duoc Proxmox API | `OFFLINE / PROXMOX LOST` |
| `E_PROXMOX_AUTH` | Token Proxmox sai hoac thieu quyen | `DENIED / API TOKEN` |
| `E_PROXMOX_ERROR` | Proxmox tra loi chung | `FAILED / PROXMOX ERR` |
| `E_TARGET_NOT_FOUND` | VM/LXC khong ton tai | `MISSING / TARGET LOST` |
| `E_WOL_FAILED` | Gui Wake-on-LAN loi | `WOL FAIL / WAKE LOST` |
| `E_NODE_SHUTDOWN_FAILED` | Shutdown host Proxmox loi | `FAILED / NODE POWER` |

HTTP status hien tai:

| HTTP | Khi nao |
| --- | --- |
| `200` | Thanh cong hoac partial batch co it nhat mot target OK |
| `400` | Code sai hoac thieu code |
| `403` | Lenh nguy hiem thieu confirm hoac permission cua app |
| `408` | Timeout khi goi Proxmox |
| `502` | Proxmox tra loi loi hoac tat ca target deu loi |
| `503` | Khong ket noi duoc Proxmox API |

`E_PROXMOX_AUTH` la loi backend/token, server tra `502`, khong phai luong UI confirm cua app.

## Kotlin Example

Vi du voi OkHttp:

```kotlin
val client = OkHttpClient()

fun sendFaizCode(code: String) {
    val request = Request.Builder()
        .url("http://100.64.0.6:5555/faiz?code=$code")
        .get()
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Loi ket noi toi faiz-api
        }

        override fun onResponse(call: Call, response: Response) {
            response.use {
                val body = it.body?.string()
                if (it.isSuccessful) {
                    // Parse JSON, status = complete
                } else {
                    // Parse JSON, status = error
                }
            }
        }
    })
}
```

## Android Manifest

Can quyen internet:

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

Neu app dung HTTP cleartext tren Android 9+:

```xml
<application
    android:usesCleartextTraffic="true">
</application>
```

Sau nay nen gioi han cleartext traffic cho rieng IP Tailscale `100.64.0.6`.

## Server Config

Server doc Proxmox config tu `.env` trong thu muc `faiz-api`.

```bash
PROXMOX_HOST=100.64.0.5
PROXMOX_NODE=pve-01
PROXMOX_TOKEN_ID=root@pam!faizGear
PROXMOX_TOKEN_SECRET=<token-secret>
PROXMOX_VERIFY_SSL=false
```

Token Proxmox chi nam tren server. App Android khong can va khong nen biet token nay.
