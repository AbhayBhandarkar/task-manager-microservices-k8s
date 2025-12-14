# EXACT STEP-BY-STEP GUIDE - Copy & Paste Ready

Follow these steps **exactly in order**. Copy and paste each command.

---

## STEP 1: Open PowerShell

1. Press `Windows Key + X`
2. Click "Windows PowerShell" or "Terminal"
3. You should see a command prompt

---

## STEP 2: Check Prerequisites

Type these commands one by one and press Enter:

```powershell
java -version
```

**Expected**: Should show Java 17 or higher. If error, install Java first.

```powershell
mvn -version
```

**Expected**: Should show Maven version. If error, install Maven first.

```powershell
docker --version
```

**Expected**: Should show Docker version. If error, install Docker Desktop.

```powershell
minikube version
```

**Expected**: Should show Minikube version. If error, install Minikube.

```powershell
kubectl version --client
```

**Expected**: Should show kubectl version. If error, install kubectl.

```powershell
ollama --version
```

**Expected**: Should show Ollama version. If error, download from https://ollama.ai/download

**If any command fails, install that tool first before continuing.**

---

## STEP 3: Start Docker Desktop

1. Open Docker Desktop application (search "Docker Desktop" in Windows)
2. Wait for the whale icon to appear in system tray (bottom right)
3. Wait until it says "Docker Desktop is running"

**Verify Docker is running:**

```powershell
docker ps
```

**Expected**: Should show a table (may be empty, that's fine). If error, Docker isn't running.

---

## STEP 4: Install/Start Ollama

### Check if Ollama is running:

```powershell
curl http://localhost:11434/api/tags
```

**If you get JSON response**: Ollama is running, skip to Step 5.

**If you get an error**: Continue below.

### Start Ollama:

Open a **NEW PowerShell window** (keep the first one open):

```powershell
ollama serve
```

**Keep this window open!** Don't close it. Minimize it if needed.

### Go back to your first PowerShell window and verify:

```powershell
curl http://localhost:11434/api/tags
```

**Expected**: Should return JSON with models list (may be empty `[]`, that's fine).

---

## STEP 5: Pull Required Models

**In your main PowerShell window** (not the one running `ollama serve`):

```powershell
ollama pull phi3:mini
```

**Wait for download** (takes 2-5 minutes, ~2.3GB). You'll see progress.

```powershell
ollama pull llava:7b-q2_K
```

**Wait for download** (takes 2-5 minutes, ~1.5GB). You'll see progress.

### Verify models downloaded:

```powershell
ollama list
```

**Expected**: Should show both `phi3:mini` and `llava:7b-q2_K` in the list.

---

## STEP 6: Start Minikube

```powershell
minikube start
```

**Wait 1-2 minutes**. You'll see it starting up.

### Verify Minikube is running:

```powershell
minikube status
```

**Expected**: Should show:
```
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
```

---

## STEP 7: Navigate to Project Directory

```powershell
cd "C:\Users\Aditya Manojkumar\OneDrive\Desktop\task-manager-microservices-k8s"
```

**Verify you're in the right place:**

```powershell
pwd
```

**Expected**: Should show the path ending with `task-manager-microservices-k8s`

---

## STEP 8: Switch to Minikube Docker Environment

```powershell
minikube docker-env | Invoke-Expression
```

**Verify it worked:**

```powershell
docker images
```

**Expected**: Should show images (may be empty or have some, that's fine).

---

## STEP 9: Build User Service

```powershell
cd user-service
```

```powershell
mvn clean package -DskipTests
```

**Wait 1-2 minutes**. You'll see Maven building.

```powershell
docker build -t user-service:1.0 .
```

**Wait 30 seconds**. You'll see Docker building.

```powershell
cd ..
```

---

## STEP 10: Build Task Service

```powershell
cd task-service
```

```powershell
mvn clean package -DskipTests
```

**Wait 1-2 minutes**.

```powershell
docker build -t task-service:1.0 .
```

**Wait 30 seconds**.

```powershell
cd ..
```

---

## STEP 11: Build Agent Service

```powershell
cd agent-service
```

```powershell
mvn clean package -DskipTests
```

**Wait 1-2 minutes**.

```powershell
docker build -t agent-service:1.0 .
```

**Wait 30 seconds**.

```powershell
cd ..
```

---

## STEP 12: Build Frontend

```powershell
cd frontend
```

```powershell
docker build -t frontend:1.0 .
```

**Wait 30 seconds**.

```powershell
cd ..
```

---

## STEP 13: Verify All Images Built

```powershell
docker images | Select-String "user-service|task-service|agent-service|frontend"
```

**Expected**: Should show 4 images:
- `user-service:1.0`
- `task-service:1.0`
- `agent-service:1.0`
- `frontend:1.0`

**If any are missing, go back and rebuild that service.**

---

## STEP 14: Deploy to Kubernetes

```powershell
cd k8s-manifests
```

### Deploy MongoDB:

```powershell
kubectl apply -f mongodb-deployment.yaml
```

**Expected**: Should say `deployment.apps/mongodb created` and `service/mongodb created`

### Deploy User Service:

```powershell
kubectl apply -f user-service-deployment.yaml
```

**Expected**: Should say `deployment.apps/user-service created` and `service/user-service created`

### Deploy Task Service:

```powershell
kubectl apply -f task-service-deployment.yaml
```

**Expected**: Should say `deployment.apps/task-service created` and `service/task-service created`

### Deploy Agent Service:

```powershell
kubectl apply -f agent-service-deployment.yaml
```

**Expected**: Should say `deployment.apps/agent-service created` and `service/agent-service created`

### Deploy Frontend:

```powershell
kubectl apply -f frontend-deployment.yaml
```

**Expected**: Should say `deployment.apps/frontend created` and `service/frontend created`

---

## STEP 15: Wait for Pods to Start

```powershell
kubectl get pods -w
```

**Watch the output**. You'll see pods starting up. Wait until ALL pods show:
- **STATUS**: `Running`
- **READY**: `1/1`

**This takes 2-3 minutes**. Example of what you want to see:

```
NAME                            READY   STATUS    RESTARTS   AGE
mongodb-xxx                     1/1     Running   0          2m
user-service-xxx                1/1     Running   0          2m
task-service-xxx                1/1     Running   0          2m
agent-service-xxx              1/1     Running   0          2m
frontend-xxx                    1/1     Running   0          2m
```

**When all show `1/1 Running`, press `Ctrl+C` to stop watching.**

---

## STEP 16: Access the Application

```powershell
minikube service frontend
```

**This will:**
1. Open your default web browser
2. Show you a URL like `http://127.0.0.1:xxxxx`

**KEEP THIS TERMINAL OPEN!** Closing it will stop the connection.

---

## STEP 17: Test Teacher UI

1. In your browser, you should see the main page
2. Click the link that says **"Switch to Teacher View"** OR
3. Manually go to: `http://YOUR_URL/teacher.html`

**You should see:**
- "Teacher Dashboard" heading
- Form to create tasks
- Lists of students and tasks

---

## STEP 18: Test Student UI

1. In your browser, click **"Switch to Student View"** OR
2. Manually go to: `http://YOUR_URL/student.html`

**You should see:**
- "Student Dashboard" heading
- Dropdown to select student
- List of tasks

---

## STEP 19: Create a Test Task (Teacher UI)

1. Go to Teacher UI (`/teacher.html`)
2. In the "Create New Task" form:
   - **Task Title**: Type `Test Assignment`
   - **Description**: Type `Complete this test task`
   - **Subject**: Type `Mathematics` (optional)
   - **Assign to Students**: Check at least one student checkbox
3. Click **"Create Task"** button

**Expected**: You should see a success message and the task appears in the list below.

---

## STEP 20: Upload File as Student

1. Go to Student UI (`/student.html`)
2. **Select a Student** from the dropdown
3. You should see the task you just created
4. Under the task, click **"Choose File"**
5. Select an image file (JPG, PNG) from your computer
6. Click **"Upload & Analyze"** button

**Wait 10-30 seconds** (vision model analyzing on CPU - this takes time!)

**Expected**: 
- Success message appears
- Task shows "File Uploaded" badge
- Task shows "Task Done" status (if analysis determines it's complete)
- May show a grade

---

## STEP 21: Verify in Teacher UI

1. Go back to Teacher UI (`/teacher.html`)
2. Look at the task you created
3. You should now see:
   - 📎 **File Uploaded** badge
   - ✓ **Task Done** badge (if analysis determined completion)
   - **Grade** (if assigned by vision model)

---

## STEP 22: Check Logs (Optional - for troubleshooting)

If something doesn't work, check logs:

```powershell
# Check agent service logs (for vision model)
kubectl logs deployment/agent-service --tail=50

# Check task service logs
kubectl logs deployment/task-service --tail=50

# Check user service logs
kubectl logs deployment/user-service --tail=50
```

---

## TROUBLESHOOTING

### Problem: Pods not starting

```powershell
# Check pod status
kubectl get pods

# Check specific pod details
kubectl describe pod POD_NAME

# Check pod logs
kubectl logs POD_NAME
```

### Problem: Can't access frontend

```powershell
# Get frontend URL manually
minikube service frontend --url

# Copy the URL and paste in browser
```

### Problem: Vision model not working

```powershell
# Verify Ollama is running
curl http://localhost:11434/api/tags

# Verify models are downloaded
ollama list

# Check agent service logs
kubectl logs deployment/agent-service --tail=100
```

### Problem: Images not building

```powershell
# Make sure you're in Minikube Docker environment
minikube docker-env | Invoke-Expression

# Verify
docker images
```

---

## STOPPING THE SYSTEM

### Quick Stop (Preserves Data):

```powershell
minikube stop
```

Then close Docker Desktop.

### Complete Cleanup:

```powershell
cd k8s-manifests
kubectl delete -f .
minikube stop
```

---

## QUICK REFERENCE - All Commands in Order

Copy and paste these in sequence:

```powershell
# 1. Start Docker Desktop (manually)

# 2. Start Ollama (in separate terminal)
ollama serve

# 3. Pull models
ollama pull phi3:mini
ollama pull llava:7b-q2_K
ollama list

# 4. Start Minikube
minikube start
minikube status

# 5. Navigate and setup
cd "C:\Users\Aditya Manojkumar\OneDrive\Desktop\task-manager-microservices-k8s"
minikube docker-env | Invoke-Expression

# 6. Build services
cd user-service
mvn clean package -DskipTests
docker build -t user-service:1.0 .
cd ..

cd task-service
mvn clean package -DskipTests
docker build -t task-service:1.0 .
cd ..

cd agent-service
mvn clean package -DskipTests
docker build -t agent-service:1.0 .
cd ..

cd frontend
docker build -t frontend:1.0 .
cd ..

# 7. Verify images
docker images | Select-String "user-service|task-service|agent-service|frontend"

# 8. Deploy
cd k8s-manifests
kubectl apply -f mongodb-deployment.yaml
kubectl apply -f user-service-deployment.yaml
kubectl apply -f task-service-deployment.yaml
kubectl apply -f agent-service-deployment.yaml
kubectl apply -f frontend-deployment.yaml

# 9. Wait for pods
kubectl get pods -w
# Press Ctrl+C when all show 1/1 Running

# 10. Access
minikube service frontend
```

---

**That's it! Follow these steps exactly and your system will be running!**

If you get any errors, check the troubleshooting section or share the error message.

