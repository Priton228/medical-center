import { useQuery } from '@tanstack/react-query';
import { NotebookText } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import EmptyState from '@/components/EmptyState';
import { recordsApi } from '@/api/endpoints';
import { formatDateTime } from '@/utils/format';

export default function DoctorRecords() {
  const { data, isLoading } = useQuery({ queryKey: ['d-records'], queryFn: () => recordsApi.byDoctor() });
  return (
    <>
      <PageHeader title="Медкарты пациентов" subtitle="Записи, созданные на ваших приёмах." />
      {isLoading ? <Loader /> :
        (data ?? []).length === 0 ? <EmptyState title="Пока нет записей" icon={<NotebookText size={32} />} /> :
        <div className="space-y-3">
          {data!.map((r) => (
            <div key={r.id} className="card">
              <div className="flex flex-wrap items-baseline justify-between">
                <div className="font-semibold">{r.patientFullName}</div>
                <div className="text-xs text-slate-500">{formatDateTime(r.createdAt)}</div>
              </div>
              <div className="mt-1 text-sm"><b>Диагноз:</b> {r.diagnosis}</div>
              {r.treatment && <div className="text-sm"><b>Лечение:</b> {r.treatment}</div>}
              {r.notes && <div className="text-sm text-slate-500">Заметка: {r.notes}</div>}
            </div>
          ))}
        </div>}
    </>
  );
}
