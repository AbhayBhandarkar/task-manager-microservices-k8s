package com.example.agentservice.model;

import lombok.Data;

@Data
public class TaskAnalysis {
    private String taskId;
    private String taskTitle;
    private String taskDescription;
    private boolean markedCompleted;
    private boolean actuallyCompleted;
    private double confidence;
    private String reasoning;
    private String recommendation;
    private Double grade; // Optional grade from vision model analysis
}

