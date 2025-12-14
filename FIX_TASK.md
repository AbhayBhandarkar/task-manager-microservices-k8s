# Quick Fix to Mark Task as Complete

## Option 1: Use the Button (Easiest)
1. Refresh Student Dashboard (Ctrl+F5)
2. Look for yellow box with "⚠️ File uploaded but task not marked as complete"
3. Click the green "✓ Mark as Complete" button

## Option 2: Browser Console Script
If the button doesn't appear, open browser console (F12) and paste this:

```javascript
async function fixTask() {
    const taskId = "693d177496ce80c5d7cf05e2";
    const task = await fetch(`/api/tasks/${taskId}`).then(r => r.json());
    task.taskDone = true;
    task.completed = true;
    await fetch(`/api/tasks/${taskId}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(task)
    });
    alert('Task marked as complete! Refresh the page.');
}
fixTask();
```

## Root Cause: File Upload Issue
The `/api/agent/analyze-file` endpoint is **never being called** when you upload a file. Instead, something is calling `/api/agent/analyze` (which doesn't handle files).

### To Debug:
1. Open browser console (F12) before uploading
2. Click "Upload & Analyze" button
3. Check console for:
   - "Uploading file: ... to /api/agent/analyze-file"
   - Any error messages
   - Network tab → see which endpoint is actually called

### Possible Issues:
- Nginx routing problem
- JavaScript error preventing upload
- CORS issue
- Form submission issue

