# Enrollment dan Lesson Access Control

## Tujuan

Melindungi materi lesson agar hanya dapat diakses oleh peserta enrolled, instructor pemilik, atau admin. Lesson wajib diselesaikan secara berurutan lintas module.

## Implementasi

1. Menambah repository untuk Lesson.
2. Menambahkan endpoint `GET /api/enrollments/{courseId}/lessons/{lessonId}`.
3. Memvalidasi enrollment sebelum lesson dapat dibuka.
4. Memvalidasi lesson milik course yang diminta.
5. Memastikan lesson sebelumnya sudah selesai sebelum membuka atau menyelesaikan lesson berikutnya.
6. Menyembunyikan content, video URL, dan file URL pada course detail untuk user tanpa akses.
7. Menambahkan unit test untuk validasi urutan lesson.

## API

- `GET /api/enrollments/{courseId}/lessons/{lessonId}`
- `POST /api/enrollments/{courseId}/lessons/{lessonId}/complete`
