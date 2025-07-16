package com.example.chatservice.service;

import com.example.chatservice.feign.UserClient;
import com.example.chatservice.model.dto.ChatRequest;
import com.example.chatservice.model.dto.ChatResponse;
import com.example.chatservice.model.entity.ChatMessage;
import com.example.chatservice.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserClient userClient;

    @CacheEvict(value = "chatHistory", key = "{#chatRequest.senderId, #chatRequest.receiverId}")
    public ChatMessage sendMessage(ChatRequest chatRequest) {
        log.info("Sending message from senderId: {} to receiverId: {}", chatRequest.getSenderId(), chatRequest.getReceiverId());

        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSenderId(chatRequest.getSenderId());
        chatMessage.setReceiverId(chatRequest.getReceiverId());
        chatMessage.setMessage(chatRequest.getMessage());
        chatMessage.setTimestamp(LocalDateTime.now());

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.info("Message saved successfully with ID: {}", savedMessage.getId());
        return savedMessage;
    }

    @Cacheable(value = "chatHistory", key = "#senderId + '_' + #receiverId")
    public List<ChatResponse> getChatHistory(Long senderId, Long receiverId) {
        log.info("Fetching chat history for senderId: {} and receiverId: {}", senderId, receiverId);

        List<ChatMessage> messages1 = chatMessageRepository.findBySenderIdAndReceiverIdOrderByTimestampAsc(senderId, receiverId);
        List<ChatMessage> messages2 = chatMessageRepository.findBySenderIdAndReceiverIdOrderByTimestampAsc(receiverId, senderId);

        List<ChatMessage> allMessages = new java.util.ArrayList<>(messages1);
        allMessages.addAll(messages2);
        allMessages.sort(Comparator.comparing(ChatMessage::getTimestamp));

        List<ChatResponse> chatResponses = allMessages.stream()
                .map(this::mapToChatResponse)
                .collect(Collectors.toList());

        log.info("Found {} messages for chat history between {} and {}", chatResponses.size(), senderId, receiverId);
        return chatResponses;
    }

    private ChatResponse mapToChatResponse(ChatMessage chatMessage) {
        ChatResponse chatResponse = new ChatResponse();
        chatResponse.setMessage(chatMessage.getMessage());
        chatResponse.setTimestamp(chatMessage.getTimestamp());

        try {
            String senderName = userClient.getUserName(chatMessage.getSenderId());
            chatResponse.setSenderName(senderName != null ? senderName : "Unknown Sender");
        } catch (Exception e) {
            log.warn("Could not fetch sender name for ID: {}. Error: {}", chatMessage.getSenderId(), e.getMessage());
            chatResponse.setSenderName("Unknown Sender");
        }

        try {
            String receiverName = userClient.getUserName(chatMessage.getReceiverId());
            chatResponse.setReceiverName(receiverName != null ? receiverName : "Unknown Receiver");
        } catch (Exception e) {
            log.warn("Could not fetch receiver name for ID: {}. Error: {}", chatMessage.getReceiverId(), e.getMessage());
            chatResponse.setReceiverName("Unknown Receiver");
        }

        return chatResponse;
    }

    @CacheEvict(value = "chatHistory", key = "#senderId + '_' + #receiverId")
    public void evictChatHistoryCache(Long senderId, Long receiverId) {
        log.info("Evicting chat history cache for senderId: {} and receiverId: {}", senderId, receiverId);
    }
}
