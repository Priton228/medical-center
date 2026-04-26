import { useEffect, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import { doctorsApi } from '@/api/endpoints';

export default function DoctorSchedule() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['d-me'], queryFn: () => doctorsApi.me() });
  const [start, setStart] = useState('09:00');
  const [end, setEnd] = useState('18:00');
  const [available, setAvailable] = useState(true);

  useEffect(() => {
    if (data) {
      setStart(data.workStart?.slice(0, 5) ?? '09:00');
      setEnd(data.workEnd?.slice(0, 5) ?? '18:00');
      setAvailable(data.available);
    }
  }, [data]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await doctorsApi.updateSchedule({ workStart: start + ':00', workEnd: end + ':00', available });
      toast.success('Расписание обновлено');
      qc.invalidateQueries({ queryKey: ['d-me'] });
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Ошибка');
    }
  };

  return (
    <>
      <PageHeader title="Расписание" subtitle="Управление часами приёма и доступностью." />
      {isLoading ? <Loader /> : (
        <form onSubmit={submit} className="card max-w-lg space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div><label className="label">Начало приёма</label><input className="input" type="time" value={start} onChange={(e) => setStart(e.target.value)} required /></div>
            <div><label className="label">Конец приёма</label><input className="input" type="time" value={end} onChange={(e) => setEnd(e.target.value)} required /></div>
          </div>
          <label className="inline-flex items-center gap-2 select-none">
            <input type="checkbox" checked={available} onChange={(e) => setAvailable(e.target.checked)} />
            <span>Принимаю пациентов</span>
          </label>
          <button className="btn-primary">Сохранить</button>
        </form>
      )}
    </>
  );
}
