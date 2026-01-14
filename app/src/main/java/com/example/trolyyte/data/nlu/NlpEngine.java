package com.example.trolyyte.data.nlu;

import com.example.trolyyte.domain.model.NlpResult;

public interface NlpEngine {
    // Cả Rule-based và TFLite đều phải tuân thủ hàm này
    NlpResult analyze(String text);

    // Hàm khởi tạo (nếu cần load model nặng)
    default void initialize() {}
}