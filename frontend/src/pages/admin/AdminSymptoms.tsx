import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { ListChecks, Plus, Trash2 } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import EmptyState from '@/components/EmptyState';
import { symptomsApi } from '@/api/endpoints';

export default function AdminSymptoms() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['a-symptoms'], queryFn: () => symptomsApi.list() });
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');

  const add = useMutation({
    mutationFn: () => symptomsApi.create({ name, description }),
    onSuccess: () => { toast.success('Симптом добавлен'); setName(''); setDescription(''); qc.invalidateQueries({ queryKey: ['a-symptoms'] }); qc.invalidateQueries({ queryKey: ['symptoms'] }); },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Ошибка'),
  });
  const remove = async (id: number) => {
    if (!confirm('Удалить симптом?')) return;
    try {
      await symptomsApi.delete(id);
      qc.invalidateQueries({ queryKey: ['a-symptoms'] });
    } catch (e: any) { toast.error(e?.response?.data?.message || 'Ошибка'); }
  };

  return (
    <>
      <PageHeader title="Симптомы" subtitle="Справочник симптомов системы рекомендаций." />
      <div className="card mb-4 grid md:grid-cols-3 gap-3">
        <input className="input" placeholder="Название" value={name} onChange={(e) => setName(e.target.value)} />
        <input className="input md:col-span-1" placeholder="Описание" value={description} onChange={(e) => setDescription(e.target.value)} />
        <button className="btn-primary" disabled={!name || add.isPending} onClick={() => add.mutate()}><Plus size={16} /> Добавить</button>
      </div>
      {isLoading ? (
        <Loader />
      ) : !data || data.length === 0 ? (
        <EmptyState
          title="Нет данных по симптомам"
          description="Добавьте первый симптом для использования в подсказках диагнозов."
          icon={<ListChecks size={32} />}
        />
      ) : (
        <div className="card divide-y divide-brand-50">
          {data.map((s) => (
            <div key={s.id} className="flex items-center justify-between py-2">
              <div>
                <div className="font-medium text-slate-900">{s.name}</div>
                {s.description && <div className="text-xs text-slate-500">{s.description}</div>}
              </div>
              <button className="btn-ghost text-xs px-2 py-1 text-rose-600 border-rose-200 hover:bg-rose-50" onClick={() => remove(s.id)}><Trash2 size={14} /></button>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
