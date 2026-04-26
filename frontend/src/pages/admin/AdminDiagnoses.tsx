import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import { Pencil, Plus, Save, Trash2, X } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import { diagnosesApi, symptomsApi } from '@/api/endpoints';
import type { DiagnosisResponse } from '@/types';

interface EditingState {
  id: number; name: string; description: string; specialization: string; symptomIds: Set<number>;
}

export default function AdminDiagnoses() {
  const qc = useQueryClient();
  const diagnoses = useQuery({ queryKey: ['a-diagnoses'], queryFn: () => diagnosesApi.list() });
  const symptoms = useQuery({ queryKey: ['a-symptoms'], queryFn: () => symptomsApi.list() });

  const [form, setForm] = useState({ name: '', description: '', specialization: '', symptomIds: new Set<number>() });
  const [editing, setEditing] = useState<EditingState | null>(null);

  const refresh = () => qc.invalidateQueries({ queryKey: ['a-diagnoses'] });

  const add = useMutation({
    mutationFn: () => diagnosesApi.create({
      name: form.name, description: form.description,
      specialization: form.specialization, symptomIds: Array.from(form.symptomIds),
    }),
    onSuccess: () => {
      toast.success('Диагноз добавлен');
      setForm({ name: '', description: '', specialization: '', symptomIds: new Set() });
      refresh();
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Ошибка'),
  });
  const remove = async (id: number) => {
    if (!confirm('Удалить диагноз?')) return;
    try { await diagnosesApi.delete(id); toast.success('Диагноз удалён'); refresh(); }
    catch (e: any) { toast.error(e?.response?.data?.message || 'Ошибка'); }
  };

  const startEdit = (d: DiagnosisResponse) => setEditing({
    id: d.id, name: d.name, description: d.description ?? '',
    specialization: d.specialization, symptomIds: new Set(d.symptomIds),
  });
  const save = async () => {
    if (!editing) return;
    try {
      await diagnosesApi.update(editing.id, {
        name: editing.name, description: editing.description,
        specialization: editing.specialization, symptomIds: Array.from(editing.symptomIds),
      });
      toast.success('Диагноз обновлён');
      setEditing(null);
      refresh();
    } catch (e: any) { toast.error(e?.response?.data?.message || 'Ошибка'); }
  };

  const toggle = (set: Set<number>, id: number) => {
    const n = new Set(set); n.has(id) ? n.delete(id) : n.add(id); return n;
  };
  const toggleSym = (id: number) => setForm({ ...form, symptomIds: toggle(form.symptomIds, id) });
  const toggleEditSym = (id: number) => editing && setEditing({ ...editing, symptomIds: toggle(editing.symptomIds, id) });

  return (
    <>
      <PageHeader title="Диагнозы" subtitle="Связь диагнозов с симптомами и специализацией." />
      <div className="card mb-4 space-y-3">
        <div className="grid md:grid-cols-3 gap-3">
          <input className="input" placeholder="Название диагноза" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <input className="input" placeholder="Специализация" value={form.specialization} onChange={(e) => setForm({ ...form, specialization: e.target.value })} />
          <input className="input" placeholder="Описание" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
        </div>
        <div>
          <div className="label">Связанные симптомы</div>
          <div className="flex flex-wrap gap-2">
            {(symptoms.data ?? []).map((s) => {
              const active = form.symptomIds.has(s.id);
              return (
                <button key={s.id} type="button" onClick={() => toggleSym(s.id)}
                  className={'px-3 py-1 rounded-full border text-xs transition ' +
                    (active ? 'bg-brand-600 text-white border-transparent' : 'bg-white text-slate-700 border-brand-200 hover:bg-brand-50')}>
                  {s.name}
                </button>
              );
            })}
          </div>
        </div>
        <div className="flex justify-end">
          <button className="btn-primary" disabled={!form.name || !form.specialization || add.isPending} onClick={() => add.mutate()}>
            <Plus size={16} /> Добавить
          </button>
        </div>
      </div>

      {diagnoses.isLoading ? <Loader /> : (
        <div className="card divide-y divide-brand-50">
          {diagnoses.data!.map((d) => editing?.id === d.id ? (
            <div key={d.id} className="py-3 space-y-2">
              <div className="grid md:grid-cols-3 gap-2">
                <input className="input" value={editing.name} onChange={(e) => setEditing({ ...editing, name: e.target.value })} />
                <input className="input" value={editing.specialization} onChange={(e) => setEditing({ ...editing, specialization: e.target.value })} />
                <input className="input" value={editing.description} onChange={(e) => setEditing({ ...editing, description: e.target.value })} />
              </div>
              <div className="flex flex-wrap gap-2">
                {(symptoms.data ?? []).map((s) => {
                  const active = editing.symptomIds.has(s.id);
                  return (
                    <button key={s.id} type="button" onClick={() => toggleEditSym(s.id)}
                      className={'px-3 py-1 rounded-full border text-xs transition ' +
                        (active ? 'bg-brand-600 text-white border-transparent' : 'bg-white text-slate-700 border-brand-200 hover:bg-brand-50')}>
                      {s.name}
                    </button>
                  );
                })}
              </div>
              <div className="flex justify-end gap-1">
                <button className="btn-primary text-xs px-2 py-1" onClick={save}><Save size={14} /> Сохранить</button>
                <button className="btn-ghost text-xs px-2 py-1" onClick={() => setEditing(null)}><X size={14} /></button>
              </div>
            </div>
          ) : (
            <div key={d.id} className="py-3 flex items-start justify-between gap-3">
              <div>
                <div className="font-medium text-slate-900">{d.name} <span className="chip ml-2">{d.specialization}</span></div>
                {d.description && <div className="text-sm text-slate-500">{d.description}</div>}
                <div className="mt-1 flex flex-wrap gap-1">
                  {d.symptomIds.map((sid) => (
                    <span key={sid} className="chip">{(symptoms.data ?? []).find((s) => s.id === sid)?.name ?? `#${sid}`}</span>
                  ))}
                </div>
              </div>
              <div className="flex gap-1">
                <button className="btn-ghost text-xs px-2 py-1" onClick={() => startEdit(d)}><Pencil size={14} /></button>
                <button className="btn-ghost text-xs px-2 py-1 text-rose-600 border-rose-200 hover:bg-rose-50" onClick={() => remove(d.id)}><Trash2 size={14} /></button>
              </div>
            </div>
          ))}
        </div>
      )}
    </>
  );
}
