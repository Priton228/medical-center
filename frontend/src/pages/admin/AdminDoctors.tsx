import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { Plus, Trash2 } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import { doctorsApi } from '@/api/endpoints';

interface FormState {
  username: string; email: string; password: string; fullName: string;
  phone: string; specialization: string; bio: string; roomNumber: string;
}

const empty: FormState = { username: '', email: '', password: '', fullName: '', phone: '', specialization: '', bio: '', roomNumber: '' };

export default function AdminDoctors() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['a-doctors'], queryFn: () => doctorsApi.list(0, 100) });
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<FormState>(empty);

  const create = useMutation({
    mutationFn: () => doctorsApi.create(form),
    onSuccess: () => {
      toast.success('Врач добавлен');
      setForm(empty); setOpen(false);
      qc.invalidateQueries({ queryKey: ['a-doctors'] });
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

  return (
    <>
      <PageHeader title="Врачи" subtitle="Управление врачами и специализациями." actions={
        <button className="btn-primary" onClick={() => setOpen(true)}><Plus size={16} /> Добавить</button>
      } />

      {isLoading ? <Loader /> : (
        <div className="card overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs uppercase text-slate-500 border-b">
                <th className="py-2 pr-3">ФИО</th>
                <th className="py-2 pr-3">Логин</th>
                <th className="py-2 pr-3">Специализация</th>
                <th className="py-2 pr-3">Часы</th>
                <th className="py-2 pr-3">Каб.</th>
                <th className="py-2"></th>
              </tr>
            </thead>
            <tbody>
              {data!.content.map((d) => (
                <tr key={d.id} className="border-b last:border-0 hover:bg-brand-50/40">
                  <td className="py-2 pr-3 font-medium">{d.fullName}</td>
                  <td className="py-2 pr-3 text-slate-500">{d.username}</td>
                  <td className="py-2 pr-3">{d.specialization}</td>
                  <td className="py-2 pr-3">{d.workStart?.slice(0, 5)}–{d.workEnd?.slice(0, 5)}</td>
                  <td className="py-2 pr-3">{d.roomNumber ?? '—'}</td>
                  <td className="py-2 text-right">
                    <button className="btn-ghost text-xs px-2 py-1 text-rose-600 border-rose-200 hover:bg-rose-50" onClick={() => remove(d.id)}><Trash2 size={14} /></button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <AnimatePresence>
        {open && (
          <motion.div className="fixed inset-0 bg-black/30 backdrop-blur-sm z-50 flex items-center justify-center p-4"
            initial={{ opacity: 0 }} animate={{ opacity: 1 }} exit={{ opacity: 0 }} onClick={() => setOpen(false)}>
            <motion.div className="card w-full max-w-2xl" initial={{ scale: 0.95 }} animate={{ scale: 1 }} exit={{ scale: 0.95 }} onClick={(e) => e.stopPropagation()}>
              <h3 className="text-lg font-bold mb-3">Новый врач</h3>
              <div className="grid md:grid-cols-2 gap-3">
                <div><label className="label">Логин</label><input className="input" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} /></div>
                <div><label className="label">Email</label><input className="input" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
                <div><label className="label">Пароль</label><input className="input" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></div>
                <div><label className="label">ФИО</label><input className="input" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} /></div>
                <div><label className="label">Телефон</label><input className="input" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} /></div>
                <div><label className="label">Специализация</label><input className="input" value={form.specialization} onChange={(e) => setForm({ ...form, specialization: e.target.value })} /></div>
                <div><label className="label">Кабинет</label><input className="input" value={form.roomNumber} onChange={(e) => setForm({ ...form, roomNumber: e.target.value })} /></div>
                <div className="md:col-span-2"><label className="label">О враче</label><textarea className="input" rows={2} value={form.bio} onChange={(e) => setForm({ ...form, bio: e.target.value })} /></div>
              </div>
              <div className="flex justify-end gap-2 mt-4">
                <button className="btn-ghost" onClick={() => setOpen(false)}>Отмена</button>
                <button className="btn-primary" disabled={create.isPending} onClick={() => create.mutate()}>
                  {create.isPending ? 'Сохраняем…' : 'Создать'}
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
