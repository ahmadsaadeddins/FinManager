# Transfer Between People Feature

## Overview
Added a transfer functionality that allows you to move money from one person to another. This automatically creates the correct transactions for both people, ensuring balances are calculated properly. The interface includes searchable dropdowns for easy person selection.

## Features
- **Keyboard-Friendly Search**: Type to search with suggestions that don't conflict with the keyboard
- **Smart Suggestions**: Shows up to 5 matching results as you type
- **Auto-Hide Suggestions**: Suggestions disappear when you focus on other fields
- **Clear Buttons**: Easy way to clear search and start over
- **Balance Display**: Shows current balances in the person suggestions
- **Automatic Calculations**: Creates correct transactions for both sender and receiver
- **Transfer Summary**: Preview of the transfer before confirming
- **Usage Tracking**: Automatically tracks usage for both people involved
- **Transaction History**: Creates proper transaction records with descriptions

## How to Use the Search

### Person Selection:
1. **Click on the field**: Start typing immediately
2. **Type to search**: Suggestions appear below the field (limited to 5 results)
3. **Tap suggestion**: Click on the person you want from the suggestion cards
4. **Clear if needed**: Use the X button to clear and search again
5. **Auto-hide**: Suggestions hide when you move to other fields

### Search Features:
- **No keyboard conflict**: Suggestions appear as cards below the field, not overlapping
- **Limited results**: Shows only 5 matches to keep the interface clean
- **Case-insensitive**: Search works regardless of capitalization
- **Partial matching**: Type part of a name to find matches
- **Real-time filtering**: Results update as you type
- **Balance visibility**: See each person's current balance in suggestions

## How It Works

### Transaction Logic:
1. **From Person**: Creates an "i_owe_them" transaction (money going out)
2. **To Person**: Creates a "they_owe_me" transaction (money coming in)
3. **Both transactions** have the same amount and timestamp
4. **Descriptions** include the other person's name for clarity

### Balance Impact:
- **From Person**: Their balance decreases (they owe you less / you owe them more)
- **To Person**: Their balance increases (they owe you more / you owe them less)

## Usage Instructions

1. **Open Transfer Dialog**: Tap the transfer icon (⇄) in the People screen top bar
2. **Search From Person**: Type to find who is giving the money
3. **Search To Person**: Type to find who is receiving the money (excludes the from person)
4. **Enter Amount**: Input the transfer amount
5. **Add Description**: Optional description (defaults to "Transfer")
6. **Review Summary**: Check the transfer details
7. **Confirm Transfer**: Tap "Transfer" to execute

## Example Scenarios

### Scenario 1: John pays back part of what he owes
- **Before**: John owes you $100 (balance: +$100)
- **Transfer**: $50 from John to Mike
- **After**: 
  - John owes you $50 (balance: +$50)
  - Mike owes you $50 more (balance increases by +$50)

### Scenario 2: Moving debt between people
- **Before**: 
  - Alice owes you $200 (balance: +$200)
  - You owe Bob $100 (balance: -$100)
- **Transfer**: $150 from Alice to Bob
- **After**:
  - Alice owes you $50 (balance: +$50)
  - You owe Bob less, Bob owes you $50 (balance: +$50)

## Search Tips

- **Quick Selection**: Type just the first few letters of a name
- **Limited Results**: Only 5 matches shown to keep interface clean
- **Clear and Retry**: Use the X button to clear and search for someone else
- **Balance Reference**: Use the displayed balances to confirm you're selecting the right person
- **Auto-Hide**: Suggestions automatically hide when you move to amount or description fields
- **No Keyboard Conflict**: Suggestions appear as cards below the field, not overlapping with keyboard

## Implementation Details

### Files Modified:
1. **PersonRepository.kt** - Added `transferBetweenPeople()` method
2. **PeopleViewModel.kt** - Added transfer functionality
3. **PeopleScreen.kt** - Added transfer button and searchable dialog

### Search Functionality:
- **Card-based suggestions**: Uses Card components instead of dropdown menus
- **Limited results**: `.take(5)` limits suggestions to 5 people maximum
- **Auto-hide logic**: Suggestions hide when focusing on amount/description fields
- **Case-insensitive search**: Uses `contains(query, ignoreCase = true)`
- **Dynamic exclusion**: "To Person" list excludes the selected "From Person"
- **Clear functionality**: X button resets search query and selection
- **Keyboard-friendly**: No overlay conflicts with on-screen keyboard

### UI Components:
- **Transfer Button**: SwapHoriz icon in top app bar
- **Search Fields**: Regular text fields with suggestion cards below
- **Suggestion Cards**: Card components showing person name and balance
- **Clear Buttons**: X icon to reset search
- **Amount Input**: Decimal keyboard for easy entry
- **Transfer Summary**: Preview card showing transaction details
- **Validation**: Ensures valid people and positive amount

This enhanced interface eliminates keyboard conflicts and provides a smooth, mobile-friendly search experience for finding people in your transfer dialog.