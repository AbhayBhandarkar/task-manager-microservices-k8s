# Eureka Server and API Gateway Setup Guide

This guide explains how to set up and deploy the Eureka Server and API Gateway for service discovery and routing.

## Architecture Overview

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
         ▼
┌─────────────────┐
│  API Gateway    │
│    :8080        │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌─────────┐ ┌─────────┐ ┌─────────┐
│  User   │ │  Task   │ │ Agent  │
│ Service │ │ Service │ │ Service│
│ :8082   │ │ :8081   │ │ :8083  │
└────┬────┘ └────┬────┘ └────┬───┘
     │           │            │
     └─────┬─────┘            │
           ▼                  │
      ┌─────────┐             │
      │ MongoDB │             │
      │ :27017  │             │
      └─────────┘             │
                              │
                         ┌────▼────┐
                         │ Ollama  │
                         │ :11434  │
                         │ (Host)  │
                         └─────────┘
                              │
                         ┌────▼────┐
                         │ Eureka  │
                         │ :8761   │
                         │ Server  │
                         └─────────┘
```

## Components Added

1. **Eureka Server** (Port 8761)
   - Service discovery server
   - Web UI dashboard to view registered services
   - Access at: `http://localhost:<NodePort>/`

2. **API Gateway** (Port 8080)
   - Spring Cloud Gateway
   - Routes requests to services via Eureka
   - Load balancing support

3. **Updated Services**
   - All services (user-service, task-service, agent-service) now register with Eureka
   - Services can discover each other through Eureka

## Building the Services

### 1. Build Eureka Server

**PowerShell:**
```powershell
cd eureka-server
mvn clean package -DskipTests
docker build -t eureka-server:1.0 .
```

**Bash:**
```bash
cd eureka-server
mvn clean package -DskipTests
docker build -t eureka-server:1.0 .
```

### 2. Build API Gateway

**PowerShell:**
```powershell
cd api-gateway
mvn clean package -DskipTests
docker build -t api-gateway:1.0 .
```

**Bash:**
```bash
cd api-gateway
mvn clean package -DskipTests
docker build -t api-gateway:1.0 .
```

### 3. Rebuild Existing Services

Since we updated the dependencies, rebuild all services:

**PowerShell:**
```powershell
# User Service
cd user-service
mvn clean package -DskipTests
docker build -t user-service:1.0 .
cd ..

# Task Service
cd task-service
mvn clean package -DskipTests
docker build -t task-service:1.0 .
cd ..

# Agent Service
cd agent-service
mvn clean package -DskipTests
docker build -t agent-service:1.0 .
cd ..
```

**Bash:**
```bash
# User Service
cd user-service
mvn clean package -DskipTests
docker build -t user-service:1.0 .

# Task Service
cd task-service
mvn clean package -DskipTests
docker build -t task-service:1.0 .

# Agent Service
cd agent-service
mvn clean package -DskipTests
docker build -t agent-service:1.0 .
```

### 4. Rebuild Frontend

**PowerShell:**
```powershell
cd frontend
docker build -t frontend:1.0 .
```

**Bash:**
```bash
cd frontend
docker build -t frontend:1.0 .
```

## Deploying to Kubernetes

### Step 1: Set Docker Environment

**For PowerShell (Windows):**
```powershell
minikube docker-env | Invoke-Expression
```

**For Bash (Linux/Mac):**
```bash
eval $(minikube docker-env)
```

### Step 2: Build All Images (in Minikube Docker)

**For PowerShell (Windows):**
```powershell
# Build Eureka Server
cd eureka-server
mvn clean package -DskipTests
docker build -t eureka-server:1.0 .
cd ..

# Build API Gateway
cd api-gateway
mvn clean package -DskipTests
docker build -t api-gateway:1.0 .
cd ..

# Build User Service
cd user-service
mvn clean package -DskipTests
docker build -t user-service:1.0 .
cd ..

# Build Task Service
cd task-service
mvn clean package -DskipTests
docker build -t task-service:1.0 .
cd ..

# Build Agent Service
cd agent-service
mvn clean package -DskipTests
docker build -t agent-service:1.0 .
cd ..

# Build Frontend
cd frontend
docker build -t frontend:1.0 .
cd ..
```

**For Bash (Linux/Mac):**
```bash
# Build all services in Minikube's Docker environment
cd eureka-server && mvn clean package -DskipTests && docker build -t eureka-server:1.0 .
cd ../api-gateway && mvn clean package -DskipTests && docker build -t api-gateway:1.0 .
cd ../user-service && mvn clean package -DskipTests && docker build -t user-service:1.0 .
cd ../task-service && mvn clean package -DskipTests && docker build -t task-service:1.0 .
cd ../agent-service && mvn clean package -DskipTests && docker build -t agent-service:1.0 .
cd ../frontend && docker build -t frontend:1.0 .
```

### Step 3: Deploy to Kubernetes

**Important**: Deploy Eureka Server first, then wait for it to be ready before deploying other services.

```bash
cd k8s-manifests

# Deploy Eureka Server first
kubectl apply -f eureka-server-deployment.yaml

# Wait for Eureka Server to be ready (check status)
kubectl get pods -w
# Wait until eureka-server pod is Running

# Deploy API Gateway
kubectl apply -f api-gateway-deployment.yaml

# Deploy all other services
kubectl apply -f mongodb-deployment.yaml
kubectl apply -f user-service-deployment.yaml
kubectl apply -f task-service-deployment.yaml
kubectl apply -f agent-service-deployment.yaml
kubectl apply -f frontend-deployment.yaml
```

### Step 4: Verify Deployment

```bash
# Check all pods are running
kubectl get pods

# Check services
kubectl get services

# Get Eureka Server NodePort
kubectl get service eureka-server
```

## Accessing the Services

### Eureka Dashboard

1. Get the NodePort for Eureka Server:
   ```bash
   kubectl get service eureka-server
   ```

2. Get Minikube IP:
   ```bash
   minikube ip
   ```

3. Access Eureka Dashboard:
   ```
   http://<minikube-ip>:<NodePort>/
   ```

   You should see all registered services:
   - `api-gateway`
   - `user-service`
   - `task-service`
   - `agent-service`

### Frontend Application

```bash
minikube service frontend
```

The frontend will route all API calls through the API Gateway, which uses Eureka to discover services.

## Service Registration Flow

1. **Eureka Server** starts and waits for registrations
2. **API Gateway** starts and registers with Eureka
3. **User Service** starts and registers with Eureka
4. **Task Service** starts and registers with Eureka
5. **Agent Service** starts and registers with Eureka

All services will appear in the Eureka dashboard once registered.

## API Gateway Routes

The API Gateway routes requests as follows:

- `/api/users/**` → `user-service` (via Eureka)
- `/api/tasks/**` → `task-service` (via Eureka)
- `/api/agent/**` → `agent-service` (via Eureka)

## Troubleshooting

### Services Not Appearing in Eureka

1. Check if Eureka Server is running:
   ```bash
   kubectl logs deployment/eureka-server
   ```

2. Check service logs for registration errors:
   ```bash
   kubectl logs deployment/user-service
   kubectl logs deployment/task-service
   kubectl logs deployment/agent-service
   kubectl logs deployment/api-gateway
   ```

3. Verify Eureka Server URL in service configurations:
   ```bash
   kubectl describe pod <service-pod-name>
   ```

### API Gateway Not Routing

1. Check API Gateway logs:
   ```bash
   kubectl logs deployment/api-gateway
   ```

2. Verify routes are configured correctly in `api-gateway/src/main/resources/application.properties`

3. Check if services are registered in Eureka dashboard

### Connection Issues

1. Verify all services can reach Eureka Server:
   ```bash
   kubectl exec -it <pod-name> -- ping eureka-server
   ```

2. Check DNS resolution:
   ```bash
   kubectl exec -it <pod-name> -- nslookup eureka-server
   ```

## Benefits of This Architecture

1. **Service Discovery**: Services automatically discover each other through Eureka
2. **Load Balancing**: API Gateway can load balance across multiple instances
3. **Centralized Routing**: All API requests go through a single entry point
4. **Monitoring**: Eureka dashboard provides visibility into all services
5. **Scalability**: Easy to scale services independently
6. **Resilience**: Services can be restarted without breaking the system

## Next Steps

- Add health checks and monitoring
- Implement circuit breakers (Resilience4j)
- Add authentication/authorization at the gateway
- Configure rate limiting
- Set up distributed tracing

