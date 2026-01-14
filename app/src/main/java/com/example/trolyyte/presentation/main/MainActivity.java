package com.example.trolyyte.presentation.main;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.trolyyte.R;
import com.example.trolyyte.presentation.home.HomeActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Giả lập thời gian tải hệ thống (2 giây)
        // Trong thực tế, việc khởi tạo AI Engine đã được làm ở Application class
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateToHome();
        }, 2000);
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        startActivity(intent);
        // Đóng MainActivity để người dùng không back lại được màn hình chào
        finish();
    }
}