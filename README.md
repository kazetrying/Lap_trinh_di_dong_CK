# Flashcard App - Ứng dụng Học tập Thông minh

Ứng dụng Flashcard giúp người dùng học tập và ghi nhớ từ vựng hoặc kiến thức mới hiệu quả thông qua phương pháp lặp lại ngắt quãng (Spaced Repetition).

##  Tính năng chính

- **Đăng nhập & Đăng ký:** Quản lý tài khoản cá nhân qua Firebase Authentication.
- **Quản lý Bộ thẻ (Deck):** Tạo, sửa, xóa các bộ thẻ theo chủ đề.
- **Quản lý Thẻ (Card):** Thêm từ vựng, định nghĩa và ví dụ vào từng bộ thẻ.
- **Chế độ Ôn tập:** Áp dụng thuật toán **SM-2** để tối ưu hóa thời gian ghi nhớ.
- **Phát âm (TTS):** Tích hợp công nghệ Text-to-Speech để đọc nội dung thẻ (hỗ trợ Tiếng Anh và Tiếng Việt).
- **Đồng bộ Realtime:** Tự động đồng bộ dữ liệu giữa thiết bị (Room Database) và đám mây (Firebase Realtime Database).
- **Nhắc nhở hàng ngày:** Gửi thông báo nhắc nhở học tập qua WorkManager.
- **Thống kê:** Theo dõi tiến độ học tập và mức độ ghi nhớ của từng bộ thẻ.

## Công nghệ sử dụng

- **Ngôn ngữ:** Kotlin
- **UI:** Jetpack Compose (Modern Android UI Toolkit)
- **Kiến trúc:** MVVM (Model-View-ViewModel)
- **Cơ sở dữ liệu:** 
  - Local: **Room Database** (Offline-first)
  - Cloud: **Firebase Realtime Database**
- **Xác thực:** Firebase Auth
- **Xử lý nền:** WorkManager
- **Luồng dữ liệu:** Coroutines & Flow/StateFlow

## Giao diện

Ứng dụng được thiết kế theo phong cách hiện đại, sử dụng tông màu xanh dương chủ đạo (#2D6CD1) kết hợp với nền Pastel nhẹ nhàng, mang lại cảm giác thoải mái khi học tập.

## Cài đặt

1. Clone dự án về máy.
2. Mở bằng Android Studio.
3. Cấu hình Firebase:
   - Tạo Project trên Firebase Console.
   - Thêm ứng dụng Android và tải file `google-services.json` đặt vào thư mục `app/`.
   - Bật Authentication (Email/Password) và Realtime Database.
4. Build và chạy ứng dụng trên Emulator hoặc thiết bị thật.

---
**Học, học nữa, học mãi!**
