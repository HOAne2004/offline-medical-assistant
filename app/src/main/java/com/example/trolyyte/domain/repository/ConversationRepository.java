package com.example.trolyyte.domain.repository;

import java.util.List;

public interface ConversationRepository {
    // Lưu một tin nhắn mới vào kho (Người dùng nói hoặc Máy nói)
    void saveMessage(String sender, String text, long timestamp);

    // Lấy toàn bộ lịch sử hội thoại để ném lên RecyclerView màn hình chính
    List<Object> getConversationHistory(); // Tạm dùng Object, sau này tạo class ChatMessage

    // Xóa lịch sử khi qua ngày mới
    void clearHistory();
}