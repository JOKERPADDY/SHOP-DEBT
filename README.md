# Thawne Debt System 🧾

A Kotlin Multiplatform (KMP) application for Android and Desktop built with Compose Multiplatform, designed for small business owners and shops to manage customer debts, payments, multi-device staff sharing, and audit logging.

---

## 🌟 Key Features

### 💰 Debt & Customer Management
- **Record & Track Debts**: Log customer name, ID, phone number, product item, total amount, and due dates.
- **Payment History**: Record partial or full payments with notes and automatically track remaining balances.
- **Automatic Interest Calculation**: Computes simple monthly interest (5%) for overdue debts.
- **Search & Filters**: Search debts by customer name, ID, or product; filter by status (*Pending*, *Partial*, *Paid*, *Overdue*).

### 📱 Multi-Device Shop Sharing & Linked Devices
- **6-Digit Link Code Sharing**: Shop owners can generate a code for staff or partners to access shop data from their own devices.
- **Device Naming & Tracking**: Staff can label their device (e.g., *"Counter Phone"*, *"John's Tablet"*).
- **Remote Access Revocation**: Owners can view all connected devices in settings and revoke access with a single tap.

### 📄 Invoicing & WhatsApp Reminders
- **Formatted Statements**: Instantly format invoice summaries showing total owed, paid, and grand balance.
- **Direct Messaging**: One-click WhatsApp reminder integration.

### 📊 Audit Logging & Exporting
- **Audit Logs**: Automatic logging of all debt additions, updates, payments, and deletions with timestamp and actor email.
- **Export/Import**: Export debt registers to PDF or CSV, and import existing data via CSV.

---

## 🛠️ Tech Stack

- **Language**: Kotlin 2.0 (Kotlin Multiplatform)
- **UI Framework**: Compose Multiplatform (Jetpack Compose)
- **Backend & Sync**: Firebase Auth & Firebase Firestore (via GitLive Firebase Kotlin SDK)
- **State & Architecture**: Jetpack ViewModel, Kotlin Coroutines, StateFlow
- **Serialization & Utilities**: `kotlinx.serialization`, `kotlinx.datetime`, `kotlinx.coroutines`

---

## 📁 Project Structure

```
├── composeApp/          # Android App Module (MainActivity, Android Manifest)
├── desktopApp/          # JVM Desktop App Module (Main entry point, PDF Export)
├── shared/              # Common Kotlin Multiplatform Module
│   ├── commonMain/      # UI Screens, ViewModels, Repositories, Models, Logic
│   ├── androidMain/     # Android-specific platform bindings
│   └── desktopMain/     # Desktop-specific platform bindings
└── gradle/              # Version Catalog & Gradle configurations
```

---

## 🚀 Getting Started

### Prerequisites
- **Android Studio** (2024.1+ recommended) with KMP plugin
- **JDK 17+**

### Building and Running

#### Android App
```bash
./gradlew :composeApp:assembleDebug
```
Or run directly from Android Studio selecting the `composeApp` run configuration on an emulator/device.

#### Desktop Application
```bash
./gradlew :desktopApp:run
```

---

## 📝 License
This project is proprietary software for Thawne Shop.
