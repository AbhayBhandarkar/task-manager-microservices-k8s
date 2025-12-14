package com.example.agentservice.model;

import lombok.Data;

@Data
public class FileUploadResponse {
    private String taskId;
    private boolean taskDone;
    private boolean fileUploaded;
    private String fileUrl;
    private Double grade;
    private String analysis;
    private String reasoning;
    private double confidence;
}

