package com.example.trolyyte.presentation.reminder;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.trolyyte.R;
import com.example.trolyyte.databinding.ItemReminderCardBinding;
import com.example.trolyyte.domain.model.Reminder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderAdapter extends ListAdapter<Reminder, ReminderAdapter.ViewHolder> {

    public interface ReminderCallback {
        void onEditClick(Reminder reminder);
        void onDeleteClick(Reminder reminder);
    }

    private final ReminderCallback callback;

    public ReminderAdapter(ReminderCallback callback) {
        super(new DiffUtil.ItemCallback<Reminder>() {
            @Override
            public boolean areItemsTheSame(@NonNull Reminder oldItem, @NonNull Reminder newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Reminder oldItem, @NonNull Reminder newItem) {
                return oldItem.equals(newItem);
            }
        });
        this.callback = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReminderCardBinding binding = ItemReminderCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(getItem(position), callback);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemReminderCardBinding binding;
        private boolean isExpanded = false;

        public ViewHolder(ItemReminderCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Reminder reminder, ReminderCallback callback) {
            // Hiển thị thời gian định dạng HH:mm từ triggerAtMillis
            String formattedTime = formatTime(reminder.getTriggerAtMillis());
            binding.tvMedicineInfo.setText(formattedTime + " – " + reminder.getTitle());
            binding.tvReminderStatus.setText(reminder.isActive() ? "Đang đặt" : "Đã tắt");

            int themeColor;
            if (reminder.getType() == Reminder.Type.MEDICINE) {
                themeColor = Color.parseColor("#1A73E8");
                binding.ivTypeIcon.setImageResource(R.drawable.ic_medicine);
            } else {
                themeColor = Color.parseColor("#F29900");
                binding.ivTypeIcon.setImageResource(R.drawable.ic_calendar);
            }

            binding.ivTypeIcon.setColorFilter(themeColor);
            binding.tvMedicineInfo.setTextColor(themeColor);
            binding.cardReminder.setStrokeColor(themeColor);

            binding.ivExpand.setOnClickListener(v -> {
                isExpanded = !isExpanded;
                binding.layoutExtraTime.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
                binding.ivExpand.animate().rotation(isExpanded ? 180 : 0).setDuration(300).start();
            });

            binding.ivEdit.setOnClickListener(v -> callback.onEditClick(reminder));
            binding.ivDelete.setOnClickListener(v -> callback.onDeleteClick(reminder));

            setupTimerButtons(themeColor);
        }

        private String formatTime(long millis) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            return sdf.format(new Date(millis));
        }

        private void setupTimerButtons(int themeColor) {
            View.OnClickListener timerListener = v -> {
                resetTimerButtons(themeColor);
                v.setBackgroundResource(R.drawable.bg_option_selected);
                if (v instanceof android.widget.TextView) {
                    ((android.widget.TextView) v).setTextColor(Color.WHITE);
                }
            };

            binding.btn5m.setOnClickListener(timerListener);
            binding.btn10m.setOnClickListener(timerListener);
            binding.btn15m.setOnClickListener(timerListener);
            binding.btn30m.setOnClickListener(timerListener);
            binding.btn1h.setOnClickListener(timerListener);
            binding.btn2h.setOnClickListener(timerListener);
        }

        private void resetTimerButtons(int themeColor) {
            View[] buttons = {binding.btn5m, binding.btn10m, binding.btn15m, binding.btn30m, binding.btn1h, binding.btn2h};
            for (View b : buttons) {
                b.setBackgroundResource(R.drawable.bg_option_unselected);
                if (b instanceof android.widget.TextView) {
                    ((android.widget.TextView) b).setTextColor(themeColor);
                }
            }
        }
    }
}
