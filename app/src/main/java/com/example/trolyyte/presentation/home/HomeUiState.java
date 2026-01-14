package com.example.trolyyte.presentation.home;

public abstract class HomeUiState {

    // Trạng thái nghỉ (Màn hình chờ)
    public static class Idle extends HomeUiState {}

    // Trạng thái đang nghe (Hiện icon mic, sóng âm)
    public static class Listening extends HomeUiState {}

    // Trạng thái hiện chữ chạy thời gian thực (Partial Result)
    public static class PartialResult extends HomeUiState {
        public final String text;
        public PartialResult(String text) { this.text = text; }
    }

    // Trạng thái đang xử lý (Hiện loading spinner)
    public static class Processing extends HomeUiState {
        public final String text;
        public Processing(String text) { this.text = text; }
    }
    // Trạng thái trợ lý đang nói (Hiện text trợ lý, animation loa - Máy đang nói)
    public static class Speaking extends HomeUiState {
        public final String text;
        public Speaking(String text) { this.text = text; }
    }
    // Trạng thái thành công (Hiện thông báo xanh)
    public static class Success extends HomeUiState {
        public final String message;
        public Success(String message) { this.message = message; }
    }

    // Trạng thái lỗi (Hiện thông báo đỏ)
    public static class Error extends HomeUiState {
        public final String message;
        public Error(String message) { this.message = message; }
    }
}