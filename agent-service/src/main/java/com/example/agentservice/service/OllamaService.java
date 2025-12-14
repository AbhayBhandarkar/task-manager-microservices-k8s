package com.example.agentservice.service;

import com.example.agentservice.model.OllamaRequest;
import com.example.agentservice.model.OllamaResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class OllamaService {
    
    private WebClient webClient;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;
    
    @Value("${ollama.url:http://host.docker.internal:11434}")
    private String ollamaUrl;
    
    @Value("${ollama.model:llama3.2}")
    private String defaultModel;
    
    @Value("${ollama.vision.model:llava}")
    private String visionModel;
    
    public OllamaService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClientBuilder = webClientBuilder;
        this.objectMapper = objectMapper;
    }
    
    @PostConstruct
    private void init() {
        this.webClient = webClientBuilder.baseUrl(ollamaUrl).build();
    }
    
    public Mono<String> generateResponse(String prompt) {
        return generateResponse(prompt, defaultModel);
    }
    
    public Mono<String> generateResponse(String prompt, String model) {
        OllamaRequest request = new OllamaRequest();
        request.setModel(model);
        request.setPrompt(prompt);
        request.setStream(false);
        
        log.info("Calling Ollama API at {} with model: {}, prompt length: {}", ollamaUrl, model, prompt.length());
        
        return webClient.post()
            .uri("/api/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(OllamaResponse.class)
            .timeout(Duration.ofSeconds(30))
            .map(OllamaResponse::getResponse)
            .doOnError(error -> log.error("Error calling Ollama API: {}", error.getMessage()))
            .onErrorReturn("Unable to analyze task. Please check if Ollama is running.");
    }
    
    public Mono<String> analyzeImageWithVision(String base64Image, String prompt) {
        return analyzeImageWithVision(base64Image, prompt, visionModel);
    }
    
    public Mono<String> analyzeImageWithVision(String base64Image, String prompt, String model) {
        // Ollama vision API format
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);
        request.put("prompt", prompt);
        request.put("stream", false);
        request.put("images", new String[]{base64Image});
        
        log.info("Calling Ollama Vision API at {} with model: {}, prompt length: {}", ollamaUrl, model, prompt.length());
        
        return webClient.post()
            .uri("/api/generate")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(OllamaResponse.class)
            .timeout(Duration.ofSeconds(60))
            .map(OllamaResponse::getResponse)
            .doOnError(error -> log.error("Error calling Ollama Vision API: {}", error.getMessage()))
            .onErrorReturn("Unable to analyze image. Please check if Ollama is running with a vision model.");
    }
    
    public String encodeImageToBase64(byte[] imageBytes) {
        return Base64.getEncoder().encodeToString(imageBytes);
    }
}

