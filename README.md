# 📅 Event Management Android App

A robust and scalable **Android application** developed using **Kotlin** and **Firebase**, designed to manage events, users, and participation workflows efficiently. The app follows a modular structure with role-based separation and real-time data handling.

---

## 🧭 Overview

This application enables organizations or institutions to manage events digitally with **multiple user roles**, **real-time updates**, and **structured data flow**. It is built with maintainability and scalability in mind using modern Android development practices.

---

## ✨ Core Features

### 🔐 Authentication System

* Firebase Authentication integration
* Secure login & session handling
* Role-based navigation (Admin / Manager / Student / Guest)

### 👥 Role-Based Architecture

The app is structured into multiple user roles:

* **Admin** → Full system control
* **Manager** → Event creation & management
* **Student** → Event participation
* **Guest** → Limited access

Each role has separate packages and responsibilities.

---

### 📌 Event Management (CRUD)

* Create, update, and delete events
* Manage event details (title, description, date, etc.)
* Organized event listing

---

### 📊 Dashboard & Reporting

* Manager dashboard for event overview
* Reports generation and monitoring
* Data visualization-ready structure

---

### 👨‍🎓 Participant Management

* Track student engagement
* Manage responses and participation
* Associate users with specific events

---

### 🔄 Real-Time Database

* Cloud Firestore integration
* Instant updates across devices
* Efficient NoSQL data handling

---

### 🎨 UI/UX

* Material Design components
* Clean and user-friendly interface
* Structured navigation and layouts

---

## 🏗️ Project Architecture

* Modular package structure (actor-based separation)
* MVVM-ready design pattern
* Scalable and maintainable codebase
* Lifecycle-aware components

---

## 📂 Project Structure

```bash
app/src/main/java/com/misty/eventmanagement/
│
├── actor/
│   ├── admin/
│   ├── manager/
│   ├── student/
│   ├── guest/
│
├── auth/                 # Authentication logic
├── menu/                 # Navigation/menu handling
├── DashboardManager.kt   # Manager dashboard
├── ReportsActivity.kt    # Reporting module
├── SettingsActivity.kt   # App settings
├── MainActivity.kt       # Entry point
```

---

## 🛠️ Tech Stack

| Category        | Technology              |
| --------------- | ----------------------- |
| Language        | Kotlin                  |
| IDE             | Android Studio          |
| Backend         | Firebase Authentication |
| Database        | Cloud Firestore         |
| UI              | Material Design         |
| Build System    | Gradle (KTS)            |
| Version Control | Git & GitHub            |

---

## ⚙️ Setup & Installation

### 1️⃣ Clone Repository

```bash
git clone https://github.com/mdAbdullahAnas/Event_Management_App.git
```

### 2️⃣ Open Project

* Open in Android Studio
* Sync Gradle

### 3️⃣ Firebase Setup

* Add `google-services.json` to `/app`
* Enable:

  * Firebase Authentication
  * Cloud Firestore

### 4️⃣ Run App

* Use emulator or real device
* Click ▶️ Run

---

## 📸 Screenshots

> Add UI screenshots here for better presentation

---

## 🚀 Future Improvements

* 🔔 Push Notifications (FCM)
* 📅 Calendar Integration
* 📍 Location-based Events
* 🧑‍💼 Advanced Admin Panel
* 📈 Analytics Dashboard

---

## 🧪 Testing

* Unit testing support available
* Tested on multiple Android versions
* Optimized for performance and responsiveness

---

## 🤝 Contribution

Contributions are welcome.
Fork the repo and submit a pull request.

---

## 📄 License

MIT License

---

## 👨‍💻 Author

**Md Abdullah Anas**
🔗 GitHub: https://github.com/mdAbdullahAnas

---

## ⭐ Support

If you found this project useful, give it a ⭐ on GitHub!
