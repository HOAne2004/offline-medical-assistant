package com.example.trolyyte.presentation.notification;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.trolyyte.databinding.ActivityNotificationBinding;
import com.example.trolyyte.domain.model.Medicine;
import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {
    private ActivityNotificationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Tạo dữ liệu mẫu
        List<Medicine> list = new ArrayList<>();
        list.add(new Medicine("08:00", "Paracetamol", 10));
        list.add(new Medicine("12:00", "Vitamin C", 15));
        list.add(new Medicine("18:00", "Thuốc bổ", 20));

        // Thiết lập danh sách cuộn
        binding.rvNotificationMedicines.setLayoutManager(new LinearLayoutManager(this));
        binding.rvNotificationMedicines.setAdapter(new NotificationMedicineAdapter(list));
    }
}