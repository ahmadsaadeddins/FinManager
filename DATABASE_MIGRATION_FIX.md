# Database Migration Fix - Sales Archive Schema Update

## Issue
When adding the sales archive feature, we added new fields (`isArchived` and `archivedAt`) to the `OutTransaction` entity but forgot to update the database version number. This caused a Room schema mismatch error:

```
Room cannot verify the data integrity. Looks like you've changed schema but forgot to update the version number.
Expected identity hash: f0c48d07da5b4e3531662acca4ce4b4f
Found: 9e06623593141ff2b4d684378261f005
```

## Root Cause
- **Schema Change**: Added `isArchived: Boolean` and `archivedAt: Long?` fields to `OutTransaction`
- **Missing Version Update**: Database version remained at 4 instead of incrementing to 5
- **Missing Migration**: No migration script to add the new columns to existing databases

## Solution Applied

### 1. Database Version Update
**File**: `app/src/main/java/com/financialmanager/app/data/database/AppDatabase.kt`
- **Changed**: `version = 4` → `version = 5`
- **Added**: `MIGRATION_4_5` to migrations list

### 2. New Migration Script
**File**: `app/src/main/java/com/financialmanager/app/data/database/Migrations.kt`
- **Added**: `MIGRATION_4_5` migration script
- **SQL Commands**:
  ```sql
  ALTER TABLE out_transactions ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0
  ALTER TABLE out_transactions ADD COLUMN archivedAt INTEGER
  ```

### 3. Import Updates
**File**: `app/src/main/java/com/financialmanager/app/data/database/AppDatabase.kt`
- **Added**: Import statements for all migration objects

## Migration Details

### MIGRATION_4_5 Script
```kotlin
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add archive fields to out_transactions table for sales archive feature
        database.execSQL("ALTER TABLE out_transactions ADD COLUMN isArchived INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE out_transactions ADD COLUMN archivedAt INTEGER")
    }
}
```

### Field Mappings
- **`isArchived`**: 
  - Type: `INTEGER NOT NULL DEFAULT 0`
  - Kotlin: `Boolean = false`
  - Purpose: Marks if transaction is archived
  
- **`archivedAt`**: 
  - Type: `INTEGER` (nullable)
  - Kotlin: `Long? = null`
  - Purpose: Timestamp when transaction was archived

## Database Migration Process

### For Existing Users
1. **App Update**: User updates to new version
2. **Database Open**: Room detects version change (4 → 5)
3. **Migration Run**: `MIGRATION_4_5` executes automatically
4. **Schema Update**: New columns added with default values
5. **App Continues**: Sales archive feature now available

### For New Users
1. **Fresh Install**: Database created with version 5 schema
2. **All Fields Present**: New columns included from start
3. **No Migration Needed**: Direct creation with latest schema

## Default Values Strategy

### `isArchived = false` (0)
- **Existing Transactions**: All marked as active (not archived)
- **New Transactions**: Default to active status
- **Backward Compatibility**: Existing sales remain visible

### `archivedAt = null`
- **Existing Transactions**: No archive timestamp (never archived)
- **New Transactions**: Null until archived
- **Archive Operation**: Set to current timestamp when archived

## Testing Scenarios

### Scenario 1: Fresh Install
- ✅ Database created with version 5
- ✅ All fields present from start
- ✅ Sales archive feature works immediately

### Scenario 2: Upgrade from Version 4
- ✅ Migration runs automatically
- ✅ Existing transactions remain visible
- ✅ New archive fields added with defaults
- ✅ Sales archive feature becomes available

### Scenario 3: Archive Operations
- ✅ Archive all sales: `isArchived = true`, `archivedAt = timestamp`
- ✅ Current sales total becomes $0.00
- ✅ Archived sales visible in archive tab
- ✅ Restore functionality works correctly

## Error Prevention

### Future Schema Changes
1. **Always increment version number** when changing entities
2. **Create migration script** for each version change
3. **Test migration** with existing data
4. **Add imports** for new migration objects

### Migration Best Practices
- **Use ALTER TABLE** for adding columns
- **Provide DEFAULT values** for NOT NULL columns
- **Test with real data** before release
- **Document migration purpose** in comments

## Status: FIXED ✅

The database migration issue is now resolved:
- ✅ Database version updated to 5
- ✅ Migration script created and tested
- ✅ New archive fields added properly
- ✅ Existing data preserved with defaults
- ✅ Sales archive feature fully functional
- ✅ No data loss or corruption

## Next Steps
Users can now:
1. **Update the app** without losing data
2. **Use sales archive feature** immediately after update
3. **Archive existing sales** with proper database support
4. **Restore archived sales** as needed

The migration ensures a smooth transition for all users while enabling the new sales archive functionality.