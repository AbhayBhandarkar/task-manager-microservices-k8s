# 🎓 Task Manager Microservices with Kubernetes & AI Analysis

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-1.34-blue.svg)](https://kubernetes.io/)
[![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green.svg)](https://www.mongodb.com/)
[![Ollama](https://img.shields.io/badge/Ollama-LLM-purple.svg)](https://ollama.ai/)

## 📋 Table of Contents
- [Project Overview](#-project-overview)
- [Key Features](#-key-features)
- [Architecture](#-architecture)
- [Prerequisites](#-prerequisites)
- [Quick Start](#-quick-start)
- [MongoDB Commands](#-mongodb-commands)
- [API Endpoints](#-api-endpoints)
- [MVC Architecture](#-mvc-architecture)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Project Overview

A production-ready microservices-based Task Management System with AI-powered analysis featuring:

### Core Services
- **Eureka Server** (Port 8761): Service discovery and registration
- **API Gateway** (Port 8080): Centralized routing and load balancing
- **User Service** (Port 8082): User management with role-based access
- **Task Service** (Port 8081): Complete CRUD operations with assignment tracking
- **Agent Service** (Port 8083): AI-powered task analysis using Ollama LLM
- **MongoDB** (Port 27017): Persistent storage (userdb & taskdb)
- **Teacher Frontend** & **Student Frontend**: Role-specific dashboards

---

## ✨ Key Features

### 🎯 Complete CRUD Operations
- ✅ Create, Read, Update, Delete for Users and Tasks
- ✅ Real-time updates across dashboards
- ✅ Confirmation dialogs for destructive operations
- ✅ Edit buttons with inline prompts
- ✅ Delete with MongoDB cascade

### 🤖 AI-Powered Task Analysis (Persistent)
- ✅ **Persistent Analysis**: AI feedback saved in MongoDB
- ✅ **Analysis Fields**: `analysisReasoning`, `analysisRecommendation`, `analysisConfidence`
- ✅ **File Upload Support**: Submit assignments for AI review
- ✅ **Survives Reload**: Analysis persists in database
- ✅ **CPU-Friendly Mode**: Optimized for local development
- ✅ **Confidence Scores**: 0.0-1.0 reliability indicators

### 👥 Role-Based Dashboards

**Teacher Dashboard:**
- Create and assign tasks to students
- Analyze student submissions
- Grade assignments
- Edit/Delete tasks
- View all student tasks

**Student Dashboard:**
- View assigned tasks
- Upload assignment files
- View persistent AI analysis
- Track completion status
- Edit/Delete own tasks

### 🏗️ Enterprise Architecture
- **Service Discovery**: Eureka Server for dynamic registration
- **API Gateway**: Single entry point with load balancing
- **Microservices**: Spring Boot independent services
- **MongoDB Persistence**: All data saved in database
- **Centralized Logging**: Slf4j with detailed logs
- **Health Monitoring**: Built-in health checks

---

## 🏗️ Architecture

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                         VIEW LAYER                          │
│  ┌──────────────────┐  ┌──────────────────┐                │
│  │  student.html    │  │  teacher.html    │   (Frontend)   │
│  │  (Nginx:8081)    │  │  (Nginx:8082)    │                │
│  └──────────────────┘  └──────────────────┘                │
└─────────────────────────────────────────────────────────────┘
                          ↓ HTTP Requests
┌─────────────────────────────────────────────────────────────┐
│                   API GATEWAY (Port 8080)                   │
│              Routes requests to microservices                │
└─────────────────────────────────────────────────────────────┘
                          ↓ Service Discovery
┌─────────────────────────────────────────────────────────────┐
│              EUREKA SERVER (Port 8761)                      │
│              Service Registry & Discovery                    │
└─────────────────────────────────────────────────────────────┘
                          ↓ Load Balancing
┌─────────────────────────────────────────────────────────────┐
│                   CONTROLLER LAYER                          │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐       │
│  │UserController│ │TaskController│ │AgentController│       │
│  │  (Port 8082) │ │  (Port 8081) │ │  (Port 8083) │       │
│  │   @Slf4j     │ │   @Slf4j     │ │   @Slf4j     │       │
│  └──────────────┘ └──────────────┘ └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
                          ↓ Business Logic
┌─────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                            │
│                 ┌────────────────────────┐                  │
│                 │ TaskAnalysisService    │                  │
│                 │ OllamaService          │                  │
│                 │ (AI Analysis Logic)    │                  │
│                 └────────────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
                          ↓ Data Access
┌─────────────────────────────────────────────────────────────┐
│               REPOSITORY LAYER (DAO)                        │
│  ┌──────────────┐ ┌──────────────┐                         │
│  │UserRepository│ │TaskRepository│                         │
│  │ (MongoRepo)  │ │ (MongoRepo)  │                         │
│  └──────────────┘ └──────────────┘                         │
└─────────────────────────────────────────────────────────────┘
                          ↓ Persistence
┌─────────────────────────────────────────────────────────────┐
│                    MODEL/ENTITY LAYER                       │
│  ┌──────────────┐ ┌──────────────────────────────┐        │
│  │  User.java   │ │       Task.java              │        │
│  │  @Document   │ │  @Document                   │        │
│  │  fields:     │ │  fields:                     │        │
│  │  - id        │ │  - id, title, description    │        │
│  │  - name      │ │  - teacherId, studentIds     │        │
│  │  - email     │ │  - fileUploaded, taskDone    │        │
│  │  - role      │ │  - analysisReasoning ✨      │        │
│  └──────────────┘ │  - analysisRecommendation ✨ │        │
│                   │  - analysisConfidence ✨     │        │
│                   └──────────────────────────────┘        │
└─────────────────────────────────────────────────────────────┘
                          ↓ Storage
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                           │
│  MongoDB (Port 27017)                                       │
│  ├── userdb.users (User collection)                        │
│  └── taskdb.tasks (Task collection with AI analysis)       │
└─────────────────────────────────────────────────────────────┘
                          ↓ AI Analysis
┌─────────────────────────────────────────────────────────────┐
│                   OLLAMA LLM (Host)                         │
│         http://host.docker.internal:11434                   │
│  Models: phi3:mini, moondream, gemma3:1b                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 💻 Prerequisites

### Required Software (macOS)

- **Java 17** (OpenJDK) - `brew install openjdk@17`
- **Maven 3.9+** - `brew install maven`
- **Docker Desktop** - [Download](https://www.docker.com/products/docker-desktop)
- **Minikube** - `brew install minikube`
- **kubectl** - `brew install kubectl`
- **Ollama** - [Install from ollama.ai](https://ollama.ai)

### Verify Installation

```bash
java -version    # Should show 17.x
mvn -version     # Should show 3.9+
docker --version
minikube version
kubectl version --client
ollama --version
```

---

## 🚀 Quick Start

### 1. Start Docker & Minikube

```bash
# Start Docker Desktop
open -a Docker

# Start Minikube
minikube start

# Verify
minikube status
```

### 2. Install Ollama Models

```bash
# Start Ollama (in separate terminal)
ollama serve

# Pull models (in another terminal)
ollama pull phi3:mini      # 2.2GB - Fast text analysis
ollama pull moondream      # 1.7GB - Vision model
ollama pull gemma3:1b      # 815MB - Lightweight model

# Verify
ollama list
```

### 3. Build Services

```bash
cd /path/to/task-manager-microservices-k8s

# Use Minikube's Docker
eval $(minikube docker-env)

# Build all services
for service in eureka-server api-gateway user-service task-service agent-service; do
    cd $service
    mvn clean package -DskipTests
    docker build -t $service:1.0 .
    cd ..
done

# Build frontends
docker build -f frontend/Dockerfile-teacher -t teacher-frontend:1.0 frontend/
docker build -f frontend/Dockerfile-student -t student-frontend:1.0 frontend/
```

### 4. Deploy to Kubernetes

```bash
cd k8s-manifests

# Deploy all services
kubectl apply -f mongodb-deployment.yaml
kubectl apply -f eureka-server-deployment.yaml
kubectl apply -f api-gateway-deployment.yaml
kubectl apply -f user-service-deployment.yaml
kubectl apply -f task-service-deployment.yaml
kubectl apply -f agent-service-deployment.yaml
kubectl apply -f teacher-frontend-deployment.yaml
kubectl apply -f student-frontend-deployment.yaml

# Wait for all pods
kubectl get pods -w
```

### 5. Access Dashboards

```bash
# Open Teacher Dashboard
minikube service teacher-frontend

# Open Student Dashboard (in new terminal)
minikube service student-frontend

# Open Eureka Server
minikube service eureka-server
```

✅ **Your application is now running!**

---

## 🗄️ MongoDB Commands

### View All Data

```bash
# Get MongoDB pod name
export MONGO_POD=$(kubectl get pods -l app=mongodb -o jsonpath='{.items[0].metadata.name}')

# Show all databases
kubectl exec $MONGO_POD -- mongosh --quiet --eval "show dbs"

# Show all users
kubectl exec $MONGO_POD -- mongosh --quiet userdb --eval "db.users.find().toArray()"

# Show all tasks with analysis
kubectl exec $MONGO_POD -- mongosh --quiet taskdb --eval "db.tasks.find().toArray()"

# Show tasks in pretty format
kubectl exec $MONGO_POD -- mongosh --quiet taskdb --eval "db.tasks.find().pretty()"

# Count documents
kubectl exec $MONGO_POD -- mongosh --quiet userdb --eval "db.users.countDocuments()"
kubectl exec $MONGO_POD -- mongosh --quiet taskdb --eval "db.tasks.countDocuments()"
```

### Query Specific Data

```bash
# Find task by ID
kubectl exec $MONGO_POD -- mongosh --quiet taskdb --eval "db.tasks.findOne({_id: ObjectId('YOUR_TASK_ID')})"

# Find tasks with AI analysis
kubectl exec $MONGO_POD -- mongosh --quiet taskdb --eval "db.tasks.find({analysisReasoning: {\$exists: true}}).toArray()"

# Find tasks by student
kubectl exec $MONGO_POD -- mongosh --quiet taskdb --eval "db.tasks.find({studentIds: 'STUDENT_ID'}).toArray()"
```

### Interactive MongoDB Shell

```bash
# Enter MongoDB shell
kubectl exec -it $MONGO_POD -- mongosh

# Inside shell:
show dbs
use taskdb
db.tasks.find()
db.tasks.find({taskDone: true})
use userdb
db.users.find()
exit
```

### Clear All Data

```bash
# Drop all data (WARNING: Irreversible!)
kubectl exec $MONGO_POD -- mongosh --quiet userdb --eval "db.dropDatabase();"
kubectl exec $MONGO_POD -- mongosh --quiet taskdb --eval "db.dropDatabase();"
```

---

## 📡 API Endpoints

### Via API Gateway (Recommended)

**Base URL:** `http://localhost:8080`

First, port-forward the gateway:
```bash
kubectl port-forward service/api-gateway 8080:8080
```

#### User Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users` | Get all users |
| GET | `/api/users/{id}` | Get user by ID |
| POST | `/api/users` | Create user |
| PUT | `/api/users/{id}` | Update user |
| DELETE | `/api/users/{id}` | Delete user |

**Create User Example:**
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@example.com","role":"student"}'
```

#### Task Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/tasks` | Get all tasks |
| GET | `/api/tasks/{id}` | Get task by ID |
| GET | `/api/tasks/student/{id}` | Get tasks by student |
| GET | `/api/tasks/teacher/{id}` | Get tasks by teacher |
| POST | `/api/tasks` | Create task |
| PUT | `/api/tasks/{id}` | Update task (with analysis) |
| DELETE | `/api/tasks/{id}` | Delete task |

**Create Task Example:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Math Homework",
    "description": "Complete Chapter 5",
    "teacherId": "TEACHER_ID",
    "studentIds": ["STUDENT_ID"],
    "subject": "Mathematics"
  }'
```

**Update Task with Analysis:**
```bash
curl -X PUT http://localhost:8080/api/tasks/TASK_ID \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Math Homework",
    "taskDone": true,
    "analysisReasoning": "Student completed all problems correctly",
    "analysisRecommendation": "Excellent work, ready for next chapter",
    "analysisConfidence": 0.95
  }'
```

#### Agent Service

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/agent/health` | Health check |
| POST | `/api/agent/analyze` | Analyze task |
| POST | `/api/agent/analyze-file` | Analyze with file |

**Analyze Task:**
```bash
curl -X POST http://localhost:8080/api/agent/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "id": "TASK_ID",
    "title": "Math Homework",
    "description": "Complete Chapter 5",
    "completed": true
  }'
```

**Upload File for Analysis:**
```bash
curl -X POST http://localhost:8080/api/agent/analyze-file \
  -F "file=@/path/to/assignment.pdf" \
  -F "taskId=TASK_ID" \
  -F "title=Math Homework" \
  -F "description=Chapter 5 solutions"
```

---

## 🏛️ MVC Architecture

This project implements **Spring Boot MVC architecture** across microservices:

### Model Layer (Entity)
```java
// User.java
@Data
@Document(collection = "users")
public class User {
    @Id private String id;
    private String name;
    private String email;
    private String role; // "teacher" or "student"
}

// Task.java
@Data
@Document(collection = "tasks")
public class Task {
    @Id private String id;
    private String title;
    private String description;
    // ... other fields
    private String analysisReasoning;      // ✨ AI analysis
    private String analysisRecommendation; // ✨ AI recommendation
    private Double analysisConfidence;     // ✨ Confidence score
}
```

### Repository Layer (Data Access)
```java
public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
}

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByStudentIdsContaining(String studentId);
    List<Task> findByTeacherId(String teacherId);
}
```

### Service Layer (Business Logic)
```java
@Service
@Slf4j
public class TaskAnalysisService {
    // AI analysis logic
    public Mono<TaskAnalysis> analyzeTaskWithFile(...) {
        // Saves analysis to MongoDB
        return updateTaskInMongoDBWithAnalysis(...);
    }
}
```

### Controller Layer (REST API)
```java
@RestController
@RequestMapping("/api/tasks")
@Slf4j
public class TaskController {
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable String id, @RequestBody Task task) {
        log.info("Updating task with analysis...");
        return taskRepository.save(task);
    }
}
```

### View Layer (Frontend)
- **student.html**: JavaScript fetches from `/api/tasks`, displays analysis
- **teacher.html**: JavaScript creates tasks, triggers analysis

---

## 🔧 Useful Commands

### Service Management

```bash
# Restart a service
kubectl rollout restart deployment/task-service

# Scale a service
kubectl scale deployment/user-service --replicas=3

# View logs
kubectl logs deployment/task-service --tail=50 -f

# Execute command in pod
kubectl exec -it POD_NAME -- /bin/bash
```

### Debugging

```bash
# Check pod status
kubectl get pods
kubectl describe pod POD_NAME

# Check service endpoints
kubectl get services
kubectl describe service user-service

# Test connectivity
kubectl run curl-test --image=curlimages/curl -i --rm --restart=Never -- \
  curl -s http://user-service:8082/api/users

# View events
kubectl get events --sort-by=.metadata.creationTimestamp
```

### Cleanup

```bash
# Delete all deployments
kubectl delete -f k8s-manifests/

# Delete Minikube cluster
minikube delete

# Remove Docker images
eval $(minikube docker-env)
docker rmi task-service:1.0 user-service:1.0 agent-service:1.0
```

---

## 🐛 Troubleshooting

### Issue: Analysis not persisting

**Solution:** Check if analysis fields are in Task model:
```bash
kubectl logs deployment/task-service | grep "analysisReasoning"
```

### Issue: Can't connect to Ollama

**Solution:** Verify Ollama is running on host:
```bash
curl http://localhost:11434/api/tags

# If fails, start Ollama:
ollama serve
```

### Issue: Frontend not loading tasks

**Solution:** Check browser console and verify API Gateway:
```bash
kubectl logs deployment/api-gateway --tail=20
kubectl port-forward service/api-gateway 8080:8080
curl http://localhost:8080/api/tasks
```

### Issue: Pods not starting

**Solution:** Check pod events:
```bash
kubectl describe pod POD_NAME
kubectl logs POD_NAME
```

---

## 📚 Technologies Used

| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 17 | Programming language |
| Spring Boot | 3.5.7 | Microservices framework |
| Spring Cloud | 2023.0.0 | Eureka, Gateway, LoadBalancer |
| MongoDB | 7.0 | NoSQL database |
| Docker | Latest | Containerization |
| Kubernetes | 1.34 | Orchestration |
| Minikube | Latest | Local K8s cluster |
| Nginx | Alpine | Web server |
| Ollama | Latest | Local LLM runtime |
| Lombok | Latest | Boilerplate reduction |

---

## 🎓 Learning Outcomes

✅ Microservices architecture with Spring Boot  
✅ Service discovery with Eureka  
✅ API Gateway pattern  
✅ MongoDB integration with Spring Data  
✅ Docker containerization  
✅ Kubernetes orchestration  
✅ REST API design  
✅ AI integration with Ollama  
✅ MVC architecture  
✅ Role-based frontends  
✅ Persistent data storage  
✅ Full CRUD operations  

---

## 📞 Support

**Issue?** Check:
1. [Troubleshooting](#-troubleshooting) section
2. Service logs: `kubectl logs deployment/SERVICE_NAME`
3. MongoDB data: `kubectl exec $MONGO_POD -- mongosh`
4. Eureka dashboard: `minikube service eureka-server`

---

## 🚀 Next Steps

- [ ] Add JWT authentication
- [ ] Add PersistentVolumes
- [ ] Add Prometheus monitoring
- [ ] Add ELK logging stack
- [ ] Deploy to cloud (EKS/GKE/AKS)
- [ ] Add CI/CD pipeline
- [ ] Add unit tests
- [ ] Add API documentation (Swagger)

---

**Last Updated:** December 20, 2025  
**Status:** ✅ Production Ready  
**Author:** Abhay Bhandarkar

---

**⭐ Star this repo if you found it helpful!**
