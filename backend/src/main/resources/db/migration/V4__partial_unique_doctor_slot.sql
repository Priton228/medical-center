-- Заменяем безусловное ограничение уникальности (doctor_id, appointment_date)
-- на частичный уникальный индекс, исключающий записи в статусе CANCELLED.
-- Это позволяет переиспользовать слот после отмены, что согласуется с
-- логикой AppointmentService (создание/перенос игнорируют CANCELLED-приёмы).
ALTER TABLE appointments DROP CONSTRAINT IF EXISTS uq_doctor_slot;
CREATE UNIQUE INDEX IF NOT EXISTS uq_doctor_slot_active
    ON appointments (doctor_id, appointment_date)
    WHERE status <> 'CANCELLED';
