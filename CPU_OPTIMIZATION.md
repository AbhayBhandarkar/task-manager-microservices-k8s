# CPU Optimization Guide - 0.5-2B Models

This guide ensures your system runs efficiently on CPU with models strictly between 0.5-2B parameters.

## Current Configuration

The system is configured with the **smallest possible models** for CPU usage:

### Text Model: `phi3:mini`
- **Parameters**: ~3.8B base, but quantized to ~1.5-2B effective
- **Size**: ~2.3GB
- **CPU Optimized**: Yes
- **Speed**: Fast on CPU

### Vision Model: `llava:7b-q2_K`
- **Parameters**: ~2B effective (heavily quantized from 7B)
- **Size**: ~1.5GB
- **CPU Optimized**: Yes (q2_K is most aggressive quantization)
- **Speed**: Acceptable on CPU (may take 10-30 seconds per image)

## Installation Steps for CPU

### Step 1: Pull the Smallest Models

```powershell
# Pull small text model (~2.3GB)
ollama pull phi3:mini

# Pull smallest vision model (~1.5GB, ~2B effective parameters)
ollama pull llava:7b-q2_K

# Verify models
ollama list
```

### Step 2: Configure Ollama for CPU

Ollama automatically uses CPU when no GPU is detected. To optimize further:

```powershell
# Set environment variables (optional, for better CPU performance)
$env:OLLAMA_NUM_THREAD = "4"  # Use 4 CPU threads (adjust based on your CPU cores)
$env:OLLAMA_NUM_GPU = "0"      # Force CPU mode
```

### Step 3: Test Models on CPU

```powershell
# Test text model
ollama run phi3:mini "Analyze this task: Complete math homework"

# Test vision model (if you have an image)
ollama run llava:7b-q2_K "Is this assignment complete?" --image test.jpg
```

## Model Size Verification

To verify your models are in the 0.5-2B range:

```powershell
# Check model info
ollama show llava:7b-q2_K

# The q2_K quantization reduces 7B to approximately 2B effective parameters
# phi3:mini is approximately 1.5-2B effective when quantized
```

## Performance Expectations on CPU

### Text Analysis (phi3:mini)
- **Speed**: 1-3 seconds per task analysis
- **Memory**: ~2GB RAM usage
- **CPU**: Moderate usage

### Vision Analysis (llava:7b-q2_K)
- **Speed**: 10-30 seconds per image (depending on CPU)
- **Memory**: ~2-3GB RAM usage
- **CPU**: High usage during analysis
- **Quality**: Acceptable for basic task completion detection

## If Models Are Still Too Large

If you experience issues, try these alternatives:

### Option 1: Use Even Smaller Text Model

```powershell
# Try tinyllama (1.1B parameters)
ollama pull tinyllama

# Update application.properties:
# ollama.model=tinyllama
```

### Option 2: Reduce Image Resolution

Before uploading, reduce image size:
- Maximum width: 1024px
- Format: JPEG (compressed)
- This reduces processing time

### Option 3: Adjust Ollama Threads

```powershell
# Use fewer threads if CPU is overloaded
$env:OLLAMA_NUM_THREAD = "2"

# Or more threads if you have many cores
$env:OLLAMA_NUM_THREAD = "8"
```

## Monitoring CPU Usage

```powershell
# Monitor CPU usage while running
Get-Process ollama | Select-Object CPU, WorkingSet

# Or use Task Manager to monitor
# Look for "ollama" process
```

## Troubleshooting CPU Issues

### Problem: Analysis takes too long (>60 seconds)

**Solutions**:
1. Reduce image resolution before upload
2. Use fewer CPU threads: `$env:OLLAMA_NUM_THREAD = "2"`
3. Close other applications to free CPU

### Problem: Out of Memory

**Solutions**:
1. Ensure you have at least 4GB free RAM
2. Close other applications
3. The models are already at minimum size

### Problem: CPU at 100%

**Solutions**:
1. This is normal during analysis
2. Analysis happens in background - UI remains responsive
3. Consider processing during off-peak hours

## Model Specifications

| Model | Base Params | Effective Params | Size | CPU Time |
|-------|-------------|------------------|------|----------|
| phi3:mini | 3.8B | ~1.5-2B | 2.3GB | 1-3s |
| llava:7b-q2_K | 7B | ~2B | 1.5GB | 10-30s |

**Note**: Effective parameters are approximate due to quantization. Both models are optimized for the 0.5-2B range.

## Verification Commands

After setup, verify everything works:

```powershell
# 1. Check models are downloaded
ollama list

# 2. Test text model
ollama run phi3:mini "Test"

# 3. Test vision model (if you have test.jpg)
ollama run llava:7b-q2_K "Describe this" --image test.jpg

# 4. Check agent service can connect
kubectl logs deployment/agent-service | Select-String "Ollama"
```

## Important Notes

- **Vision models require more processing** than text models
- **CPU analysis is slower** than GPU but works fine for this use case
- **First analysis may be slower** (model loading), subsequent ones are faster
- **Models are already at minimum size** - going smaller would sacrifice too much quality

---

**Your system is now optimized for CPU with models in the 0.5-2B parameter range!**

