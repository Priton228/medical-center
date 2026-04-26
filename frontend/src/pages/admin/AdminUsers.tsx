import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { motion, AnimatePresence } from 'framer-motion';
import toast from 'react-hot-toast';
import { Pencil, Plus, Trash2, UserPlus } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import EmptyState from '@/components/EmptyState';
import { adminUsersApi, usersApi } from '@/api/endpoints';
import type { Role, UserResponse } from '@/types';
import { roleLabel } from '@/utils/format';

const ALL_ROLES: { value: Role; label: string }[] = [
  { value: 'ROLE_PATIENT', label: 'Пациент' },
  { value: 'ROLE_DOCTOR', label: 'Врач' },
  { value: 'ROLE_ADMIN', label: 'Администратор' },
];

interface FormState {
  username: string;
  email: string;
  password: string;
  fullName: string;
  phone: string;
  roles: Set<Role>;
}

const empty: FormState = {
  username: '',
  email: '',
  password: '',
  fullName: '',
  phone: '',
  roles: new Set<Role>(['ROLE_PATIENT']),
};

export default function AdminUsers() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['a-users'], queryFn: () => usersApi.list(0, 200) });

  const [createOpen, setCreateOpen] = useState(false);
  const [editing, setEditing] = useState<UserResponse | null>(null);
  const [form, setForm] = useState<FormState>(empty);
  const [editForm, setEditForm] = useState<FormState>(empty);
  const [editPwd, setEditPwd] = useState('');

  const createM = useMutation({
    mutationFn: () =>
      adminUsersApi.create({
        username: form.username,
        email: form.email,
        password: form.password,
        fullName: form.fullName,
        phone: form.phone || null,
        roles: Array.from(form.roles),
      }),
    onSuccess: () => {
      toast.success('Пользователь создан');
      setForm(empty);
      setCreateOpen(false);
      qc.invalidateQueries({ queryKey: ['a-users'] });
      qc.invalidateQueries({ queryKey: ['a-doctors'] });
      qc.invalidateQueries({ queryKey: ['a-patients'] });
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Ошибка создания'),
  });

  const updateM = useMutation({
    mutationFn: () =>
      adminUsersApi.update(editing!.id, {
        email: editForm.email,
        fullName: editForm.fullName,
        phone: editForm.phone || null,
        roles: Array.from(editForm.roles),
        newPassword: editPwd ? editPwd : null,
      }),
    onSuccess: () => {
      toast.success('Пользователь обновлён');
      setEditing(null);
      setEditPwd('');
      qc.invalidateQueries({ queryKey: ['a-users'] });
      qc.invalidateQueries({ queryKey: ['a-doctors'] });
      qc.invalidateQueries({ queryKey: ['a-patients'] });
    },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Ошибка обновления'),
  });

  const toggle = async (id: number, enabled: boolean) => {
    try {
      await usersApi.setEnabled(id, enabled);
      toast.success('Статус обновлён');
      qc.invalidateQueries({ queryKey: ['a-users'] });
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Ошибка');
    }
  };

  const remove = async (id: number) => {
    if (!confirm('Удалить пользователя? Это действие необратимо.')) return;
    try {
      await adminUsersApi.delete(id);
      toast.success('Пользователь удалён');
      qc.invalidateQueries({ queryKey: ['a-users'] });
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Ошибка удаления');
    }
  };

  const startEdit = (u: UserResponse) => {
    setEditing(u);
    setEditForm({
      username: u.username,
      email: u.email,
      password: '',
      fullName: u.fullName,
      phone: u.phone ?? '',
      roles: new Set<Role>(u.roles),
    });
    setEditPwd('');
  };

  const toggleFormRole = (r: Role, target: 'create' | 'edit') => {
    if (target === 'create') {
      const next = new Set(form.roles);
      next.has(r) ? next.delete(r) : next.add(r);
      setForm({ ...form, roles: next });
    } else {
      const next = new Set(editForm.roles);
      next.has(r) ? next.delete(r) : next.add(r);
      setEditForm({ ...editForm, roles: next });
    }
  };

  return (
    <>
      <PageHeader
        title="Пользователи"
        subtitle="Создание учётных записей и управление ролями (пациент / врач / администратор)."
        actions={
          <button className="btn-primary" onClick={() => setCreateOpen(true)}>
            <UserPlus size={16} /> Добавить
          </button>
        }
      />

      {isLoading ? (
        <Loader />
      ) : !data || data.content.length === 0 ? (
        <EmptyState
          title="Нет данных по пользователям"
          description="Добавьте первого пользователя — пациента, врача или администратора."
        />
      ) : (
        <div className="card overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-xs uppercase text-slate-500 border-b">
                <th className="py-2 pr-3">ID</th>
                <th className="py-2 pr-3">Логин</th>
                <th className="py-2 pr-3">ФИО</th>
                <th className="py-2 pr-3">Email</th>
                <th className="py-2 pr-3">Роли</th>
                <th className="py-2 pr-3">Статус</th>
                <th className="py-2"></th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((u) => (
                <tr key={u.id} className="border-b last:border-0 hover:bg-brand-50/40">
                  <td className="py-2 pr-3">{u.id}</td>
                  <td className="py-2 pr-3 font-medium">{u.username}</td>
                  <td className="py-2 pr-3">{u.fullName}</td>
                  <td className="py-2 pr-3 text-slate-500">{u.email}</td>
                  <td className="py-2 pr-3">
                    {u.roles.map((r) => (
                      <span key={r} className="chip mr-1">
                        {roleLabel(r)}
                      </span>
                    ))}
                  </td>
                  <td className="py-2 pr-3">
                    {u.enabled ? (
                      <span className="chip">активен</span>
                    ) : (
                      <span className="chip bg-rose-50 text-rose-700 border-rose-100">отключён</span>
                    )}
                  </td>
                  <td className="py-2 text-right whitespace-nowrap">
                    <button className="btn-ghost text-xs px-2 py-1 mr-1" onClick={() => startEdit(u)}>
                      <Pencil size={14} />
                    </button>
                    <button className="btn-ghost text-xs px-2 py-1 mr-1" onClick={() => toggle(u.id, !u.enabled)}>
                      {u.enabled ? 'Отключить' : 'Включить'}
                    </button>
                    <button
                      className="btn-ghost text-xs px-2 py-1 text-rose-600 border-rose-200 hover:bg-rose-50"
                      onClick={() => remove(u.id)}
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
        {createOpen && (
          <motion.div
            className="fixed inset-0 bg-black/30 backdrop-blur-sm z-50 flex items-center justify-center p-4"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setCreateOpen(false)}
          >
            <motion.div
              className="card w-full max-w-2xl"
              initial={{ scale: 0.95 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0.95 }}
              onClick={(e) => e.stopPropagation()}
            >
              <h3 className="text-lg font-bold mb-3">Новый пользователь</h3>
              <div className="grid md:grid-cols-2 gap-3">
                <div>
                  <label className="label">Логин</label>
                  <input className="input" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
                </div>
                <div>
                  <label className="label">Email</label>
                  <input className="input" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
                </div>
                <div>
                  <label className="label">Пароль</label>
                  <input className="input" type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
                </div>
                <div>
                  <label className="label">ФИО</label>
                  <input className="input" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
                </div>
                <div>
                  <label className="label">Телефон</label>
                  <input className="input" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
                </div>
                <div>
                  <label className="label">Роли</label>
                  <div className="flex flex-wrap gap-2 pt-2">
                    {ALL_ROLES.map((r) => {
                      const active = form.roles.has(r.value);
                      return (
                        <button
                          key={r.value}
                          type="button"
                          onClick={() => toggleFormRole(r.value, 'create')}
                          className={
                            'px-3 py-1 rounded-full border text-xs transition ' +
                            (active
                              ? 'bg-brand-600 text-white border-transparent'
                              : 'bg-white text-slate-700 border-brand-200 hover:bg-brand-50')
                          }
                        >
                          {r.label}
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>
              <div className="flex justify-end gap-2 mt-4">
                <button className="btn-ghost" onClick={() => setCreateOpen(false)}>Отмена</button>
                <button
                  className="btn-primary"
                  disabled={
                    !form.username ||
                    !form.email ||
                    !form.password ||
                    !form.fullName ||
                    form.roles.size === 0 ||
                    createM.isPending
                  }
                  onClick={() => createM.mutate()}
                >
                  <Plus size={16} /> {createM.isPending ? 'Сохраняем…' : 'Создать'}
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}

        {editing && (
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
                Редактирование: <span className="text-brand-700">{editing.username}</span>
              </h3>
              <div className="grid md:grid-cols-2 gap-3">
                <div>
                  <label className="label">Email</label>
                  <input className="input" type="email" value={editForm.email} onChange={(e) => setEditForm({ ...editForm, email: e.target.value })} />
                </div>
                <div>
                  <label className="label">ФИО</label>
                  <input className="input" value={editForm.fullName} onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })} />
                </div>
                <div>
                  <label className="label">Телефон</label>
                  <input className="input" value={editForm.phone} onChange={(e) => setEditForm({ ...editForm, phone: e.target.value })} />
                </div>
                <div>
                  <label className="label">Новый пароль (необязательно)</label>
                  <input className="input" type="password" value={editPwd} onChange={(e) => setEditPwd(e.target.value)} placeholder="Оставьте пустым, чтобы не менять" />
                </div>
                <div className="md:col-span-2">
                  <label className="label">Роли</label>
                  <div className="flex flex-wrap gap-2 pt-2">
                    {ALL_ROLES.map((r) => {
                      const active = editForm.roles.has(r.value);
                      return (
                        <button
                          key={r.value}
                          type="button"
                          onClick={() => toggleFormRole(r.value, 'edit')}
                          className={
                            'px-3 py-1 rounded-full border text-xs transition ' +
                            (active
                              ? 'bg-brand-600 text-white border-transparent'
                              : 'bg-white text-slate-700 border-brand-200 hover:bg-brand-50')
                          }
                        >
                          {r.label}
                        </button>
                      );
                    })}
                  </div>
                </div>
              </div>
              <div className="flex justify-end gap-2 mt-4">
                <button className="btn-ghost" onClick={() => setEditing(null)}>Отмена</button>
                <button
                  className="btn-primary"
                  disabled={!editForm.email || !editForm.fullName || editForm.roles.size === 0 || updateM.isPending}
                  onClick={() => updateM.mutate()}
                >
                  {updateM.isPending ? 'Сохраняем…' : 'Сохранить'}
                </button>
              </div>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </>
  );
}
