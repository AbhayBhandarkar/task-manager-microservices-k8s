# PowerShell script to fix the task
$taskId = "693d177496ce80c5d7cf05e2"
$minikubeIP = "192.168.49.2"

# Get task-service port
$taskPort = kubectl get svc task-service -o jsonpath='{.spec.ports[0].nodePort}'
$taskUrl = "http://${minikubeIP}:${taskPort}/api/tasks/${taskId}"

Write-Host "Fetching task..."
try {
    $task = Invoke-RestMethod -Uri $taskUrl -Method Get
    Write-Host "Current taskDone: $($task.taskDone)"
    
    $task.taskDone = $true
    $task.completed = $true
    
    Write-Host "Updating task..."
    $updated = Invoke-RestMethod -Uri $taskUrl -Method Put -Body ($task | ConvertTo-Json -Depth 10) -ContentType "application/json"
    Write-Host "✅ Task updated! taskDone: $($updated.taskDone)" -ForegroundColor Green
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host "`nTry running this in browser console instead:"
    Write-Host @"
async function fixTask() {
    const taskId = '693d177496ce80c5d7cf05e2';
    const task = await fetch(`/api/tasks/`${taskId}`).then(r => r.json());
    task.taskDone = true;
    task.completed = true;
    await fetch(`/api/tasks/`${taskId}`, {
        method: 'PUT',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(task)
    });
    alert('Task marked as complete! Refresh the page.');
}
fixTask();
"@ -ForegroundColor Yellow
}

