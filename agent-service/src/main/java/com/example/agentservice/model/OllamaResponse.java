package com.example.agentservice.model;

import lombok.Data;

@Data
public class OllamaResponse {
    private String model;
    private String response;
    private boolean done;
}

