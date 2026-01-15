# Privacy Feature - Number Masking

## Overview
Added a privacy feature that allows users to hide financial numbers on the home screen by replacing them with stars (*).

## Features
- **Toggle Button**: Eye icon in the top app bar to toggle between showing and hiding numbers
- **Star Masking**: When enabled, all digits in financial amounts are replaced with asterisks
- **Persistent Setting**: The preference is saved using DataStore and persists across app sessions
- **Currency Format Preservation**: Currency symbols and formatting are maintained while hiding the actual numbers

## Implementation Details

### Files Added/Modified:
1. **UserPreferences.kt** - DataStore implementation for saving the toggle state
2. **NumberFormatter.kt** - Utility for formatting numbers with star masking
3. **HomeViewModel.kt** - Added preference handling and toggle function
4. **HomeScreen.kt** - Added toggle button and updated card display logic
5. **build.gradle.kts** & **libs.versions.toml** - Added DataStore dependency

### How It Works:
- When the eye icon is tapped, it toggles between visibility states
- The `NumberFormatter.formatCurrency()` function handles the star replacement
- All digits (0-9) are replaced with asterisks while preserving currency symbols and commas
- The setting is automatically saved and restored when the app is reopened

### Usage:
1. Open the app and navigate to the home screen
2. Tap the eye icon in the top right corner
3. Numbers will be replaced with stars (e.g., "$1,234.56" becomes "$*,***.***")
4. Tap again to show numbers normally

## Example Output:
- **Normal**: $1,234.56
- **Hidden**: $*,***.***