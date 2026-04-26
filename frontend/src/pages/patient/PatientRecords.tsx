import { useQuery } from '@tanstack/react-query';
import { FileHeart } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import EmptyState from '@/components/EmptyState';
import { recordsApi } from '@/api/endpoints';
import { formatDateTime } from '@/utils/format';

export default function PatientRecords() {
  const { data, isLoading } = useQuery({ queryKey: ['p-records'], queryFn: () => recordsApi.mine() });
  return (
    <>
      <PageHeader title="Медкарта" subtitle="История ваших приёмов и назначений." />
      {isLoading ? <Loader /> :
        (data ?? []).length === 0 ? <EmptyState title="Записей в медкарте пока нет" icon={<FileHeart size={32} />} /> :
        <div className="space-y-3">
          {data!.map((r) => (
            <div key={r.id} className="card">
              <div className="flex flex-wrap items-baseline justify-between gap-2">
                <div className="font-semibold text-slate-900">{r.diagnosis}</div>
                <div className="text-xs text-slate-500">{formatDateTime(r.createdAt)}</div>
              </div>
              <div className="text-sm text-slate-500 mt-1">Врач: {r.doctorFullName}</div>
              {r.treatment && <div className="mt-2"><span className="text-xs uppercase text-slate-400">Лечение</span><div className="text-sm">{r.treatment}</div></div>}
              {r.notes && <div className="mt-2"><span className="text-xs uppercase text-slate-400">Комментарий</span><div className="text-sm">{r.notes}</div></div>}
            </div>
          ))}
        </div>}
    </>
  );
}
