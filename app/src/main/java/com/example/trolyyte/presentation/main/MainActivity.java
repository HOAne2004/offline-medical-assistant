package com.example.trolyyte.presentation.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.trolyyte.NLUHelper;
import com.example.trolyyte.R;

public class MainActivity extends AppCompatActivity {

    private NLUHelper nluHelper;
    private EditText edtInput;
    private TextView txtResult;
    private Button btnPredict;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Đảm bảo bạn đã tạo layout

        // 1. Khởi tạo NLU Helper
        nluHelper = new NLUHelper(this);

        // 2. Ánh xạ View (Bạn cần tạo các view này trong activity_main.xml)
        edtInput = findViewById(R.id.edtInput);
        txtResult = findViewById(R.id.txtResult);
        btnPredict = findViewById(R.id.btnPredict);

        // 3. Sự kiện bấm nút
        btnPredict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = edtInput.getText().toString();
                if (!text.isEmpty()) {
                    // GỌI HÀM DỰ ĐOÁN
                    String result = nluHelper.predict(text);
                    txtResult.setText("Intent: " + result);
                }
            }
        });
    }
}