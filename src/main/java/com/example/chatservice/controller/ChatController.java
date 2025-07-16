package com.example.chatservice.controller;

import com.example.chatservice.model.dto.ChatRequest;
import com.example.chatservice.model.dto.ChatResponse;
import com.example.chatservice.model.entity.ChatMessage;
import com.example.chatservice.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/send")
    public ResponseEntity<ChatMessage> sendMessage(@Valid @RequestBody ChatRequest chatRequest) {
        log.info("Received request to send message: {}", chatRequest);
        ChatMessage savedMessage = chatService.sendMessage(chatRequest);
        return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
    }

    @GetMapping("/history/{senderId}/{receiverId}")
    public ResponseEntity<List<ChatResponse>> getChatHistory(
            @PathVariable Long senderId,
            @PathVariable Long receiverId) {
        log.info("Received request to get chat history for senderId: {} and receiverId: {}", senderId, receiverId);
        List<ChatResponse> chatHistory = chatService.getChatHistory(senderId, receiverId);
        return new ResponseEntity<>(chatHistory, HttpStatus.OK);
    }
}
