# Google Sign-In Setup Guide

## Step 1: Enable Google Drive API (REQUIRED - CURRENT ISSUE)

The Google Drive API must be enabled in your Google Cloud Console project.

1. Open the [Google Drive API Enable Page](https://console.developers.google.com/apis/api/drive.googleapis.com/overview?project=122441764238)
   - Or go to [Google Cloud Console](https://console.cloud.google.com/)
   - Select your project: **122441764238** (financialmanager-483012)
   - Navigate to **APIs & Services** → **Library**
   - Search for "Google Drive API"
   - Click on "Google Drive API"
   - Click **ENABLE**

2. Wait 2-3 minutes for the API to be fully enabled

**This must be done before the backup feature will work!**

## Step 2: Add Test Users (For OAuth Sign-In)

### Go to Google Cloud Console
1. Open [Google Cloud Console](https://console.cloud.google.com/)
2. Select your project: **financialmanager-483012** (ID: 122441764238)
3. Navigate to **APIs & Services** → **OAuth consent screen**

### Add Test Users
1. Scroll down to the **Test users** section
2. Click **+ ADD USERS**
3. Add the email address: `Ahmadsaadedin@gmail.com`
4. Click **ADD**

### Try Signing In Again
After adding your email as a test user, wait a few minutes for the changes to propagate, then try signing in again from your app.

---

## Quick Setup Checklist

Follow these steps in order:

1. ✅ **Enable Google Drive API** (See Step 1 above)
   - [Direct Link](https://console.developers.google.com/apis/api/drive.googleapis.com/overview?project=122441764238)
   
2. ✅ **Add Test Users** (See Step 2 above)
   - Add `Ahmadsaadedin@gmail.com` to test users
   
3. ✅ **Wait 2-3 minutes** for changes to propagate
   
4. ✅ **Try the app again**

## Important Notes

### For Development/Testing:
- Keep the OAuth consent screen in **Testing** mode
- Add all test user emails to the test users list
- Testing mode allows up to 100 test users

### For Production:
If you want to publish your app for all users:
1. Complete the OAuth consent screen verification process
2. This requires providing:
   - App information (name, logo, support email)
   - Scopes justification (explain why you need Drive API access)
   - Privacy policy and terms of service URLs
   - Verification process can take several days/weeks

### Alternative: Use Service Account (For Backend/Server Operations)
If you're doing server-side operations, consider using a Service Account instead of OAuth. However, for client-side Android apps, OAuth is the recommended approach.

## Current Configuration
- **Project ID**: financialmanager-483012
- **Client ID**: 122441764238-3gt2ti48nphfq3lgtos5eeufpvg6piqd.apps.googleusercontent.com
- **Scope**: Drive API (file access)

## Troubleshooting

### Error: "Google Drive API has not been used... or it is disabled"
**Solution**: Enable the Google Drive API (Step 1 above)
- Make sure you clicked "ENABLE" and it shows as enabled
- Wait 2-3 minutes after enabling
- Try again

### Error: "access_denied" or "has not completed the Google verification process"
**Solution**: Add your email as a test user (Step 2 above)
- Wait 5-10 minutes after adding test users
- Clear app data/cache and try again
- Verify the email address is exactly correct (case-sensitive)
- Make sure you're using the correct Google account

### Error: "SERVICE_DISABLED"
**Solution**: The API is disabled. Enable it following Step 1 above.

### Need to add more test users?
Just repeat Step 2 for each additional email address.

