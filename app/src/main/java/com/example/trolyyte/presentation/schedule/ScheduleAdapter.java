package com.example.trolyyte.presentation.schedule;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.trolyyte.R;
import com.example.trolyyte.domain.model.Appointment;
import com.example.trolyyte.domain.model.ReminderHistory;
import com.example.trolyyte.domain.model.ReminderHistoryWithTemplate;
import com.example.trolyyte.domain.model.ReminderTemplate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ScheduleItem> items = new ArrayList<>();
    private final ScheduleCallback callback;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    // Cờ xác định Adapter đang được dùng ở màn hình Lịch trình hay Lịch sử
    private boolean isHistoryMode = false;

    public interface ScheduleCallback {
        void onEditReminder(ReminderHistoryWithTemplate data);
        void onCompleteReminder(ReminderHistoryWithTemplate data);
        void onDeleteReminder(ReminderHistoryWithTemplate data);

        void onEditAppointment(Appointment appointment);
        void onCompleteAppointment(Appointment appointment);
        void onDeleteAppointment(Appointment appointment);
    }

    public ScheduleAdapter(ScheduleCallback callback) {
        this.callback = callback;
    }

    public void setHistoryMode(boolean isHistory) {
        this.isHistoryMode = isHistory;
    }

    public void submitList(List<ScheduleItem> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ScheduleItem.TYPE_REMINDER) {
            View view = inflater.inflate(R.layout.item_reminder_card, parent, false);
            return new ReminderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_appointment_card, parent, false);
            return new AppointmentViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ScheduleItem item = items.get(position);
        if (holder instanceof ReminderViewHolder) {
            ((ReminderViewHolder) holder).bind(item.getReminderData());
        } else if (holder instanceof AppointmentViewHolder) {
            ((AppointmentViewHolder) holder).bind(item.getAppointment());
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // --------------------------------------------------------
    // --- VIEWHOLDER CHO NHẮC THUỐC ---
    // --------------------------------------------------------
    class ReminderViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardReminder;
        TextView tvMedicineName, tvTime, tvStatus, tvReminderBefore, tvDosage, tvInstruction;
        ImageView ivTypeIcon;
        com.google.android.material.button.MaterialButton btnCompleted, btnEdit, btnDelete;

        ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            cardReminder = itemView.findViewById(R.id.cardReminder);
            tvMedicineName = itemView.findViewById(R.id.tvMedicineName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvReminderBefore = itemView.findViewById(R.id.tvReminderBefore);

            tvDosage = itemView.findViewById(R.id.tvDosage);
            tvInstruction = itemView.findViewById(R.id.tvInstruction);

            ivTypeIcon = itemView.findViewById(R.id.ivTypeIcon);
            btnCompleted = itemView.findViewById(R.id.btnCompleted);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(ReminderHistoryWithTemplate data) {
            ReminderTemplate template = data.template;
            ReminderHistory history = data.history;

            String timeStr = timeFormat.format(new Date(history.getScheduledTimeMillis()));
            tvTime.setText(timeStr);
            tvMedicineName.setText(template.getTitle());

            // TÁCH RIÊNG DỮ LIỆU HIỂN THỊ
            tvReminderBefore.setText("🔔 Trước " + template.getRemindMinutes() + "p");

            // Hiển thị Liều lượng (Chỉ hiện nếu là loại THUỐC và có nhập liều lượng)
            if (template.getType() == ReminderTemplate.Type.MEDICINE
                    && template.getDosage() != null
                    && !template.getDosage().trim().isEmpty()) {
                tvDosage.setVisibility(View.VISIBLE);
                tvDosage.setText("💊 Liều lượng: " + template.getDosage());
            } else {
                tvDosage.setVisibility(View.GONE);
            }

            // Hiển thị Ghi chú (Chỉ hiện nếu có nhập ghi chú)
            if (template.getInstruction() != null && !template.getInstruction().trim().isEmpty()) {
                tvInstruction.setVisibility(View.VISIBLE);
                tvInstruction.setText("📝 Ghi chú: " + template.getInstruction());
            } else {
                tvInstruction.setVisibility(View.GONE);
            }

            if (template.getType() == ReminderTemplate.Type.MEDICINE) {
                ivTypeIcon.setImageResource(R.drawable.ic_capsule);
            } else {
                ivTypeIcon.setImageResource(R.drawable.ic_health);
            }

            // Tự động phân biệt hành động dựa trên Loại (Thuốc hay Sinh hoạt)
            String actionWord = (template.getType() == ReminderTemplate.Type.MEDICINE) ? "uống" : "thực hiện";

            // --- 1. XỬ LÝ MÀU SẮC ---
            if (history.isCompleted()) {
                cardReminder.setCardBackgroundColor(android.graphics.Color.parseColor("#E8F5E9"));
                tvStatus.setText("🟢 Đã hoàn thành");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
            } else if (history.isMissed()) {
                cardReminder.setCardBackgroundColor(android.graphics.Color.parseColor("#EEEEEE"));
                tvStatus.setText("⚫ Bỏ lỡ");
                tvStatus.setTextColor(android.graphics.Color.parseColor("#757575"));
            } else {
                long currentTime = System.currentTimeMillis();
                long timeDiff = history.getScheduledTimeMillis() - currentTime;

                if (timeDiff > 0 && timeDiff <= (template.getRemindMinutes() * 60 * 1000L)) {
                    cardReminder.setCardBackgroundColor(android.graphics.Color.parseColor("#FFF9C4"));
                    tvStatus.setText("🟡 Sắp đến giờ");
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#F57F17"));
                } else if (timeDiff < 0) {
                    cardReminder.setCardBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
                    // Dùng biến actionWord thay cho chữ "uống" cứng nhắc
                    tvStatus.setText("🔴 Quá giờ " + actionWord);
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#C62828"));
                } else {
                    cardReminder.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
                    tvStatus.setText("🔴 Chưa " + actionWord);
                    tvStatus.setTextColor(android.graphics.Color.parseColor("#D32F2F"));
                }
            }

            // --- 2. XỬ LÝ ẨN HIỆN NÚT BẤM (Dựa vào isHistoryMode) ---
            if (isHistoryMode) {
                btnEdit.setVisibility(View.GONE);
                btnDelete.setVisibility(View.GONE);
                btnCompleted.setVisibility(View.GONE);
            } else {
                btnEdit.setVisibility(View.VISIBLE);
                btnEdit.setEnabled(true);
                btnDelete.setVisibility(View.VISIBLE);

                if (history.isCompleted() || history.isMissed()) {
                    btnCompleted.setVisibility(View.GONE);
                } else {
                    btnCompleted.setVisibility(View.VISIBLE);
                }
            }

            // --- 3. Xử lý sự kiện click ---
            btnEdit.setOnClickListener(v -> callback.onEditReminder(data));
            btnDelete.setOnClickListener(v -> callback.onDeleteReminder(data));
            btnCompleted.setOnClickListener(v -> callback.onCompleteReminder(data));
        }
    }

    // --------------------------------------------------------
    // --- VIEWHOLDER CHO LỊCH KHÁM ---
    // --------------------------------------------------------
    class AppointmentViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.card.MaterialCardView cardAppointment;
        TextView tvApptTitle, tvApptTime, tvApptDate, tvApptLocation, tvApptStatus, tvApptNotes;
        ImageView ivApptIcon;
        com.google.android.material.button.MaterialButton btnApptCompleted, btnApptEdit, btnApptCancel;

        AppointmentViewHolder(@NonNull View itemView) {
            super(itemView);
            cardAppointment = itemView.findViewById(R.id.cardAppointment);
            tvApptTitle = itemView.findViewById(R.id.tvApptTitle);
            tvApptTime = itemView.findViewById(R.id.tvApptTime);
            tvApptDate = itemView.findViewById(R.id.tvApptDate);
            tvApptLocation = itemView.findViewById(R.id.tvApptLocation);
            tvApptStatus = itemView.findViewById(R.id.tvApptStatus);
            tvApptNotes = itemView.findViewById(R.id.tvApptNotes);
            ivApptIcon = itemView.findViewById(R.id.ivApptIcon);
            btnApptCompleted = itemView.findViewById(R.id.btnApptCompleted);
            btnApptEdit = itemView.findViewById(R.id.btnApptEdit);
            btnApptCancel = itemView.findViewById(R.id.btnApptCancel);
        }

        void bind(Appointment appointment) {
            tvApptTitle.setText(appointment.getTitle());
            tvApptTime.setText(timeFormat.format(new Date(appointment.getTimeMillis())));
            tvApptDate.setText("Ngày " + dateFormat.format(new Date(appointment.getTimeMillis())));
            tvApptLocation.setText("🏥 Đơn vị: " + appointment.getLocation());
            ivApptIcon.setImageResource(R.drawable.ic_medical_briefcase);

            if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
                tvApptNotes.setVisibility(View.VISIBLE);
                tvApptNotes.setText("Ghi chú: " + appointment.getNotes());
            } else {
                tvApptNotes.setVisibility(View.GONE);
            }

            // --- 1. MÀU SẮC ---
            long currentTime = System.currentTimeMillis();
            if (appointment.getStatus() == Appointment.Status.COMPLETED) {
                cardAppointment.setCardBackgroundColor(android.graphics.Color.parseColor("#E0F2F1"));
                tvApptStatus.setText("🟢 Đã khám xong");
                tvApptStatus.setTextColor(android.graphics.Color.parseColor("#004D40"));
            } else if (appointment.getStatus() == Appointment.Status.CANCELLED) {
                cardAppointment.setCardBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
                tvApptStatus.setText("✖ Đã hủy lịch trình");
                tvApptStatus.setTextColor(android.graphics.Color.parseColor("#757575"));
            } else {
                if (appointment.getTimeMillis() < currentTime) {
                    cardAppointment.setCardBackgroundColor(android.graphics.Color.parseColor("#FFEBEE"));
                    tvApptStatus.setText("🔴 Quá hạn lịch khám");
                    tvApptStatus.setTextColor(android.graphics.Color.parseColor("#C62828"));
                } else {
                    cardAppointment.setCardBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"));
                    tvApptStatus.setText("🟡 Sắp tới lịch khám");
                    tvApptStatus.setTextColor(android.graphics.Color.parseColor("#F57F17"));
                }
            }

            // --- 2. ẨN HIỆN NÚT BẤM DỰA VÀO CHẾ ĐỘ ---
            if (isHistoryMode) {
                btnApptEdit.setVisibility(View.GONE);
                btnApptCancel.setVisibility(View.GONE);
                btnApptCompleted.setVisibility(View.GONE);
            } else {
                btnApptEdit.setVisibility(View.VISIBLE);
                btnApptCancel.setVisibility(View.VISIBLE);

                if (appointment.getStatus() == Appointment.Status.COMPLETED || appointment.getStatus() == Appointment.Status.CANCELLED) {
                    btnApptCompleted.setVisibility(View.GONE);
                } else {
                    btnApptCompleted.setVisibility(View.VISIBLE);
                }
            }

            // --- 3. SỰ KIỆN CLICK ---
            btnApptEdit.setOnClickListener(v -> callback.onEditAppointment(appointment));
            btnApptCancel.setOnClickListener(v -> callback.onDeleteAppointment(appointment));
            btnApptCompleted.setOnClickListener(v -> callback.onCompleteAppointment(appointment));
        }
    }
}