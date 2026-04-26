import { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { X } from 'lucide-react';
import { appointmentsApi } from '@/api/endpoints';
import type { AppointmentResponse } from '@/types';

interface Props {
  appointment: AppointmentResponse | null;
  onClose: () => void;
  onDone: () => void;
}

/**
 * Модалка переноса записи на новую дату/время. Шлёт PATCH /appointments/{id}/reschedule.
 */
export default function RescheduleModal({ appointment, onClose, onDone }: Props) {
  const [date, setDate] = useState('');
  const [time, setTime] = useState('');
  const [notes, setNotes] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (appointment) {
      const d = new Date(appointment.appointmentDate);
      setDate(d.toISOString().slice(0, 10));
      setTime(d.toTimeString().slice(0, 5));
      setNotes(appointment.notes ?? '');
    }
  }, [appointment]);

  if (!appointment) return null;

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    try {
      const iso = `${date}T${time}:00`;
      await appointmentsApi.reschedule(appointment.id, { appointmentDate: iso, notes: notes || undefined });
      toast.success('Запись перенесена. Уведомление переотправлено.');
      onDone();
      onClose();
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Не удалось перенести запись');
    } finally { setBusy(false); }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 backdrop-blur-sm p-4">
      <div className="card w-full max-w-md relative">
        <button type="button" className="absolute top-3 right-3 text-slate-400 hover:text-slate-600" onClick={onClose}><X size={18} /></button>
        <h3 className="text-lg font-bold text-slate-800">Перенос записи</h3>
        <p className="text-sm text-slate-500 mt-1">{appointment.doctorFullName} · {appointment.doctorSpecialization}</p>
        <form onSubmit={submit} className="mt-4 space-y-3">
          <div className="grid grid-cols-2 gap-3">
            <div><label className="label">Дата</label><input type="date" className="input" required value={date} onChange={(e) => setDate(e.target.value)} /></div>
            <div><label className="label">Время</label><input type="time" className="input" required step={300} value={time} onChange={(e) => setTime(e.target.value)} /></div>
          </div>
          <div>
            <label className="label">Комментарий (необязательно)</label>
            <textarea className="input" rows={3} value={notes} onChange={(e) => setNotes(e.target.value)} />
          </div>
          <div className="flex justify-end gap-2 pt-2">
            <button type="button" className="btn-ghost" onClick={onClose}>Отмена</button>
            <button className="btn-primary" disabled={busy}>{busy ? 'Переносим…' : 'Перенести'}</button>
          </div>
        </form>
      </div>
    </div>
  );
}
