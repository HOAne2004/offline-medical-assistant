package com.example.trolyyte.data.nlu;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;

import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.domain.model.NluIntent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.tensorflow.lite.Interpreter;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TfliteNlpEngine implements NlpEngine {

    private static final String TAG = "TfliteNlpEngine";

    private static final String MODEL_FILE = "nlu_model.tflite";
    private static final String TOKENIZER_FILE = "keras_tokenizer.json";
    private static final String LABEL_FILE = "label_encoder.json";

    private static final int MAX_SEQUENCE_LENGTH = 20;

    private final Context context;
    private Interpreter tflite;

    private Map<String, Double> wordIndex;
    private List<String> labelList;

    public TfliteNlpEngine(Context context) {
        this.context = context;
    }

    @Override
    public void initialize() {
        try {
            // --- SỬA ĐỔI: Dùng hàm load thủ công thay vì FileUtil ---
            MappedByteBuffer tfliteModel = loadModelFile(MODEL_FILE);
            // -------------------------------------------------------

            Interpreter.Options options = new Interpreter.Options();
            tflite = new Interpreter(tfliteModel, options);

            loadTokenizer();
            loadLabels();

            Log.d(TAG, "TFLite Engine initialized successfully");

        } catch (IOException e) {
            Log.e(TAG, "Error initializing TFLite Engine: " + e.getMessage());
        }
    }

    // --- HÀM MỚI: Load model TFLite từ Assets bằng Java thuần ---
    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    // -----------------------------------------------------------

    @Override
    public NlpResult analyze(String text) {
        if (tflite == null || wordIndex == null || labelList == null) {
            Log.e(TAG, "Engine chưa được khởi tạo!");
            return new NlpResult(text, NluIntent.UNKNOWN, new HashMap<>(), 0.0f);
        }

        float[][] input = tokenizeAndPad(text);
        float[][] output = new float[1][labelList.size()];
        tflite.run(input, output);

        int maxIndex = argMax(output[0]);
        // Kiểm tra bounds để tránh crash nếu model trả về index lạ
        if (maxIndex >= labelList.size()) return new NlpResult(text, NluIntent.UNKNOWN, new HashMap<>(), 0.0f);

        float confidence = output[0][maxIndex];
        String labelStr = labelList.get(maxIndex);
        NluIntent intent = mapLabelToIntent(labelStr);

        Map<String, String> entities = new HashMap<>();

        return new NlpResult(text, intent, entities, confidence);
    }

    // --- Các hàm phụ trợ cũ giữ nguyên ---

    private void loadTokenizer() {
        try {
            String json = loadJsonFromAsset(TOKENIZER_FILE);
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> root = gson.fromJson(json, mapType);

            if (root.containsKey("config")) {
                Map<String, Object> config = (Map<String, Object>) root.get("config");
                wordIndex = (Map<String, Double>) config.get("word_index");
            } else if (root.containsKey("word_index")) {
                wordIndex = (Map<String, Double>) root.get("word_index");
            } else {
                wordIndex = new HashMap<>();
                for (Map.Entry<String, Object> entry : root.entrySet()) {
                    try {
                        wordIndex.put(entry.getKey(), Double.parseDouble(entry.getValue().toString()));
                    } catch (NumberFormatException e) { }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Lỗi load Tokenizer: " + e.getMessage());
            wordIndex = new HashMap<>();
        }
    }

    private void loadLabels() {
        try {
            String json = loadJsonFromAsset(LABEL_FILE);
            Gson gson = new Gson();
            Type listType = new TypeToken<ArrayList<String>>(){}.getType();
            labelList = gson.fromJson(json, listType);
        } catch (Exception e) {
            Log.e(TAG, "Lỗi load Label Encoder: " + e.getMessage());
            labelList = new ArrayList<>();
        }
    }

    private float[][] tokenizeAndPad(String text) {
        String cleanText = text.toLowerCase().trim();
        String[] words = cleanText.split("\\s+");

        List<Float> sequence = new ArrayList<>();

        for (String w : words) {
            if (wordIndex != null && wordIndex.containsKey(w)) {
                sequence.add(wordIndex.get(w).floatValue());
            } else {
                sequence.add(1.0f);
            }
        }

        float[] padArray = new float[MAX_SEQUENCE_LENGTH];
        for (int i = 0; i < MAX_SEQUENCE_LENGTH; i++) {
            if (i < sequence.size()) {
                padArray[i] = sequence.get(i);
            } else {
                padArray[i] = 0.0f;
            }
        }
        return new float[][]{padArray};
    }

    private int argMax(float[] array) {
        int maxIdx = -1;
        float maxVal = -Float.MAX_VALUE;
        for (int i = 0; i < array.length; i++) {
            if (array[i] > maxVal) {
                maxVal = array[i];
                maxIdx = i;
            }
        }
        return maxIdx;
    }

    private String loadJsonFromAsset(String filename) throws IOException {
        InputStream is = context.getAssets().open(filename);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }

    private NluIntent mapLabelToIntent(String label) {
        try {
            return NluIntent.valueOf(label.toUpperCase());
        } catch (IllegalArgumentException e) {
            return NluIntent.UNKNOWN;
        }
    }
}