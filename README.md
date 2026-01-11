# ElderCare Assistant – Android Offline Medical Assistant

## 1. Tổng quan dự án

**ElderCare Assistant** là ứng dụng trợ lý y tế offline hỗ trợ người cao tuổi, tập trung vào các chức năng:

* Nhắc uống thuốc đúng giờ, đúng liều lượng
* Nhắc lịch khám bệnh
* Tra cứu thông tin thuốc và triệu chứng phổ biến
* Ghi nhận triệu chứng sức khỏe bằng giọng nói
* Hỗ trợ khẩn cấp (cảnh báo âm thanh, gọi số khẩn cấp)

Ứng dụng được thiết kế để **hoạt động offline**, ưu tiên tính ổn định, dễ sử dụng và phát âm rõ ràng, phù hợp với người cao tuổi.

---

## 2. Mục tiêu kiến trúc

Dự án áp dụng **Clean Architecture**, nhằm đạt được các mục tiêu sau:

* Tách biệt rõ ràng giữa UI – nghiệp vụ – hạ tầng
* Dễ mở rộng (thay ASR/NLU/TTS mà không ảnh hưởng UI)
* Thuận lợi cho làm việc nhóm (phân quyền rõ ràng)
* Phù hợp triển khai nghiên cứu / luận văn

Nguyên tắc cốt lõi:

> **Presentation không phụ thuộc Data**
> **Data phụ thuộc Domain**
> **Domain không phụ thuộc Android framework**

---

## 3. Cấu trúc thư mục tổng thể

```
com.yourapp.medassistant
│
├── core
│   ├── constants        # Hằng số dùng chung
│   └── utils            # Tiện ích không chứa nghiệp vụ
│
├── domain               # Lõi nghiệp vụ (không phụ thuộc Android)
│   ├── ai               # Interface cho các module AI
│   ├── dialog           # Quản lý hội thoại
│   ├── model            # Model nghiệp vụ
│   ├── repository       # Interface truy xuất dữ liệu
│   └── usecase          # Điều phối nghiệp vụ
│
├── data                 # Triển khai chi tiết
│   ├── asr              # Nhận dạng giọng nói (VOSK)
│   ├── nlu              # Xử lý ngôn ngữ tự nhiên (rule-based)
│   ├── tts              # Tổng hợp giọng nói (Android TTS)
│   ├── dialog           # Lưu trạng thái hội thoại
│   └── repository       # Implement repository
│
├── presentation         # UI layer
│   ├── main             # Home / điều phối chính
│   └── voice            # Sự kiện giao tiếp bằng giọng nói
│
└── di
    └── AppContainer     # Dependency Injection thủ công
```

---

## 4. Giải thích từng layer

### 4.1 Domain Layer (Quan trọng nhất)

**Domain** chứa toàn bộ logic nghiệp vụ cốt lõi và hoàn toàn độc lập với Android.

#### 4.1.1 `domain/ai`

Định nghĩa *contract* cho các module AI:

* `AsrService` – Nhận dạng giọng nói
* `NlpService` – Phân tích câu lệnh, intent, entity
* `TtsService` – Phát phản hồi bằng giọng nói

> Đây là API chuẩn mà các module AI phải tuân theo. Không code Android tại đây.

---

#### 4.1.2 `domain/dialog`

* `DialogueManager`: Điều phối luồng hội thoại
* `DialogContext`: Lưu ngữ cảnh hội thoại hiện tại

Đây là trung tâm xử lý logic tương tác người dùng – hệ thống.

---

#### 4.1.3 `domain/model`

Các model nghiệp vụ thuần:

* `NluIntent`
* `EntityType`
* `NlpResult`
* `MedicalCommand`
* `DialogState`

Không chứa logic Android, không gọi service trực tiếp.

---

#### 4.1.4 `domain/repository`

Định nghĩa interface cho lưu trữ và truy xuất dữ liệu:

* `ConversationRepository`
* `ReminderRepository`
* `UserProfileRepository`
* `AsrRepository`, `TtsRepository`

---

#### 4.1.5 `domain/usecase`

Điều phối nghiệp vụ:

* `ListenVoiceUseCase`
* `ProcessTextUseCase`
* `HandleDialogueUseCase`
* `SpeakResponseUseCase`

UseCase là **điểm vào duy nhất** từ Presentation xuống Domain.

---

### 4.2 Data Layer

Triển khai chi tiết cho các interface trong Domain.

* `asr`: VOSK ASR engine
* `nlu`: Rule-based NLP engine
* `tts`: Android Text-to-Speech
* `repository`: Cài đặt Repository

> Data layer **được phép phụ thuộc Android SDK**.

---

### 4.3 Presentation Layer

Chịu trách nhiệm UI và tương tác người dùng:

* `MainActivity`: hiển thị và nhận input
* `MainViewModel`: gọi UseCase
* `UiState`, `VoiceUiEvent`: quản lý trạng thái UI

⚠️ **Presentation không được gọi trực tiếp Data hoặc Engine**.

---

### 4.4 Dependency Injection (`di`)

`AppContainer` chịu trách nhiệm:

* Khởi tạo Service / Repository
* Inject dependency cho ViewModel

Dùng DI thủ công để dễ kiểm soát trong nghiên cứu.

---

## 5. Quy ước làm việc nhóm (BẮT BUỘC TUÂN THỦ)

1. **Không import ngược layer**

    * Presentation → Domain → Data (một chiều)

2. **Không code Android trong Domain**

3. **Không sửa interface trong `domain/ai` nếu không thông qua nhóm trưởng**

4. **Mỗi thành viên chỉ code trong phạm vi được phân công**

5. **Mọi module mới phải có UseCase tương ứng**

---

## 6. Định hướng mở rộng

* Thay Rule-based NLP bằng ML-based NLP
* Thêm Room/SQLite cho lưu trữ lịch sử
* Đồng bộ cloud (tuỳ chọn)
* Đánh giá ASR/NLU/TTS phục vụ luận văn

---

## 7. Ghi chú quan trọng

* File test cục bộ (helper, prototype) **không được commit lên Git**
* Mọi thay đổi cấu trúc phải được thống nhất trước

---

**Tài liệu này là tài liệu kỹ thuật nội bộ, dùng để thống nhất kiến trúc và phân công công việc trong nhóm.**
