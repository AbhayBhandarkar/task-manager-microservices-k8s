# Rebuild all services with Eureka dependencies
# Run from C:\projects\task-manager-microservices-k8s

Write-Host "Setting up Minikube Docker environment..." -ForegroundColor Green
minikube docker-env | Invoke-Expression

# Update application.properties files
Write-Host "`nUpdating application.properties files..." -ForegroundColor Yellow

# User Service
$file = "user-service\src\main\resources\application.properties"
$content = Get-Content $file -Raw
if ($content -notmatch "spring.cloud.compatibility-verifier.enabled=false") {
    $content = $content -replace '(spring.application.name=user-service)', "`$1`n`n# Disable compatibility check`nspring.cloud.compatibility-verifier.enabled=false"
    Set-Content $file $content
}

# Task Service
$file = "task-service\src\main\resources\application.properties"
$content = Get-Content $file -Raw
if ($content -notmatch "spring.cloud.compatibility-verifier.enabled=false") {
    $content = $content -replace '(spring.application.name=task-service)', "`$1`n`n# Disable compatibility check`nspring.cloud.compatibility-verifier.enabled=false"
    Set-Content $file $content
}

# Agent Service
$file = "agent-service\src\main\resources\application.properties"
$content = Get-Content $file -Raw
if ($content -notmatch "spring.cloud.compatibility-verifier.enabled=false") {
    $content = $content -replace '(spring.application.name=agent-service)', "`$1`n`n# Disable compatibility check`nspring.cloud.compatibility-verifier.enabled=false"
    Set-Content $file $content
}

Write-Host "`nRebuilding services..." -ForegroundColor Yellow

# Rebuild User Service
Write-Host "`nBuilding User Service..." -ForegroundColor Cyan
cd user-service
.\mvnw.cmd clean package -DskipTests
docker build -t user-service:1.0 .
cd ..

# Rebuild Task Service
Write-Host "`nBuilding Task Service..." -ForegroundColor Cyan
cd task-service
.\mvnw.cmd clean package -DskipTests
docker build -t task-service:1.0 .
cd ..

# Rebuild Agent Service
Write-Host "`nBuilding Agent Service..." -ForegroundColor Cyan
cd agent-service
.\mvnw.cmd clean package -DskipTests
docker build -t agent-service:1.0 .
cd ..

Write-Host "`nRestarting deployments..." -ForegroundColor Yellow
kubectl rollout restart deployment/user-service
kubectl rollout restart deployment/task-service
kubectl rollout restart deployment/agent-service

Write-Host "`n✅ Done! Wait 1-2 minutes, then refresh Eureka Dashboard" -ForegroundColor Green

