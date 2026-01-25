package com.example.trolyyte.presentation.notification;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.trolyyte.R;
import com.example.trolyyte.databinding.ItemMedicineCardBinding;
import com.example.trolyyte.domain.model.Medicine;
import java.util.List;

public class NotificationMedicineAdapter extends RecyclerView.Adapter<NotificationMedicineAdapter.ViewHolder> {

    private final List<Medicine> medicineList;

    public NotificationMedicineAdapter(List<Medicine> medicineList) {
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng View Binding để kết nối với item_medicine_card.xml
        ItemMedicineCardBinding binding = ItemMedicineCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicineList.get(position);
        ItemMedicineCardBinding b = holder.binding;

        // 1. Đổ dữ liệu vào các ID khớp với XML
        b.tvMedicineInfo.setText(medicine.getTime() + " – " + medicine.getName());
        b.tvReminderStatus.setText("Nhắc trước " + medicine.getRemindMinutes() + " phút");

        // 2. Xử lý logic Mờ (Mute) khi nhấn chuông
        // b.getRoot() đại diện cho MaterialCardView ngoài cùng
        b.getRoot().setAlpha(medicine.isMuted() ? 0.4f : 1.0f);

        // 3. Sự kiện bấm nút Chuông (cvBellContainer)
        b.cvBellContainer.setOnClickListener(v -> {
            medicine.setMuted(!medicine.isMuted());
            notifyItemChanged(position); // Cập nhật lại giao diện hàng này
        });

        // 4. Xử lý Highlight các nút thời gian (btn10m, btn15m, btn20m)
        updateTimerButtonsUI(b, medicine.getRemindMinutes());

        b.btn10m.setOnClickListener(v -> {
            medicine.setRemindMinutes(10);
            notifyItemChanged(position);
        });

        b.btn15m.setOnClickListener(v -> {
            medicine.setRemindMinutes(15);
            notifyItemChanged(position);
        });

        b.btn20m.setOnClickListener(v -> {
            medicine.setRemindMinutes(20);
            notifyItemChanged(position);
        });
    }

    private void updateTimerButtonsUI(ItemMedicineCardBinding b, int selectedMinutes) {
        // Cập nhật background dựa trên thời gian đang chọn
        // Lưu ý: Bạn nên sử dụng các drawable đã tạo (bg_option_selected / bg_option_unselected)

        b.btn10m.setBackgroundResource(selectedMinutes == 10 ?
                R.drawable.bg_option_selected : R.drawable.bg_option_unselected);
        b.btn10m.setTextColor(selectedMinutes == 10 ? 0xFFFFFFFF : 0xFF3366FF);

        b.btn15m.setBackgroundResource(selectedMinutes == 15 ?
                R.drawable.bg_option_selected : R.drawable.bg_option_unselected);
        b.btn15m.setTextColor(selectedMinutes == 15 ? 0xFFFFFFFF : 0xFF3366FF);

        b.btn20m.setBackgroundResource(selectedMinutes == 20 ?
                R.drawable.bg_option_selected : R.drawable.bg_option_unselected);
        b.btn20m.setTextColor(selectedMinutes == 20 ? 0xFFFFFFFF : 0xFF3366FF);
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemMedicineCardBinding binding;
        public ViewHolder(ItemMedicineCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}