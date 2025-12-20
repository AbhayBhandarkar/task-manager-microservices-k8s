package com.example.taskservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.util.List;

@Data
@Document(collection = "tasks")
public class Task {
    @Id
    private String id;
    private String title;
    private String description;
    private String teacherId; // ID of the teacher who assigned the task
    private String teacherName; // Name of the teacher who assigned the task
    private String studentId; // Single student ID (for backward compatibility)
    private String studentName; // Name of the student (for single assignment)
    private List<String> studentIds; // List of student IDs for group assignments
    private List<String> studentNames; // List of student names for group assignments
    private String subject; // Optional subject/course name
    private boolean fileUploaded; // Whether student has uploaded a file
    private boolean taskDone; // Whether task is completed (determined by vision model)
    private boolean completed; // Legacy field for backward compatibility
    private String fileUrl; // URL/path to uploaded file
    private Double grade; // Optional grade assigned by vision model
    private String analysisReasoning; // AI analysis reasoning/feedback
    private String analysisRecommendation; // AI analysis recommendation
    private Double analysisConfidence; // AI confidence score (0.0 to 1.0)
}
