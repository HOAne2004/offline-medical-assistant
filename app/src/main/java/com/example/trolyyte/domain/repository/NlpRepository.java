package com.example.trolyyte.domain.repository;

import com.example.trolyyte.domain.model.NlpResult;

public interface NlpRepository {

    // Định nghĩa Callback ngay bên trong Interface
    interface NlpCallback {
        void onSuccess(NlpResult result);
        void onError(String error);
    }

    // Hàm xử lý chính
    void processText(String text, NlpCallback callback);
}