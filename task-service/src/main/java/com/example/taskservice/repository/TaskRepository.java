package com.example.taskservice.repository;

import com.example.taskservice.model.Task;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByTeacherId(String teacherId);
    List<Task> findByStudentId(String studentId);
    List<Task> findByStudentIdsContaining(String studentId);
}