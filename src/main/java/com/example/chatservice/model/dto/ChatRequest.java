package com.example.chatservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatRequest {

    @NotNull(message = "Sender ID cannot be null")
    @Positive(message = "Sender ID must be a positive number")
    private Long senderId;

    @NotNull(message = "Receiver ID cannot be null")
    @Positive(message = "Receiver ID must be a positive number")
    private Long receiverId;

    @NotBlank(message = "Message cannot be empty")
    @Size(max = 1000, message = "Message cannot be longer than 1000 characters")
    private String message;
}