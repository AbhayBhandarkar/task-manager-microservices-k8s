# Building Services with PowerShell

## Prerequisites

1. **Maven must be installed** or use Maven wrapper from existing services
2. **Minikube must be running**
3. **Docker must be accessible**

## Quick Build Script

Run the provided PowerShell script:

```powershell
.\build-services.ps1
```

## Manual Build Steps (PowerShell)

### Step 1: Set Minikube Docker Environment

```powershell
minikube docker-env | Invoke-Expression
```

### Step 2: Build Each Service

**If you have Maven installed globally:**

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

**If you need to use Maven wrapper (mvnw):**

First, copy the Maven wrapper from an existing service:

```powershell
# Copy Maven wrapper files
Copy-Item user-service\mvnw eureka-server\mvnw
Copy-Item user-service\mvnw.cmd eureka-server\mvnw.cmd
Copy-Item user-service\.mvn eureka-server\.mvn -Recurse

Copy-Item user-service\mvnw api-gateway\mvnw
Copy-Item user-service\mvnw.cmd api-gateway\mvnw.cmd
Copy-Item user-service\.mvn api-gateway\.mvn -Recurse
```

Then use `.\mvnw` instead of `mvn`:

```powershell
cd eureka-server
.\mvnw.cmd clean package -DskipTests
docker build -t eureka-server:1.0 .
cd ..
```

## Install Maven (if not installed)

### Option 1: Using Chocolatey

```powershell
choco install maven
```

### Option 2: Manual Installation

1. Download Maven from: https://maven.apache.org/download.cgi
2. Extract to `C:\Program Files\Apache\maven`
3. Add to PATH:
   - System Properties → Environment Variables
   - Add `C:\Program Files\Apache\maven\bin` to PATH

### Option 3: Use Maven Wrapper

Copy the `.mvn` folder and `mvnw`/`mvnw.cmd` files from `user-service` to `eureka-server` and `api-gateway`.

## Verify Maven Installation

```powershell
mvn --version
```

If this works, you can use `mvn` commands. Otherwise, use the Maven wrapper (`.\mvnw.cmd`).

