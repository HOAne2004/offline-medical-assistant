package com.example.trolyyte.data.asr;

import android.content.Context;
import android.util.Log;
import com.example.trolyyte.domain.usecase.ListenVoiceResult; // Đảm bảo import đúng

import org.json.JSONArray;
import org.json.JSONObject;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.RecognitionListener;
import org.vosk.android.SpeechService;
import org.vosk.android.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class VoskAsrEngine implements AsrEngine, RecognitionListener {

    private static final String TAG = "VoskAsrEngine";
    private final Context context;

    private Model model;
    private SpeechService speechService;
    private Callback callback;

    private boolean isModelLoaded = false;
    private boolean isInitializing = false; // Cờ mới để chặn init nhiều lần

    private String cachedGrammar = null;

    public VoskAsrEngine(Context context) {
        this.context = context;
    }

    @Override
    public void initialize() {
        // Nếu đã load xong hoặc đang load thì không làm gì cả
        if (isModelLoaded || isInitializing) return;

        isInitializing = true; // Đánh dấu đang load
        Log.d(TAG, "Bắt đầu giải nén Model Vosk...");
        prepareGrammar();
        StorageService.unpack(context, "model-vn", "model",
                (model) -> {
                    this.model = model;
                    isModelLoaded = true;
                    isInitializing = false; // Load xong
                    Log.d(TAG, "Vosk Model loaded successfully");
                    if (callback != null) callback.onReady();
                },
                (exception) -> {
                    isInitializing = false; // Lỗi cũng phải reset cờ
                    Log.e(TAG, "Failed to load model: " + exception.getMessage());
                    if (callback != null) callback.onError(exception);
                });
    }

    @Override
    public void startListening(Callback callback) {
        this.callback = callback;

        if (!isModelLoaded) {
            if (!isInitializing) initialize();
            callback.onError(new Exception("Hệ thống đang khởi động, vui lòng đợi 2 giây..."));
            return;
        }

        try {
            // Nâng cấp của ChatGPT: Giải phóng mic triệt để
            if (speechService != null) {
                speechService.stop();
                speechService.shutdown();
                speechService = null;
            }

            // 1. ÉP CỨNG SAMPLE RATE CHUẨN CỦA VOSK MODEL
            float sampleRate = 16000.0f;
            Log.d(TAG, "Recognizer chạy ở 16000Hz");

            /// =====================================================================
            // BƯỚC TEST 2: NẾU VOSK QUÁ TỆ, BẬT CHẾ ĐỘ GRAMMAR LAI
            // =====================================================================
            String hybridGrammar = "[" +
                    // 1. Nhóm Cụm từ cố định (Tăng tối đa độ chính xác cho Intent)
                    "\"đặt lịch khám\", \"đặt lịch tái khám\", \"cho tôi đặt lịch\", \"tôi muốn đặt lịch\", " +
                    "\"nhắc tôi uống thuốc\", \"đặt lịch uống thuốc\", \"nhắc tôi đi\", " +
                    "\"gọi cấp cứu\", \"cứu tôi với\", \"tôi bị khó thở\", \"tôi bị đau ngực\", \"gọi cho người thân\", " +
                    "\"có\", \"không\", \"hủy\", \"đồng ý\", " +

                    // 2. Nhóm từ vựng - Hành động & Đại từ
                    "\"nhắc\", \"tôi\", \"uống\", \"thuốc\", \"đặt\", \"lịch\", \"khám\", \"đi\", \"đo\", \"tập\", \"gọi\", \"muốn\", \"cho\", \"hẹn\", " +

                    // 3. Nhóm từ vựng - Tên thuốc, Bệnh lý & Hoạt động (Phục vụ Entity Extraction)
                    "\"huyết\", \"áp\", \"paracetamol\", \"vitamin\", \"c\", \"dạ\", \"dày\", \"tiểu\", \"đường\", \"cảm\", \"cúm\", \"bổ\", " +
                    "\"thể\", \"dục\", \"bộ\", \"mắt\", \"răng\", \"tim\", \"mạch\", \"tai\", \"mũi\", \"họng\", \"tổng\", \"quát\", " +
                    "\"sức\", \"khỏe\", \"bệnh\", \"viện\", \"bạch\", \"mai\", \"bác\", \"sĩ\", " +

                    // 4. Nhóm từ vựng - Thời gian & Ngày tháng (Phục vụ Entity Extraction)
                    "\"lúc\", \"vào\", \"giờ\", \"phút\", \"sáng\", \"trưa\", \"chiều\", \"tối\", \"nay\", \"ngày\", \"mai\", \"kia\", " +
                    "\"tuần\", \"sau\", \"tới\", \"thứ\", \"hai\", \"ba\", \"tư\", \"năm\", \"sáu\", \"bảy\", \"chủ\", \"nhật\", " +
                    "\"trước\", \"khi\", \"ăn\", \"bữa\", \"báo\", \"thức\", " +

                    // 5. Nhóm từ vựng - Số đếm (Giờ giấc)
                    "\"một\", \"hai\", \"ba\", \"bốn\", \"năm\", \"sáu\", \"bảy\", \"tám\", \"chín\", \"mười\", \"mười một\", \"mười hai\", " +
                    "\"ngày mai\", \"ngày kia\", " +

                    // 6. Nhóm từ vựng - Bổ trợ
                    "\"với\", \"quá\", \"đang\", \"bị\", \"đau\", \"ngay\", \"lập\", \"tức\", \"giúp\", \"dữ\", \"dội\", " +

                    // 7. Fallback (Bắt buộc phải có)
                    "\"[unk]\"]";

            Recognizer recognizer = new Recognizer(model, sampleRate, hybridGrammar);

            speechService = new SpeechService(recognizer, sampleRate);
            speechService.startListening(this);
            callback.onReady();

        } catch (IOException e) {
            callback.onError(e);
        }
    }

    @Override
    public void stopListening() {
        if (speechService != null) {
            speechService.stop();
            speechService = null;
        }
    }

    // ... (Giữ nguyên các hàm RecognitionListener bên dưới của bạn) ...
    @Override
    public void onResult(String hypothesis) {
        String cleanText = parseVoskJson(hypothesis, "text");
        if (!cleanText.isEmpty() && callback != null) {
            callback.onFinalResult(cleanText);
        }
    }

    @Override
    public void onPartialResult(String hypothesis) {
        String cleanText = parseVoskJson(hypothesis, "partial");
        if (!cleanText.isEmpty() && callback != null) {
            callback.onPartialResult(cleanText);
        }
    }

    @Override
    public void onFinalResult(String hypothesis) {
        String cleanText = parseVoskJson(hypothesis, "text");
        if (!cleanText.isEmpty() && callback != null) {
            callback.onFinalResult(cleanText);
        }
    }

    @Override
    public void onError(Exception exception) {
        if (callback != null) callback.onError(exception);
    }

    @Override
    public void onTimeout() {}

    @Override
    public void release() {
        stopListening();
        if (model != null) {
            model.close();
            model = null;
        }
        isModelLoaded = false;
    }

    private String parseVoskJson(String jsonString, String key) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject.optString(key, "");
        } catch (Exception e) {
            return "";
        }
    }

    private void prepareGrammar() {
        if (cachedGrammar != null) return; // Nếu đã build rồi thì thôi

        try {
            Set<String> vocab = new HashSet<>();

            // 1. Đọc file medical_dict.json từ thư mục assets
            InputStream is = context.getAssets().open("medical_dict.json");
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String jsonStr = new String(buffer, "UTF-8");

            // 2. Phân tích JSON
            JSONArray root = new JSONArray(jsonStr);
            for (int i = 0; i < root.length(); i++) {
                JSONObject intentObj = root.getJSONObject(i);
                JSONArray entries = intentObj.getJSONArray("entries");

                for (int j = 0; j < entries.length(); j++) {
                    JSONObject entry = entries.getJSONObject(j);
                    String raw = entry.optString("raw", "").toLowerCase().trim();
                    String norm = entry.optString("normalized", "").toLowerCase().trim();

                    // Thêm nguyên Cụm từ (Để fix cứng độ chính xác cho Vosk)
                    if (!raw.isEmpty()) vocab.add(raw);
                    if (!norm.isEmpty()) vocab.add(norm);

                    // Băm nhỏ thành Từng từ (Để NLU linh hoạt bóc tách slot: giờ, tên thuốc)
                    if (!raw.isEmpty()) {
                        for (String w : raw.split("\\s+")) vocab.add(w);
                    }
                    if (!norm.isEmpty()) {
                        for (String w : norm.split("\\s+")) vocab.add(w);
                    }
                }
            }

            // 3. Bổ sung các từ khóa thời gian, số đếm (Phòng hờ trong file JSON bị thiếu)
            String[] extras = {"một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín", "mười", "mười một", "mười hai",
                    "giờ", "phút", "sáng", "trưa", "chiều", "tối", "nay", "mai", "kia", "có", "không", "[unk]"};
            for (String e : extras) vocab.add(e);

            // 4. Đóng gói thành chuỗi JSON Array cho Vosk
            StringBuilder sb = new StringBuilder("[");
            int count = 0;
            for (String w : vocab) {
                sb.append("\"").append(w).append("\"");
                if (count < vocab.size() - 1) sb.append(", ");
                count++;
            }
            sb.append("]");

            cachedGrammar = sb.toString();
            Log.d(TAG, "Đã build Grammar tự động từ JSON thành công với " + vocab.size() + " từ/cụm từ.");

        } catch (Exception e) {
            Log.e(TAG, "Lỗi đọc medical_dict.json: " + e.getMessage());
            cachedGrammar = "[\"[unk]\"]"; // Fallback an toàn nếu file JSON lỗi
        }
    }
}