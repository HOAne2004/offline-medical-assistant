package com.example.trolyyte.data.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class VietnameseTextNormalizer {

    private static final Map<String, String> MEDICAL_DICT = new LinkedHashMap<>();
    private static boolean isInitialized = false;

    /**
     * Hàm đọc file JSON từ thư mục assets.
     * Chỉ thực thi 1 lần duy nhất khi khởi động App để tối ưu hiệu năng.
     */
    public static void init(Context context) {
        if (isInitialized) return;

        try {
            // 1. Đọc nội dung file medical_dict.json
            InputStream is = context.getAssets().open("medical_dict.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String jsonStr = new String(buffer, StandardCharsets.UTF_8).trim();
            List<Map.Entry<String, String>> tempEntries = new ArrayList<>();

            // 2. Tự động nhận diện cấu trúc JSON để bóc tách dữ liệu
            if (jsonStr.startsWith("[")) {
                // Trường hợp 1: JSON là Mảng các Intent (Giống output AI đã sinh)
                JSONArray rootArray = new JSONArray(jsonStr);
                for (int i = 0; i < rootArray.length(); i++) {
                    JSONObject intentObj = rootArray.getJSONObject(i);
                    if (intentObj.has("entries")) {
                        JSONArray entriesArray = intentObj.getJSONArray("entries");
                        for (int j = 0; j < entriesArray.length(); j++) {
                            JSONObject entry = entriesArray.getJSONObject(j);
                            String raw = entry.getString("raw").toLowerCase();
                            String normalized = entry.getString("normalized").toLowerCase();
                            tempEntries.add(new java.util.AbstractMap.SimpleEntry<>(raw, normalized));
                        }
                    }
                }
            } else if (jsonStr.startsWith("{")) {
                // Trường hợp 2: JSON là Object phẳng (Giống ví dụ tối giản của bạn)
                JSONObject rootObj = new JSONObject(jsonStr);
                Iterator<String> keys = rootObj.keys();
                while (keys.hasNext()) {
                    String raw = keys.next();
                    String normalized = rootObj.getString(raw);
                    tempEntries.add(new java.util.AbstractMap.SimpleEntry<>(raw.toLowerCase(), normalized.toLowerCase()));
                }
            }

            // 3. Thuật toán tối quan trọng: Sắp xếp theo độ dài từ khóa (Dài thay thế trước)
            tempEntries.sort((e1, e2) -> Integer.compare(e2.getKey().length(), e1.getKey().length()));

            // 4. Lưu vào Bộ nhớ đệm (LinkedHashMap giữ nguyên thứ tự sắp xếp)
            for (Map.Entry<String, String> entry : tempEntries) {
                MEDICAL_DICT.put(entry.getKey(), entry.getValue());
            }

            isInitialized = true;
            Log.d("TextNormalizer", "Đã nạp thành công " + MEDICAL_DICT.size() + " từ vựng y tế.");

        } catch (Exception e) {
            Log.e("TextNormalizer", "Lỗi khi đọc file medical_dict.json", e);
        }
    }

    /**
     * Hàm chuẩn hóa: Chuyển text không dấu thành có dấu dựa trên Dict đã nạp
     */
    public static String normalizeForTts(String input) {
        if (!isInitialized || input == null || input.isEmpty()) return input;

        String normalizedText = input.toLowerCase();

        for (Map.Entry<String, String> entry : MEDICAL_DICT.entrySet()) {
            String regex = "\\b" + entry.getKey() + "\\b";
            normalizedText = normalizedText.replaceAll(regex, entry.getValue());
        }

        return normalizedText;
    }
}