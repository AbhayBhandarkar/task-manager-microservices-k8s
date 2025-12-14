# Quick Start Guide - Step by Step

Follow these exact steps to run the updated task manager system with separate Teacher and Student UIs.

## Prerequisites Check

First, verify you have everything installed:

```powershell
# Check Java
java -version  # Should show Java 17 or higher

# Check Maven
mvn -version

# Check Docker
docker --version

# Check Minikube
minikube version

# Check kubectl
kubectl version --client

# Check Ollama
ollama --version
```

If any are missing, install them first.

---

## Step 1: Start Docker

```powershell
# If using Docker Desktop:
# Open Docker Desktop application and wait for it to start (whale icon in system tray)

# OR if using Colima:
colima start

# Verify Docker is running
docker ps
```

---

## Step 2: Install and Start Ollama (If Not Already Done)

```powershell
# If Ollama is not installed, download from https://ollama.ai/download
# Or install via winget:
winget install Ollama.Ollama

# Verify Ollama is running
curl http://localhost:11434/api/tags

# If not running, start it (keep this terminal open):
ollama serve
```

**In a NEW terminal window**, pull the required models:

```powershell
# Pull the CPU-optimized text model (~2.3GB, ~1.5-2B effective)
ollama pull phi3:mini

# Pull the smallest vision model for file analysis (~1.5GB, ~2B effective)
ollama pull llava:7b-q2_K

# Verify models are downloaded
ollama list
```

You should see both `llama3.2` and `llava:7b-q2_K` in the list.

---

## Step 3: Start Minikube

```powershell
# Start Minikube cluster
minikube start

# Wait for it to be ready (takes 1-2 minutes)
minikube status

# Should show all components as "Running"
```

---

## Step 4: Build Docker Images

**Important**: You must build images in Minikube's Docker environment since we made code changes.

```powershell
# Switch to Minikube's Docker environment
eval $(minikube docker-env)

# Navigate to project root
cd "C:\Users\Aditya Manojkumar\OneDrive\Desktop\task-manager-microservices-k8s"
```

### Build User Service

```powershell
cd user-service
mvn clean package -DskipTests
docker build -t user-service:1.0 .
cd ..
```

### Build Task Service

```powershell
cd task-service
mvn clean package -DskipTests
docker build -t task-service:1.0 .
cd ..
```

### Build Agent Service

```powershell
cd agent-service
mvn clean package -DskipTests
docker build -t agent-service:1.0 .
cd ..
```

### Build Frontend

```powershell
cd frontend
docker build -t frontend:1.0 .
cd ..
```

### Verify Images Were Built

```powershell
docker images | findstr -i "user-service task-service agent-service frontend"
```

You should see all four images with tag `1.0`.

---

## Step 5: Deploy to Kubernetes

```powershell
# Navigate to k8s-manifests directory
cd k8s-manifests

# Deploy all services (in order)
kubectl apply -f mongodb-deployment.yaml
kubectl apply -f user-service-deployment.yaml
kubectl apply -f task-service-deployment.yaml
kubectl apply -f agent-service-deployment.yaml
kubectl apply -f frontend-deployment.yaml

# Watch pods start (wait until all show "Running" and "1/1" ready)
kubectl get pods -w
```

**Press `Ctrl+C`** when all pods show:
- STATUS: `Running`
- READY: `1/1`

This may take 2-3 minutes. Expected output:
```
NAME                            READY   STATUS    RESTARTS   AGE
mongodb-xxx                     1/1     Running   0          2m
user-service-xxx                1/1     Running   0          2m
task-service-xxx                1/1     Running   0          2m
agent-service-xxx              1/1     Running   0          2m
frontend-xxx                    1/1     Running   0          2m
```

---

## Step 6: Access the Application

### Option A: Using minikube service (Recommended)

```powershell
# This will open the frontend in your browser
minikube service frontend
```

**Keep this terminal open** - closing it will stop the tunnel.

### Option B: Get URL manually

```powershell
# Get the frontend URL
minikube service frontend --url

# Copy the URL (e.g., http://127.0.0.1:xxxxx)
# Open it in your browser
```

---

## Step 7: Test the Application

### Access Teacher UI

1. In your browser, go to: `http://YOUR_URL/teacher.html`
   - Or click the "Switch to Teacher View" link if you're on the main page

2. **Create Students** (if needed):
   - The system will auto-create a default teacher
   - You can create students via API or they'll be created automatically

3. **Create a Task**:
   - Enter task title: "Complete Math Assignment"
   - Enter description: "Solve problems 1-10 from chapter 5"
   - Enter subject (optional): "Mathematics"
   - Select one or more students
   - Click "Create Task"

### Access Student UI

1. In your browser, go to: `http://YOUR_URL/student.html`
   - Or click the "Switch to Student View" link

2. **Select a Student** from the dropdown

3. **View Assigned Tasks**:
   - You should see the task you just created

4. **Upload a File**:
   - Click "Choose File" under a task
   - Select an image file (JPG, PNG) or document
   - Click "Upload & Analyze"
   - Wait for analysis (may take 10-30 seconds)
   - The system will:
     - Analyze the file using the vision model
     - Determine if task is completed
     - Optionally assign a grade
     - Update MongoDB automatically

5. **Check Results**:
   - Go back to Teacher UI
   - You should see:
     - File uploaded badge
     - Task done status
     - Grade (if assigned)

---

## Step 8: Verify Everything Works

### Check Service Logs

```powershell
# Check agent service (for vision model analysis)
kubectl logs deployment/agent-service --tail=50

# Check task service
kubectl logs deployment/task-service --tail=50

# Check user service
kubectl logs deployment/user-service --tail=50
```

### Test API Endpoints

```powershell
# Port-forward services for testing
kubectl port-forward service/user-service 8082:8082 &
kubectl port-forward service/task-service 8081:8081 &
kubectl port-forward service/agent-service 8083:8083 &

# Test user service
curl http://localhost:8082/api/users

# Test task service
curl http://localhost:8081/api/tasks

# Test agent service health
curl http://localhost:8083/api/agent/health
```

---

## Troubleshooting

### Problem: Pods not starting

```powershell
# Check pod status
kubectl get pods

# Check pod details
kubectl describe pod POD_NAME

# Check logs
kubectl logs POD_NAME
```

### Problem: Agent service can't connect to Ollama

```powershell
# Verify Ollama is running on host
curl http://localhost:11434/api/tags

# Check agent service logs
kubectl logs deployment/agent-service

# Test from inside the pod
kubectl exec -it $(kubectl get pod -l app=agent-service -o jsonpath='{.items[0].metadata.name}') -- sh
# Inside pod:
apk add curl
curl http://host.docker.internal:11434/api/tags
exit
```

### Problem: Vision model not found

```powershell
# Verify model is downloaded
ollama list

# If missing, pull it
ollama pull llava:7b-q2_K

# Restart agent service
kubectl rollout restart deployment/agent-service
```

### Problem: Frontend shows "Disconnected"

```powershell
# Check if all services are running
kubectl get pods

# Check service endpoints
kubectl get services

# Restart frontend
kubectl rollout restart deployment/frontend
```

### Problem: File upload fails

```powershell
# Check agent service logs
kubectl logs deployment/agent-service --tail=100

# Verify multipart is enabled (should be in application.properties)
# Check file size limits (currently 10MB)
```

---

## Stopping the System

### Quick Stop (Preserves Data)

```powershell
# Stop Minikube (keeps everything)
minikube stop

# Stop Docker Desktop or Colima
# Docker Desktop: Right-click whale icon → Quit
# Colima: colima stop
```

### Complete Cleanup

```powershell
# Delete all Kubernetes resources
cd k8s-manifests
kubectl delete -f .

# Stop Minikube
minikube stop

# Delete Minikube cluster (optional)
minikube delete
```

---

## Quick Reference

### Start Everything
```powershell
# 1. Start Docker
# 2. Start Ollama: ollama serve (in separate terminal)
# 3. Pull models: ollama pull llama3.2 && ollama pull llava:7b-q2_K
# 4. Start Minikube: minikube start
# 5. Build images: eval $(minikube docker-env) && [build commands above]
# 6. Deploy: kubectl apply -f k8s-manifests/
# 7. Access: minikube service frontend
```

### Check Status
```powershell
kubectl get pods
kubectl get services
minikube status
ollama list
```

### View Logs
```powershell
kubectl logs deployment/agent-service
kubectl logs deployment/task-service
kubectl logs deployment/user-service
```

---

## Expected Workflow

1. **Teacher** creates task → Saved to MongoDB
2. **Student** views task → Sees in their dashboard
3. **Student** uploads file → Sent to agent service
4. **Agent service** analyzes with vision model → Determines completion & grade
5. **MongoDB** updated automatically → Both UIs show updated status

---

**You're all set!** Follow these steps and you should have the system running with separate Teacher and Student UIs, and vision model analysis working.

