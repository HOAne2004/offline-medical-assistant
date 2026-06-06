package com.example.trolyyte.presentation.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.trolyyte.MedicalAssistantApplication;
import com.example.trolyyte.data.local.AppDatabase;
import com.example.trolyyte.databinding.FragmentHistoryBinding;
import com.example.trolyyte.di.AppContainer;
import com.example.trolyyte.domain.model.Appointment;
import com.example.trolyyte.domain.model.ReminderHistoryWithTemplate;
import com.example.trolyyte.presentation.schedule.ScheduleAdapter;
import com.example.trolyyte.presentation.schedule.ScheduleItem;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryFragment extends Fragment implements ScheduleAdapter.ScheduleCallback {

    private FragmentHistoryBinding binding;
    private ScheduleAdapter adapter;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHistoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupTabLayout();
        setupNavigation();
        loadHistoryData();
    }

    private void setupRecyclerView() {
        // Tái sử dụng ScheduleAdapter. Vì đây là xem lịch sử, các nút tương tác đã tự động ẩn/khóa dựa trên trạng thái trong Adapter
        adapter = new ScheduleAdapter(this);
        adapter.setHistoryMode(true);
        binding.rvHistoryList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvHistoryList.setAdapter(adapter);
    }

    private void setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    binding.rvHistoryList.setVisibility(View.VISIBLE);
                    binding.layoutStatistics.setVisibility(View.GONE);
                } else {
                    binding.rvHistoryList.setVisibility(View.GONE);
                    binding.layoutStatistics.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupNavigation() {
        // Xử lý sự kiện click nút Back để quay về ProfileFragment cha
        binding.btnBack.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void loadHistoryData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        AppContainer container = ((MedicalAssistantApplication) requireActivity().getApplication()).appContainer;

        executorService.execute(() -> {
            try {
                int retentionDays = container.settingsRepository.getHistoryRetentionDays();
                long cutOffTime = System.currentTimeMillis() - ((long) retentionDays * 24 * 60 * 60 * 1000);
                AppDatabase db = AppDatabase.getDatabase(getContext());
                db.query("DELETE FROM reminder_histories WHERE scheduledTimeMillis < " + cutOffTime, null);

                // 1. Lấy dữ liệu lịch sử từ Database
                List<ReminderHistoryWithTemplate> reminders = container.getRemindersUseCase.execute();
                List<Appointment> appointments = container.getAppointmentUseCase.execute();

                // 2. Đóng gói gộp đổ ra List chi tiết cho Tab 1
                List<ScheduleItem> combinedList = new ArrayList<>();
                int completedReminders = 0;
                int missedReminders = 0;

                for (ReminderHistoryWithTemplate r : reminders) {
                    combinedList.add(new ScheduleItem(r));
                    if (r.history.isCompleted()) completedReminders++;
                    if (r.history.isMissed()) missedReminders++;
                }

                int completedAppts = 0;
                int missedAppts = 0; // Bổ sung biến đếm lịch khám bị lỡ
                for (Appointment a : appointments) {
                    combinedList.add(new ScheduleItem(a));
                    if (a.getStatus() == Appointment.Status.COMPLETED) completedAppts++;
                    if (a.getStatus() == Appointment.Status.MISSED) missedAppts++;
                }

                Collections.sort(combinedList, (item1, item2) ->
                        Long.compare(item1.getTimeMillis(), item2.getTimeMillis()));

                // ==========================================================
                // 3. THUẬT TOÁN TÍNH CHỈ SỐ TUÂN THỦ (ĐÃ SỬA LỖI LOGIC)
                // ==========================================================

                // Tổng số lịch ĐÃ ĐẾN HẠN đánh giá (Chỉ tính Đã làm + Đã lỡ)
                int totalEvaluableTasks = completedReminders + missedReminders + completedAppts + missedAppts;
                // Tổng số lịch ĐÃ HOÀN THÀNH
                int totalCompleted = completedReminders + completedAppts;

                // Nếu chưa có lịch nào đến hạn đánh giá -> Mặc định hiển thị 100%
                int adherenceRate = totalEvaluableTasks > 0 ? (totalCompleted * 100) / totalEvaluableTasks : 100;

                // 4. Đẩy ngược dữ liệu về Main Thread cập nhật giao diện
                final int finalCompletedReminders = completedReminders;
                final int finalMissedReminders = missedReminders;
                final int finalCompletedAppts = completedAppts;

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);

                        // Đổ dữ liệu vào Tab 1
                        adapter.submitList(combinedList);

                        // Đổ dữ liệu vào Tab 2
                        binding.tvAdherenceRate.setText(adherenceRate + "%");
                        binding.progressAdherence.setProgress(adherenceRate);

                        binding.tvTotalReminders.setText("Tổng số cữ nhắc: " + reminders.size());
                        binding.tvCompletedReminders.setText("🟢 Đã thực hiện: " + finalCompletedReminders);
                        binding.tvMissedReminders.setText("🔴 Bỏ lỡ: " + finalMissedReminders);

                        binding.tvTotalAppointments.setText("Tổng lịch khám: " + appointments.size());
                        binding.tvCompletedAppointments.setText("🟢 Đã đi khám: " + finalCompletedAppts);

                        // Lời khuyên y tế cá nhân hóa dựa trên chỉ số
                        if (totalEvaluableTasks == 0) {
                            // Thêm trường hợp: Chưa có dữ liệu thực tế để đánh giá
                            binding.tvEvaluationMessage.setText("Bác chưa có lịch trình nào đến hạn. Hãy chú ý nhắc nhở để thực hiện đúng giờ nhé!");
                            binding.tvEvaluationMessage.setTextColor(android.graphics.Color.parseColor("#5F6368"));
                        } else if (adherenceRate >= 90) {
                            binding.tvEvaluationMessage.setText("🌟 Bác tuân thủ y lệnh cực kỳ tuyệt vời! Hãy tiếp tục duy trì bác nhé.");
                            binding.tvEvaluationMessage.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
                        } else if (adherenceRate >= 70) {
                            binding.tvEvaluationMessage.setText("🟡 Bác thực hiện lịch tương đối tốt, nhưng cố gắng đừng quên cữ thuốc nào nhé bác.");
                            binding.tvEvaluationMessage.setTextColor(android.graphics.Color.parseColor("#F57F17"));
                        } else {
                            binding.tvEvaluationMessage.setText("🔴 Chỉ số tuân thủ đang ở mức thấp. Bác hãy chú ý chuông nhắc nhở để uống thuốc đúng giờ.");
                            binding.tvEvaluationMessage.setTextColor(android.graphics.Color.parseColor("#C62828"));
                        }
                    });
                }

            } catch (Exception e) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.GONE);
                        android.widget.Toast.makeText(getContext(), "Lỗi tải lịch sử: " + e.getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    // Vì xem lịch sử không cho phép tương tác chỉnh sửa nên các Callbacks này để trống an toàn
    @Override public void onEditReminder(ReminderHistoryWithTemplate data) {}
    @Override public void onCompleteReminder(ReminderHistoryWithTemplate data) {}
    @Override public void onDeleteReminder(ReminderHistoryWithTemplate data) {}
    @Override public void onEditAppointment(Appointment appointment) {}
    @Override public void onCompleteAppointment(Appointment appointment) {}
    @Override public void onDeleteAppointment(Appointment appointment) {}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        executorService.shutdown();
    }
}