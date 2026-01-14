package com.example.trolyyte.domain.dialog;

import com.example.trolyyte.domain.model.ResponseKey;

public interface ResponseTemplateProvider {
    /**
     * Lấy mẫu câu dựa trên Key.
     * Ví dụ: ASK_TIME -> "Bác muốn uống thuốc này vào lúc mấy giờ ạ?"
     */
    String getTemplate(ResponseKey key);
}