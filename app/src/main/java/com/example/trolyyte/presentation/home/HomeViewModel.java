package com.example.trolyyte.presentation.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.trolyyte.domain.dialog.DialogContext;
import com.example.trolyyte.domain.dialog.DialogueAction;
import com.example.trolyyte.domain.dialog.DialogueResult;
import com.example.trolyyte.domain.model.NlpResult;
import com.example.trolyyte.domain.model.SpeakResult;
import com.example.trolyyte.domain.repository.TtsRepository;
import com.example.trolyyte.domain.usecase.HandleDialogueUseCase;
import com.example.trolyyte.domain.usecase.ListenVoiceResult;
import com.example.trolyyte.domain.usecase.ListenVoiceUseCase;
import com.example.trolyyte.domain.usecase.ProcessTextUseCase;
import com.example.trolyyte.domain.usecase.SpeakResponseUseCase;
import com.example.trolyyte.presentation.common.ResponseTextProvider;

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

    // UI State (Dùng LiveData để UI observe)
    private final MutableLiveData<HomeUiState> uiState = new MutableLiveData<>(new HomeUiState.Idle());
    public LiveData<HomeUiState> getUiState() { return uiState; }

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
        processTextUseCase.execute(text, new ProcessTextUseCase.Callback() {
            @Override
            public void onSuccess(NlpResult nlpResult) {
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
        // ViewModel giữ context, truyền vào UseCase để xử lý logic chuyển trạng thái
        DialogueResult result = handleDialogueUseCase.execute(nlpResult, dialogContext);

        // Cập nhật lại context mới (quan trọng để nhớ trạng thái cho vòng sau)
        this.dialogContext = result.getUpdatedContext();

        SpeakResult speakResult = speakResponseUseCase.execute(result);
        // Xử lý hành động tiếp theo dựa trên kết quả
        processDialogueAction(result);
    }

    private void processDialogueAction(DialogueResult result) {
        // Lấy câu trả lời text từ key (Ví dụ: ASK_TIME -> "Bác muốn uống lúc mấy giờ?")
        // Lưu ý: responseTextProvider nên lấy string từ resources hoặc file config
        String responseText = responseTextProvider.getText(result.getAction().name());
        // Hoặc logic mapping key phức tạp hơn nằm ở SpeakResponseUseCase sau này

        switch (result.getAction()) {
            case ASK_MEDICINE_NAME:
            case ASK_TIME:
            case ASK_CONFIRMATION:
                // Cần hỏi lại -> Nói xong thì bật mic nghe tiếp
                speakAndListen(responseText);
                break;

            case CONFIRM_MEDICINE_REMINDER_CREATED:
                // Thành công -> Lưu DB (Giả lập) -> Nói xác nhận -> Kết thúc
                // saveReminderToDb(dialogContext); // TODO: Implement sau
                speak(responseText);
                uiState.postValue(new HomeUiState.Success("Đã đặt lịch nhắc thuốc thành công"));
                break;

            case COMPLETE_DIALOGUE:
                speak("Dạ vâng ạ.");
                uiState.postValue(new HomeUiState.Idle());
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

    // Chỉ nói rồi thôi (Kết thúc câu chuyện)
    private void speak(String text) {
        if (text != null && !text.isEmpty()) {
            ttsRepository.speak(text);
        }
    }

    // Nói xong rồi tự động bật mic nghe tiếp (Hội thoại liên tục)
    private void speakAndListen(String text) {
        if (text != null && !text.isEmpty()) {
            ttsRepository.speak(text, new TtsRepository.Callback() {
                @Override
                public void onDone() {
                    // TTS nói xong -> Bật mic ngay lập tức (Chạy trên Main Thread)
                    startListening();
                }

                // --- BỔ SUNG HÀM NÀY ĐỂ HẾT LỖI ---
                @Override
                public void onError() {
                    // Nếu TTS lỗi không nói được, ta vẫn bật mic để người dùng không bị kẹt
                    startListening();

                    // Hoặc update UI báo lỗi nhẹ
                    // uiState.postValue(new HomeUiState.Error("Lỗi phát âm thanh"));
                }
            });
        } else {
            startListening(); // Nếu không có text, nghe luôn
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
}