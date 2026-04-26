import { useEffect, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import AvatarUploader from '@/components/AvatarUploader';
import { authApi, usersApi } from '@/api/endpoints';
import { useAuth } from '@/hooks/useAuth';

export default function AdminProfile() {
  const qc = useQueryClient();
  const { user } = useAuth();
  const { data, isLoading } = useQuery({ queryKey: ['admin-me'], queryFn: () => authApi.me() });

  const [contacts, setContacts] = useState({ email: '', fullName: '', phone: '' });
  const [pwd, setPwd] = useState({ oldPassword: '', newPassword: '' });

  useEffect(() => {
    if (data) setContacts({ email: data.email ?? '', fullName: data.fullName ?? '', phone: data.phone ?? '' });
  }, [data]);

  const saveContacts = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await usersApi.updateMe({ email: contacts.email, fullName: contacts.fullName, phone: contacts.phone || undefined });
      toast.success('Контактные данные обновлены');
      qc.invalidateQueries({ queryKey: ['admin-me'] });
    } catch (e: any) { toast.error(e?.response?.data?.message || 'Ошибка'); }
  };

  const changePassword = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await usersApi.changePassword(pwd.oldPassword, pwd.newPassword);
      setPwd({ oldPassword: '', newPassword: '' });
      toast.success('Пароль изменён');
    } catch (e: any) { toast.error(e?.response?.data?.message || 'Ошибка'); }
  };

  return (
    <>
      <PageHeader title="Профиль администратора" subtitle="Контактные данные и пароль." />
      {isLoading || !data ? <Loader /> : (
        <div className="space-y-4 max-w-3xl">
          <div className="card">
            <h3 className="text-base font-semibold text-slate-700 mb-4">Аватар</h3>
            <AvatarUploader avatarUrl={user?.avatarUrl ?? data.avatarUrl} fullName={data.fullName}
              onChanged={() => qc.invalidateQueries({ queryKey: ['admin-me'] })} />
          </div>

          <form onSubmit={saveContacts} className="card grid md:grid-cols-2 gap-3">
            <h3 className="md:col-span-2 text-base font-semibold text-slate-700">Контактные данные</h3>
            <div><label className="label">Логин</label><input className="input" value={data.login} disabled /></div>
            <div><label className="label">ФИО</label><input className="input" value={contacts.fullName} onChange={(e) => setContacts({ ...contacts, fullName: e.target.value })} /></div>
            <div><label className="label">Email</label><input className="input" type="email" value={contacts.email} onChange={(e) => setContacts({ ...contacts, email: e.target.value })} /></div>
            <div><label className="label">Телефон</label><input className="input" value={contacts.phone} onChange={(e) => setContacts({ ...contacts, phone: e.target.value })} /></div>
            <div className="md:col-span-2 flex justify-end"><button className="btn-primary">Сохранить</button></div>
          </form>

          <form onSubmit={changePassword} className="card grid md:grid-cols-2 gap-3">
            <h3 className="md:col-span-2 text-base font-semibold text-slate-700">Смена пароля</h3>
            <div><label className="label">Старый пароль</label><input className="input" type="password" value={pwd.oldPassword} onChange={(e) => setPwd({ ...pwd, oldPassword: e.target.value })} /></div>
            <div><label className="label">Новый пароль</label><input className="input" type="password" minLength={6} value={pwd.newPassword} onChange={(e) => setPwd({ ...pwd, newPassword: e.target.value })} /></div>
            <div className="md:col-span-2 flex justify-end"><button className="btn-primary">Изменить</button></div>
          </form>
        </div>
      )}
    </>
  );
}
