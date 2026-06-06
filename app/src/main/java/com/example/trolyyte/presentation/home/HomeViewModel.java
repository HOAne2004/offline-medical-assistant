package com.example.trolyyte.presentation.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trolyyte.domain.dialog.DialogContext;
import com.example.trolyyte.domain.dialog.DialogueAction;
import com.example.trolyyte.domain.dialog.DialogueResult;
import com.example.trolyyte.domain.model.NlpResult;
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
    private NlpResult lastNlpResult;

        public HomeViewModel(
                ListenVoiceUseCase listenVoiceUseCase,
                ProcessTextUseCase processTextUseCase,
                HandleDialogueUseCase handleDialogueUseCase,
                SpeakResponseUseCase speakResponseUseCase,
                TtsRepository ttsRepository,
                ResponseTextProvider responseTextProvider
        ) {
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
        String intentName = nlpResult.getIntent() != null ? nlpResult.getIntent().name() : "";
        String userText = nlpResult.getText().toLowerCase();

        // =======================================================
        // 1. XỬ LÝ LUỒNG KHẨN CẤP (Ưu tiên cao nhất)
        // =======================================================
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

        // =======================================================
        // 2. CẬP NHẬT TRÍ NHỚ TỪ CÂU NÓI HIỆN TẠI (SLOT FILLING)
        // =======================================================
        Map<String, String> entities = nlpResult.getEntities();
        if (entities.containsKey("medicine_name")) dialogContext.setMedicineName(entities.get("medicine_name"));
        if (entities.containsKey("routine_name")) dialogContext.setRoutineName(entities.get("routine_name"));
        if (entities.containsKey("activity")) dialogContext.setRoutineName(entities.get("activity"));
        if (entities.containsKey("time")) dialogContext.setReminderTime(entities.get("time"));
        if (entities.containsKey("appointment_name")) dialogContext.setAppointmentReason(entities.get("appointment_name"));
        if (entities.containsKey("reason")) dialogContext.setAppointmentReason(entities.get("reason"));
        if (entities.containsKey("location")) dialogContext.setAppointmentLocation(entities.get("location"));
        if (entities.containsKey("date")) dialogContext.setAppointmentDate(entities.get("date"));

        // Xác định xem user đang muốn bắt đầu luồng nào
        if (intentName.equals("SET_OR_UPDATE_MEDICATION") || intentName.equals("SET_OR_UPDATE_APPOINTMENT")) {
            dialogContext.setCurrentIntent(intentName);
        }

        // =======================================================
        // 3. KIỂM TRA THIẾU THÔNG TIN THÌ HỎI LẠI
        // =======================================================
        String currentFlow = dialogContext.getCurrentIntent();

        // LUỒNG 3.1: ĐẶT LỊCH THUỐC / SINH HOẠT
        if ("SET_OR_UPDATE_MEDICATION".equals(currentFlow)) {
            boolean hasName = (dialogContext.getMedicineName() != null) || (dialogContext.getRoutineName() != null);

            if (!hasName) {
                processDialogueAction(new DialogueResult(DialogueAction.ASK_MEDICINE_NAME, dialogContext));
            } else if (dialogContext.getReminderTime() == null) {
                processDialogueAction(new DialogueResult(DialogueAction.ASK_TIME, dialogContext));
            } else {
                // Đủ thông tin -> Đổ trí nhớ vào Result và mở Form
                nlpResult.getEntities().putAll(dialogContext.getFilledEntities());
                this.lastNlpResult = nlpResult;
                processDialogueAction(new DialogueResult(DialogueAction.CONFIRM_MEDICINE_REMINDER_CREATED, dialogContext));
                dialogContext.reset(); // Xóa trí nhớ
            }
            return;
        }

        // LUỒNG 3.2: ĐẶT LỊCH KHÁM
        if ("SET_OR_UPDATE_APPOINTMENT".equals(currentFlow)) {
            if (dialogContext.getAppointmentReason() == null) dialogContext.setAppointmentReason("Tái khám"); // Mặc định

            if (dialogContext.getAppointmentLocation() == null) {
                processDialogueAction(new DialogueResult(DialogueAction.ASK_LOCATION, dialogContext));
            } else if (dialogContext.getReminderTime() == null) {
                processDialogueAction(new DialogueResult(DialogueAction.ASK_TIME, dialogContext));
            } else {
                // Đủ thông tin -> Đổ trí nhớ vào Result và mở Form
                nlpResult.getEntities().putAll(dialogContext.getFilledEntities());
                this.lastNlpResult = nlpResult;
                processDialogueAction(new DialogueResult(DialogueAction.CONFIRM_APPOINTMENT_CREATED, dialogContext));
                dialogContext.reset(); // Xóa trí nhớ
            }
            return;
        }

        // Các luồng khác (Small talk, hỏi đáp...)
        this.lastNlpResult = nlpResult;
        DialogueResult result = handleDialogueUseCase.execute(nlpResult, dialogContext);
        processDialogueAction(result);
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
}