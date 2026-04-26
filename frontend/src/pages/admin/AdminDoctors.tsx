import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { Pencil, Plus, Trash2 } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import EmptyState from '@/components/EmptyState';
import { doctorsApi } from '@/api/endpoints';
import type { DoctorResponse } from '@/types';

interface CreateForm {
  username: string; email: string; password: string; fullName: string;
  phone: string; specialization: string; bio: string; roomNumber: string;
  workStart: string; workEnd: string;
}

const emptyCreate: CreateForm = {
  username: '', email: '', password: '', fullName: '', phone: '',
  specialization: '', bio: '', roomNumber: '',
  workStart: '09:00', workEnd: '18:00',
};

interface EditForm {
  specialization: string; bio: string; photoUrl: string;
  available: boolean; workStart: string; workEnd: string; roomNumber: string;
}

export default function AdminDoctors() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['a-doctors'], queryFn: () => doctorsApi.list(0, 200) });

  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<CreateForm>(emptyCreate);
  const [editing, setEditing] = useState<DoctorResponse | null>(null);
  const [editForm, setEditForm] = useState<EditForm | null>(null);

  const create = useMutation({
    mutationFn: () =>
      doctorsApi.create({
        username: form.username,
        email: form.email,
        password: form.password,
        fullName: form.fullName,
        phone: form.phone || null,
        specialization: form.specialization,
        bio: form.bio || null,
        roomNumber: form.roomNumber || null,
        workStart: form.workStart || null,
        workEnd: form.workEnd || null,
        available: true,
      }),
    onSuccess: () => {
      toast.success('Врач добавлен');
      setForm(emptyCreate);
      setOpen(false);
      qc.invalidateQueries({ queryKey: ['a-doctors'] });
      qc.invalidateQueries({ queryKey: ['doctors'] });
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Ошибка'),
  });

  const update = useMutation({
    mutationFn: () =>
      doctorsApi.update(editing!.id, {
        specialization: editForm!.specialization,
        bio: editForm!.bio || null,
        photoUrl: editForm!.photoUrl || null,
        available: editForm!.available,
        workStart: editForm!.workStart,
        workEnd: editForm!.workEnd,
        roomNumber: editForm!.roomNumber || null,
      }),
    onSuccess: () => {
      toast.success('Сохранено');
      setEditing(null);
      setEditForm(null);
      qc.invalidateQueries({ queryKey: ['a-doctors'] });
      qc.invalidateQueries({ queryKey: ['doctors'] });
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Ошибка'),
  });

  const remove = async (id: number) => {
    if (!confirm('Удалить врача?')) return;
    try {
      await doctorsApi.delete(id);
      toast.success('Удалено');
      qc.invalidateQueries({ queryKey: ['a-doctors'] });
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Ошибка');
    }
  };

  const startEdit = (d: DoctorResponse) => {
    setEditing(d);
    setEditForm({
      specialization: d.specialization,
      bio: d.bio ?? '',
      photoUrl: d.photoUrl ?? '',
      available: d.available,
      workStart: (d.workStart ?? '09:00:00').slice(0, 5),
      workEnd: (d.workEnd ?? '18:00:00').slice(0, 5),
      roomNumber: d.roomNumber ?? '',
    });
  };

  return (
    <>
      <PageHeader
        title="Врачи"
        subtitle="Управление врачами и специализациями. Создавать врачей могут только администраторы."
        actions={
          <button className="btn-primary" onClick={() => setOpen(true)}>
            <Plus size={16} /> Добавить
          </button>
        }
      />

      {isLoading ? (
        <Loader />
      ) : !data || data.content.length === 0 ? (
        <EmptyState
          title="Нет данных по врачам"
          description="Добавьте первого врача, чтобы он появился в расписаниях и поиске пациентов."
        />
      ) : (
        <div className="card overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs uppercase text-slate-500 border-b">
                <th className="py-2 pr-3">ФИО</th>
                <th className="py-2 pr-3">Логин</th>
                <th className="py-2 pr-3">Специализация</th>
                <th className="py-2 pr-3">Часы</th>
                <th className="py-2 pr-3">Каб.</th>
                <th className="py-2 pr-3">Статус</th>
                <th className="py-2"></th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((d) => (
                <tr key={d.id} className="border-b last:border-0 hover:bg-brand-50/40">
                  <td className="py-2 pr-3 font-medium">{d.fullName}</td>
                  <td className="py-2 pr-3 text-slate-500">{d.username}</td>
                  <td className="py-2 pr-3">{d.specialization}</td>
                  <td className="py-2 pr-3">
                    {d.workStart?.slice(0, 5) ?? '—'}–{d.workEnd?.slice(0, 5) ?? '—'}
                  </td>
                  <td className="py-2 pr-3">{d.roomNumber ?? '—'}</td>
                  <td className="py-2 pr-3">
                    {d.available ? <span className="chip">принимает</span> : <span className="chip bg-slate-100 text-slate-500 border-slate-200">выходной</span>}
                  </td>
                  <td className="py-2 text-right whitespace-nowrap">
                    <button className="btn-ghost text-xs px-2 py-1 mr-1" onClick={() => startEdit(d)}>
                      <Pencil size={14} />
                    </button>
                    <button
                      className="btn-ghost text-xs px-2 py-1 text-rose-600 border-rose-200 hover:bg-rose-50"
                      onClick={() => remove(d.id)}
                    >
                      <Trash2 size={14} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <AnimatePresence>
        {open && (
          <motion.div
            className="fixed inset-0 bg-black/30 backdrop-blur-sm z-50 flex items-center justify-center p-4"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setOpen(false)}
          >
            <motion.div
              className="card w-full max-w-2xl"
              initial={{ scale: 0.95 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0.95 }}
              onClick={(e) => e.stopPropagation()}
            >
              <h3 className="text-lg font-bold mb-3">Новый врач</h3>
              <div className="grid md:grid-cols-2 gap-3">
                <div><label className="label">Логин</label><input className="input" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} /></div>
                <div><label className="label">Email</label><input className="input" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
                <div><label className="label">Пароль</label><input className="input" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></div>
                <div><label className="label">ФИО</label><input className="input" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} /></div>
                <div><label className="label">Телефон</label><input className="input" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} /></div>
                <div><label className="label">Специализация</label><input className="input" value={form.specialization} onChange={(e) => setForm({ ...form, specialization: e.target.value })} placeholder="Терапевт" /></div>
                <div><label className="label">Кабинет</label><input className="input" value={form.roomNumber} onChange={(e) => setForm({ ...form, roomNumber: e.target.value })} /></div>
                <div className="grid grid-cols-2 gap-2">
                  <div><label className="label">Начало</label><input className="input" type="time" value={form.workStart} onChange={(e) => setForm({ ...form, workStart: e.target.value })} /></div>
                  <div><label className="label">Конец</label><input className="input" type="time" value={form.workEnd} onChange={(e) => setForm({ ...form, workEnd: e.target.value })} /></div>
                </div>
                <div className="md:col-span-2"><label className="label">О враче</label><textarea className="input" rows={2} value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} /></div>
              </div>
              <div className="flex justify-end gap-2 mt-4">
                <button className="btn-ghost" onClick={() => setOpen(false)}>Отмена</button>
                <button
                  className="btn-primary"
                  disabled={
                    !form.username || !form.email || !form.password ||
                    !form.fullName || !form.specialization || create.isPending
                  }
                  onClick={() => create.mutate()}
                >
                  {create.isPending ? 'Сохраняем…' : 'Создать'}
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}

        {editing && editForm && (
          <motion.div
            className="fixed inset-0 bg-black/30 backdrop-blur-sm z-50 flex items-center justify-center p-4"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setEditing(null)}
          >
            <motion.div
              className="card w-full max-w-2xl"
              initial={{ scale: 0.95 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0.95 }}
              onClick={(e) => e.stopPropagation()}
            >
              <h3 className="text-lg font-bold mb-3">
                Редактирование врача: <span className="text-brand-700">{editing.fullName}</span>
              </h3>
              <div className="grid md:grid-cols-2 gap-3">
                <div><label className="label">Специализация</label><input className="input" value={editForm.specialization} onChange={(e) => setEditForm({ ...editForm, specialization: e.target.value })} /></div>
                <div><label className="label">Кабинет</label><input className="input" value={editForm.roomNumber} onChange={(e) => setEditForm({ ...editForm, roomNumber: e.target.value })} /></div>
                <div><label className="label">Начало</label><input className="input" type="time" value={editForm.workStart} onChange={(e) => setEditForm({ ...editForm, workStart: e.target.value })} /></div>
                <div><label className="label">Конец</label><input className="input" type="time" value={editForm.workEnd} onChange={(e) => setEditForm({ ...editForm, workEnd: e.target.value })} /></div>
                <div className="md:col-span-2"><label className="label">Фото (URL)</label><input className="input" value={editForm.photoUrl} onChange={(e) => setEditForm({ ...editForm, photoUrl: e.target.value })} /></div>
                <div className="md:col-span-2"><label className="label">О враче</label><textarea className="input" rows={2} value={editForm.bio} onChange={(e) => setEditForm({ ...editForm, bio: e.target.value })} /></div>
                <label className="md:col-span-2 flex items-center gap-2 text-sm">
                  <input type="checkbox" checked={editForm.available} onChange={(e) => setEditForm({ ...editForm, available: e.target.checked })} />
                  Принимает пациентов
                </label>
              </div>
              <div className="flex justify-end gap-2 mt-4">
                <button className="btn-ghost" onClick={() => setEditing(null)}>Отмена</button>
                <button className="btn-primary" disabled={update.isPending} onClick={() => update.mutate()}>
                  {update.isPending ? 'Сохраняем…' : 'Сохранить'}
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
