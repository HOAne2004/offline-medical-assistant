package com.example.trolyyte.presentation.common;

import com.example.trolyyte.domain.model.ResponseKey; // Nếu bạn muốn giữ overload

public interface ResponseTextProvider {

    // Sửa/Thêm hàm này để nhận String key (từ DialogueAction.name())
    String getText(String key);

    // Giữ lại hàm cũ nếu muốn support Enum ResponseKey
    default String getText(ResponseKey key) {
        return getText(key.name());
    }
}