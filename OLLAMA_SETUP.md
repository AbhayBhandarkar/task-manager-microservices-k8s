# Ollama Setup Guide for Agent Service

This guide will help you install and configure Ollama on Windows to work with the agent-service.

## Step 1: Install Ollama

### Option A: Download Installer (Recommended)
1. Visit https://ollama.ai/download
2. Download the Windows installer (`.exe` file)
3. Run the installer and follow the setup wizard
4. Ollama will be installed and added to your PATH

### Option B: Using PowerShell (Alternative)
```powershell
# Download and install Ollama
winget install Ollama.Ollama
```

## Step 2: Verify Installation

Open a new PowerShell or Command Prompt window and verify Ollama is installed:

```powershell
ollama --version
```

You should see something like: `ollama version is 1.x.x`

## Step 3: Start Ollama Service

Ollama runs as a service on Windows. After installation, it should start automatically. To verify:

```powershell
# Check if Ollama is running
curl http://localhost:11434/api/tags
```

If you get a JSON response, Ollama is running. If not, start it manually:

```powershell
# Start Ollama (if not running)
ollama serve
```

**Note**: Keep this terminal open while using the agent-service, or configure Ollama to run as a Windows service.

## Step 4: Pull Language and Vision Models

You need to download at least two models:

### Text Model (for task analysis) - CPU Optimized
The agent-service is configured to use `phi3:mini` (smallest for CPU):

```powershell
# Pull the CPU-optimized text model (~2.3GB, ~1.5-2B effective parameters)
ollama pull phi3:mini

# Alternative (if phi3:mini not available):
ollama pull llama3.2      # ~2GB, slightly larger
```

### Vision Model (for file/image analysis)
The system uses a quantized LLaVA model for analyzing uploaded files:

```powershell
# Pull the quantized vision model (~1.5GB, ~2B effective parameters)
ollama pull llava:7b-q2_K

# Alternative options:
ollama pull llava:7b-q4_0   # ~2.5GB, better quality
ollama pull llava:7b       # ~4GB, full quality (if you have more memory)
```

**First-time download**: This will take a few minutes depending on your internet speed.

## Step 5: Verify Models are Available

```powershell
# List all downloaded models
ollama list

# Test the text model
ollama run llama3.2 "Hello, can you analyze a task?"

# Test the vision model (if you have an image)
ollama run llava:7b-q2_K "Describe this image" --image path/to/image.jpg
```

You should see responses from both models. The vision model is used when students upload assignment files.

## Step 6: Configure for Kubernetes Access

The agent-service needs to connect to Ollama running on your host machine. For Minikube on Windows, we use `host.docker.internal` which should work automatically.

### Verify Ollama is Accessible

```powershell
# Test from your host machine
curl http://localhost:11434/api/tags

# Should return JSON with available models
```

## Step 7: Test Agent Service Connection

Once your Kubernetes cluster is running with the agent-service deployed:

```powershell
# Port-forward the agent-service
kubectl port-forward service/agent-service 8083:8083

# In another terminal, test the connection
curl -X POST http://localhost:8083/api/agent/analyze `
  -H "Content-Type: application/json" `
  -d '{\"id\":\"test\",\"title\":\"Complete project documentation\",\"description\":\"Write README and API docs\",\"completed\":true}'
```

## Troubleshooting

### Ollama not accessible from Kubernetes

If `host.docker.internal` doesn't work, you may need to:

1. **Get your host IP address:**
   ```powershell
   ipconfig
   # Look for IPv4 Address (usually 192.168.x.x or 10.x.x.x)
   ```

2. **Update agent-service-deployment.yaml:**
   ```yaml
   env:
   - name: OLLAMA_URL
     value: "http://YOUR_HOST_IP:11434"  # Replace with your IP
   ```

3. **Redeploy:**
   ```powershell
   kubectl apply -f k8s-manifests/agent-service-deployment.yaml
   kubectl rollout restart deployment/agent-service
   ```

### Ollama service not starting

```powershell
# Check if port 11434 is in use
netstat -ano | findstr :11434

# If something else is using it, stop that service or change Ollama port
# (requires editing Ollama config - advanced)
```

### Model not found

```powershell
# List available models
ollama list

# If llama3.2 is not listed, pull it:
ollama pull llama3.2

# Or update agent-service to use a different model:
# Edit agent-service/src/main/resources/application.properties
# Change: ollama.model=llama3.2 to ollama.model=YOUR_MODEL
```

## Quick Start Commands Summary

```powershell
# 1. Install Ollama (if not installed)
# Visit https://ollama.ai/download

# 2. Verify installation
ollama --version

# 3. Start Ollama (if not running)
ollama serve

# 4. Pull model
ollama pull llama3.2

# 5. Verify it works
ollama run llama3.2 "Test"

# 6. Keep Ollama running, then deploy your services
```

## Next Steps

After Ollama is set up:
1. Build and deploy the agent-service (see main README.md)
2. Access the frontend dashboard
3. Click "🤖 Analyze" on any task to test the AI agent

---

**Note**: Ollama must be running on your host machine (not in Kubernetes) for the agent-service to connect to it. The agent-service runs in Kubernetes and connects to Ollama via `host.docker.internal:11434`.

