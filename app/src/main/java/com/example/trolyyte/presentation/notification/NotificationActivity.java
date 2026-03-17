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

        // 1. Tạo dữ liệu mẫu (Gán phút mặc định là 15)
        List<Medicine> list = new ArrayList<>();
        list.add(new Medicine("08:00", "Paracetamol", 15));
        list.add(new Medicine("12:00", "Vitamin C", 15));
        list.add(new Medicine("19:00", "Thuốc bổ", 15));

        // 2. Thiết lập danh sách hiển thị
        binding.rvNotificationMedicines.setLayoutManager(new LinearLayoutManager(this));

        // Quan trọng: Vì RecyclerView nằm trong NestedScrollView nên cần tắt cuộn riêng
        binding.rvNotificationMedicines.setNestedScrollingEnabled(false);

        NotificationMedicineAdapter adapter = new NotificationMedicineAdapter(list);
        binding.rvNotificationMedicines.setAdapter(adapter);
    }
}