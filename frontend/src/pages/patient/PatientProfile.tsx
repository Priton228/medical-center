import { useEffect, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';
import toast from 'react-hot-toast';
import PageHeader from '@/components/PageHeader';
import Loader from '@/components/Loader';
import { patientsApi, usersApi } from '@/api/endpoints';

export default function PatientProfile() {
  const qc = useQueryClient();
  const { data, isLoading } = useQuery({ queryKey: ['p-me'], queryFn: () => patientsApi.me() });
  const [form, setForm] = useState({ email: '', fullName: '', phone: '', birthDate: '', address: '', insuranceNumber: '' });

  useEffect(() => {
    if (data) {
      setForm({
        email: data.email ?? '', fullName: data.fullName ?? '', phone: data.phone ?? '',
        birthDate: data.birthDate ?? '', address: data.address ?? '', insuranceNumber: data.insuranceNumber ?? '',
      });
    }
  }, [data]);

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await usersApi.updateMe({ email: form.email, fullName: form.fullName, phone: form.phone || undefined });
      await patientsApi.updateMe({
        birthDate: form.birthDate || null,
        address: form.address || null,
        insuranceNumber: form.insuranceNumber || null,
      });
      toast.success('Профиль обновлён');
      qc.invalidateQueries({ queryKey: ['p-me'] });
    } catch (e: any) {
      toast.error(e?.response?.data?.message || 'Ошибка');
    }
  };

  return (
    <>
      <PageHeader title="Профиль" subtitle="Контактные данные и информация о пациенте." />
      {isLoading || !data ? <Loader /> : (
        <form onSubmit={submit} className="card grid md:grid-cols-2 gap-3 max-w-3xl">
          <div><label className="label">ФИО</label><input className="input" value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} /></div>
          <div><label className="label">Email</label><input className="input" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
          <div><label className="label">Телефон</label><input className="input" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} /></div>
          <div><label className="label">Дата рождения</label><input className="input" type="date" value={form.birthDate ?? ''} onChange={(e) => setForm({ ...form, birthDate: e.target.value })} /></div>
          <div className="md:col-span-2"><label className="label">Адрес</label><input className="input" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} /></div>
          <div className="md:col-span-2"><label className="label">№ полиса</label><input className="input" value={form.insuranceNumber} onChange={(e) => setForm({ ...form, insuranceNumber: e.target.value })} /></div>
          <div className="md:col-span-2 flex justify-end">
            <button className="btn-primary">Сохранить</button>
          </div>
        </form>
      )}
    </>
  );
}
