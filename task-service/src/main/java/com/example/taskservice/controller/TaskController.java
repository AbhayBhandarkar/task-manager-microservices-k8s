package com.example.taskservice.controller;

import com.example.taskservice.model.Task;
import com.example.taskservice.repository.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable String id) {
        return taskRepository.findById(id).orElse(null);
    }
    
    @GetMapping("/user/{userId}")
    public List<Task> getTasksByUserId(@PathVariable String userId) {
        // Get tasks where user is teacher or student
        List<Task> teacherTasks = taskRepository.findByTeacherId(userId);
        List<Task> studentTasks = taskRepository.findByStudentId(userId);
        List<Task> studentIdsTasks = taskRepository.findByStudentIdsContaining(userId);
        
        // Combine and deduplicate using a map
        java.util.Map<String, Task> taskMap = new java.util.HashMap<>();
        teacherTasks.forEach(task -> taskMap.put(task.getId(), task));
        studentTasks.forEach(task -> taskMap.putIfAbsent(task.getId(), task));
        studentIdsTasks.forEach(task -> taskMap.putIfAbsent(task.getId(), task));
        
        return new java.util.ArrayList<>(taskMap.values());
    }
    
    @GetMapping("/teacher/{teacherId}")
    public List<Task> getTasksByTeacherId(@PathVariable String teacherId) {
        return taskRepository.findByTeacherId(teacherId);
    }
    
    @GetMapping("/student/{studentId}")
    public List<Task> getTasksByStudentId(@PathVariable String studentId) {
        // Get tasks where studentId matches or is in studentIds list
        List<Task> byStudentId = taskRepository.findByStudentId(studentId);
        List<Task> byStudentIds = taskRepository.findByStudentIdsContaining(studentId);
        
        // Combine and deduplicate using a map
        java.util.Map<String, Task> taskMap = new java.util.HashMap<>();
        byStudentId.forEach(task -> taskMap.put(task.getId(), task));
        byStudentIds.forEach(task -> taskMap.putIfAbsent(task.getId(), task));
        
        return new java.util.ArrayList<>(taskMap.values());
    }
    
    @PostMapping
    public Task createTask(@RequestBody Task task) {
        return taskRepository.save(task);
    }
    
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable String id, @RequestBody Task task) {
        log.info("Updating task {}: fileUploaded={}, taskDone={}, analysisReasoning={}, analysisRecommendation={}, analysisConfidence={}", 
                id, task.isFileUploaded(), task.isTaskDone(), 
                task.getAnalysisReasoning(), task.getAnalysisRecommendation(), task.getAnalysisConfidence());
        task.setId(id);
        Task savedTask = taskRepository.save(task);
        log.info("Task {} saved successfully with analysis fields: reasoning={}, recommendation={}, confidence={}", 
                id, savedTask.getAnalysisReasoning(), savedTask.getAnalysisRecommendation(), savedTask.getAnalysisConfidence());
        return savedTask;
    }
    
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable String id) {
        taskRepository.deleteById(id);
    }
}
