# PowerShell script to build all services for Minikube
# Make sure Minikube is running and Docker environment is set

Write-Host "Setting up Minikube Docker environment..." -ForegroundColor Green
minikube docker-env | Invoke-Expression

Write-Host "`nBuilding Eureka Server..." -ForegroundColor Yellow
cd eureka-server
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build Eureka Server" -ForegroundColor Red
    exit 1
}
docker build -t eureka-server:1.0 .
cd ..

Write-Host "`nBuilding API Gateway..." -ForegroundColor Yellow
cd api-gateway
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build API Gateway" -ForegroundColor Red
    exit 1
}
docker build -t api-gateway:1.0 .
cd ..

Write-Host "`nBuilding User Service..." -ForegroundColor Yellow
cd user-service
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build User Service" -ForegroundColor Red
    exit 1
}
docker build -t user-service:1.0 .
cd ..

Write-Host "`nBuilding Task Service..." -ForegroundColor Yellow
cd task-service
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build Task Service" -ForegroundColor Red
    exit 1
}
docker build -t task-service:1.0 .
cd ..

Write-Host "`nBuilding Agent Service..." -ForegroundColor Yellow
cd agent-service
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "Failed to build Agent Service" -ForegroundColor Red
    exit 1
}
docker build -t agent-service:1.0 .
cd ..

Write-Host "`nBuilding Frontend..." -ForegroundColor Yellow
cd frontend
docker build -t frontend:1.0 .
cd ..

Write-Host "`nAll services built successfully!" -ForegroundColor Green
Write-Host "You can now deploy to Kubernetes using:" -ForegroundColor Cyan
Write-Host "  cd k8s-manifests" -ForegroundColor Cyan
Write-Host "  kubectl apply -f eureka-server-deployment.yaml" -ForegroundColor Cyan
Write-Host "  kubectl apply -f api-gateway-deployment.yaml" -ForegroundColor Cyan
Write-Host "  kubectl apply -f ." -ForegroundColor Cyan

