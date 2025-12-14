# File Upload Troubleshooting Guide

## Issue: Unable to Upload Files

### Fixed Issues:
1. ✅ Added CORS support to agent-service
2. ✅ Enhanced error logging in file upload endpoint
3. ✅ Improved nginx proxy configuration for file uploads
4. ✅ Added better error handling in student.html

### Current Status:
- Agent service: Running with CORS enabled
- Frontend: Updated with better error logging
- Nginx: Configured for 10MB file uploads

### Required: Pull Ollama Models

**Ollama is running but has NO models installed.** You need to pull the vision model:

```powershell
# Make sure Ollama is running
# In a separate PowerShell window:
ollama serve

# Then pull the required models:
ollama pull phi3:mini
ollama pull llava:7b-q2_K

# Verify models are installed:
ollama list
```

You should see:
```
NAME              ID            SIZE      MODIFIED
phi3:mini         ...           ...       ...
llava:7b-q2_K     ...           ...       ...
```

### Testing File Upload:

1. **Access Student UI:** `http://localhost:31465`
2. **Select a student**
3. **Select a task assigned to that student**
4. **Choose a file** (image or PDF)
5. **Click "Upload & Analyze"**

### Check Browser Console:

Open browser DevTools (F12) → Console tab to see:
- Upload request details
- Response status
- Any error messages

### Check Agent Service Logs:

```powershell
kubectl logs deployment/agent-service --tail=50 -f
```

Look for:
- `Analyzing task with file: id=...` - confirms upload received
- `File bytes read: X bytes` - confirms file processing
- Any error messages

### Common Issues:

1. **Ollama models not installed** → Pull models (see above)
2. **File too large** → Must be < 10MB
3. **Wrong file type** → Use images (jpg, png) or PDFs
4. **Network error** → Check if agent-service pod is running

### Service Ports:

- Teacher Frontend: `http://localhost:30718`
- Student Frontend: `http://localhost:31465`
- Agent Service: Port 8083 (internal)

---

**Next Steps:**
1. Pull Ollama models (phi3:mini and llava:7b-q2_K)
2. Try uploading a file again
3. Check browser console for errors
4. Check agent-service logs if still not working

