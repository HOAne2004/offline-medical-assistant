package com.example.trolyyte.data.repository;

import com.example.trolyyte.data.nlu.NlpEngine;
import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.domain.repository.NlpRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NlpRepositoryImpl implements NlpRepository {

    // SỬA Ở ĐÂY: Dùng Interface thay vì Class cụ thể
    private final NlpEngine nlpEngine;

    private final ExecutorService executorService;

    // SỬA Ở ĐÂY: Constructor nhận vào Interface NlpEngine
    public NlpRepositoryImpl(NlpEngine nlpEngine) {
        this.nlpEngine = nlpEngine;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void processText(String text, NlpRepository.NlpCallback callback) {
        executorService.execute(() -> {
            try {
                // Interface NlpEngine đã có hàm analyze, nên code này vẫn chạy tốt
                NlpResult result = nlpEngine.analyze(text);

                if (callback != null) {
                    callback.onSuccess(result);
                }

            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
}