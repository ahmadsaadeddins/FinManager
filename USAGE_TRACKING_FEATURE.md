# Usage Tracking Feature

## Overview
Enhanced the People screen with usage tracking functionality. People you interact with frequently now appear at the top of their respective balance groups (positive/negative), making it easier to find frequently accessed contacts.

## Features
- **Usage Tracking**: Automatically tracks how often you access each person
- **Smart Sorting**: Frequently used people appear first within their balance group
- **Visual Indicators**: Star icon appears next to people with high usage (>5 interactions)
- **Persistent Data**: Usage statistics are saved in the database
- **Balance Priority**: Positive balances still appear above negative balances

## Sorting Priority (in order):
1. **Balance Type**: Positive balances first, then negative balances
2. **Usage Frequency**: Most frequently used people first within each balance group
3. **Recent Usage**: Most recently used people first among those with same usage count
4. **Balance Amount**: Higher balances first within same usage level
5. **Alphabetical**: Name sorting as final tiebreaker

## Implementation Details

### Database Changes:
- **PersonAccount Entity**: Added `usageCount` and `lastUsedAt` fields
- **Database Migration**: MIGRATION_3_4 adds the new fields with default values
- **Database Version**: Updated from 3 to 4

### Files Modified:
1. **PersonAccount.kt** - Added usage tracking fields
2. **PersonDao.kt** - Added `incrementUsage()` method
3. **PersonRepository.kt** - Added `incrementPersonUsage()` method
4. **PeopleViewModel.kt** - Enhanced sorting logic and added usage tracking
5. **PeopleScreen.kt** - Added usage tracking on card clicks and star indicator
6. **AppDatabase.kt** - Updated version and added migration
7. **Migrations.kt** - Added MIGRATION_3_4 for new fields

### Usage Tracking:
- **Automatic**: Usage is tracked when you tap on a person card
- **Incremental**: Each interaction increments the usage counter
- **Timestamp**: Records when the person was last accessed
- **Visual Feedback**: Star icon appears for frequently used people (>5 uses)

### Visual Indicators:
- **Star Icon**: Green star appears next to people with >5 interactions
- **Sorting**: Frequently used people appear at the top of their balance group
- **Consistent**: Maintains the positive/negative balance grouping

## Example Sorting:
```
POSITIVE BALANCES (Green):
⭐ John Doe (used 10 times)     +$500.00
⭐ Jane Smith (used 8 times)    +$300.00
   Bob Wilson (used 2 times)    +$100.00
   
NEGATIVE BALANCES (Red):
⭐ Alice Brown (used 12 times)  -$200.00
   Mike Davis (used 1 time)     -$150.00
```

This enhancement makes it much easier to find and interact with your most important contacts while maintaining the financial priority of positive balances.