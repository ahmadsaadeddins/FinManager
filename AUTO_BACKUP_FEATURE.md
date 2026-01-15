# Automatic Backup on Exit Feature (Enhanced)

## Overview
Enhanced automatic backup functionality that reliably backs up your data to Google Drive when you exit or minimize the app. Uses multiple backup triggers to ensure your financial data is always safely stored in the cloud.

## Features
- **Multiple Backup Triggers**: Uses Activity lifecycle, Application lifecycle, and WorkManager
- **Reliable Backup**: Works when app is minimized, closed, or killed by system
- **User Control**: Toggle to enable/disable auto backup in settings
- **Google Account Integration**: Uses your signed-in Google account for backup
- **Background Operation**: Backup happens in the background without blocking the app
- **Network Aware**: Only backs up when internet connection is available

## How It Works (Multi-Layer Approach)

### 1. Activity Lifecycle Backup:
- **onPause()**: Triggers when app goes to background
- **onStop()**: Triggers when app is no longer visible
- **onDestroy()**: Final backup attempt when app is finishing

### 2. Application Lifecycle Backup:
- **ProcessLifecycleOwner**: Monitors entire app lifecycle
- **onStop()**: Triggers when app moves to background
- More reliable than Activity lifecycle

### 3. WorkManager Backup (Fallback):
- **Background Worker**: Runs even if app is killed
- **Network Constraints**: Only runs when connected to internet
- **Retry Logic**: Automatically retries if backup fails
- **System Managed**: Android system ensures execution

### Requirements:
1. **Google Account**: Must be signed in to Google Drive in the Backup screen
2. **Auto Backup Enabled**: Toggle must be turned on in Backup settings
3. **Internet Connection**: Required for uploading to Google Drive

## Backup Triggers

### When Backup Happens:
- **App Minimized**: When you press home button or switch apps
- **App Closed**: When you swipe away the app or use back button
- **System Kill**: When Android kills the app to free memory
- **Phone Restart**: WorkManager reschedules backup after reboot

### Backup Methods (in order of execution):
1. **Immediate Thread**: Fast backup in separate thread
2. **Application Lifecycle**: App-level background detection
3. **WorkManager**: System-managed background task with retry

## Technical Implementation

### Files Added/Modified:
1. **MainActivity.kt**:
   - Added `onPause()`, `onStop()`, and enhanced `onDestroy()`
   - Uses separate thread for non-blocking backup
   - Multiple backup triggers for reliability

2. **FinancialManagerApplication.kt** (NEW):
   - Application-level lifecycle observer
   - ProcessLifecycleOwner integration
   - WorkManager backup scheduling

3. **AutoBackupWorker.kt** (NEW):
   - Hilt-enabled WorkManager worker
   - Network-aware backup execution
   - Retry logic for failed backups

4. **UserPreferences.kt**:
   - Auto backup and Google account preferences
   - Persistent settings storage

5. **BackupScreen.kt** & **BackupViewModel.kt**:
   - Auto backup settings UI
   - Google account integration

### Dependencies Added:
- **WorkManager**: For reliable background backup
- **Hilt Work**: For dependency injection in workers
- **ProcessLifecycleOwner**: For app-level lifecycle monitoring

### Backup Process:
1. **Check Settings**: Verifies auto backup is enabled
2. **Check Account**: Ensures Google account is configured
3. **Check Network**: WorkManager ensures internet connectivity
4. **Initialize Service**: Sets up Google Drive service
5. **Create Backup**: Uploads database to Google Drive
6. **Retry Logic**: WorkManager retries failed backups
7. **Log Results**: Records success/failure in logs

## Reliability Features

### Multiple Triggers:
- **Primary**: Activity lifecycle (onPause/onStop)
- **Secondary**: Application lifecycle (ProcessLifecycleOwner)
- **Fallback**: WorkManager background task

### Error Handling:
- **Graceful Failure**: Continues app operation if backup fails
- **Retry Logic**: WorkManager automatically retries failed backups
- **Network Awareness**: Only attempts backup when connected
- **Logging**: Detailed logs for troubleshooting

### System Integration:
- **Battery Optimization**: WorkManager respects system battery settings
- **Doze Mode**: Works even when device is in deep sleep
- **App Standby**: Backup continues even if app is in standby
- **Boot Recovery**: WorkManager reschedules after device restart

## Usage Instructions

### First Time Setup:
1. Open the app and go to **Backup & Restore**
2. Tap **"Sign in with Google"** and choose your account
3. The **"Auto Backup on Exit"** toggle will be enabled by default
4. Your data will now backup automatically when you exit/minimize the app

### Testing the Feature:
1. **Minimize App**: Press home button - backup should trigger
2. **Close App**: Swipe away from recent apps - backup should trigger
3. **Check Logs**: Use `adb logcat | grep "AutoBackup"` to see backup activity
4. **Verify Backups**: Go to Backup screen and refresh to see new backups

### Troubleshooting:
- **No Internet**: Backup will retry when connection is restored
- **Battery Optimization**: Disable battery optimization for the app
- **Background Restrictions**: Allow background activity for the app
- **Storage Space**: Ensure Google Drive has sufficient space

## Benefits
- **Maximum Reliability**: Multiple backup methods ensure data is saved
- **System Integration**: Works with Android's power management
- **User Friendly**: Automatic operation without user intervention
- **Network Efficient**: Only backs up when connected to internet
- **Battery Conscious**: Respects system battery optimization settings

This enhanced implementation provides the most reliable automatic backup possible on Android, ensuring your financial data is always protected regardless of how the app is closed or what system restrictions are in place.