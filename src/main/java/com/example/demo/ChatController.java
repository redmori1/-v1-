package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatMessageRepository messageRepository;

    public ChatController(SimpMessagingTemplate messagingTemplate, ChatMessageRepository messageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendMessage(@DestinationVariable("roomId") Long roomId, @Payload ChatMessage chatMessage) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String fomattedDate = LocalDateTime.now().format(formatter);
        chatMessage.setTimestamp(fomattedDate);
        chatService.saveMessage(chatMessage);
        messagingTemplate.convertAndSend("/topic/public/" + roomId, chatMessage); // 메시지를 클라이언트로 전달
    }

    @MessageMapping("/chat.addUser")
    public void addUser(ChatMessage chatMessage) {
        chatMessage.setContent("User joined");
        chatMessage.setTimestamp(String.valueOf(LocalDateTime.now()));
        chatService.saveMessage(chatMessage);
        messagingTemplate.convertAndSend("/topic/public/" + chatMessage.getChatRoom().getId(), chatMessage); // 메시지를 클라이언트로 전달
    }

    @PostMapping("/uploadFile")
    @ResponseBody
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        String fileName = file.getOriginalFilename();
        try {
            Files.write(Paths.get("uploads/" + fileName), file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to upload file: " + fileName;
        }
        return "File uploaded successfully: " + fileName;
    }

    @PostMapping("/joinRoom")
    @ResponseBody
    public String joinRoom(@RequestParam("roomId") Long roomId, @RequestParam("password") String password) {
        Optional<ChatRoom> chatRoom = chatService.findChatRoomByIdAndPassword(roomId, password);
        if (chatRoom.isPresent()) {
            return "success";
        } else {
            return "failure";
        }
    }

    @PostMapping("/createRoom")
    @ResponseBody
    public ChatRoom createRoom(@RequestParam("name") String name, @RequestParam("password") String password) {
        return chatService.createChatRoom(name, password);
    }

    @GetMapping("/getPreviousMessages")
    public ResponseEntity<List<ChatMessage>> getPreviousMessages(@RequestParam Long roomId) {
        List<ChatMessage> messages = chatService.getAllMessagesByRoomId(roomId);
        return ResponseEntity.ok(messages);
    }
}
