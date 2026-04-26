import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import { motion, AnimatePresence } from 'framer-motion';
import { CalendarPlus, Stethoscope } from 'lucide-react';
import toast from 'react-hot-toast';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import EmptyState from '@/components/EmptyState';
import { appointmentsApi, doctorsApi } from '@/api/endpoints';

export default function PatientDoctors() {
  const [filter, setFilter] = useState('');
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['doctors'], queryFn: () => doctorsApi.list(0, 100) });
  const list = (data?.content ?? []).filter(
    (d) => !filter || d.fullName.toLowerCase().includes(filter.toLowerCase())
      || d.specialization.toLowerCase().includes(filter.toLowerCase())
  );
  const [bookFor, setBookFor] = useState<number | null>(null);
  const [date, setDate] = useState('');
  const [notes, setNotes] = useState('');
  const [busy, setBusy] = useState(false);

  const submitBooking = async () => {
    if (!bookFor || !date) return;
    setBusy(true);
    try {
      await appointmentsApi.create({ doctorId: bookFor, appointmentDate: date, notes });
      toast.success('Запись создана');
      setBookFor(null); setDate(''); setNotes('');
      qc.invalidateQueries({ queryKey: ['p-upcoming'] });
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Не удалось создать запись');
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <PageHeader title="Врачи" subtitle="Найдите специалиста и запишитесь на удобное время." />
      <div className="card mb-4">
        <input className="input" placeholder="Поиск по ФИО или специализации…" value={filter} onChange={(e) => setFilter(e.target.value)} />
      </div>
      {isLoading ? <Loader /> :
        list.length === 0 ? <EmptyState title="Врачи не найдены" icon={<Stethoscope size={32} />} /> :
        <div className="grid md:grid-cols-2 xl:grid-cols-3 gap-4">
          {list.map((d, i) => (
            <motion.div
              key={d.id}
              initial={{ opacity: 0, y: 14 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3, delay: i * 0.04 }}
              whileHover={{ y: -2 }}
              className="card h-full flex flex-col"
            >
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 rounded-full bg-gradient-to-br from-brand-400 to-brand-700 flex items-center justify-center text-white font-bold">
                  {d.fullName.split(' ').map((p) => p[0]).slice(0, 2).join('')}
                </div>
                <div className="min-w-0">
                  <div className="font-semibold text-slate-900 truncate">{d.fullName}</div>
                  <div className="text-xs text-brand-700">{d.specialization}</div>
                </div>
              </div>
              {d.bio && <p className="text-sm text-slate-600 mt-3 line-clamp-3">{d.bio}</p>}
              <div className="flex items-center justify-between mt-3 text-xs text-slate-500">
                <span>Часы: {d.workStart?.slice(0, 5)}–{d.workEnd?.slice(0, 5)}</span>
                <span>Каб. {d.roomNumber ?? '—'}</span>
              </div>
              <button className="btn-primary mt-4" onClick={() => setBookFor(d.id)} disabled={!d.available}>
                <CalendarPlus size={16} />
                {d.available ? 'Записаться' : 'Недоступен'}
              </button>
            </motion.div>
          ))}
        </div>}

      <AnimatePresence>
        {bookFor !== null && (
          <motion.div
            className="fixed inset-0 bg-black/30 backdrop-blur-sm z-50 flex items-center justify-center p-4"
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }}
            onClick={() => setBookFor(null)}
          >
            <motion.div
              className="card w-full max-w-md"
              initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }}
              onClick={(e) => e.stopPropagation()}
            >
              <h3 className="text-lg font-bold text-slate-900 mb-3">Запись на приём</h3>
              <label className="label">Дата и время</label>
              <input className="input mb-3" type="datetime-local" value={date} onChange={(e) => setDate(e.target.value)} />
              <label className="label">Примечание</label>
              <textarea className="input mb-3" rows={3} value={notes} onChange={(e) => setNotes(e.target.value)} />
              <div className="flex gap-2 justify-end">
                <button className="btn-ghost" onClick={() => setBookFor(null)}>Отмена</button>
                <button className="btn-primary" disabled={!date || busy} onClick={submitBooking}>
                  {busy ? 'Сохраняем…' : 'Подтвердить'}
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
