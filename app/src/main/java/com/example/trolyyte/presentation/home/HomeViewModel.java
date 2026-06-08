package com.example.trolyyte.presentation.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trolyyte.di.AppContainer;
import com.example.trolyyte.domain.dialog.DialogContext;
import com.example.trolyyte.domain.dialog.DialogueAction;
import com.example.trolyyte.domain.dialog.DialogueResult;
import com.example.trolyyte.domain.model.Appointment;
import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.domain.model.ReminderTemplate;
import com.example.trolyyte.domain.model.SpeakResult;
import com.example.trolyyte.domain.model.actions.AppointmentAction;
import com.example.trolyyte.domain.model.actions.EmergencyAction;
import com.example.trolyyte.domain.model.actions.IntentAction;
import com.example.trolyyte.domain.model.actions.MedicationAction;
import com.example.trolyyte.domain.repository.TtsRepository;
import com.example.trolyyte.domain.usecase.HandleDialogueUseCase;
import com.example.trolyyte.domain.usecase.ListenVoiceResult;
import com.example.trolyyte.domain.usecase.ListenVoiceUseCase;
import com.example.trolyyte.domain.usecase.ProcessTextUseCase;
import com.example.trolyyte.domain.usecase.SpeakResponseUseCase;
import com.example.trolyyte.presentation.common.ResponseTextProvider;

import java.util.Map;

public class HomeViewModel extends ViewModel {

    // --- Dependencies ---
    private final ListenVoiceUseCase listenVoiceUseCase;
    private final ProcessTextUseCase processTextUseCase;
    private final HandleDialogueUseCase handleDialogueUseCase;
    private final SpeakResponseUseCase speakResponseUseCase;
    private final TtsRepository ttsRepository;
    private final ResponseTextProvider responseTextProvider;

    // --- State ---
    // Context hội thoại hiện tại (Bộ nhớ ngắn hạn)
    private DialogContext dialogContext = new DialogContext();
    private boolean isWaitingEmergencyConfirm = false;

    // UI State (Dùng LiveData để UI observe)
    private final MutableLiveData<HomeUiState> uiState = new MutableLiveData<>(new HomeUiState.Idle());
    public LiveData<HomeUiState> getUiState() { return uiState; }

    private final MutableLiveData<IntentAction> actionLiveData = new MutableLiveData<>();
    public LiveData<IntentAction> getActionLiveData() { return actionLiveData; }

    private final java.util.concurrent.ExecutorService executorService;
    private final com.example.trolyyte.di.AppContainer container;

    private NlpResult lastNlpResult;

        public HomeViewModel(
                AppContainer appContainer,
                ListenVoiceUseCase listenVoiceUseCase,
                ProcessTextUseCase processTextUseCase,
                HandleDialogueUseCase handleDialogueUseCase,
                SpeakResponseUseCase speakResponseUseCase,
                TtsRepository ttsRepository,
                ResponseTextProvider responseTextProvider
        ) {
            this.container = appContainer;
            this.executorService = java.util.concurrent.Executors.newSingleThreadExecutor();
            this.listenVoiceUseCase = listenVoiceUseCase;
            this.processTextUseCase = processTextUseCase;
            this.handleDialogueUseCase = handleDialogueUseCase;
            this.speakResponseUseCase = speakResponseUseCase; // UseCase này sẽ dùng ở bước sau
            this.ttsRepository = ttsRepository;
            this.responseTextProvider = responseTextProvider;
        }

    // =================================================================================
    // 1. LISTEN VOICE (Nghe & Nhận dạng)
    // =================================================================================
    public void startListening() {
        listenVoiceUseCase.start(new ListenVoiceUseCase.Listener() {
            @Override
            public void onResult(ListenVoiceResult result) {
                switch (result.getStatus()) {
                    case LISTENING:
                        uiState.postValue(new HomeUiState.Listening());
                        break;

                    case PARTIAL:
                        // Hiển thị text mờ thời gian thực
                        uiState.postValue(new HomeUiState.PartialResult(result.getText()));
                        break;

                    case FINAL:
                        // Có text -> Dừng mic -> Chuyển sang xử lý
                        String asrText = result.getText();
                        uiState.postValue(new HomeUiState.Processing(asrText));
                        listenVoiceUseCase.stop();

                        // Gọi bước 2: Xử lý văn bản
                        processText(asrText);
                        break;

                    case ERROR:
                        uiState.postValue(new HomeUiState.Error(result.getError()));
                        break;
                }
            }
        });
    }

    public void stopListening() {
        listenVoiceUseCase.stop();
        uiState.postValue(new HomeUiState.Idle());
    }

    // =================================================================================
    // 2. PROCESS TEXT (Hiểu ý định)
    // =================================================================================
    private void processText(String text) {
        Log.d("FLOW_TEST", "1. Đã nghe được giọng nói, chuẩn bị đưa vào NLU: " + text);
        processTextUseCase.execute(text, new ProcessTextUseCase.Callback() {
            @Override
            public void onSuccess(NlpResult nlpResult) {
                Log.d("FLOW_TEST", "2. NLU phân tích XONG, trả về Intent: " + nlpResult.getIntent());
                lastNlpResult = nlpResult;
                // Đã hiểu ý định -> Gọi bước 3: Xử lý hội thoại
                handleDialogue(nlpResult);
            }

            @Override
            public void onFailure(String errorMessage) {
                uiState.postValue(new HomeUiState.Error("Không hiểu câu lệnh: " + errorMessage));
                speak("Xin lỗi, cháu không hiểu. Bác nói lại giúp cháu nhé.");
            }
        });
    }

    // =================================================================================
    // 3. HANDLE DIALOGUE (Quyết định hành động)
    // =================================================================================
    private void handleDialogue(NlpResult nlpResult) {
        String intentName = nlpResult.getIntent() != null ? nlpResult.getIntent().name().toUpperCase() : "";
        String userText = nlpResult.getText().toLowerCase();

        // 1. LUỒNG KHẨN CẤP (Xử lý như bài trước)
        if (dialogContext.isWaitingEmergencyConfirm()) {
            if (intentName.equals("AFFIRM") || userText.contains("có") || userText.contains("gọi")) {
                dialogContext.setWaitingEmergencyConfirm(false);
                processDialogueAction(new DialogueResult(DialogueAction.TRIGGER_EMERGENCY, dialogContext));
            } else if (intentName.equals("DENY") || userText.contains("không") || userText.contains("hủy")) {
                dialogContext.setWaitingEmergencyConfirm(false);
                speak("Dạ vâng, cháu đã hủy lệnh gọi.");
                uiState.postValue(new HomeUiState.Idle());
            } else {
                speakAndListen("Bác có muốn gọi cho người thân không ạ? Bác nói Có hoặc Không nhé.");
            }
            return;
        }

        if (intentName.equals("REQUEST_EMERGENCY")) {
            dialogContext.setWaitingEmergencyConfirm(true);
            processDialogueAction(new DialogueResult(DialogueAction.ASK_CONFIRM_EMERGENCY, dialogContext));
            return;
        }

        // 2. KHÓA NGỮ CẢNH (CHỐNG TRƯỢT LUỒNG)
        // Nếu AI đang hỏi dở (VD đang hỏi giờ), mà NLU lại nhận nhầm là ask_date_time, ta ÉP nó về đúng luồng
        String currentFlow = dialogContext.getCurrentIntent();
        if (currentFlow != null && !currentFlow.isEmpty()) {
            if (intentName.equals("ASK_DATE_TIME") || intentName.equals("SMALL_TALK") || intentName.equals("FALLBACK")) {
                intentName = currentFlow;
            }
        }

        // Nếu là lệnh mới bắt đầu, lưu luồng lại
        if (intentName.equals("SET_OR_UPDATE_MEDICATION") || intentName.equals("SET_OR_UPDATE_APPOINTMENT")) {
            dialogContext.setCurrentIntent(intentName);
            currentFlow = intentName;
        }

        // 3. TRÍCH XUẤT THÔNG TIN TỪ NLU (Slot Filling - CÓ HACK ĐỂ CHỐNG KẸT)
        java.util.Map<String, String> entities = nlpResult.getEntities();

        // ---- A. LẤY DỮ LIỆU TỪ NLU ----
        if (entities.containsKey("medicine_name")) dialogContext.setMedicineName(entities.get("medicine_name"));
        if (entities.containsKey("routine_name")) dialogContext.setRoutineName(entities.get("routine_name"));
        if (entities.containsKey("time")) dialogContext.setReminderTime(entities.get("time"));
        if (entities.containsKey("appointment_name")) dialogContext.setAppointmentReason(entities.get("appointment_name"));
        if (entities.containsKey("location")) dialogContext.setAppointmentLocation(entities.get("location"));

        // ---- B. HACK ÉP THÔNG TIN NẾU NLU BỊ "NGU" ----
        if ("SET_OR_UPDATE_APPOINTMENT".equals(currentFlow)) {
            // Đang chờ điền Bệnh viện, user nói nhưng NLU không bóc được -> Lấy luôn cả câu!
            if (dialogContext.getAppointmentLocation() == null && !userText.isEmpty()) {
                if (!intentName.equals("DENY") && !intentName.equals("CANCEL_APPOINTMENT")) {
                    dialogContext.setAppointmentLocation(userText);
                }
            }
        }

        if ("SET_OR_UPDATE_MEDICATION".equals(currentFlow)) {
            // Đang chờ tên thuốc, NLU không bóc được -> Lấy cả câu
            if (dialogContext.getMedicineName() == null && !userText.isEmpty()) {
                if (!intentName.equals("DENY") && !intentName.equals("CANCEL_MEDICATION")) {
                    dialogContext.setMedicineName(userText);
                }
            }
            // Mẹo làm sạch tên thuốc: Nếu dính "sau ăn", "2 viên" thì băm nó ra
            String med = dialogContext.getMedicineName();
            if (med != null) {
                // Xóa các chữ thừa để tên thuốc đẹp hơn
                med = med.replaceAll("(sau ăn.*|trước ăn.*|lúc.*|vào.*|\\d+ viên.*|\\d+ lần.*)", "").trim();
                dialogContext.setMedicineName(med);
            }
        }

        // 4. XỬ LÝ ĐIỀN FORM
        if ("SET_OR_UPDATE_MEDICATION".equals(currentFlow)) {
            boolean hasName = (dialogContext.getMedicineName() != null) || (dialogContext.getRoutineName() != null);
            if (!hasName) {
                processDialogueAction(new DialogueResult(DialogueAction.ASK_MEDICINE_NAME, dialogContext));
            } else if (dialogContext.getReminderTime() == null) {
                processDialogueAction(new DialogueResult(DialogueAction.ASK_TIME, dialogContext));
            } else {
                nlpResult.getEntities().putAll(dialogContext.getFilledEntities());
                this.lastNlpResult = nlpResult;
                processDialogueAction(new DialogueResult(DialogueAction.CONFIRM_MEDICINE_REMINDER_CREATED, dialogContext));
                dialogContext.reset();
            }
            return;
        }

        if ("SET_OR_UPDATE_APPOINTMENT".equals(currentFlow)) {
            if (dialogContext.getAppointmentReason() == null) dialogContext.setAppointmentReason("Tái khám");
            if (dialogContext.getAppointmentLocation() == null) {
                processDialogueAction(new DialogueResult(DialogueAction.ASK_LOCATION, dialogContext));
            } else if (dialogContext.getReminderTime() == null) {
                processDialogueAction(new DialogueResult(DialogueAction.ASK_TIME, dialogContext));
            } else {
                nlpResult.getEntities().putAll(dialogContext.getFilledEntities());
                this.lastNlpResult = nlpResult;
                processDialogueAction(new DialogueResult(DialogueAction.CONFIRM_APPOINTMENT_CREATED, dialogContext));
                dialogContext.reset();
            }
            return;
        }

        // =================================================================
        // 5. XỬ LÝ CÁC INTENT GIAO TIẾP (Báo bệnh, trò chuyện, hỏi giờ...)
        // =================================================================
        switch (intentName) {
            case "REPORT_SYMPTOM":
                speak("Bác đang thấy khó chịu ở đâu ạ? Bác nhớ theo dõi, nếu mệt quá thì bảo cháu gọi người nhà nhé.");
                uiState.postValue(new HomeUiState.Idle());
                break;
            case "SMALL_TALK":
                speak("Dạ, cháu nghe đây ạ. Hôm nay bác thấy trong người thế nào?");
                uiState.postValue(new HomeUiState.Idle());
                break;
            case "ASK_DATE_TIME":
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm, 'ngày' dd 'tháng' MM", java.util.Locale.getDefault());
                speak("Dạ, bây giờ là " + sdf.format(new java.util.Date()) + " bác ạ.");
                uiState.postValue(new HomeUiState.Idle());
                break;
            case "ASK_HELP":
                speak("Bác có thể bảo cháu đặt lịch uống thuốc, đặt lịch khám, hoặc gọi cấp cứu khi cần thiết ạ.");
                uiState.postValue(new HomeUiState.Idle());
                break;

            // ĐÃ SỬA TÊN INTENT CHO ĐÚNG
            case "CHECK_MEDICATION":
            case "CHECK_APPOINTMENT":
                // Đọc lịch trình hôm nay cho bác nghe
                speak("Dạ, bác đợi cháu xem lại lịch trình hôm nay nhé. Bác có thể kiểm tra chi tiết ở thẻ Đặt lịch phía dưới ạ.");
                // TODO (Mở rộng): Bác có thể nhúng Repository vào đây để query DB lấy lịch hôm nay và đọc: "Hôm nay bác có lịch uống Paracetamol lúc 8h..."
                uiState.postValue(new HomeUiState.Idle());
                break;

            case "CANCEL_MEDICATION":
            case "CANCEL_APPOINTMENT":
                speak("Dạ, bác có thể vào màn hình Đặt Lịch, chọn vào lịch tương ứng để xóa bỏ bác nhé.");
                uiState.postValue(new HomeUiState.Idle());
                break;

            default:
                speak("Cháu chưa hiểu rõ ý bác. Bác có thể nói lại được không ạ?");
                uiState.postValue(new HomeUiState.Idle());
                break;
        }
    }
    private void processDialogueAction(DialogueResult result) {
        String responseText = responseTextProvider.getText(result.getAction().name());
        Log.d("FLOW_TEST", "3. Đang chuẩn bị đọc TTS câu: " + responseText);

        switch (result.getAction()) {
            case ASK_MEDICINE_NAME:
            case ASK_TIME:
            case ASK_CONFIRMATION:
            case ASK_DOSAGE:
            case ASK_LOCATION:
                speakAndListen(responseText);
                break;

            case CONFIRM_MEDICINE_REMINDER_CREATED:
                // BẮN ACTION MỞ FORM THUỐC
                if (lastNlpResult != null) {
                    actionLiveData.postValue(new MedicationAction(lastNlpResult));
                }
                speak(responseText); // "Cháu đang mở form tạo lịch nhắc thuốc đây ạ"
                uiState.postValue(new HomeUiState.Success("Đang mở form thuốc..."));
                break;

            case CONFIRM_APPOINTMENT_CREATED:
                // BẮN ACTION MỞ FORM LỊCH KHÁM
                if (lastNlpResult != null) {
                    actionLiveData.postValue(new AppointmentAction(lastNlpResult));
                }
                speak(responseText);
                uiState.postValue(new HomeUiState.Success("Đang mở form lịch khám..."));
                break;

            case COMPLETE_DIALOGUE:
                speak("Dạ vâng ạ.");
                uiState.postValue(new HomeUiState.Idle());
                break;

            case ASK_CONFIRM_EMERGENCY:
                speakAndListen("Hệ thống phát hiện tình trạng khẩn cấp. Bác có muốn gọi cho người thân ngay không ạ?");
                break;

            case TRIGGER_EMERGENCY:
                // BẮN ACTION MỞ MÀN HÌNH/GỌI ĐIỆN
                actionLiveData.postValue(new EmergencyAction());
                speak("Cháu đang kết nối cuộc gọi. Bác giữ bình tĩnh nhé!");
                break;
            case UNKNOWN_COMMAND:
            default:
                speak("Cháu chưa rõ, bác nói lại được không ạ?");
                break;
        }
    }

    // =================================================================================
    // 4. TTS OUTPUT (Phản hồi)
    // =================================================================================

    // Chỉ nói rồi thôi (Kết thúc câu chuyện -> Về trạng thái Idle)
    private void speak(String text) {
        if (text != null && !text.isEmpty()) {
            // 1. Cập nhật UI sang trạng thái "Đang nói"
            uiState.postValue(new HomeUiState.Speaking(text));

            // 2. Gọi TTS đọc
            ttsRepository.speak(text, new TtsRepository.Callback() {
                @Override
                public void onDone() {
                    // 3. Đọc xong -> Trả UI về trạng thái mặc định (Cái nút xanh)
                    uiState.postValue(new HomeUiState.Idle());
                }

                @Override
                public void onError() {
                    uiState.postValue(new HomeUiState.Idle());
                }
            });
        } else {
            uiState.postValue(new HomeUiState.Idle());
        }
    }

    // Nói xong rồi tự động bật mic nghe tiếp (Hội thoại liên tục)
    private void speakAndListen(String text) {
        if (text != null && !text.isEmpty()) {
            // 1. Cập nhật UI sang trạng thái "Đang nói"
            uiState.postValue(new HomeUiState.Speaking(text));

            // 2. Gọi TTS đọc
            ttsRepository.speak(text, new TtsRepository.Callback() {
                @Override
                public void onDone() {
                    // 3. Đọc xong -> Tự động bật Mic nghe tiếp (Đỏ)
                    startListening();
                }

                @Override
                public void onError() {
                    startListening();
                }
            });
        } else {
            startListening();
        }
    }

    private void processSpeakResult(SpeakResult result) {
        // Cập nhật UI (Ví dụ: hiện chữ trợ lý đang nói)
        uiState.postValue(new HomeUiState.Speaking(result.getTextToSpeak()));

        if (result.shouldSpeakImmediately()) {
            ttsRepository.speak(result.getTextToSpeak(), new TtsRepository.Callback() {
                @Override
                public void onDone() {
                    if (result.shouldListen()) {
                        // Nếu câu hỏi (Cần nghe lại) -> Tự động bật Mic
                        startListening();
                    } else {
                        // Nếu câu kết thúc -> Về trạng thái nghỉ
                        uiState.postValue(new HomeUiState.Idle());
                    }
                }

                @Override
                public void onError() {
                    // Xử lý lỗi TTS, fallback về Idle hoặc bật mic
                    if (result.shouldListen()) startListening();
                    else uiState.postValue(new HomeUiState.Idle());
                }
            });
        }
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        listenVoiceUseCase.stop();
        ttsRepository.stop(); // Nhớ dừng TTS khi thoát
    }

    public void testTextDirectly(String text) {
        uiState.postValue(new HomeUiState.Processing(text));
        processText(text); // Nhảy thẳng vào bước 2 (NLU), bỏ qua bước 1 (ASR)
    }

// Trong HomeViewModel.java

    public void saveMedicineReminder(String title, String dosage, String instruction, long triggerAt,
                                     ReminderTemplate.Type type, ReminderTemplate.RepeatType repeat, int remindMinutes) {
        executorService.execute(() -> {
            // ĐÃ SỬA: Gọi đúng hàm addOrUpdateReminder của bác, truyền null cho ID để báo là Tạo Mới
            container.manageReminderUseCase.addOrUpdateReminder(
                    null,           // templateId (null = tạo mới)
                    title,          // title
                    dosage,         // dosage
                    instruction,    // instruction (ghi chú)
                    triggerAt,      // firstTriggerTimeMillis
                    type,           // type (MEDICINE/ROUTINE)
                    repeat,         // repeatType
                    remindMinutes   // remindMinutes
            );

            // Cập nhật lại UI
            uiState.postValue(new HomeUiState.Idle());

            // TODO: Nếu ViewModel của bác có hàm tải lại lịch sử để cập nhật danh sách, hãy gọi ở đây
            // ví dụ: loadHistoryData();
        });
    }

    public void saveAppointment(String title, String location, String doctor, String notes, long timeMillis) {
        executorService.execute(() -> {
            // ĐÃ SỬA: Gọi đúng hàm addOrUpdateAppointment, truyền null cho ID để tạo mới
            container.manageAppointmentUseCase.addOrUpdateAppointment(
                    null,           // id (null = tạo mới)
                    title,          // title
                    location,       // location
                    doctor,         // doctorName
                    timeMillis,     // timeMillis
                    notes,          // notes
                    null            // status (truyền null để UseCase tự set UPCOMING)
            );

            // Cập nhật UI
            uiState.postValue(new HomeUiState.Idle());

            // TODO: Gọi hàm tải lại lịch sử ở đây để cập nhật danh sách
        });
    }
}