# Sales Archive Feature

## Overview
Added a comprehensive sales archive system that allows users to archive all current sales transactions, resetting the current sales total to $0.00 while preserving historical sales data in an archive.

## Features

### 🗄️ Sales Archive System
- **Archive All Sales**: One-click button to archive all current sales transactions
- **Reset Current Total**: After archiving, current sales total becomes $0.00
- **Preserve History**: All archived sales are preserved with archive timestamps
- **Restore Capability**: Individual archived sales can be restored to active status

### 📊 Dual View System
- **Active Sales Tab**: Shows current (non-archived) sales transactions
- **Archived Sales Tab**: Shows all previously archived sales transactions
- **Summary Cards**: Display totals and counts for both active and archived sales

### 🔄 Archive Operations
- **Bulk Archive**: Archive all current sales at once
- **Individual Restore**: Restore specific archived sales back to active
- **Confirmation Dialog**: Prevents accidental archiving with detailed preview
- **Status Feedback**: Success/error messages for all operations

## Implementation Details

### Database Changes

#### 1. OutTransaction Entity
- **Added Fields**:
  - `isArchived: Boolean = false` - Marks if transaction is archived
  - `archivedAt: Long? = null` - Timestamp when archived

#### 2. TransactionDao Updates
- **Modified Queries**: All existing queries now filter `WHERE isArchived = 0`
- **New Archive Queries**:
  - `getArchivedSales()` - Get all archived sales
  - `getTotalArchivedSales()` - Sum of archived sales amounts
  - `archiveAllSales()` - Archive all current sales
  - `unarchiveTransaction()` - Restore archived transaction
  - `getActiveSalesCount()` - Count of active sales

#### 3. TransactionRepository
- **New Methods**: Added all archive-related repository methods
- **Backward Compatibility**: Existing methods work unchanged (only show active transactions)

### UI Components

#### 1. SalesArchiveScreen
- **Tab Interface**: Switch between Active and Archived sales
- **Summary Cards**: Visual overview of sales totals
- **Archive Button**: Prominent archive action in top bar
- **Transaction Lists**: Detailed view of sales with restore options

#### 2. SalesArchiveViewModel
- **State Management**: Handles archive/restore operations
- **Data Flows**: Reactive data for active and archived sales
- **Error Handling**: Comprehensive error states and messages

#### 3. Navigation Integration
- **New Route**: `Screen.SalesArchive`
- **Access Point**: Archive button in Transactions screen top bar

## User Experience

### 📱 Access Path
1. **Transactions** screen → **Archive button** (top bar)
2. Or navigate directly to Sales Archive

### 🎯 Archive Workflow
1. **View Current Sales**: See active sales total and count
2. **Archive Confirmation**: Dialog shows what will be archived
3. **One-Click Archive**: All current sales moved to archive
4. **Reset Confirmation**: Current sales total now shows $0.00

### 📋 Archive Management
- **View Archived Sales**: Switch to "Archived Sales" tab
- **Restore Individual Sales**: Click "Restore" button on any archived sale
- **Track Archive History**: See when each sale was archived

## Use Cases

### 1. Monthly Sales Reset
- Archive all sales at month-end
- Start fresh with $0.00 for new month
- Keep historical data for reporting

### 2. Quarterly Reporting
- Archive sales by quarter
- Generate period-specific reports
- Maintain clean current sales view

### 3. Seasonal Business
- Archive off-season sales
- Focus on current season performance
- Preserve historical seasonal data

### 4. Mistake Correction
- Archive incorrect sales entries
- Clean up current sales total
- Restore if needed later

## Benefits

### 📈 Business Benefits
- **Clean Reporting**: Current sales show only relevant period
- **Historical Tracking**: All sales data preserved forever
- **Flexible Periods**: Archive on any schedule (daily, weekly, monthly)
- **Error Recovery**: Restore accidentally archived sales

### 🔧 Technical Benefits
- **Database Efficiency**: Queries focus on active data
- **Data Integrity**: No data loss, only status changes
- **Scalable**: Archive system handles any volume
- **Backward Compatible**: Existing features work unchanged

## Archive Process Details

### What Gets Archived
- **Only Sales**: Expenses are not affected by archive operation
- **All Current Sales**: Every non-archived sale transaction
- **Metadata Preserved**: All original transaction data kept
- **Archive Timestamp**: Records when archiving occurred

### What Happens After Archive
- **Current Sales Total**: Becomes $0.00
- **Reports**: Show only active (non-archived) sales
- **Transaction Lists**: Show only active sales by default
- **Archive Access**: Archived sales accessible via archive screen

### Restore Process
- **Individual Restore**: Only one transaction at a time
- **Status Change**: `isArchived` becomes `false`, `archivedAt` becomes `null`
- **Immediate Effect**: Restored sale appears in current totals
- **No Data Loss**: All original transaction data preserved

## Status: IMPLEMENTED ✅ - Compilation Fixed

The sales archive feature is now fully functional:
- ✅ Archive all current sales with one click
- ✅ Reset current sales total to $0.00 after archiving
- ✅ View archived sales in separate tab
- ✅ Restore individual archived sales
- ✅ Confirmation dialogs prevent accidents
- ✅ Summary cards show totals for both active and archived
- ✅ Integrated with existing navigation
- ✅ Backward compatible with all existing features
- ✅ **FIXED**: Compilation errors resolved (added initial values to collectAsState calls)

## Next Steps
Users can now:
1. Navigate to **Transactions** → **Archive button**
2. View current sales total and count
3. Archive all sales with confirmation
4. See sales total reset to $0.00
5. Access archived sales anytime
6. Restore individual sales if needed

Perfect for monthly sales resets, quarterly reporting, or any period-based sales tracking!