package com.example.trolyyte.presentation.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.trolyyte.R;
import com.example.trolyyte.data.nlu.TfliteNlpEngine;
import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.presentation.home.HomeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Khởi tạo TFLite Engine
        TfliteNlpEngine nluEngine = new TfliteNlpEngine(this);
        nluEngine.initialize(); // QUAN TRỌNG: Phải gọi initialize() để load file model lên RAM

        // Thử cho AI phân tích 1 câu
        String testSentence = "nhắc tôi uống 2 viên paracetamol sau ăn lúc 8 giờ sáng";

        // Gọi hàm analyze thay vì predictIntent
        NlpResult result = nluEngine.analyze(testSentence);

        // In kết quả ra Logcat
        android.util.Log.d("NLU_TEST", "Câu hỏi: " + testSentence);
        android.util.Log.d("NLU_TEST", "Dự đoán Intent: " + result.getIntent().name());
        android.util.Log.d("NLU_TEST", "Độ tự tin: " + (result.getConfidence() * 100) + "%");

        // Kiểm tra quyền trên Android 13+ (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Đứng yên ở màn hình này và xin quyền
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            } else {
                // Đã có quyền từ trước -> Đếm ngược vào app
                startTimer();
            }
        } else {
            // Android cũ không cần xin quyền này
            startTimer();
        }
    }

    // Hàm này tự động chạy sau khi người dùng bấm "Cho phép" hoặc "Từ chối"
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            // Dù họ cho phép hay từ chối, ta vẫn đếm ngược 1 giây rồi vào màn hình chính
            startTimer();
        }
    }

    private void startTimer() {
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToHome, 1000);
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}