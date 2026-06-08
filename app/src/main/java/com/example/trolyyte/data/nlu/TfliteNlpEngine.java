package com.example.trolyyte.data.nlu;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.domain.model.NluIntent;
import com.example.trolyyte.data.utils.NluTextNormalizer;

import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.flex.FlexDelegate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TfliteNlpEngine implements NlpEngine {

    private static final String TAG = "TfliteNlpEngine";
    private static final String MODEL_FILE = "models/nlu_model.tflite";
    private static final String TOKENIZER_FILE = "models/keras_tokenizer.json";
    private static final String LABEL_FILE = "models/label_encoder.json";

    private static final int MAX_SEQUENCE_LENGTH = 30;

    // ĐÃ SỬA: Hạ Threshold xuống để AI đỡ bị "bắt bẻ" do dữ liệu train nhỏ
    private static final float CONFIDENCE_THRESHOLD = 0.65f;

    private final Context context;
    private Interpreter tflite;

    private Map<String, Float> wordIndex = new HashMap<>();
    private Map<Integer, String> labelMap = new HashMap<>();

    public TfliteNlpEngine(Context context) {
        this.context = context;
        initialize();
    }

    @Override
    public void initialize() {
        try {
            MappedByteBuffer tfliteModel = loadModelFile(MODEL_FILE);

            Interpreter.Options options = new Interpreter.Options();

            // --- THÊM ĐOẠN NÀY: Ép Android dùng Flex Delegate để xử lý các Op mới ---
            try {
                FlexDelegate flexDelegate = new FlexDelegate();
                options.addDelegate(flexDelegate);
                Log.d(TAG, "✅ Đã thêm FlexDelegate thành công!");
            } catch (Exception e) {
                Log.w(TAG, "⚠️ FlexDelegate không khả dụng, thử dùng Op chuẩn...");
            }
            // ----------------------------------------------------------------------

            tflite = new Interpreter(tfliteModel, options);
            Log.d(TAG, "✅ [1/3] Nạp TFLite Model: THÀNH CÔNG!");
        } catch (Exception e) {
            Log.e(TAG, "❌ [1/3] Nạp TFLite Model: THẤT BẠI! Lỗi: " + e.getMessage());
        }

        loadTokenizer();
        loadLabels();
    }

    // Thêm synchronized để Thread-safe (Chống crash khi nhiều luồng cùng gọi)
    @Override
    public synchronized NlpResult analyze(String text) {
        // KIỂM TRA CHỐT CHẶN (Nguyên nhân gây ra UNKNOWN 0.0%)
        if (tflite == null) {
            Log.e(TAG, "❌ LỖI: Engine không chạy vì tflite Model bị null!");
            return new NlpResult(text, NluIntent.UNKNOWN, new HashMap<>(), 0.0f);
        }
        if (wordIndex == null || wordIndex.isEmpty()) {
            Log.e(TAG, "❌ LỖI: Engine không chạy vì Bộ từ điển (Tokenizer) rỗng!");
            return new NlpResult(text, NluIntent.UNKNOWN, new HashMap<>(), 0.0f);
        }
        if (labelMap == null || labelMap.isEmpty()) {
            Log.e(TAG, "❌ LỖI: Engine không chạy vì Label Encoder rỗng!");
            return new NlpResult(text, NluIntent.UNKNOWN, new HashMap<>(), 0.0f);
        }

        // BƯỚC 1: Tiền xử lý chữ -> số (VD: "tám giờ" -> "8 giờ")
        String nluText = NluTextNormalizer.normalizeForNlu(text);

        // BƯỚC 2: Tokenization
        float[][] input = tokenizeAndPad(nluText);
        float[][] output = new float[1][labelMap.size()];
        tflite.run(input, output);

        // BƯỚC 3: Log Top 3 Intent để dễ Debug
        logTop3Intents(nluText, output[0]);

// BƯỚC 4: Lấy Top 1 Intent
        int maxIndex = argMax(output[0]);
        float confidence = output[0][maxIndex];
        String aiLabel = labelMap.getOrDefault(maxIndex, "FALLBACK");

        Log.d("NLU_TEST", "AI Confidence = " + confidence + " | AI Label = " + aiLabel);

        // ======================================================
        // ÁP DỤNG HYBRID RULE-BASED (Bản sửa lỗi)
        // Lọc các từ khóa tử huyệt trước để cứu AI
        // ======================================================
        String labelStr = aiLabel; // Mặc định tin AI trước
        String lower = nluText.toLowerCase();

        // 1. RULE CẤP CỨU (Ưu tiên tuyệt đối)
        if (lower.contains("cấp cứu") || lower.contains("khó thở") || lower.contains("đau ngực") || lower.contains("cứu tôi")) {
            labelStr = "REQUEST_EMERGENCY";
        }
        // 2. RULE ĐẶT LỊCH THUỐC (Chứa chữ nhắc + uống)
        else if ((lower.contains("nhắc") || lower.contains("đặt lịch")) && (lower.contains("uống") || lower.contains("thuốc"))) {
            labelStr = "SET_OR_UPDATE_MEDICATION";
        }
        // 3. RULE ĐẶT LỊCH KHÁM
        else if (lower.contains("đặt lịch khám") || lower.contains("tái khám") || (lower.contains("khám") && lower.contains("bệnh viện"))) {
            labelStr = "SET_OR_UPDATE_APPOINTMENT";
        }
        // 4. RULE TRA CỨU THUỐC (Nằm dưới để không đè lên nhắc thuốc)
        else if (lower.contains("tác dụng") || lower.contains("liều dùng") || lower.contains("thuốc này")) {
            labelStr = "INQUIRE_MEDICINE";
        }
        // 5. NẾU KHÔNG TRÚNG RULE NÀO -> KIỂM TRA ĐIỂM CỦA AI
        else {
            if (confidence < 0.60f) { // Nếu AI ko chắc chắn (< 60%)
                labelStr = "FALLBACK";
            }
        }

        NluIntent intent = mapLabelToIntent(labelStr);
        Log.d("NLU_TEST", "FINAL INTENT (Sau khi qua Rule) = " + intent.name());

        // BƯỚC 5: TRÍCH XUẤT THỰC THỂ (Entity Extraction bằng Rule-based)
        Map<String, String> entities = RuleBasedEntityExtractor.extract(nluText);

        return new NlpResult(nluText, intent, entities, confidence);
    }

    // Log Top 3 Intents (Cực kỳ hữu ích khi báo cáo NCKH)
    private void logTop3Intents(String text, float[] probabilities) {
        List<IntentProb> list = new ArrayList<>();
        for (int i = 0; i < probabilities.length; i++) {
            list.add(new IntentProb(labelMap.getOrDefault(i, "unknown"), probabilities[i]));
        }
        Collections.sort(list, (a, b) -> Float.compare(b.prob, a.prob));

        Log.d("NLU_TEST", "--- KẾT QUẢ AI PHÂN TÍCH: \"" + text + "\" ---");
        for (int i = 0; i < Math.min(3, list.size()); i++) {
            Log.d("NLU_TEST", String.format("Top %d: %-25s | %.2f%%", (i+1), list.get(i).intent, list.get(i).prob * 100));
        }
    }

    // Lớp phụ trợ để sort
    private static class IntentProb {
        String intent; float prob;
        IntentProb(String i, float p) { intent = i; prob = p; }
    }

    // Hàm giải phóng bộ nhớ (Tránh Memory Leak)
    public void shutdown() {
        if (tflite != null) {
            tflite.close();
            tflite = null;
        }
    }

    // --- Các hàm Load Model, Tokenizer, Pad giữ nguyên như cũ ---
    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.getStartOffset(), fileDescriptor.getDeclaredLength());
    }

    // Thuật toán quét JSON Tokenizer siêu cấp, chống crash 100%
    private void loadTokenizer() {
        wordIndex = new HashMap<>();
        try {
            String jsonStr = loadJsonFromAsset(TOKENIZER_FILE);
            JSONObject root = new JSONObject(jsonStr);
            JSONObject wordIndexJson = null;

            if (root.has("config")) {
                JSONObject config = root.getJSONObject("config");
                if (config.has("word_index")) {
                    Object obj = config.get("word_index");
                    if (obj instanceof String) {
                        // Trường hợp word_index bị lưu dưới dạng String
                        wordIndexJson = new JSONObject((String) obj);
                    } else {
                        // Trường hợp word_index là một Object bình thường
                        wordIndexJson = config.getJSONObject("word_index");
                    }
                }
            } else if (root.has("word_index")) {
                wordIndexJson = root.getJSONObject("word_index");
            }

            if (wordIndexJson != null) {
                Iterator<String> keys = wordIndexJson.keys();
                while (keys.hasNext()) {
                    String word = keys.next();
                    wordIndex.put(word, (float) wordIndexJson.getDouble(word));
                }
                Log.d(TAG, "Đã nạp thành công " + wordIndex.size() + " từ vựng từ Tokenizer.");
            } else {
                Log.e(TAG, "Không tìm thấy cấu trúc word_index trong file JSON!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi rớt đài khi load Tokenizer: ", e);
        }
    }

    private void loadLabels() {
        labelMap = new HashMap<>();
        try {
            JSONObject root = new JSONObject(loadJsonFromAsset(LABEL_FILE));
            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                int id = Integer.parseInt(keys.next());
                labelMap.put(id, root.getString(String.valueOf(id)));
            }
            Log.d(TAG, "✅ [3/3] Nạp Label Encoder: THÀNH CÔNG (" + labelMap.size() + " intents)");
        } catch (Exception e) {
            Log.e(TAG, "❌ [3/3] Nạp Label: THẤT BẠI! Lỗi: " + e.getMessage());
        }
    }

    private float[][] tokenizeAndPad(String text) {
        String[] words = text.split("\\s+");
        float[][] inputSequence = new float[1][MAX_SEQUENCE_LENGTH];

        // Thêm đoạn này để bật X-Quang theo dõi Tokenizer
        StringBuilder logTokens = new StringBuilder("Mảng Token: [");

        for (int i = 0; i < MAX_SEQUENCE_LENGTH; i++) {
            if (i < words.length) {
                float tokenId = wordIndex.getOrDefault(words[i], 1.0f); // 1.0f là từ không biết (OOV)
                inputSequence[0][i] = tokenId;
                logTokens.append(tokenId).append(", ");
            } else {
                inputSequence[0][i] = 0.0f; // Post-padding
            }
        }

        logTokens.append("]");
        Log.d(TAG, "Câu gốc sau khi chuẩn hóa: " + text);
        Log.d("NLU_TEST", logTokens.toString());

        return inputSequence;
    }

    private int argMax(float[] array) {
        int maxIdx = 0;
        for (int i = 1; i < array.length; i++) {
            if (array[i] > array[maxIdx]) maxIdx = i;
        }
        return maxIdx;
    }

    private String loadJsonFromAsset(String filename) throws IOException {
        InputStream is = context.getAssets().open(filename);
        byte[] buffer = new byte[is.available()];
        is.read(buffer);
        is.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }

    private NluIntent mapLabelToIntent(String label) {
        try { return NluIntent.valueOf(label.toUpperCase()); }
        catch (IllegalArgumentException e) { return NluIntent.UNKNOWN; }
    }
}