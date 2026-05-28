# SB-555P FAIZPHONE IoT Controller

A high-fidelity **Faiz Phone** (Kamen Rider 555) simulator built with **Jetpack Compose**. This application acts as a Zero-Trust IoT Remote Controller for a Proxmox Homelab via Tailscale API.

![Faiz Phone Banner](app/src/main/res/drawable/faiz_logo.png)

## 🚀 Features

- **Cyber Tactical UI**: Authentic Sol Metal 228 chassis design with animated red light strips and a pulsing industrial theme.
- **Lore-Accurate State Machine**: 
  - `555 + ENTER`: Triggers the system activation sequence (Wake-on-LAN).
  - `103 + ENTER`: Single Mode.
  - `106 + ENTER`: Burst Mode.
  - `279 + ENTER`: Charge.
- **Advanced Audio Engine**: 
  - **Multi-channel Button Sounds**: Overlapping feedback for rapid typing.
  - **Gapless Activation Sequence**: Custom manual cross-fade looping for a seamless "Standing By -> Loading -> Complete" auditory experience.
- **Haptic Feedback**: Tactile response on every tactical keypress.
- **Zero-Trust Networking**: Securely triggers IoT devices over a Tailscale private network.

## 🛠 Tech Stack

- **UI**: Jetpack Compose (Material 3)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: OkHttp3 + Kotlin Coroutines
- **Hardware**: Android Vibrator API, SoundPool (Low-latency audio)

## 🔧 Setup

1. **Prerequisites**: Ensure you have an active **Tailscale** connection on your Android device.
2. **Endpoint**: The app targets `http://100.64.0.6:5555/faiz?code=555` by default.
3. **Build**: 
   ```bash
   ./gradlew assembleDebug
   ```

## 📜 License

MIT License - Created by [phanmanhcuongdev](https://github.com/phanmanhcuongdev)
