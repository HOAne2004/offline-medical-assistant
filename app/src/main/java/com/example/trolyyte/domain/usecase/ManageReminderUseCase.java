package com.example.trolyyte.domain.usecase;

import com.example.trolyyte.domain.model.ReminderHistory;
import com.example.trolyyte.domain.model.ReminderHistoryWithTemplate;
import com.example.trolyyte.domain.model.ReminderTemplate;
import com.example.trolyyte.domain.repository.ReminderRepository;

import java.util.Calendar;
import java.util.List;

public class ManageReminderUseCase {
    private final ReminderRepository repository;

    public ManageReminderUseCase(ReminderRepository repository) {
        this.repository = repository;
    }

    // =========================================================================
    // 1. TẠO MỚI HOẶC CẬP NHẬT KHUÔN MẪU (TEMPLATE)
    // =========================================================================
    public void addOrUpdateReminder(String templateId, String title, String dosage, String instruction,
                                    long firstTriggerTimeMillis, ReminderTemplate.Type type,
                                    ReminderTemplate.RepeatType repeatType, int remindMinutes) {

        ReminderTemplate template;

        if (templateId == null || templateId.isEmpty()) {
            // ==========================================
            // TRƯỜNG HỢP 1: TẠO MỚI (CREATE)
            // ==========================================
            // Chỉ đẩy sang ngày mai nếu là TẠO MỚI, có lặp lại và giờ nằm trong quá khứ
            if (firstTriggerTimeMillis < System.currentTimeMillis() && repeatType != ReminderTemplate.RepeatType.NONE) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(firstTriggerTimeMillis);
                cal.add(Calendar.DAY_OF_YEAR, 1);
                firstTriggerTimeMillis = cal.getTimeInMillis();
            }

            template = new ReminderTemplate(null, type, title);
            mapTemplateData(template, dosage, instruction, firstTriggerTimeMillis, repeatType, remindMinutes);
            repository.insertTemplate(template);

            ReminderHistory firstHistory = new ReminderHistory(template.getId(), firstTriggerTimeMillis);
            repository.insertHistory(firstHistory);
        } else {
            // ==========================================
            // TRƯỜNG HỢP 2: CẬP NHẬT (UPDATE)
            // ==========================================
            template = repository.getTemplateById(templateId);
            if (template != null) {
                mapTemplateData(template, dosage, instruction, firstTriggerTimeMillis, repeatType, remindMinutes);
                repository.updateTemplate(template);

                // Lấy Giờ và Phút mới mà người dùng vừa chọn trên UI
                Calendar newTimeCal = Calendar.getInstance();
                newTimeCal.setTimeInMillis(firstTriggerTimeMillis);

                // Cập nhật TẤT CẢ các History thuộc Template này
                List<ReminderHistoryWithTemplate> allHistories = repository.getAllHistoryWithTemplates();
                for (ReminderHistoryWithTemplate item : allHistories) {
                    if (item.template.getId().equals(templateId)) {

                        // Giữ nguyên NGÀY cũ của History, chỉ thay thế GIỜ và PHÚT mới
                        Calendar oldCal = Calendar.getInstance();
                        oldCal.setTimeInMillis(item.history.getScheduledTimeMillis());
                        oldCal.set(Calendar.HOUR_OF_DAY, newTimeCal.get(Calendar.HOUR_OF_DAY));
                        oldCal.set(Calendar.MINUTE, newTimeCal.get(Calendar.MINUTE));
                        oldCal.set(Calendar.SECOND, 0);

                        long updatedTimeMillis = oldCal.getTimeInMillis();
                        item.history.setScheduledTimeMillis(updatedTimeMillis);
                        item.history.setActualTriggerTimeMillis(updatedTimeMillis);

                        // QUAN TRỌNG NHẤT: Bẻ khóa mọi trạng thái!
                        // Nếu giờ mới sửa nằm ở TƯƠNG LAI -> Khôi phục trạng thái về SCHEDULED
                        // để hệ thống kích hoạt lại báo thức, bất chấp việc trước đó đã lỡ hay đã uống.
                        if (updatedTimeMillis > System.currentTimeMillis()) {
                            item.history.setStatus(ReminderHistory.Status.SCHEDULED);
                            item.history.setCompletedTimeMillis(null); // Xóa vết đã uống (nếu có)
                        }

                        repository.updateHistory(item.history);
                    }
                }
            }
        }
    }

    private void mapTemplateData(ReminderTemplate template, String dosage, String instruction,
                                 long timeMillis, ReminderTemplate.RepeatType repeatType, int remindMinutes) {
        template.setInstruction(instruction); // Ghi chú trước/sau ăn
        template.setBaseTriggerTimeOfDay(timeMillis);
        template.setRepeatType(repeatType);
        template.setRemindMinutes(remindMinutes);

        if (template.getType() == ReminderTemplate.Type.MEDICINE) {
            template.setDosage(dosage);
        }
    }

    // =========================================================================
    // 2. XỬ LÝ VÒNG ĐỜI CỦA 1 CỮ THUỐC CỤ THỂ (HISTORY)
    // =========================================================================

    // Nút "Đã uống" (Chuyển sang chờ hoàn tác)
    public void markAsCompleted(String historyId) {
        ReminderHistory history = repository.getHistoryById(historyId);

        if (history != null && (history.getStatus() == ReminderHistory.Status.SCHEDULED
                || history.getStatus() == ReminderHistory.Status.TRIGGERED
                || history.getStatus() == ReminderHistory.Status.SNOOZED)) {

            // Đổi trạng thái thành PENDING để cho phép Undo
            history.setStatus(ReminderHistory.Status.COMPLETED_PENDING);
            history.setCompletedTimeMillis(System.currentTimeMillis());
            repository.updateHistory(history);

            // BƯỚC ĐỘT PHÁ 2: Tự động đẻ ra cữ thuốc cho chu kỳ tiếp theo (Clone cho ngày mai)
            ReminderTemplate template = repository.getTemplateById(history.getTemplateId());
            if (template != null && template.getRepeatType() != ReminderTemplate.RepeatType.NONE) {
                generateNextHistory(template, history.getScheduledTimeMillis());
            }
        }
    }

    // Nút "Hoàn tác" (Undo) trong vòng 15 phút
    public void undoCompletion(String historyId) {
        ReminderHistory history = repository.getHistoryById(historyId);
        if (history != null && history.canUndo()) {
            history.setStatus(ReminderHistory.Status.SCHEDULED); // Trả về trạng thái chờ
            history.setCompletedTimeMillis(null); // Xóa vết thời gian đã bấm
            repository.updateHistory(history);
        }
    }

    // Nút "Nhắc lại sau 10 phút" (Snooze)
    public void snoozeReminder(String historyId) {
        ReminderHistory history = repository.getHistoryById(historyId);
        if (history != null && history.canSnooze()) {
            history.setStatus(ReminderHistory.Status.SNOOZED);
            // Cộng thêm 10 phút (600,000 milliseconds) vào giờ chuông kêu thực tế
            history.setActualTriggerTimeMillis(history.getActualTriggerTimeMillis() + (10 * 60 * 1000));
            repository.updateHistory(history);
        }
    }

    // Nút "Bỏ qua cữ thuốc này" (Cancel 1 lần)
    public void cancelReminderHistory(String historyId) {
        ReminderHistory history = repository.getHistoryById(historyId);
        if (history != null) {
            history.setStatus(ReminderHistory.Status.CANCELLED);
            repository.updateHistory(history);
        }
    }

    // =========================================================================
    // 3. XÓA MỀM
    // =========================================================================

    public void deleteReminderTemplate(String templateId) {
        ReminderTemplate template = repository.getTemplateById(templateId);
        if (template != null) {
            template.setActive(false);
            repository.updateTemplate(template);

            List<ReminderHistoryWithTemplate> allHistories = repository.getAllHistoryWithTemplates();
            for (ReminderHistoryWithTemplate item : allHistories) {
                if (item.template.getId().equals(templateId) &&
                        item.history.getStatus() == ReminderHistory.Status.SCHEDULED) {
                    item.history.setStatus(ReminderHistory.Status.CANCELLED);
                    repository.updateHistory(item.history);
                }
            }
        }
    }

    // =========================================================================
    // HELPER: THUẬT TOÁN TÍNH TOÁN CHU KỲ LẶP (CLONE)
    // =========================================================================
    private void generateNextHistory(ReminderTemplate template, long currentScheduledTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(currentScheduledTime);

        switch (template.getRepeatType()) {
            case DAILY:
                cal.add(Calendar.DAY_OF_YEAR, 1);
                break;
            case WEEKLY:
                cal.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case MONTHLY:
                cal.add(Calendar.MONTH, 1);
                break;
            default:
                return; // KHÔNG LẶP thì dừng lại
        }

        long nextScheduledTime = cal.getTimeInMillis();
        ReminderHistory nextHistory = new ReminderHistory(template.getId(), nextScheduledTime);
        repository.insertHistory(nextHistory);
    }
}