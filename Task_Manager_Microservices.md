# Microservices Kubernetes Project - Complete Guide

## 📋 Table of Contents
- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Project Structure](#project-structure)
- [Starting the Project](#starting-the-project)
- [Stopping the Project](#stopping-the-project)
- [Testing the APIs](#testing-the-apis)
- [Useful Commands](#useful-commands)
- [Troubleshooting](#troubleshooting)
- [What Was Built](#what-was-built)

---

## 🎯 Project Overview

A simple microservices-based Task Management System with:
- **User Service**: Manages users (CRUD operations)
- **Task Service**: Manages tasks (CRUD operations)
- **MongoDB**: Database for both services
- **Frontend**: Web UI dashboard for all operations
- **Kubernetes**: Orchestration using Minikube
- **Docker**: Containerization

**Cost**: Zero! Everything runs locally.

---

## 🏗️ Architecture

```
┌─────────────┐
│   Browser   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Frontend (UI)  │
│   (Nginx:80)    │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌─────────┐ ┌─────────┐
│  User   │ │  Task   │
│ Service │ │ Service │
│ :8082   │ │ :8081   │
└────┬────┘ └────┬────┘
     │           │
     └─────┬─────┘
           ▼
      ┌─────────┐
      │ MongoDB │
      │ :27017  │
      └─────────┘
```

All components run as Docker containers orchestrated by Kubernetes in Minikube.

---

## 💻 Prerequisites

These tools should already be installed on your MacOS:

- **Java 17** (OpenJDK)
- **Maven** (Build tool)
- **Docker Desktop** or **Colima** (Container runtime)
- **Minikube** (Local Kubernetes)
- **kubectl** (Kubernetes CLI)
- **VSCode** (IDE with Java extensions)

---

## 📁 Project Structure

```
~/Downloads/microservices-k8s/
│
├── task-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/task_service/
│   │       │   ├── TaskServiceApplication.java
│   │       │   ├── model/Task.java
│   │       │   ├── repository/TaskRepository.java
│   │       │   └── controller/TaskController.java
│   │       └── resources/
│   │           └── application.properties
│   ├── target/
│   │   └── task-service-0.0.1-SNAPSHOT.jar
│   ├── pom.xml
│   └── Dockerfile
│
├── user-service/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/user_service/
│   │       │   ├── UserServiceApplication.java
│   │       │   ├── model/User.java
│   │       │   ├── repository/UserRepository.java
│   │       │   └── controller/UserController.java
│   │       └── resources/
│   │           └── application.properties
│   ├── target/
│   │   └── user-service-0.0.1-SNAPSHOT.jar
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/
│   ├── index.html
│   ├── nginx.conf
│   └── Dockerfile
│
└── k8s-manifests/
    ├── mongodb-deployment.yaml
    ├── task-service-deployment.yaml
    ├── user-service-deployment.yaml
    └── frontend-deployment.yaml
```

---

## 🚀 Starting the Project

### Step 1: Start Docker/Colima

```bash
# If using Docker Desktop:
open -a Docker
# Wait for Docker whale icon to appear in menu bar (1-2 minutes)

# OR if using Colima:
colima start
```

### Step 2: Start Minikube

```bash
# Start Minikube cluster
minikube start

# Verify it's running
minikube status
```

Expected output:
```
minikube
type: Control Plane
host: Running
kubelet: Running
apiserver: Running
kubeconfig: Configured
```

### Step 3: Check if Images Exist (First Time Skip This)

```bash
# Use Minikube's Docker environment
eval $(minikube docker-env)

# Check if images exist
docker images | grep -E "(task-service|user-service|frontend)"
```

**If images don't exist** (first time or after cleanup):

```bash
# Build task-service
cd ~/Downloads/microservices-k8s/task-service
mvn clean package -DskipTests
docker build -t task-service:1.0 .

# Build user-service
cd ~/Downloads/microservices-k8s/user-service
mvn clean package -DskipTests
docker build -t user-service:1.0 .

# Build frontend
cd ~/Downloads/microservices-k8s/frontend
docker build -t frontend:1.0 .
```

### Step 4: Deploy to Kubernetes

```bash
cd ~/Downloads/microservices-k8s/k8s-manifests

# Deploy all services
kubectl apply -f mongodb-deployment.yaml
kubectl apply -f task-service-deployment.yaml
kubectl apply -f user-service-deployment.yaml
kubectl apply -f frontend-deployment.yaml

# Wait for all pods to be ready (may take 1-2 minutes)
kubectl get pods -w
```

Press `Ctrl+C` when all pods show `1/1` in READY column and `Running` in STATUS.

Expected output:
```
NAME                            READY   STATUS    RESTARTS   AGE
mongodb-xxx                     1/1     Running   0          2m
task-service-xxx                1/1     Running   0          2m
user-service-xxx                1/1     Running   0          2m
frontend-xxx                    1/1     Running   0          2m
```

### Step 5: Access the Dashboard

```bash
# Open the frontend dashboard in your browser
minikube service frontend
```

This will automatically open your browser with the dashboard URL (e.g., `http://127.0.0.1:xxxxx`)

**Keep this terminal open** - closing it will stop the tunnel.

✅ **Your application is now running!**

---

## 🛑 Stopping the Project

### Quick Stop (Preserves Everything)

```bash
# Stop Minikube (keeps all data and deployments)
minikube stop

# Stop Docker/Colima
# Docker Desktop: Click whale icon → Quit
# Colima:
colima stop
```

### Clean Stop (Removes Everything)

```bash
# Delete all Kubernetes resources
cd ~/Downloads/microservices-k8s/k8s-manifests
kubectl delete -f .

# Or delete Minikube cluster entirely
minikube delete

# Stop Docker/Colima (same as above)
```

---

## 🧪 Testing the APIs

### Using the Web Dashboard

1. Open: `minikube service frontend`
2. **Create Users**: Fill in name and email, click "Create User"
3. **Create Tasks**: Select user, add title/description, click "Create Task"
4. **Update**: Click "Edit" on users or "Complete/Mark Pending" on tasks
5. **Delete**: Click "Delete" buttons

### Using curl Commands

```bash
# Port-forward services (in separate terminals)
kubectl port-forward service/user-service 8082:8082 &
kubectl port-forward service/task-service 8081:8081 &

# Create a user
curl -X POST http://localhost:8082/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com"}'

# Get all users
curl http://localhost:8082/api/users

# Create a task (use user ID from above)
curl -X POST http://localhost:8081/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Learn K8s","description":"Deploy microservices","userId":"USER_ID_HERE","completed":false}'

# Get all tasks
curl http://localhost:8081/api/tasks

# Get tasks by user
curl http://localhost:8081/api/tasks/user/USER_ID_HERE

# Update a task
curl -X PUT http://localhost:8081/api/tasks/TASK_ID_HERE \
  -H "Content-Type: application/json" \
  -d '{"title":"Learn K8s","description":"Complete","userId":"USER_ID","completed":true}'

# Delete a task
curl -X DELETE http://localhost:8081/api/tasks/TASK_ID_HERE
```

### Using Postman

1. Install: `brew install --cask postman`
2. Create requests for the endpoints above
3. Base URLs:
   - User Service: `http://localhost:8082/api/users`
   - Task Service: `http://localhost:8081/api/tasks`

---

## 🛠️ Useful Commands

### Viewing Logs

```bash
# View logs for a specific service
kubectl logs deployment/user-service
kubectl logs deployment/task-service
kubectl logs deployment/mongodb
kubectl logs deployment/frontend

# Follow logs in real-time
kubectl logs -f deployment/user-service

# View last 50 lines
kubectl logs deployment/user-service --tail=50

# View logs for all containers with a label
kubectl logs -l app=user-service
```

### Checking Status

```bash
# View all pods
kubectl get pods

# View all services
kubectl get services

# View all deployments
kubectl get deployments

# View everything
kubectl get all

# Detailed pod information
kubectl describe pod POD_NAME

# Check Minikube status
minikube status

# Check Docker images in Minikube
eval $(minikube docker-env)
docker images
```

### Scaling Services

```bash
# Scale user-service to 3 replicas
kubectl scale deployment user-service --replicas=3

# Scale back to 1
kubectl scale deployment user-service --replicas=1

# Check scaled pods
kubectl get pods
```

### Restarting Services

```bash
# Restart a deployment (after code changes)
kubectl rollout restart deployment/user-service
kubectl rollout restart deployment/task-service
kubectl rollout restart deployment/frontend

# Check rollout status
kubectl rollout status deployment/user-service

# View rollout history
kubectl rollout history deployment/user-service
```

### Accessing MongoDB

```bash
# Port-forward MongoDB
kubectl port-forward service/mongodb 27017:27017

# In another terminal, install mongosh
brew install mongosh

# Connect to MongoDB
mongosh mongodb://localhost:27017

# MongoDB commands:
show dbs
use taskdb
show collections
db.tasks.find()

use userdb
db.users.find()
exit
```

### Debugging

```bash
# Execute command inside a pod
kubectl exec -it POD_NAME -- /bin/sh

# Get pod events
kubectl get events --sort-by=.metadata.creationTimestamp

# Check resource usage
kubectl top pods

# Describe service to see endpoints
kubectl describe service user-service

# Test connectivity between pods
kubectl run curl-test --image=curlimages/curl -i --rm --restart=Never -- \
  curl -s http://user-service:8082/api/users
```

### Cleanup

```bash
# Delete specific deployment
kubectl delete deployment user-service

# Delete specific service
kubectl delete service user-service

# Delete everything in k8s-manifests
cd ~/Downloads/microservices-k8s/k8s-manifests
kubectl delete -f .

# Clean up Minikube completely
minikube delete

# Remove Docker images
eval $(minikube docker-env)
docker rmi task-service:1.0
docker rmi user-service:1.0
docker rmi frontend:1.0
```

---

## 🔧 Troubleshooting

### Problem: Pods showing `ErrImageNeverPull`

**Solution**: Images weren't built in Minikube's Docker environment

```bash
eval $(minikube docker-env)
cd ~/Downloads/microservices-k8s/task-service
docker build -t task-service:1.0 .

cd ~/Downloads/microservices-k8s/user-service
docker build -t user-service:1.0 .

kubectl rollout restart deployment/task-service
kubectl rollout restart deployment/user-service
```

### Problem: Service returning 404

**Solution**: Controller/Repository not loaded

```bash
# Check logs for "Found X MongoDB repository interfaces"
kubectl logs deployment/user-service | grep "Found"

# Should show "Found 1" not "Found 0"
# If shows 0, check package names match in all Java files
```

### Problem: Minikube won't start

**Solution**: Docker isn't running

```bash
# Check Docker
docker ps

# If fails, start Docker Desktop or Colima
open -a Docker
# OR
colima start

# Then try again
minikube start
```

### Problem: Can't access frontend

**Solution**: Tunnel not running

```bash
# Make sure terminal is open with:
minikube service frontend

# Or get URL and open manually:
minikube service frontend --url
# Copy URL and paste in browser
```

### Problem: Frontend shows "Disconnected"

**Solution**: Service endpoints wrong or services not running

```bash
# Check if services are running
kubectl get pods

# Check service endpoints
kubectl get services

# Test connectivity from frontend pod
kubectl exec -it $(kubectl get pod -l app=frontend -o jsonpath='{.items[0].metadata.name}') -- sh
apk add curl
curl http://user-service:8082/api/users
curl http://task-service:8081/api/tasks
exit
```

### Problem: Data disappears after restart

**Explanation**: MongoDB data is not persisted (by design for this simple setup)

**Solution** (if you want persistent data):
Add persistent volume to mongodb-deployment.yaml (advanced topic)

---

## 📚 What Was Built

### Technologies Used

| Technology | Purpose | Port |
|------------|---------|------|
| Java 17 | Programming language | - |
| Spring Boot 3.5.7 | Microservices framework | - |
| Spring Data MongoDB | Database integration | - |
| Maven | Build tool | - |
| MongoDB 7.0 | NoSQL database | 27017 |
| Docker | Containerization | - |
| Kubernetes (Minikube) | Orchestration | - |
| Nginx | Web server for frontend | 80 |
| HTML/CSS/JavaScript | Frontend UI | - |

### API Endpoints

#### User Service (Port 8082)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create new user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |

#### Task Service (Port 8081)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | Get all tasks |
| GET | `/api/tasks/{id}` | Get task by ID |
| GET | `/api/tasks/user/{userId}` | Get tasks by user |
| POST | `/api/tasks` | Create new task |
| PUT | `/api/tasks/{id}` | Update task |
| DELETE | `/api/tasks/{id}` | Delete task |

### Kubernetes Resources

| Resource | Type | Replicas | Purpose |
|----------|------|----------|---------|
| mongodb | Deployment + Service | 1 | Database |
| user-service | Deployment + Service | 1 | User management API |
| task-service | Deployment + Service | 1 | Task management API |
| frontend | Deployment + Service | 1 | Web UI |

---

## 🎓 Learning Outcomes

By completing this project, you've learned:

✅ How to create Spring Boot microservices  
✅ How to containerize Java applications with Docker  
✅ How to deploy applications to Kubernetes  
✅ How to connect microservices to MongoDB  
✅ How to create Kubernetes deployments and services  
✅ How to build a frontend that consumes REST APIs  
✅ How to use kubectl for managing Kubernetes resources  
✅ How to debug containerized applications  
✅ How to work with Minikube for local development  
✅ Microservices architecture patterns  

---

## 📝 Quick Reference Card

### Start Everything
```bash
# 1. Start Docker
open -a Docker  # or: colima start

# 2. Start Minikube
minikube start

# 3. Deploy (if not already deployed)
cd ~/Downloads/microservices-k8s/k8s-manifests
kubectl apply -f .

# 4. Open dashboard
minikube service frontend
```

### Stop Everything
```bash
# 1. Stop Minikube
minikube stop

# 2. Stop Docker
# Docker Desktop: Click quit
# Colima: colima stop
```

### Check Status
```bash
kubectl get pods
kubectl get services
minikube status
```

### View Logs
```bash
kubectl logs deployment/user-service
kubectl logs deployment/task-service
```

### Rebuild Service
```bash
eval $(minikube docker-env)
cd ~/Downloads/microservices-k8s/SERVICE_NAME
mvn clean package -DskipTests
docker build -t SERVICE_NAME:1.0 .
kubectl rollout restart deployment/SERVICE_NAME
```

---

## 🌟 Next Steps (Optional Enhancements)

1. **Add Authentication**: Implement JWT-based authentication
2. **Add Persistent Storage**: Use PersistentVolumes for MongoDB
3. **Add API Gateway**: Use Spring Cloud Gateway
4. **Add Service Discovery**: Use Eureka or Consul
5. **Add Monitoring**: Use Prometheus and Grafana
6. **Add Logging**: Use ELK stack (Elasticsearch, Logstash, Kibana)
7. **Add CI/CD**: Use GitHub Actions or Jenkins
8. **Deploy to Cloud**: AWS EKS, Google GKE, or Azure AKS

---

## 📞 Support

If you encounter issues:

1. Check the [Troubleshooting](#troubleshooting) section
2. View service logs: `kubectl logs deployment/SERVICE_NAME`
3. Check pod status: `kubectl describe pod POD_NAME`
4. Verify Docker is running: `docker ps`
5. Verify Minikube is running: `minikube status`

---

**Project Location**: `~/Downloads/microservices-k8s/`

**Last Updated**: November 2, 2025

**Status**: ✅ Complete and Working

---

**Happy Coding! 🚀**