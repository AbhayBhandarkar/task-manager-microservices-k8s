# Create a start script
cat > ~/Downloads/microservices-k8s/start.sh << 'EOF'
#!/bin/bash
echo "🚀 Starting Microservices Project..."

echo "1️⃣ Starting Minikube..."
minikube start

echo "2️⃣ Deploying services..."
cd ~/Downloads/microservices-k8s/k8s-manifests
kubectl apply -f .

echo "3️⃣ Waiting for pods to be ready..."
kubectl wait --for=condition=ready pod --all --timeout=120s

echo "4️⃣ Opening dashboard..."
minikube service frontend

echo "✅ All done!"
EOF

# Make it executable
chmod +x ~/Downloads/microservices-k8s/start.sh