# Fix for Docker Build Issues with Minikube

## Problem
Minikube's Docker environment may have issues accessing files in OneDrive paths (paths with spaces).

## Solution Options

### Option 1: Build with Local Docker, then Load into Minikube

```powershell
# Exit Minikube Docker environment (use local Docker)
# Remove the minikube docker-env if you set it
$env:DOCKER_HOST = $null
$env:DOCKER_TLS_VERIFY = $null
$env:DOCKER_CERT_PATH = $null
$env:MINIKUBE_ACTIVE_DOCKERD = $null

# Build with local Docker
cd eureka-server
docker build -t eureka-server:1.0 .

# Load image into Minikube
minikube image load eureka-server:1.0

# Repeat for other services
cd ../api-gateway
.\mvnw.cmd clean package -DskipTests
docker build -t api-gateway:1.0 .
minikube image load api-gateway:1.0
```

### Option 2: Use Minikube's Docker but Build from Project Root

```powershell
# Set Minikube Docker environment
minikube docker-env | Invoke-Expression

# Build from project root with full path
cd "C:\Users\Aditya Manojkumar\OneDrive\Desktop\task-manager-microservices-k8s\eureka-server"
docker build -f Dockerfile -t eureka-server:1.0 .
```

### Option 3: Copy Project to a Path Without Spaces

If the issue persists, copy the project to a location without spaces:

```powershell
# Copy to C:\projects (or any path without spaces)
Copy-Item -Path "C:\Users\Aditya Manojkumar\OneDrive\Desktop\task-manager-microservices-k8s" -Destination "C:\projects\task-manager-microservices-k8s" -Recurse

# Then work from the new location
cd C:\projects\task-manager-microservices-k8s
minikube docker-env | Invoke-Expression
cd eureka-server
.\mvnw.cmd clean package -DskipTests
docker build -t eureka-server:1.0 .
```

## Recommended: Option 1 (Build Locally, Load to Minikube)

This is the most reliable approach on Windows with OneDrive:

```powershell
# Navigate to project root
cd "C:\Users\Aditya Manojkumar\OneDrive\Desktop\task-manager-microservices-k8s"

# Build Eureka Server
cd eureka-server
.\mvnw.cmd clean package -DskipTests
docker build -t eureka-server:1.0 .
minikube image load eureka-server:1.0
cd ..

# Build API Gateway
cd api-gateway
.\mvnw.cmd clean package -DskipTests
docker build -t api-gateway:1.0 .
minikube image load api-gateway:1.0
cd ..

# Rebuild existing services
cd user-service
.\mvnw.cmd clean package -DskipTests
docker build -t user-service:1.0 .
minikube image load user-service:1.0
cd ..

cd task-service
.\mvnw.cmd clean package -DskipTests
docker build -t task-service:1.0 .
minikube image load task-service:1.0
cd ..

cd agent-service
.\mvnw.cmd clean package -DskipTests
docker build -t agent-service:1.0 .
minikube image load agent-service:1.0
cd ..

cd frontend
docker build -t frontend:1.0 .
minikube image load frontend:1.0
cd ..
```

## Verify Images in Minikube

After loading images, verify they're available:

```powershell
minikube ssh -- docker images | Select-String "eureka-server|api-gateway|user-service|task-service|agent-service|frontend"
```

Or use kubectl to check if pods can pull the images after deployment.

