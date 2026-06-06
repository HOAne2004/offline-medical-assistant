package com.example.trolyyte.data.repository;

import com.example.trolyyte.data.local.dao.ReminderDao;
import com.example.trolyyte.data.utils.ReminderAlarmScheduler;
import com.example.trolyyte.domain.model.ReminderHistory;
import com.example.trolyyte.domain.model.ReminderHistoryWithTemplate;
import com.example.trolyyte.domain.model.ReminderTemplate;
import com.example.trolyyte.domain.repository.ReminderRepository;

import java.util.List;

public class ReminderRepositoryImpl implements ReminderRepository {
    private final ReminderDao reminderDao;
    private final ReminderAlarmScheduler scheduler;

    public ReminderRepositoryImpl(ReminderDao reminderDao, ReminderAlarmScheduler scheduler) {
        this.reminderDao = reminderDao;
        this.scheduler = scheduler;
    }

    // ==========================================
    // THAO TÁC VỚI TEMPLATE
    // ==========================================
    @Override
    public void insertTemplate(ReminderTemplate template) {
        reminderDao.insertTemplate(template);
    }

    @Override
    public void updateTemplate(ReminderTemplate template) {
        reminderDao.updateTemplate(template);
    }

    @Override
    public ReminderTemplate getTemplateById(String id) {
        return reminderDao.getTemplateById(id);
    }

    @Override
    public void deleteTemplate(ReminderTemplate template) {
        reminderDao.deleteTemplate(template);
        // Tạm ẩn: scheduler.cancel(template.getId());
    }

    // ==========================================
    // THAO TÁC VỚI HISTORY
    // ==========================================
    @Override
    public void insertHistory(ReminderHistory history) {
        reminderDao.insertHistory(history);

        // --- ĐOẠN NÀY LÀ CẦU NỐI ĐỂ KÍCH HOẠT BÁO THỨC ---
        ReminderTemplate template = reminderDao.getTemplateById(history.getTemplateId());
        if (template != null && history.getStatus() == ReminderHistory.Status.SCHEDULED) {
            scheduler.schedule(history, template);
        }
    }

    @Override
    public void updateHistory(ReminderHistory history) {
        reminderDao.updateHistory(history);

        ReminderTemplate template = reminderDao.getTemplateById(history.getTemplateId());
        if (template != null) {
            // NẾU TRẠNG THÁI LÀ CHỜ (SCHEDULED / SNOOZED) -> CÀI LẠI BÁO THỨC MỚI
            if (history.getStatus() == ReminderHistory.Status.SCHEDULED || history.getStatus() == ReminderHistory.Status.SNOOZED) {
                scheduler.schedule(history, template);
            } else {
                // NẾU ĐÃ QUÁ GIỜ, ĐÃ UỐNG, ĐÃ HỦY -> XÓA BÁO THỨC KHỎI HỆ THỐNG ĐỂ TRÁNH KÊU OAN
                scheduler.cancel(history.getId());
            }
        }
    }

    @Override
    public ReminderHistory getHistoryById(String id) {
        return reminderDao.getHistoryById(id);
    }

    // ==========================================
    // TRUY VẤN DỮ LIỆU GỘP CHO GIAO DIỆN
    // ==========================================
    @Override
    public List<ReminderHistoryWithTemplate> getAllHistoryWithTemplates() {
        return reminderDao.getAllHistoryWithTemplates();
    }

    @Override
    public List<ReminderHistoryWithTemplate> getHistoriesForTimeRange(long startTime, long endTime) {
        return reminderDao.getHistoriesForTimeRange(startTime, endTime);
    }
}