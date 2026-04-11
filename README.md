# FlashMind

FlashMind là ứng dụng Android học từ vựng theo mô hình tương tự Anki/Quizlet, dùng thuật toán SuperMemo SM-2 để tự động lên lịch ôn tập theo độ khó, hỗ trợ offline-first với Room, đồng bộ tiến độ qua backend REST/WebSocket, và nhắc học hằng ngày bằng WorkManager.

Hiện trạng skeleton đã hỗ trợ:

- xem danh sách deck
- tạo deck mới local
- xem chi tiết deck
- thêm, sửa, xóa card trong deck
- review thẻ theo `Again/Hard/Good/Easy`
- seed dữ liệu mẫu khi backend chưa sẵn sàng
- lưu `pending sync queue` cơ bản cho review/card/deck khi backend lỗi
- phát âm cục bộ bằng Android TextToSpeech ngay trong màn card/review
- có nút `Sync now` trên home để thử replay queue thủ công khi demo

## Stack

- Kotlin + Android Studio
- Jetpack Compose + MVVM
- Clean Architecture theo chiều `presentation -> domain -> data`
- Hilt cho dependency injection
- Coroutines/Flow cho bất đồng bộ
- Room cho cache offline
- Retrofit + OkHttp + WebSocket cho backend sync
- WorkManager cho daily reminder

## Android config hiện tại

- `compileSdk = 34`
- `targetSdk = 34`
- `minSdk = 24`

Vì vậy trong Android Studio bạn chỉ cần cài `Android 14 (API 34)` để sync/build theo cấu hình hiện tại.

## Cấu trúc module

- `app`: entry point, navigation, WorkManager, cấu hình Hilt toàn app
- `core:model`: entity/domain model dùng chung
- `core:domain`: use case, contract repository, thuật toán SM-2
- `core:database`: Room DB, entity, DAO
- `core:network`: REST API, DTO, WebSocket, Google TTS client
- `core:data`: repository offline-first, mapping, orchestration network/local
- `feature:deck`: màn hình danh sách bộ thẻ
- `feature:review`: màn hình ôn tập và chấm độ khó

## Luồng dữ liệu

1. `feature:*` gọi `UseCase` trong `core:domain`.
2. `UseCase` làm việc với `FlashcardRepository`.
3. `core:data` lấy dữ liệu từ `Room` để hiển thị ngay.
4. Song song, repository gọi REST API để đồng bộ bản mới nhất.
5. Khi người dùng review thẻ, `Sm2Scheduler` tính lại `easeFactor`, `interval`, `nextReviewAt`.
6. Repository cập nhật Room trước để không block UI.
7. Nếu gọi backend lỗi, app đưa payload vào `pending_sync_tasks`.
8. Worker hoặc app foreground có thể thử sync lại queue này.
9. WebSocket dùng để nhận sự kiện sync gần realtime từ thiết bị khác.

## Thuật toán SM-2

File chính: `core/domain/src/main/java/com/example/flashmind/core/domain/spacedrepetition/Sm2Scheduler.kt`

- `Again` reset repetition, giảm ease factor, lịch lại sau 1 ngày.
- `Hard/Good/Easy` tăng repetition.
- Lần 1: 1 ngày.
- Lần 2: 6 ngày.
- Từ lần 3: `interval = interval * easeFactor`, có chặn tối thiểu để tránh lùi lịch vô lý.
- `easeFactor` không giảm dưới `1.3`.

## Thiết kế local DB

### Bảng `decks`

- `id`
- `title`
- `description`

### Bảng `cards`

- `id`
- `deckId`
- `front`
- `back`
- `pronunciation`
- `exampleSentence`
- `audioUrl`
- `repetition`
- `intervalDays`
- `easeFactor`
- `nextReviewAt`
- `lastReviewedAt`

### Bảng `pending_sync_tasks`

- `id`
- `type`
- `payload`
- `createdAt`

## API contract đề xuất

### REST

- `GET /v1/decks`
- `GET /v1/decks/{deckId}/due-cards`
- `POST /v1/reviews`
- `POST /v1/audio/google-tts`

### WebSocket

- `wss://api.flashmind.dev/v1/sync`
- Event gợi ý:
  - `progress_updated`
  - `deck_shared`
  - `card_audio_generated`
  - `sync_conflict_detected`

## Xử lý offline/mất mạng

- UI luôn đọc từ Room qua `Flow`.
- Nếu mất mạng, dữ liệu cũ vẫn hiển thị và review vẫn cập nhật local trước.
- Nếu backend chưa chạy, app sẽ seed deck/card mẫu để vẫn demo được.
- Khi có mạng lại, repository/worker hoặc nút `Sync now` có thể replay các task pending lên backend.
- App đã có replay contract cho `REVIEW`, `CREATE_DECK`, `CREATE_CARD`, `UPDATE_CARD`, `DELETE_CARD`, `DELETE_DECK`.
- Để replay chạy thật end-to-end, backend cần expose đúng các endpoint đã định nghĩa trong `core:network`.

## WorkManager

`DailyReviewWorker` tạo periodic work chạy mỗi ngày, đếm số thẻ đến hạn và đẩy notification nếu có card cần học.

## Google TTS

`core:network` đã có endpoint cho Google Text-to-Speech.

Luồng chuẩn:

1. User mở mặt sau thẻ hoặc bấm loa.
2. App gọi backend proxy tới Google TTS.
3. Backend trả `audioUrl`.
4. App cache URL hoặc file cục bộ để phát lại offline.

Hiện trạng demo:

- UI đang dùng `Android TextToSpeech` cục bộ để phát âm ổn định khi demo trên máy/emulator.
- Contract Google TTS vẫn giữ ở tầng network để có thể thay bằng backend thật khi hoàn thiện.

## Git Flow bắt buộc

Nhánh đề xuất:

- `main`: production-ready
- `develop`: integration branch
- `feature/sm2-review-engine`
- `feature/offline-room-cache`
- `feature/daily-review-worker`
- `feature/google-tts-integration`
- `feature/deck-compose-ui`
- `release/x.y.z`
- `hotfix/...`

Quy tắc:

- Không commit trực tiếp lên `main`.
- Mỗi tính năng tách branch rõ nghĩa theo chức năng.
- PR vào `develop` phải có mô tả mục tiêu, ảnh UI, test scope, rủi ro.
- Peer review bắt buộc trước merge.
- Commit nhỏ, rải đều, tránh dump một commit lớn.

## PR template gợi ý

- Mục tiêu
- Phạm vi thay đổi
- Ảnh/GIF demo
- API/DB bị ảnh hưởng
- Test đã chạy
- Rủi ro còn lại
- Câu hỏi cần reviewer tập trung

## Demo checklist

- Tạo bộ thẻ từ vựng.
- Thêm/sửa/xóa card trong deck.
- Review bằng 4 mức độ.
- Sau mỗi lần chấm, lịch ôn đổi theo SM-2.
- Bấm `Speak` để phát âm mặt trước của card.
- Tắt mạng vẫn xem và học được thẻ đã cache.
- Mở mạng lại thì sync tiến độ.
- Notification daily reminder hoạt động.
- Phát âm qua TTS chạy được.

## Điểm cần hoàn thiện thêm trước khi nộp

- Thêm test cho `Sm2Scheduler`, `Repository`, `DAO`.
- Bổ sung queue sync khi offline kéo dài.
- Thêm auth/token refresh cho backend thật.
- Thêm animation, theme, onboarding và analytics.
- Tạo backend thật hoặc mock server để demo end-to-end.

## Lưu ý hiện trạng repo

Repo ban đầu chưa có Android project sẵn. Phần hiện tại là skeleton kiến trúc và lõi nghiệp vụ để mở bằng Android Studio và tiếp tục phát triển, chưa được verify bằng build thực tế trong môi trường hiện tại vì repo chưa có Gradle wrapper/SDK cấu hình sẵn.
