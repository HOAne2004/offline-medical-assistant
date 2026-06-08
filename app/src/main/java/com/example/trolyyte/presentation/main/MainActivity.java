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
import com.example.trolyyte.data.wakeword.WakeWordService;
import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.presentation.home.HomeActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Nếu app được mở do nói "Bác sĩ ơi"
        if (getIntent() != null && getIntent().getBooleanExtra("WAKE_WORD_DETECTED", false)) {
            //startWakeWordService();
            stopWakeWordService();
            navigateToHome();
            return;
        }

        //testNluEngine();

        requestNeededPermissions();
    }

    private void testNluEngine() {
        TfliteNlpEngine nluEngine = new TfliteNlpEngine(this);
        nluEngine.initialize();

        String testSentence = "nhắc tôi uống 2 viên paracetamol sau ăn lúc 8 giờ sáng";

        NlpResult result = nluEngine.analyze(testSentence);

        android.util.Log.d("NLU_TEST", "Câu hỏi: " + testSentence);
        android.util.Log.d("NLU_TEST", "Dự đoán Intent: " + result.getIntent().name());
        android.util.Log.d("NLU_TEST", "Độ tự tin: " + (result.getConfidence() * 100) + "%");
    }

    private void requestNeededPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS,
                            Manifest.permission.RECORD_AUDIO
                    },
                    REQUEST_PERMISSIONS
            );
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.RECORD_AUDIO
                    },
                    REQUEST_PERMISSIONS
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {

            startTimer();
        }
    }

    private void startWakeWordService() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Intent intent = new Intent(this, WakeWordService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent);
        } else {
            startService(intent);
        }
    }

    private void startTimer() {
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToHome, 1000);
    }

    private void navigateToHome() {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);

        if (getIntent() != null && getIntent().getBooleanExtra("WAKE_WORD_DETECTED", false)) {
            intent.putExtra("WAKE_WORD_DETECTED", true);
        }

        startActivity(intent);
        finish();
    }
    private void stopWakeWordService() {
        Intent intent = new Intent(this, WakeWordService.class);
        stopService(intent);
    }

}