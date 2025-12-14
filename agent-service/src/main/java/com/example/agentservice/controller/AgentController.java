package com.example.agentservice.controller;

import com.example.agentservice.model.TaskAnalysis;
import com.example.agentservice.service.TaskAnalysisService;
import com.example.agentservice.service.OllamaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@CrossOrigin(origins = "*")
@Slf4j
public class AgentController {
    
    @Autowired
    private TaskAnalysisService taskAnalysisService;
    
    @Autowired
    private OllamaService ollamaService;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "agent-service"));
    }
    
    @PostMapping("/analyze")
    public Mono<ResponseEntity<TaskAnalysis>> analyzeTask(@RequestBody Map<String, Object> taskData) {
        String taskId = (String) taskData.get("id");
        String title = (String) taskData.get("title");
        String description = (String) taskData.get("description");
        Boolean completed = (Boolean) taskData.getOrDefault("completed", false);
        Boolean fileUploaded = (Boolean) taskData.getOrDefault("fileUploaded", false);
        Boolean taskDone = (Boolean) taskData.getOrDefault("taskDone", false);
        
        log.info("Analyzing task: id={}, title={}, completed={}, fileUploaded={}, taskDone={}", 
                taskId, title, completed, fileUploaded, taskDone);
        
        // CPU-friendly mode: If file is uploaded, mark as actually completed
        if (fileUploaded || taskDone) {
            log.info("Task has file uploaded or is marked as done - marking as actually completed");
            TaskAnalysis analysis = new TaskAnalysis();
            analysis.setTaskId(taskId);
            analysis.setTaskTitle(title);
            analysis.setTaskDescription(description);
            analysis.setMarkedCompleted(completed);
            analysis.setActuallyCompleted(true); // File uploaded = actually completed
            analysis.setConfidence(1.0);
            analysis.setReasoning("File has been uploaded. Task is marked as completed.");
            analysis.setRecommendation("Task is complete based on file upload.");
            analysis.setGrade(null);
            
            // Update task in MongoDB if not already done
            if (!taskDone) {
                return taskAnalysisService.updateTaskInMongoDB(taskId, true, null, null)
                    .doOnSuccess(v -> log.info("Updated task {} to taskDone=true via analyze endpoint", taskId))
                    .thenReturn(ResponseEntity.ok(analysis))
                    .onErrorResume(error -> {
                        log.error("Error updating task: {}", error.getMessage());
                        return Mono.just(ResponseEntity.ok(analysis)); // Return analysis even if update fails
                    });
            }
            
            return Mono.just(ResponseEntity.ok(analysis));
        }
        
        // If no file uploaded, do normal analysis (but skip Ollama in CPU-friendly mode)
        TaskAnalysis analysis = new TaskAnalysis();
        analysis.setTaskId(taskId);
        analysis.setTaskTitle(title);
        analysis.setTaskDescription(description);
        analysis.setMarkedCompleted(completed);
        analysis.setActuallyCompleted(completed); // If marked completed and no file, assume it's done
        analysis.setConfidence(completed ? 0.8 : 0.5);
        analysis.setReasoning(completed ? 
            "Task is marked as completed. No file uploaded, so completion is based on task status." :
            "Task is not yet completed. Student should upload a file to complete the task.");
        analysis.setRecommendation(completed ?
            "Task appears to be completed. Consider verifying if file upload is needed." :
            "Please upload a file to complete this task.");
        analysis.setGrade(null);
        
        return Mono.just(ResponseEntity.ok(analysis));
    }
    
    @PostMapping("/analyze-file")
    public Mono<ResponseEntity<TaskAnalysis>> analyzeTaskWithFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("taskId") String taskId,
            @RequestParam("title") String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String fileUrl) {
        
        log.info("=== FILE UPLOAD ENDPOINT CALLED ===");
        log.info("Received file upload request: taskId={}, title={}, filename={}", 
                taskId, title, file != null ? file.getOriginalFilename() : "null");
        
        if (file == null || file.isEmpty()) {
            log.error("File is null or empty");
            TaskAnalysis errorAnalysis = new TaskAnalysis();
            errorAnalysis.setTaskId(taskId);
            errorAnalysis.setTaskTitle(title);
            errorAnalysis.setTaskDescription(description);
            errorAnalysis.setMarkedCompleted(false);
            errorAnalysis.setActuallyCompleted(false);
            errorAnalysis.setConfidence(0.5);
            errorAnalysis.setReasoning("File is null or empty");
            errorAnalysis.setRecommendation("Please select a valid file to upload.");
            return Mono.just(ResponseEntity.badRequest().body(errorAnalysis));
        }
        
        log.info("Analyzing task with file: id={}, title={}, filename={}, size={}", 
                taskId, title, file.getOriginalFilename(), file.getSize());
        
        try {
            byte[] fileBytes = file.getBytes();
            log.info("File uploaded: {} bytes, filename: {}", fileBytes.length, file.getOriginalFilename());
            // CPU-friendly mode: No need to encode to base64 or analyze with AI
            String base64Image = ""; // Not used in CPU-friendly mode
            
            return taskAnalysisService.analyzeTaskWithFile(taskId, title, description, base64Image, fileUrl)
                .map(ResponseEntity::ok)
                .doOnSuccess(result -> log.info("File analysis completed successfully for task: {}", taskId))
                .onErrorResume(error -> {
                    log.error("Error analyzing file: {}", error.getMessage(), error);
                    TaskAnalysis errorAnalysis = new TaskAnalysis();
                    errorAnalysis.setTaskId(taskId);
                    errorAnalysis.setTaskTitle(title);
                    errorAnalysis.setTaskDescription(description);
                    errorAnalysis.setMarkedCompleted(false);
                    errorAnalysis.setActuallyCompleted(false);
                    errorAnalysis.setConfidence(0.5);
                    errorAnalysis.setReasoning("Unable to analyze file: " + error.getMessage());
                    errorAnalysis.setRecommendation("Please verify task completion manually.");
                    return Mono.just(ResponseEntity.ok(errorAnalysis));
                });
        } catch (Exception e) {
            log.error("Error processing file: {}", e.getMessage(), e);
            TaskAnalysis errorAnalysis = new TaskAnalysis();
            errorAnalysis.setTaskId(taskId);
            errorAnalysis.setTaskTitle(title);
            errorAnalysis.setTaskDescription(description);
            errorAnalysis.setMarkedCompleted(false);
            errorAnalysis.setActuallyCompleted(false);
            errorAnalysis.setConfidence(0.5);
            errorAnalysis.setReasoning("Error processing file: " + e.getMessage());
            errorAnalysis.setRecommendation("Please try uploading the file again.");
            return Mono.just(ResponseEntity.ok(errorAnalysis));
        }
    }
    
    @GetMapping("/analyze/{taskId}")
    public Mono<ResponseEntity<TaskAnalysis>> analyzeTaskById(
            @PathVariable String taskId,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean completed) {
        
        log.info("Analyzing task by ID: id={}, title={}, completed={}", taskId, title, completed);
        
        return taskAnalysisService.analyzeTask(taskId, title, description, completed)
            .map(ResponseEntity::ok)
            .onErrorResume(error -> {
                log.error("Error analyzing task: {}", error.getMessage());
                TaskAnalysis errorAnalysis = new TaskAnalysis();
                errorAnalysis.setTaskId(taskId);
                errorAnalysis.setTaskTitle(title);
                errorAnalysis.setTaskDescription(description);
                errorAnalysis.setMarkedCompleted(completed);
                errorAnalysis.setActuallyCompleted(completed);
                errorAnalysis.setConfidence(0.5);
                errorAnalysis.setReasoning("Unable to analyze task: " + error.getMessage());
                errorAnalysis.setRecommendation("Please verify task completion manually.");
                return Mono.just(ResponseEntity.ok(errorAnalysis));
            });
    }
}

