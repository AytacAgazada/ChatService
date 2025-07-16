package com.example.chatservice.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatResponse {

    private String senderName;
    private String receiverName;
    private String message;
    private LocalDateTime timestamp;

}

