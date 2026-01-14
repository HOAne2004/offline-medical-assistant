package com.example.trolyyte.di;

import android.content.Context;

import com.example.trolyyte.data.asr.AsrEngine;
import com.example.trolyyte.data.asr.VoskAsrEngine;
import com.example.trolyyte.data.nlu.NlpEngine;
import com.example.trolyyte.data.nlu.RuleBasedNlpEngine;
import com.example.trolyyte.data.nlu.TfliteNlpEngine;
import com.example.trolyyte.data.repository.AsrRepositoryImpl;
import com.example.trolyyte.data.repository.NlpRepositoryImpl;
import com.example.trolyyte.data.repository.TtsRepositoryImpl;
import com.example.trolyyte.data.tts.AndroidTtsEngine;
import com.example.trolyyte.data.tts.TtsEngine;
import com.example.trolyyte.domain.dialog.DialogueManager;
import com.example.trolyyte.domain.dialog.DialogueManagerImpl;
import com.example.trolyyte.domain.dialog.ResponseTemplateProvider;
import com.example.trolyyte.domain.repository.AsrRepository;
import com.example.trolyyte.domain.repository.NlpRepository;
import com.example.trolyyte.domain.repository.TtsRepository;
import com.example.trolyyte.domain.usecase.HandleDialogueUseCase;
import com.example.trolyyte.domain.usecase.HandleDialogueUseCaseImpl;
import com.example.trolyyte.domain.usecase.ListenVoiceUseCase;
import com.example.trolyyte.domain.usecase.ListenVoiceUseCaseImpl;
import com.example.trolyyte.domain.usecase.ProcessTextUseCase;
import com.example.trolyyte.domain.usecase.ProcessTextUseCaseImpl;
import com.example.trolyyte.domain.usecase.SpeakResponseUseCase;
import com.example.trolyyte.domain.usecase.SpeakResponseUseCaseImpl;
import com.example.trolyyte.presentation.common.DefaultResponseTextProvider;
import com.example.trolyyte.presentation.common.ResponseTextProvider;
import com.example.trolyyte.presentation.home.HomeViewModelFactory;

public class AppContainer {

    // --- 1. Engines (Tầng thấp nhất - Data Source) ---
    private AsrEngine asrEngine;
    private NlpEngine nlpEngine;
    private TtsEngine ttsEngine;

    // --- 2. Repositories (Tầng trung gian - Data) ---
    public AsrRepository asrRepository;
    public NlpRepository nlpRepository;
    public TtsRepository ttsRepository;

    // --- 3. Providers & Managers (Tầng Domain) ---
    public ResponseTextProvider responseTextProvider;
    public DialogueManager dialogueManager;

    // --- 4. UseCases (Tầng nghiệp vụ - Domain) ---
    public ListenVoiceUseCase listenVoiceUseCase;
    public ProcessTextUseCase processTextUseCase;
    public HandleDialogueUseCase handleDialogueUseCase;
    public SpeakResponseUseCase speakResponseUseCase;

    // Context của Application
    private final Context context;

    public AppContainer(Context context) {
        this.context = context;
        initializeDependencies();
    }

    private void initializeDependencies() {
// A. Khởi tạo Data Engines
        asrEngine = new VoskAsrEngine(context);
        asrEngine.initialize(); //giải nén model khi mở app
        ttsEngine = new AndroidTtsEngine(context);

// [QUAN TRỌNG] Chỗ này dễ dàng switch giữa Rule-Based và TFLite cho luận văn
// Cách 1: Dùng Regex (Giai đoạn 1)
        nlpEngine = new RuleBasedNlpEngine();

// Cách 2: Dùng AI TFLite (Giai đoạn 2)
//nlpEngine = new TfliteNlpEngine(context);
//nlpEngine.initialize();

// B. Khởi tạo Repositories
        asrRepository = new AsrRepositoryImpl(asrEngine);
        nlpRepository = new NlpRepositoryImpl(nlpEngine);
        ttsRepository = new TtsRepositoryImpl(ttsEngine);

// C. Khởi tạo Helpers
        dialogueManager = new DialogueManagerImpl();
// DefaultResponseTextProvider vừa dùng cho UI, vừa dùng cho TTS
        responseTextProvider = new DefaultResponseTextProvider();

// D. Khởi tạo UseCases
        listenVoiceUseCase = new ListenVoiceUseCaseImpl(asrRepository);
        processTextUseCase = new ProcessTextUseCaseImpl(nlpRepository);
        handleDialogueUseCase = new HandleDialogueUseCaseImpl(dialogueManager);

// Adapter: Chuyển đổi ResponseTextProvider thành ResponseTemplateProvider cho UseCase
// (Do UseCase cần Interface Template, mà UI cần Interface TextProvider)
        ResponseTemplateProvider templateProvider = key -> responseTextProvider.getText(key.name());
        speakResponseUseCase = new SpeakResponseUseCaseImpl(templateProvider);
    }

// E. Cung cấp Factory cho ViewModel
// ViewModel cần Factory vì nó có tham số trong constructor
    public HomeViewModelFactory getHomeViewModelFactory() {
        return new HomeViewModelFactory(listenVoiceUseCase, processTextUseCase, handleDialogueUseCase, speakResponseUseCase, ttsRepository, responseTextProvider);
    }
}

