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
        ItemMedicineCardBinding binding = ItemMedicineCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine medicine = medicineList.get(holder.getBindingAdapterPosition());
        ItemMedicineCardBinding b = holder.binding;

        // 1. Đổ dữ liệu vào các ID khớp hoàn toàn với item_medicine_card.xml
        b.tvMedicineInfo.setText(medicine.getTime() + " – " + medicine.getName());
        b.tvReminderStatus.setText("Nhắc trước " + medicine.getRemindMinutes() + " phút");

        // 2. Xử lý logic Mờ khi nhấn chuông
        b.getRoot().setAlpha(medicine.isMuted() ? 0.4f : 1.0f);

        // 3. Sự kiện bấm nút Chuông (cvBellContainer)
        b.cvBellContainer.setOnClickListener(v -> {
            int currentPos = holder.getBindingAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                medicine.setMuted(!medicine.isMuted());
                notifyItemChanged(currentPos);
            }
        });

        // 4. Xử lý Highlight các nút thời gian (btn10m, btn15m, btn20m)
        updateTimerButtonsUI(b, medicine.getRemindMinutes());

        b.btn10m.setOnClickListener(v -> updateMinutes(holder, medicine, 10));
        b.btn15m.setOnClickListener(v -> updateMinutes(holder, medicine, 15));
        b.btn20m.setOnClickListener(v -> updateMinutes(holder, medicine, 20));
    }

    private void updateMinutes(ViewHolder holder, Medicine medicine, int minutes) {
        int currentPos = holder.getBindingAdapterPosition();
        if (currentPos != RecyclerView.NO_POSITION) {
            medicine.setRemindMinutes(minutes);
            notifyItemChanged(currentPos);
        }
    }

    private void updateTimerButtonsUI(ItemMedicineCardBinding b, int selectedMinutes) {
        // Cập nhật background và màu chữ dựa trên phút được chọn
        b.btn10m.setBackgroundResource(selectedMinutes == 10 ? R.drawable.bg_option_selected : R.drawable.bg_option_unselected);
        b.btn10m.setTextColor(selectedMinutes == 10 ? 0xFFFFFFFF : 0xFF3366FF);

        b.btn15m.setBackgroundResource(selectedMinutes == 15 ? R.drawable.bg_option_selected : R.drawable.bg_option_unselected);
        b.btn15m.setTextColor(selectedMinutes == 15 ? 0xFFFFFFFF : 0xFF3366FF);

        b.btn20m.setBackgroundResource(selectedMinutes == 20 ? R.drawable.bg_option_selected : R.drawable.bg_option_unselected);
        b.btn20m.setTextColor(selectedMinutes == 20 ? 0xFFFFFFFF : 0xFF3366FF);
    }

    @Override
    public int getItemCount() {
        return medicineList != null ? medicineList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemMedicineCardBinding binding;
        public ViewHolder(ItemMedicineCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}