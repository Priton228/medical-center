import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { Pencil, Plus, Save, Trash2, X } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import { symptomsApi } from '@/api/endpoints';

export default function AdminSymptoms() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['a-symptoms'], queryFn: () => symptomsApi.list() });
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [editing, setEditing] = useState<{ id: number; name: string; description: string } | null>(null);

  const refresh = () => {
    qc.invalidateQueries({ queryKey: ['a-symptoms'] });
    qc.invalidateQueries({ queryKey: ['symptoms'] });
  };

  const add = useMutation({
    mutationFn: () => symptomsApi.create({ name, description }),
    onSuccess: () => { toast.success('Симптом добавлен'); setName(''); setDescription(''); refresh(); },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Ошибка'),
  });
  const remove = async (id: number) => {
    if (!confirm('Удалить симптом?')) return;
    try {
      await symptomsApi.delete(id);
      toast.success('Симптом удалён');
      refresh();
    } catch (e: any) { toast.error(e?.response?.data?.message || 'Ошибка'); }
  };
  const save = async () => {
    if (!editing) return;
    try {
      await symptomsApi.update(editing.id, { name: editing.name, description: editing.description });
      toast.success('Симптом обновлён');
      setEditing(null);
      refresh();
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
      {isLoading ? <Loader /> : (
        <div className="card divide-y divide-brand-50">
          {data!.map((s) => editing?.id === s.id ? (
            <div key={s.id} className="grid md:grid-cols-3 gap-2 py-2 items-center">
              <input className="input" value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} />
              <input className="input" value={editing.description} onChange={(e) => setEditing({ ...editing, description: e.target.value })} placeholder="Описание" />
              <div className="flex justify-end gap-1">
                <button className="btn-primary text-xs px-2 py-1" onClick={save}><Save size={14} /> Сохранить</button>
                <button className="btn-ghost text-xs px-2 py-1" onClick={() => setEditing(null)}><X size={14} /></button>
              </div>
            </div>
          ) : (
            <div key={s.id} className="flex items-center justify-between py-2">
              <div>
                <div className="font-medium text-slate-900">{s.name}</div>
                {s.description && <div className="text-xs text-slate-500">{s.description}</div>}
              </div>
              <div className="flex gap-1">
                <button className="btn-ghost text-xs px-2 py-1" onClick={() => setEditing({ id: s.id, name: s.name, description: s.description ?? '' })}>
                  <Pencil size={14} />
                </button>
                <button className="btn-ghost text-xs px-2 py-1 text-rose-600 border-rose-200 hover:bg-rose-50" onClick={() => remove(s.id)}><Trash2 size={14} /></button>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
