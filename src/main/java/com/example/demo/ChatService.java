package com.example.demo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ChatService {
    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    public Optional<ChatRoom> findChatRoomByIdAndPassword(Long roomId, String password) {
        return chatRoomRepository.findByIdAndPassword(roomId, password);
    }
    public ChatRoom findChatRoomById(Long id) {
        return chatRoomRepository.findChatRoomById(id);
    }

    public void saveMessage(ChatMessage chatMessage) {
        chatMessageRepository.save(chatMessage);
    }

    public ChatRoom createChatRoom(String name, String password) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(name);
        chatRoom.setPassword(password);
        return chatRoomRepository.save(chatRoom);
    }

    public List<ChatMessage> getAllMessagesByRoomId(Long roomId) {
        return chatMessageRepository.findAllByChatRoom_Id(roomId);
    }
}
