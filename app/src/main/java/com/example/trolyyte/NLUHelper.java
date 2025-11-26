package com.example.trolyyte;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class NLUHelper {
    private static final String TAG = "NLUHelper";

    // Cấu hình (Phải khớp với file preprocess_data.py trên Colab)
    private static final int MAX_SEQ_LEN = 20;

    private Interpreter tflite;
    private Map<String, Integer> wordIndex; // Tokenizer map
    private Map<String, String> labelMap;   // Label encoder map (ID -> Name)

    public NLUHelper(Context context) {
        try {
            // 1. Tải Model TFLite
            tflite = new Interpreter(loadModelFile(context, "nlu_model.tflite"));

            // 2. Tải Tokenizer và Label Map
            loadTokenizer(context);
            loadLabelEncoder(context);

            Log.d(TAG, "NLU Initialized successfully!");
        } catch (Exception e) {
            Log.e(TAG, "Error initializing NLU", e);
        }
    }

    // --- HÀM DỰ ĐOÁN (QUAN TRỌNG NHẤT) ---
    public String predict(String text) {
        if (tflite == null || wordIndex == null) return "Error: Model not loaded";

        // 1. Tiền xử lý (Tokenize + Padding)
        float[][] input = tokenizeAndPad(text);

        // 2. Chạy Model
        // Output shape: [1, số_lượng_intent]
        float[][] output = new float[1][labelMap.size()];
        tflite.run(input, output);

        // 3. Giải mã kết quả (Tìm chỉ số có xác suất cao nhất)
        int bestIndex = -1;
        float bestConfidence = -1;

        for (int i = 0; i < output[0].length; i++) {
            if (output[0][i] > bestConfidence) {
                bestConfidence = output[0][i];
                bestIndex = i;
            }
        }

        // 4. Trả về tên Intent và độ tin cậy
        String intentName = labelMap.get(String.valueOf(bestIndex));
        return intentName + " (" + String.format("%.2f", bestConfidence) + ")";
    }

    // --- CÁC HÀM PHỤ TRỢ ---

    // Chuyển văn bản thành mảng số float[1][20] giống Python
    private float[][] tokenizeAndPad(String text) {
        float[][] sequence = new float[1][MAX_SEQ_LEN];

        // Xử lý chuỗi: chữ thường, bỏ dấu câu cơ bản
        String cleanText = text.toLowerCase().replaceAll("[^a-z0-9a-áàảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệđìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵ ]", "");
        String[] words = cleanText.split("\\s+");

        // Map từ -> số (Padding = post, nghĩa là điền từ trái sang, còn lại là 0)
        for (int i = 0; i < Math.min(words.length, MAX_SEQ_LEN); i++) {
            String word = words[i];
            if (wordIndex.containsKey(word)) {
                sequence[0][i] = (float) wordIndex.get(word);
            } else {
                // Nếu từ không có trong từ điển, dùng token <OOV> (thường là 1)
                // Trong Python Tokenizer mặc định, <OOV> thường có index 1.
                // Nếu bạn không chắc, có thể để 0 hoặc 1. Ở đây để an toàn ta bỏ qua (để 0)
                // hoặc gán giá trị của <OOV> nếu biết.
                if (wordIndex.containsKey("<OOV>")) {
                    sequence[0][i] = (float) wordIndex.get("<OOV>");
                } else {
                    sequence[0][i] = 0; // Coi như không biết (padding)
                }
            }
        }
        return sequence;
    }

    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private void loadTokenizer(Context context) {
        try {
            String jsonString = loadJSONFromAsset(context, "keras_tokenizer.json");
            JSONObject json = new JSONObject(jsonString);
            String wordIndexStr = json.getString("word_index"); // Lấy phần word_index

            // Dùng Gson để map JSON String thành Map
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Integer>>(){}.getType();
            wordIndex = gson.fromJson(wordIndexStr, type);

        } catch (Exception e) {
            Log.e(TAG, "Error loading tokenizer", e);
        }
    }

    private void loadLabelEncoder(Context context) {
        try {
            String jsonString = loadJSONFromAsset(context, "label_encoder.json");
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            labelMap = gson.fromJson(jsonString, type);
        } catch (Exception e) {
            Log.e(TAG, "Error loading label encoder", e);
        }
    }

    private String loadJSONFromAsset(Context context, String filename) {
        String json = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}