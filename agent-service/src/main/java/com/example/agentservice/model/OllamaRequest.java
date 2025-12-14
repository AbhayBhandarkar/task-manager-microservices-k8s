package com.example.agentservice.model;

import lombok.Data;

@Data
public class OllamaRequest {
    private String model;
    private String prompt;
    private boolean stream = false;
}

