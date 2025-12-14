package com.example.agentservice.service;

import com.example.agentservice.model.TaskAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

@Service
@Slf4j
public class TaskAnalysisService {
    
    @Autowired
    private OllamaService ollamaService;
    
    @Autowired
    private WebClient.Builder webClientBuilder;
    
    @org.springframework.beans.factory.annotation.Value("${task.service.url:http://task-service:8081}")
    private String taskServiceUrl;
    
    private static final Pattern YES_PATTERN = Pattern.compile("\\b(yes|true|completed|done|finished|accomplished)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern NO_PATTERN = Pattern.compile("\\b(no|false|not completed|not done|pending|incomplete|unfinished)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern GRADE_PATTERN = Pattern.compile("\\b(\\d{1,3}(?:\\.\\d{1,2})?)\\s*(?:out of|/|percent|%)?\\s*(?:\\d{1,3})?\\b", Pattern.CASE_INSENSITIVE);
    
    public Mono<TaskAnalysis> analyzeTask(String taskId, String title, String description, boolean markedCompleted) {
        String prompt = buildAnalysisPrompt(title, description, markedCompleted);
        
        return ollamaService.generateResponse(prompt)
            .map(response -> parseAnalysisResponse(taskId, title, description, markedCompleted, response));
    }
    
    public Mono<TaskAnalysis> analyzeTaskWithFile(String taskId, String title, String description, String base64Image, String fileUrl) {
        // CPU-friendly mode: Skip AI analysis, just mark as completed when file is uploaded
        log.info("File uploaded for task {} - marking as completed (CPU-friendly mode, no AI analysis)", taskId);
        
        TaskAnalysis analysis = new TaskAnalysis();
        analysis.setTaskId(taskId);
        analysis.setTaskTitle(title);
        analysis.setTaskDescription(description);
        analysis.setMarkedCompleted(false);
        analysis.setActuallyCompleted(true); // File uploaded = task completed
        analysis.setConfidence(1.0);
        analysis.setReasoning("File successfully uploaded. Task marked as completed.");
        analysis.setRecommendation("Task completed based on file upload.");
        analysis.setGrade(null); // No grading in CPU-friendly mode
        
        // Update task in MongoDB - mark as done without AI analysis
        // Use doOnSuccess to ensure we wait for the update to complete
        return updateTaskInMongoDB(taskId, true, fileUrl, null)
            .doOnSuccess(v -> log.info("Successfully updated task {} in MongoDB: taskDone=true, fileUploaded=true", taskId))
            .doOnError(e -> log.error("Failed to update task {} in MongoDB: {}", taskId, e.getMessage()))
            .then(Mono.just(analysis));
    }
    
    public Mono<Void> updateTaskInMongoDB(String taskId, boolean taskDone, String fileUrl, Double grade) {
        WebClient webClient = webClientBuilder.baseUrl(taskServiceUrl).build();
        
        log.info("Updating task {} in MongoDB: taskDone={}, fileUrl={}, grade={}", taskId, taskDone, fileUrl, grade);
        
        // First get the task
        return webClient.get()
            .uri("/api/tasks/{id}", taskId)
            .retrieve()
            .bodyToMono(java.util.Map.class)
            .doOnNext(task -> log.info("Retrieved task from MongoDB: {}", task))
            .flatMap(task -> {
                task.put("fileUploaded", true);
                task.put("taskDone", taskDone);
                if (fileUrl != null) {
                    task.put("fileUrl", fileUrl);
                }
                if (grade != null) {
                    task.put("grade", grade);
                }
                // Also update completed for backward compatibility
                task.put("completed", taskDone);
                
                log.info("Updating task with new values: fileUploaded=true, taskDone={}, fileUrl={}", taskDone, fileUrl);
                
                return webClient.put()
                    .uri("/api/tasks/{id}", taskId)
                    .bodyValue(task)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSuccess(v -> log.info("Successfully updated task {} in MongoDB", taskId))
                    .doOnError(err -> log.error("Failed to update task {}: {}", taskId, err.getMessage()));
            })
            .doOnError(error -> log.error("Error updating task in MongoDB: {}", error.getMessage(), error))
            .onErrorResume(error -> {
                log.error("Failed to update task, but continuing: {}", error.getMessage());
                return Mono.empty(); // Continue even if update fails
            });
    }
    
    private String buildAnalysisPrompt(String title, String description, boolean markedCompleted) {
        return String.format(
            "Analyze the following task and determine if it is actually completed or not.\n\n" +
            "Task Title: %s\n" +
            "Task Description: %s\n" +
            "Marked as Completed: %s\n\n" +
            "Based on the task details, determine:\n" +
            "1. Is this task actually completed? (Answer: YES or NO)\n" +
            "2. What is your confidence level? (Answer: HIGH, MEDIUM, or LOW)\n" +
            "3. Provide brief reasoning (1-2 sentences)\n" +
            "4. Provide a recommendation (1 sentence)\n\n" +
            "Format your response as:\n" +
            "COMPLETED: [YES/NO]\n" +
            "CONFIDENCE: [HIGH/MEDIUM/LOW]\n" +
            "REASONING: [your reasoning]\n" +
            "RECOMMENDATION: [your recommendation]",
            title != null ? title : "N/A",
            description != null ? description : "No description",
            markedCompleted ? "Yes" : "No"
        );
    }
    
    private TaskAnalysis parseAnalysisResponse(String taskId, String title, String description, 
                                                boolean markedCompleted, String response) {
        TaskAnalysis analysis = new TaskAnalysis();
        analysis.setTaskId(taskId);
        analysis.setTaskTitle(title);
        analysis.setTaskDescription(description);
        analysis.setMarkedCompleted(markedCompleted);
        
        // Parse response
        String upperResponse = response.toUpperCase();
        boolean actuallyCompleted = YES_PATTERN.matcher(upperResponse).find() && 
                                   !NO_PATTERN.matcher(upperResponse).find();
        
        analysis.setActuallyCompleted(actuallyCompleted);
        
        // Extract confidence
        double confidence = 0.5;
        if (upperResponse.contains("HIGH")) {
            confidence = 0.9;
        } else if (upperResponse.contains("MEDIUM")) {
            confidence = 0.7;
        } else if (upperResponse.contains("LOW")) {
            confidence = 0.5;
        }
        analysis.setConfidence(confidence);
        
        // Extract reasoning
        String reasoning = extractField(response, "REASONING:");
        if (reasoning == null || reasoning.isEmpty()) {
            reasoning = "Analysis completed based on task details.";
        }
        analysis.setReasoning(reasoning);
        
        // Extract recommendation
        String recommendation = extractField(response, "RECOMMENDATION:");
        if (recommendation == null || recommendation.isEmpty()) {
            recommendation = actuallyCompleted ? 
                "Task appears to be completed." : 
                "Task may need further work or verification.";
        }
        analysis.setRecommendation(recommendation);
        
        log.info("Task analysis completed for task {}: actuallyCompleted={}, confidence={}", 
                taskId, actuallyCompleted, confidence);
        
        return analysis;
    }
    
    private String buildFileAnalysisPrompt(String title, String description) {
        return String.format(
            "Analyze the uploaded file/image for this assignment task.\n\n" +
            "Task Title: %s\n" +
            "Task Description: %s\n\n" +
            "Please analyze the uploaded file and determine:\n" +
            "1. Is the task completed based on the uploaded work? (Answer: YES or NO)\n" +
            "2. What is your confidence level? (Answer: HIGH, MEDIUM, or LOW)\n" +
            "3. If applicable, provide a grade (0-100) based on the quality and completeness of the work\n" +
            "4. Provide brief reasoning (2-3 sentences)\n\n" +
            "Format your response as:\n" +
            "COMPLETED: [YES/NO]\n" +
            "CONFIDENCE: [HIGH/MEDIUM/LOW]\n" +
            "GRADE: [0-100 or N/A if not applicable]\n" +
            "REASONING: [your reasoning]\n" +
            "RECOMMENDATION: [your recommendation]",
            title != null ? title : "N/A",
            description != null ? description : "No description"
        );
    }
    
    private TaskAnalysis parseFileAnalysisResponse(String taskId, String title, String description, String response) {
        TaskAnalysis analysis = new TaskAnalysis();
        analysis.setTaskId(taskId);
        analysis.setTaskTitle(title);
        analysis.setTaskDescription(description);
        analysis.setMarkedCompleted(false); // Initially not marked
        
        // Parse response
        String upperResponse = response.toUpperCase();
        boolean actuallyCompleted = YES_PATTERN.matcher(upperResponse).find() && 
                                   !NO_PATTERN.matcher(upperResponse).find();
        
        analysis.setActuallyCompleted(actuallyCompleted);
        
        // Extract confidence
        double confidence = 0.5;
        if (upperResponse.contains("HIGH")) {
            confidence = 0.9;
        } else if (upperResponse.contains("MEDIUM")) {
            confidence = 0.7;
        } else if (upperResponse.contains("LOW")) {
            confidence = 0.5;
        }
        analysis.setConfidence(confidence);
        
        // Extract grade
        java.util.regex.Matcher gradeMatcher = GRADE_PATTERN.matcher(response);
        if (gradeMatcher.find()) {
            try {
                double grade = Double.parseDouble(gradeMatcher.group(1));
                // Normalize to 0-100 if needed
                if (grade > 100) {
                    grade = 100;
                }
                analysis.setGrade(grade);
            } catch (NumberFormatException e) {
                log.warn("Could not parse grade from response: {}", gradeMatcher.group(1));
            }
        }
        
        // Extract reasoning
        String reasoning = extractField(response, "REASONING:");
        if (reasoning == null || reasoning.isEmpty()) {
            reasoning = "Analysis completed based on uploaded file.";
        }
        analysis.setReasoning(reasoning);
        
        // Extract recommendation
        String recommendation = extractField(response, "RECOMMENDATION:");
        if (recommendation == null || recommendation.isEmpty()) {
            recommendation = actuallyCompleted ? 
                "Task appears to be completed based on the uploaded work." : 
                "Task may need further work or the uploaded file does not meet requirements.";
        }
        analysis.setRecommendation(recommendation);
        
        log.info("File analysis completed for task {}: actuallyCompleted={}, confidence={}, grade={}", 
                taskId, actuallyCompleted, confidence, analysis.getGrade());
        
        return analysis;
    }
    
    private String extractField(String response, String fieldName) {
        int index = response.indexOf(fieldName);
        if (index == -1) {
            return null;
        }
        int start = index + fieldName.length();
        int end = response.indexOf("\n", start);
        if (end == -1) {
            end = response.length();
        }
        return response.substring(start, end).trim();
    }
}

