# Architecture Notes

## Clean Architecture mapping

- Presentation: Compose Screen, ViewModel, UI state
- Domain: UseCase, repository interface, SM-2 scheduler
- Data: repository implementation, mapper, network + database sources

## Review flow

1. User chọn `Again/Hard/Good/Easy`
2. `ReviewViewModel` gọi `SubmitReviewUseCase`
3. `OfflineFirstFlashcardRepository` đọc progress hiện tại từ Room
4. `Sm2Scheduler` tính progress mới
5. Repository ghi Room
6. Repository đẩy payload review lên backend
7. Các thiết bị khác có thể nhận event sync qua WebSocket

## Vì sao Room + Flow

- UI phản ứng realtime
- Không block main thread
- Giảm memory leak vì lifecycle-aware collection ở Compose
- Hoạt động tốt khi mạng chập chờn

## Hướng mở rộng

- Thêm paging cho deck/card lớn
- Thêm `pending_sync_queue`
- Thêm protobuf/cache encryption nếu dữ liệu nhạy cảm
- Tách `core:tts` và `core:notification` thành module riêng nếu scope tăng
