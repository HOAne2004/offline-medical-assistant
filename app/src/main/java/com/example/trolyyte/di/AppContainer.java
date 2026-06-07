package com.example.trolyyte.di;

import android.content.Context;

import com.example.trolyyte.data.local.AppDatabase;
import com.example.trolyyte.data.repository.AppointmentRepositoryImpl;
import com.example.trolyyte.data.repository.SettingsRepositoryImpl;
import com.example.trolyyte.data.repository.UserProfileRepositoryImpl;
import com.example.trolyyte.data.utils.ReminderAlarmScheduler;
import com.example.trolyyte.domain.repository.AppointmentRepository;
import com.example.trolyyte.domain.repository.ReminderRepository;
import com.example.trolyyte.domain.repository.SettingsRepository;
import com.example.trolyyte.domain.repository.UserProfileRepository;
import com.example.trolyyte.data.repository.ReminderRepositoryImpl;

import com.example.trolyyte.data.asr.AsrEngine;
import com.example.trolyyte.data.asr.VoskAsrEngine;
import com.example.trolyyte.data.nlu.NlpEngine;
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
import com.example.trolyyte.domain.usecase.GetAppointmentsUseCase;
import com.example.trolyyte.domain.usecase.GetRemindersUseCase;
import com.example.trolyyte.domain.usecase.GetUserProfileUseCase;
import com.example.trolyyte.domain.usecase.HandleDialogueUseCase;
import com.example.trolyyte.domain.usecase.HandleDialogueUseCaseImpl;
import com.example.trolyyte.domain.usecase.ListenVoiceUseCase;
import com.example.trolyyte.domain.usecase.ListenVoiceUseCaseImpl;
import com.example.trolyyte.domain.usecase.ManageAppointmentUseCase;
import com.example.trolyyte.domain.usecase.ManageReminderUseCase;
import com.example.trolyyte.domain.usecase.ProcessTextUseCase;
import com.example.trolyyte.domain.usecase.ProcessTextUseCaseImpl;
import com.example.trolyyte.domain.usecase.SaveUserProfileUseCase;
import com.example.trolyyte.domain.usecase.SpeakResponseUseCase;
import com.example.trolyyte.domain.usecase.SpeakResponseUseCaseImpl;
import com.example.trolyyte.presentation.common.DefaultResponseTextProvider;
import com.example.trolyyte.presentation.common.ResponseTextProvider;
import com.example.trolyyte.presentation.home.HomeViewModelFactory;
import com.example.trolyyte.presentation.profile.ProfileViewModelFactory;
import com.example.trolyyte.presentation.schedule.ScheduleViewModelFactory;

public class AppContainer {

    // --- 0. Database & Utils ---
    private AppDatabase database;
    private ReminderAlarmScheduler alarmScheduler;

    // --- 1. Engines (Tầng thấp nhất - Data Source) ---
    private AsrEngine asrEngine;
    private NlpEngine nlpEngine;
    private TtsEngine ttsEngine;

    // --- 2. Repositories (Tầng trung gian - Data) ---
    public AsrRepository asrRepository;
    public NlpRepository nlpRepository;
    public TtsRepository ttsRepository;
    public ReminderRepository reminderRepository;
    public AppointmentRepository appointRepository;
    public UserProfileRepository userProfileRepository;
    public SettingsRepository settingsRepository;

    // --- 3. Providers & Managers (Tầng Domain) ---
    public ResponseTextProvider responseTextProvider;
    public DialogueManager dialogueManager;

    // --- 4. UseCases (Tầng nghiệp vụ - Domain) ---
    public ListenVoiceUseCase listenVoiceUseCase;
    public ProcessTextUseCase processTextUseCase;
    public HandleDialogueUseCase handleDialogueUseCase;
    public SpeakResponseUseCase speakResponseUseCase;
    public GetRemindersUseCase getRemindersUseCase;
    public ManageReminderUseCase manageReminderUseCase;
    public GetAppointmentsUseCase getAppointmentUseCase;
    public ManageAppointmentUseCase manageAppointmentUseCase;
    public GetUserProfileUseCase getUserProfileUseCase;
    public SaveUserProfileUseCase saveUserProfileUseCase;
    // Context của Application
    private final Context context;

    public AppContainer(Context context) {
        this.context = context;
        initializeDependencies();
    }

    private void initializeDependencies() {
        // 0. Khởi tạo Database & Utils
        database = AppDatabase.getDatabase(context);
        alarmScheduler = new ReminderAlarmScheduler(context);

        // A. Khởi tạo Data Engines
        asrEngine = new VoskAsrEngine(context);
        //asrEngine.initialize();
        ttsEngine = new AndroidTtsEngine(context);

        nlpEngine = new TfliteNlpEngine(context);
        //nlpEngine.initialize();

        // B. Khởi tạo Repositories
        asrRepository = new AsrRepositoryImpl(asrEngine);
        nlpRepository = new NlpRepositoryImpl(nlpEngine);
        ttsRepository = new TtsRepositoryImpl(ttsEngine);

        // CẬP NHẬT: Truyền cả Dao và Scheduler vào Repository
        reminderRepository = new ReminderRepositoryImpl(database.reminderDao(), alarmScheduler);
        appointRepository = new AppointmentRepositoryImpl(database.appointmentDao());

        userProfileRepository = new UserProfileRepositoryImpl(database.userProfileDao());
        settingsRepository = new SettingsRepositoryImpl(context);

        // C. Khởi tạo Helpers
        dialogueManager = new DialogueManagerImpl();
        responseTextProvider = new DefaultResponseTextProvider();

        // D. Khởi tạo UseCases
        listenVoiceUseCase = new ListenVoiceUseCaseImpl(asrRepository);
        processTextUseCase = new ProcessTextUseCaseImpl(nlpRepository);
        handleDialogueUseCase = new HandleDialogueUseCaseImpl(dialogueManager);

        getRemindersUseCase = new GetRemindersUseCase(reminderRepository);
        manageReminderUseCase = new ManageReminderUseCase(reminderRepository);

        getAppointmentUseCase = new GetAppointmentsUseCase(appointRepository);
        manageAppointmentUseCase = new ManageAppointmentUseCase(appointRepository);

        ResponseTemplateProvider templateProvider = key -> responseTextProvider.getText(key.name());
        speakResponseUseCase = new SpeakResponseUseCaseImpl(templateProvider);

        getUserProfileUseCase = new GetUserProfileUseCase(userProfileRepository);
        saveUserProfileUseCase = new SaveUserProfileUseCase(userProfileRepository);

        // Khởi tạo bộ chuẩn hóa từ vựng AI (Đọc từ file JSON)
        com.example.trolyyte.data.utils.VietnameseTextNormalizer.init(context);
    }
    public ProfileViewModelFactory getProfileViewModelFactory() {
        return new ProfileViewModelFactory(getUserProfileUseCase, saveUserProfileUseCase);
    }

    public ScheduleViewModelFactory getScheduleViewModelFactory() {
        return new ScheduleViewModelFactory(
                getRemindersUseCase,
                getAppointmentUseCase,
                manageReminderUseCase,
                manageAppointmentUseCase
        );
    }
    public HomeViewModelFactory getHomeViewModelFactory() {
        return new HomeViewModelFactory(listenVoiceUseCase, processTextUseCase, handleDialogueUseCase, speakResponseUseCase, ttsRepository, responseTextProvider);
    }
}
