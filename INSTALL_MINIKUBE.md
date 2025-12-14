# How to Install Minikube on Windows

## Method 1: Using Chocolatey (Easiest)

If you have Chocolatey installed:

```powershell
choco install minikube
```

## Method 2: Using winget (Windows Package Manager)

```powershell
winget install minikube
```

## Method 3: Manual Installation (If above don't work)

### Step 1: Download Minikube

1. Go to: https://minikube.sigs.k8s.io/docs/start/
2. Click "Download" for Windows
3. Or direct download: https://github.com/kubernetes/minikube/releases/latest
4. Download `minikube-windows-amd64.exe`

### Step 2: Install Minikube

1. Rename the downloaded file to `minikube.exe`
2. Create a folder: `C:\minikube`
3. Move `minikube.exe` to `C:\minikube\`
4. Add to PATH:
   - Press `Windows Key`, type "Environment Variables"
   - Click "Edit the system environment variables"
   - Click "Environment Variables" button
   - Under "System variables", find "Path" and click "Edit"
   - Click "New" and add: `C:\minikube`
   - Click "OK" on all windows

### Step 3: Verify Installation

**Close and reopen PowerShell**, then:

```powershell
minikube version
```

Should show version number.

---

## Also Install kubectl (Required)

Minikube needs kubectl. Install it:

### Using winget:

```powershell
winget install kubectl
```

### Using Chocolatey:

```powershell
choco install kubernetes-cli
```

### Manual Installation:

1. Download from: https://kubernetes.io/docs/tasks/tools/install-kubectl-windows/
2. Or direct: https://dl.k8s.io/release/v1.28.0/bin/windows/amd64/kubectl.exe
3. Save as `kubectl.exe` in `C:\minikube\` (same folder as minikube)
4. PATH is already set from above

### Verify kubectl:

**Close and reopen PowerShell**, then:

```powershell
kubectl version --client
```

Should show version.

---

## Quick Install Script (All-in-One)

Run this in PowerShell as Administrator:

```powershell
# Install Minikube
winget install minikube

# Install kubectl
winget install kubectl

# Close and reopen PowerShell, then verify:
minikube version
kubectl version --client
```

---

## After Installation

1. **Close and reopen PowerShell** (important for PATH to update)
2. Verify both are installed:

```powershell
minikube version
kubectl version --client
```

3. If both work, continue with Step 6 in EXACT_STEPS.md

---

## Troubleshooting

### "minikube: command not found" after installation

**Solution**: Close and reopen PowerShell. PATH updates require new session.

### Still not working?

1. Check if files exist:
   ```powershell
   Test-Path "C:\minikube\minikube.exe"
   Test-Path "C:\minikube\kubectl.exe"
   ```

2. Check PATH:
   ```powershell
   $env:PATH -split ';' | Select-String "minikube"
   ```

3. If PATH doesn't include minikube, add it manually (see Method 3, Step 2 above)

### Need Administrator Rights?

Some installations require admin. Right-click PowerShell and select "Run as Administrator"

### Chocolatey Permission Errors

If you see errors like:
- "Access to the path 'C:\ProgramData\chocolatey\lib-bad' is denied"
- "Unable to obtain lock file access"
- "Chocolatey detected you are not running from an elevated command shell"

**Solution 1: Run PowerShell as Administrator (Recommended)**

1. Close current PowerShell window
2. Right-click PowerShell icon
3. Select "Run as Administrator"
4. Run the install command again:
   ```powershell
   choco install minikube
   ```

**Solution 2: Clean up Chocolatey lock files (if Solution 1 doesn't work)**

If you still get lock file errors after running as admin:

1. **Run PowerShell as Administrator**
2. Remove the lock file:
   ```powershell
   Remove-Item "C:\ProgramData\chocolatey\lib\c1eedec61dc9e765bad7820a51b7d76298cbb8e7" -Force -ErrorAction SilentlyContinue
   ```
3. Clean up lib-bad directory:
   ```powershell
   Remove-Item "C:\ProgramData\chocolatey\lib-bad" -Recurse -Force -ErrorAction SilentlyContinue
   ```
4. Try installation again:
   ```powershell
   choco install minikube
   ```

**Solution 3: Use winget instead (No admin issues)**

If Chocolatey continues to have issues, use Windows Package Manager (winget):

1. Open PowerShell (doesn't need admin for winget)
2. Install minikube:
   ```powershell
   winget install minikube
   ```
3. Install kubectl:
   ```powershell
   winget install kubectl
   ```

This usually works without permission issues.

