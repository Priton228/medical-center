import { useEffect, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import AvatarUploader from '@/components/AvatarUploader';
import { doctorsApi, usersApi } from '@/api/endpoints';
import { useAuth } from '@/hooks/useAuth';

export default function DoctorProfile() {
  const qc = useQueryClient();
  const { user } = useAuth();
  const { data, isLoading } = useQuery({ queryKey: ['d-me'], queryFn: () => doctorsApi.me() });

  const [contacts, setContacts] = useState({ email: '', fullName: '', phone: '' });
  const [profile, setProfile] = useState({ bio: '', photoUrl: '', roomNumber: '' });
  const [pwd, setPwd] = useState({ oldPassword: '', newPassword: '' });

  useEffect(() => {
    if (data) {
      setContacts({ email: data.email ?? '', fullName: data.fullName ?? '', phone: data.phone ?? '' });
      setProfile({ bio: data.bio ?? '', photoUrl: data.photoUrl ?? '', roomNumber: data.roomNumber ?? '' });
    }
  }, [data]);

  const saveContacts = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await usersApi.updateMe({ email: contacts.email, fullName: contacts.fullName, phone: contacts.phone || undefined });
      toast.success('Контактные данные обновлены');
      qc.invalidateQueries({ queryKey: ['d-me'] });
    } catch (e: any) { toast.error(e?.response?.data?.message || 'Ошибка'); }
  };

  const saveProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await doctorsApi.updateOwnProfile({
        bio: profile.bio || undefined,
        photoUrl: profile.photoUrl || undefined,
        roomNumber: profile.roomNumber || undefined,
      });
      toast.success('Профиль врача обновлён');
      qc.invalidateQueries({ queryKey: ['d-me'] });
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
      <PageHeader title="Профиль" subtitle="Контактные данные, информация для пациентов и пароль." />
      {isLoading || !data ? <Loader /> : (
        <div className="space-y-4 max-w-3xl">
          <div className="card">
            <h3 className="text-base font-semibold text-slate-700 mb-4">Аватар</h3>
            <AvatarUploader avatarUrl={user?.avatarUrl ?? data.avatarUrl} fullName={data.fullName}
              onChanged={() => qc.invalidateQueries({ queryKey: ['d-me'] })} />
          </div>

          <form onSubmit={saveContacts} className="card grid md:grid-cols-2 gap-3">
            <h3 className="md:col-span-2 text-base font-semibold text-slate-700">Контактные данные</h3>
            <div><label className="label">ФИО</label><input className="input" value={contacts.fullName} onChange={(e) => setContacts({ ...contacts, fullName: e.target.value })} /></div>
            <div><label className="label">Email</label><input className="input" type="email" value={contacts.email} onChange={(e) => setContacts({ ...contacts, email: e.target.value })} /></div>
            <div><label className="label">Телефон</label><input className="input" value={contacts.phone} onChange={(e) => setContacts({ ...contacts, phone: e.target.value })} /></div>
            <div className="md:col-span-2 flex justify-end"><button className="btn-primary">Сохранить</button></div>
          </form>

          <form onSubmit={saveProfile} className="card grid md:grid-cols-2 gap-3">
            <h3 className="md:col-span-2 text-base font-semibold text-slate-700">Профиль для пациентов</h3>
            <div><label className="label">№ кабинета</label><input className="input" value={profile.roomNumber} onChange={(e) => setProfile({ ...profile, roomNumber: e.target.value })} /></div>
            <div><label className="label">Ссылка на фото (необязательно)</label><input className="input" value={profile.photoUrl} onChange={(e) => setProfile({ ...profile, photoUrl: e.target.value })} placeholder="https://..." /></div>
            <div className="md:col-span-2"><label className="label">О себе</label><textarea className="input" rows={4} value={profile.bio} onChange={(e) => setProfile({ ...profile, bio: e.target.value })} /></div>
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
