# CargoConnect - Mobile App Setup Guide

## Project Overview
CargoConnect is an Android mobile application for tracking cargo schedules and items. The application is built using Kotlin and Jetpack Compose for the UI. It connects to a backend server to fetch cargo data and uses authentication for user login.

## Prerequisites
To run this application, you will need:

- **Android Studio Hedgehog (2023.1.1) or newer**
- **JDK 17** (Java Development Kit)
- **Android SDK 34** (Android 14)
- **Running Backend Server** (see backend connection details below)
- **Emulator or Physical Android Device** (running Android 7.0/API 24 or higher)

## Project Setup Instructions

### 1. Opening the Project
1. Launch Android Studio
2. Select "Open an Existing Project"
3. Navigate to and select the `Cargo_Connect_Frontend` directory
4. Wait for Gradle sync to complete

### 2. Configure Backend, DALI, Airport, & Google Maps API Connection
1. Open `Cargo_Connect_Frontend/app/src/main/res/values/strings.xml`
2. Modify the `backend_url`, `google_maps_key`, `DALI_ENDPOINT`, `AIRPORT_ENDPOINT` with the actual end points.
   
### 3. Running the Backend, DALI, and  Airport Services
Before running the app, ensure that all the services are running.

### 4. Running the Application
1. Select your target device from the dropdown in the toolbar (either an emulator or connected physical device)
2. Click the "Run" button (green triangle) in the toolbar
3. The app will build and install on your selected device

### 5. Login Information
- The app uses an authentication system
- For testing purposes, you can use default credentials:
- Username: 'demo@cargolive.com'
- Password: 'demo123'

## Application Features
- User authentication and registration
- View cargo schedules
- View detailed cargo items for each schedule
- Notification system for cargo updates

## Troubleshooting

### Connection Issues
If the app fails to connect to the backend:
1. Verify the backend server is running
2. Check that the BASE_URL in RetrofitClient.kt is correctly set
3. If using a physical device, ensure the device is on the same network as your development machine

### Build Errors
If you encounter build errors:
1. Ensure you have the correct JDK installed (JDK 17)
2. Verify Android SDK version 34 is installed
3. Try "File > Invalidate Caches / Restart"
4. Run "Build > Clean Project" followed by "Build > Rebuild Project"

## Additional Notes
- The app uses Retrofit for API calls with a 30-second timeout
- OneSignal is integrated for push notifications
- The minimum supported Android version is 7.0 (API 24)
