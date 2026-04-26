import { useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { CalendarX } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import EmptyState from '@/components/EmptyState';
import StatusBadge from '@/components/StatusBadge';
import RescheduleModal from '@/components/RescheduleModal';
import { appointmentsApi } from '@/api/endpoints';
import { formatDateTime } from '@/utils/format';
import type { AppointmentResponse } from '@/types';

export default function PatientAppointments() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['p-appointments'], queryFn: () => appointmentsApi.listMine() });
  const [reschedTarget, setReschedTarget] = useState<AppointmentResponse | null>(null);

  const cancel = async (id: number) => {
    try {
      await appointmentsApi.setStatus(id, 'CANCELLED');
      toast.success('Запись отменена');
      qc.invalidateQueries({ queryKey: ['p-appointments'] });
      qc.invalidateQueries({ queryKey: ['p-upcoming'] });
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Ошибка');
    }
  };

  const refresh = () => {
    qc.invalidateQueries({ queryKey: ['p-appointments'] });
    qc.invalidateQueries({ queryKey: ['p-upcoming'] });
  };

  return (
    <>
      <PageHeader title="Мои записи" subtitle="История и предстоящие приёмы." />
      {isLoading ? <Loader /> :
        (data?.content ?? []).length === 0 ? <EmptyState title="Записей пока нет" icon={<CalendarX size={32} />} /> :
        <div className="card overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs uppercase text-slate-500 border-b">
                <th className="py-2 pr-3">Дата</th>
                <th className="py-2 pr-3">Врач</th>
                <th className="py-2 pr-3">Специализация</th>
                <th className="py-2 pr-3">Статус</th>
                <th className="py-2"></th>
              </tr>
            </thead>
            <tbody>
              {data!.content.map((a) => (
                <tr key={a.id} className="border-b last:border-0 hover:bg-brand-50/40 transition">
                  <td className="py-2 pr-3 whitespace-nowrap">{formatDateTime(a.appointmentDate)}</td>
                  <td className="py-2 pr-3">{a.doctorFullName}</td>
                  <td className="py-2 pr-3 text-slate-500">{a.doctorSpecialization}</td>
                  <td className="py-2 pr-3"><StatusBadge status={a.status} /></td>
                  <td className="py-2 text-right">
                    {(a.status === 'PLANNED' || a.status === 'CONFIRMED') && (
                      <div className="flex justify-end gap-1">
                        <button className="btn-ghost text-xs px-2 py-1" onClick={() => setReschedTarget(a)}>Перенести</button>
                        <button className="btn-ghost text-xs px-2 py-1 text-rose-600" onClick={() => cancel(a.id)}>Отменить</button>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>}
      <RescheduleModal appointment={reschedTarget} onClose={() => setReschedTarget(null)} onDone={refresh} />
    </>
  );
}
