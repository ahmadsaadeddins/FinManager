# People Sorting and Count Feature

## Overview
Enhanced the People screen with sorting functionality and improved count display. People with positive balances (green numbers) now appear above those with negative balances, and the count display shows both total people and those with positive balances.

## Features
- **Smart Sorting**: People with positive balances appear first in the list
- **Enhanced Count Display**: Shows total people count and positive balance count
- **Real-time Updates**: Sorting and counts update automatically when data changes
- **Search Integration**: Sorting and counts work with search filtering
- **Green Theme**: Uses MoneyIn color (green) for positive indicators

## Sorting Logic
1. **Primary Sort**: People with positive balances (≥ 0) appear first
2. **Secondary Sort**: Within each group, sorted by balance amount (highest first)
3. **Tertiary Sort**: People with same balance sorted alphabetically by name

## Implementation Details

### Files Modified:
1. **PeopleViewModel.kt** - Added sorting logic and `positiveBalanceCount` property
2. **PeopleScreen.kt** - Enhanced count display to show positive balance count

### Sorting Implementation:
```kotlin
balances.toList().sortedWith(
    compareByDescending<PersonWithBalance> { it.balance >= 0 }
        .thenByDescending { it.balance }
        .thenBy { it.person.name.lowercase() }
)
```

### UI Design:
- **Card Layout**: Light green background with enhanced information
- **Two-line Display**: 
  - "Total People" with total count
  - "Positive Balance: X" showing count of people with positive balances
- **Typography**: Clear hierarchy with different text sizes and colors

## Visual Example:
```
┌─────────────────────────────────┐
│  Total People              15   │
│  Positive Balance: 8            │  <- Green card with details
└─────────────────────────────────┘
┌─────────────────────────────────┐
│  🔍 Search people...            │
└─────────────────────────────────┘
│  John Doe              +$500.00 │  <- Positive balances first
│  Jane Smith            +$250.00 │
│  Bob Wilson             $0.00   │
│  ─────────────────────────────── │
│  Alice Brown           -$100.00 │  <- Negative balances after
│  Mike Davis            -$300.00 │
```

This enhancement makes it easier to identify people who owe money (positive balances) versus those who are owed money (negative balances), with positive balances prominently displayed first.