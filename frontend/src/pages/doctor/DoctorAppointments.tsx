import { useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { ClipboardList } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import EmptyState from '@/components/EmptyState';
import StatusBadge from '@/components/StatusBadge';
import { appointmentsApi, recordsApi } from '@/api/endpoints';
import { formatDateTime } from '@/utils/format';
import type { AppointmentResponse, AppointmentStatus } from '@/types';
import { useState } from 'react';
import RescheduleModal from '@/components/RescheduleModal';
import { motion, AnimatePresence } from 'framer-motion';

export default function DoctorAppointments() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['d-appointments'], queryFn: () => appointmentsApi.listMine() });
  const [recordFor, setRecordFor] = useState<{ apptId: number; patientName: string } | null>(null);
  const [diagnosis, setDiagnosis] = useState('');
  const [treatment, setTreatment] = useState('');
  const [notes, setNotes] = useState('');
  const [busy, setBusy] = useState(false);
  const [reschedTarget, setReschedTarget] = useState<AppointmentResponse | null>(null);

  const setStatus = async (id: number, status: AppointmentStatus) => {
    try {
      await appointmentsApi.setStatus(id, status);
      toast.success('Статус обновлён');
      qc.invalidateQueries({ queryKey: ['d-appointments'] });
      qc.invalidateQueries({ queryKey: ['d-upcoming'] });
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Ошибка');
    }
  };

  const submitRecord = async () => {
    if (!recordFor) return;
    setBusy(true);
    try {
      await recordsApi.create({ appointmentId: recordFor.apptId, diagnosis, treatment, notes });
      await appointmentsApi.setStatus(recordFor.apptId, 'COMPLETED');
      toast.success('Запись в медкарте создана');
      setRecordFor(null); setDiagnosis(''); setTreatment(''); setNotes('');
      qc.invalidateQueries({ queryKey: ['d-appointments'] });
      qc.invalidateQueries({ queryKey: ['d-records'] });
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Ошибка');
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <PageHeader title="Записи на приём" subtitle="Подтверждайте, отменяйте и заполняйте медкарту." />
      {isLoading ? <Loader /> :
        (data?.content ?? []).length === 0 ? <EmptyState title="Записей нет" icon={<ClipboardList size={32} />} /> :
        <div className="card overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs uppercase text-slate-500 border-b">
                <th className="py-2 pr-3">Дата</th>
                <th className="py-2 pr-3">Пациент</th>
                <th className="py-2 pr-3">Заметка</th>
                <th className="py-2 pr-3">Статус</th>
                <th className="py-2"></th>
              </tr>
            </thead>
            <tbody>
              {data!.content.map((a) => (
                <tr key={a.id} className="border-b last:border-0 hover:bg-brand-50/40 transition">
                  <td className="py-2 pr-3 whitespace-nowrap">{formatDateTime(a.appointmentDate)}</td>
                  <td className="py-2 pr-3">{a.patientFullName}</td>
                  <td className="py-2 pr-3 text-slate-500">{a.notes ?? '—'}</td>
                  <td className="py-2 pr-3"><StatusBadge status={a.status} /></td>
                  <td className="py-2 text-right space-x-1 whitespace-nowrap">
                    {a.status === 'PLANNED' && <button className="btn-ghost text-xs px-2 py-1" onClick={() => setStatus(a.id, 'CONFIRMED')}>Подтвердить</button>}
                    {(a.status === 'PLANNED' || a.status === 'CONFIRMED') && <>
                      <button className="btn-primary text-xs px-2 py-1" onClick={() => setRecordFor({ apptId: a.id, patientName: a.patientFullName })}>Завершить</button>
                      <button className="btn-ghost text-xs px-2 py-1" onClick={() => setReschedTarget(a)}>Перенести</button>
                      <button className="btn-ghost text-xs px-2 py-1 text-rose-600" onClick={() => setStatus(a.id, 'CANCELLED')}>Отменить</button>
                    </>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>}

      <RescheduleModal appointment={reschedTarget} onClose={() => setReschedTarget(null)}
        onDone={() => { qc.invalidateQueries({ queryKey: ['d-appointments'] }); qc.invalidateQueries({ queryKey: ['d-upcoming'] }); }} />

      <AnimatePresence>
        {recordFor && (
          <motion.div className="fixed inset-0 bg-black/30 backdrop-blur-sm z-50 flex items-center justify-center p-4"
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            onClick={() => setRecordFor(null)}>
            <motion.div className="card w-full max-w-lg"
              initial={{ scale: 0.95 }} animate={{ scale: 1 }} exit={{ scale: 0.95 }}
              onClick={(e) => e.stopPropagation()}>
              <h3 className="text-lg font-bold mb-1">Завершение приёма</h3>
              <p className="text-sm text-slate-500 mb-3">Пациент: <b>{recordFor.patientName}</b></p>
              <label className="label">Диагноз</label>
              <input className="input mb-2" required value={diagnosis} onChange={(e) => setDiagnosis(e.target.value)} />
              <label className="label">Назначения</label>
              <textarea className="input mb-2" rows={3} value={treatment} onChange={(e) => setTreatment(e.target.value)} />
              <label className="label">Заметки</label>
              <textarea className="input mb-3" rows={2} value={notes} onChange={(e) => setNotes(e.target.value)} />
              <div className="flex justify-end gap-2">
                <button className="btn-ghost" onClick={() => setRecordFor(null)}>Отмена</button>
                <button className="btn-primary" disabled={!diagnosis || busy} onClick={submitRecord}>
                  {busy ? 'Сохраняем…' : 'Сохранить и завершить'}
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
