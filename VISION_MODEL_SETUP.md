# Vision Model Setup Guide

## Small Vision Models (0.5-2B Parameters)

For the task manager system, you can use smaller vision models to analyze uploaded assignment files. Here are the recommended options:

### Option 1: Quantized LLaVA (Recommended for 0.5-2B range)

```bash
# Pull quantized LLaVA model (smaller, faster)
ollama pull llava:7b-q4_0    # ~4GB, quantized 7B model
# or
ollama pull llava:7b-q2_K    # ~2.5GB, more aggressive quantization
```

**Note**: Quantized models use fewer parameters effectively, making them run faster while maintaining reasonable quality.

### Option 2: Use Smaller Base Model (If available)

```bash
# Check available small vision models
ollama list

# Try smaller variants if available
ollama pull llava:3b    # If available
```

### Option 3: Configure Custom Model

If you have a custom small vision model, update the configuration:

1. **Update `agent-service/src/main/resources/application.properties`**:
   ```properties
   ollama.vision.model=your-small-model:tag
   ```

2. **Update `k8s-manifests/agent-service-deployment.yaml`**:
   ```yaml
   env:
   - name: OLLAMA_VISION_MODEL
     value: "your-small-model:tag"
   ```

3. **Rebuild and redeploy**:
   ```bash
   eval $(minikube docker-env)
   cd agent-service
   mvn clean package -DskipTests
   docker build -t agent-service:1.0 .
   kubectl rollout restart deployment/agent-service
   ```

## Current Configuration

The system is configured to use `llava:7b` by default. To use a smaller model:

1. **Pull the smaller model**:
   ```bash
   ollama pull llava:7b-q4_0
   ```

2. **Update application.properties**:
   ```properties
   ollama.vision.model=llava:7b-q4_0
   ```

3. **Rebuild the service** (see steps above)

## Model Size Comparison

| Model | Parameters | Size | Speed | Quality |
|-------|-----------|------|-------|---------|
| llava:7b | 7B | ~4GB | Medium | High |
| llava:7b-q4_0 | ~4B (quantized) | ~2.5GB | Fast | Good |
| llava:7b-q2_K | ~2B (quantized) | ~1.5GB | Very Fast | Acceptable |

## Testing the Vision Model

After setting up your model:

```bash
# Test from command line
ollama run llava:7b-q4_0 "Describe this image" --image path/to/image.jpg

# Or test via the agent service
curl -X POST http://localhost:8083/api/agent/analyze-file \
  -F "file=@test-image.jpg" \
  -F "taskId=test-123" \
  -F "title=Test Task" \
  -F "description=Test Description"
```

## Important Notes

- **Vision models require more parameters** than text-only models for good performance
- **0.5-2B parameter models** may have limited vision understanding capabilities
- **Quantized models** (q4_0, q2_K) provide a good balance between size and quality
- **For best results**, use at least a 3-4B quantized model for vision tasks

## Troubleshooting

### Model not found
```bash
# List available models
ollama list

# Pull the model if not available
ollama pull llava:7b-q4_0
```

### Poor analysis quality
- Try a less aggressive quantization (q4_0 instead of q2_K)
- Use the full 7B model if quality is critical
- Ensure images are clear and well-lit

### Out of memory
- Use more aggressive quantization (q2_K)
- Reduce image resolution before upload
- Increase system memory if possible

