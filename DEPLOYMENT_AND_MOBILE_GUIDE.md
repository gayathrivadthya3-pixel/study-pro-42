# StudyPulse: Deployment & Mobile Usage Guide

**StudyPulse** is a full-stack, gamified study planner and daily to-do application designed for students preparing for **Semester Exams**, **GATE Exams**, **Language Learning**, and **Interview Preparation**.

Built with:
- **Frontend**: HTML5, Modern Dynamic CSS (Tailwind CSS 3.4 CDN, Glassmorphism, Animations), Vanilla JavaScript (ES6+), Web Audio API, Canvas Confetti.
- **Backend**: Java 17 + Spring Boot 3.2.5 RESTful microservice.
- **Database**: Aiven Cloud Database support (MySQL & PostgreSQL) + local persistent fallback.
- **Mobile Experience**: Responsive Touch UI + Progressive Web App (PWA) + Capacitor APK ready.

---

## 🌟 Implemented Features Checklist

| # | Requirement | Implementation Details |
|---|---|---|
| **1** | **Study Timings for 4 Tracks** | Dedicated study schedules for **Semester Exams**, **GATE Exams**, **Language Learning**, and **Interview Preparation** with custom start/end times and recurring days. |
| **2** | **Login Options** | **Mobile Number + 6-Digit OTP** generation & verification, **Continue with Google**, and **Guest Scholar** login. |
| **3** | **Dashboard** | Hero mastery gauge, active study track cards, Pomodoro focus timer with ticking chimes, priority study tasks highlight. |
| **4** | **Profile Tab** | User profile, target exam dates customizer (Semester finals, GATE year, Target language, Interview role), audio settings, live Aiven Cloud DB status. |
| **5** | **Gamified View** | Scholar Progression (Level 1 Novice Scholar to Level 10 Legendary Polymath), XP progress bar with shimmer animation, 7-day flame streak counter, and unlocked achievement badges. |
| **6** | **Daily To-Do Tasks** | Categorized tasks with checkboxes, XP tags, priority indicators (High, Med, Low), duration, scheduled time, and sound effects. |
| **7** | **Edit Tasks & Default Presets** | Edit task modal for modifying title, notes, priority, timing, alarms. Quick "Load Default Tasks" for Semester, GATE, Language, Interview, or Master Plan. |
| **8** | **Alarm for Important Tasks** | Web Audio API chime synthesizer + Browser Notifications API alerts when task scheduled time arrives + in-app alert banner with Snooze and Mark Done. |
| **9** | **Appreciation Celebrations** | Dynamic appreciation when completing >= 70% of goals today or throughout the week. Triggers **Canvas Confetti**, personalized praise, Dr. A.P.J. Abdul Kalam & Swami Vivekananda quotes, and bonus XP awards. |
| **10** | **Dynamic CSS Framework** | Tailwind CSS with responsive glassmorphism, glowing gradients, safe-area inset padding, mobile bottom navigation bar. |

---

## 🚀 1. Quick Start Locally

### Run the App with One Click (Windows):
Double click the provided `start.bat` file or run:
```powershell
.\start.bat
```
The server will boot and open:
👉 **http://localhost:8080**

### Run via Command Line:
```powershell
java -jar target\study-pulse-app-1.0.0.jar
```

---

## ☁️ 2. Connecting to Aiven Cloud Database

StudyPulse has built-in support for **Aiven Cloud MySQL** and **Aiven Cloud PostgreSQL**.

### Step 1: Create your Aiven Service
1. Go to [https://aiven.io](https://aiven.io) and sign up (free trial available).
2. Click **Create Service**:
   - Choose **Aiven for MySQL** or **Aiven for PostgreSQL**.
   - Select cloud provider (AWS/GCP) and region closest to you.
   - Click **Create Service**.

### Step 2: Retrieve Connection Credentials
In your Aiven Console Overview tab, find:
- **Host** (e.g. `mysql-xxxxx.aivencloud.com`)
- **Port** (e.g. `12345`)
- **User** (usually `avnadmin`)
- **Password**
- **Database** (usually `defaultdb`)

### Step 3: Run with Aiven Cloud Credentials

#### Option A: Set Environment Variables (Recommended)
**For Aiven MySQL:**
```powershell
$env:AIVEN_DB_URL="jdbc:mysql://<AIVEN_HOST>:<AIVEN_PORT>/defaultdb?sslmode=REQUIRED&serverTimezone=UTC"
$env:AIVEN_DB_USERNAME="avnadmin"
$env:AIVEN_DB_PASSWORD="<YOUR_AIVEN_PASSWORD>"
$env:AIVEN_DB_DRIVER="com.mysql.cj.jdbc.Driver"
java -jar target\study-pulse-app-1.0.0.jar
```

**For Aiven PostgreSQL:**
```powershell
$env:AIVEN_DB_URL="jdbc:postgresql://<AIVEN_HOST>:<AIVEN_PORT>/defaultdb?sslmode=require"
$env:AIVEN_DB_USERNAME="avnadmin"
$env:AIVEN_DB_PASSWORD="<YOUR_AIVEN_PASSWORD>"
$env:AIVEN_DB_DRIVER="org.postgresql.Driver"
java -jar target\study-pulse-app-1.0.0.jar
```

#### Option B: Configure `application.properties`
Open `src/main/resources/application.properties` and paste your Aiven URI directly into `spring.datasource.url`.

---

## 📱 3. Deploy for Mobile Use

You can deploy StudyPulse for mobile devices in 3 distinct ways:

### Method A: Install as Progressive Web App (PWA) - Instant & No App Store Needed!
StudyPulse is configured as an installable PWA with `manifest.json` and `sw.js`.

1. Deploy the backend to a cloud host (or open `http://<your-computer-ip>:8080` from your phone on the same Wi-Fi).
2. **On Android (Google Chrome)**:
   - Open the web address.
   - Tap the 3 dots in the top right.
   - Tap **"Install App"** or **"Add to Home screen"**.
   - An icon named **StudyPulse** will appear on your phone home screen, running full-screen like a native app!
3. **On iPhone (Safari)**:
   - Open the web address in Safari.
   - Tap the **Share** button (box with upward arrow).
   - Scroll down and tap **"Add to Home Screen"**.

---

### Method B: Package into an Android APK with Capacitor

We've provided `capacitor.config.json` pre-configured for Android packaging.

1. Ensure Node.js is installed.
2. In the project directory `d:\Daily_to_do_list`:
   ```bash
   npm install @capacitor/core @capacitor/cli @capacitor/android
   npx cap init StudyPulse com.studypulse.app --web-dir src/main/resources/static
   npx cap add android
   npx cap copy
   npx cap open android
   ```
3. Android Studio will open:
   - Click **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**.
   - Transfer the generated `.apk` to your phone and install!

---

### Method C: Deploy Online for Free (For Everyone to Use)

Deploy StudyPulse online so you and your peers can access it anywhere:

#### Deploy to Render.com (Free)
1. Push this repository to GitHub.
2. Create a free account at [https://render.com](https://render.com).
3. Click **New** > **Web Service** > Connect your GitHub repo.
4. Set:
   - **Environment**: Docker
   - **Environment Variables**:
     - `AIVEN_DB_URL`: your Aiven database URL
     - `AIVEN_DB_USERNAME`: `avnadmin`
     - `AIVEN_DB_PASSWORD`: your Aiven password
5. Render will automatically build the `Dockerfile` and provide you with a live HTTPS URL (e.g. `https://studypulse.onrender.com`) that anyone can open on mobile or desktop!

---

## 🔒 Test Accounts & OTP Simulation

- **Mobile OTP**: Enter any 10-digit number (e.g. `9876543210`). The app will display the generated 6-digit OTP code on-screen for testing ease. Alternatively, `123456` is accepted as a universal demo test code.
- **Google Login**: Click "Continue with Google" and enter any email address to sign in immediately.
- **Guest Access**: Click "Skip and continue as Guest Scholar" for instant access.
