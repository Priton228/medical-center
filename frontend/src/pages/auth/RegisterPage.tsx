import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { authApi } from '@/api/endpoints';
import { useAuth } from '@/hooks/useAuth';

type RoleChoice = 'ROLE_PATIENT' | 'ROLE_DOCTOR';

export default function RegisterPage() {
  const [form, setForm] = useState({
    login: '', email: '', password: '', fullName: '', phone: '',
    role: 'ROLE_PATIENT' as RoleChoice,
    birthDate: '', address: '', insuranceNumber: '',
    specialization: '', bio: '',
  });
  const [busy, setBusy] = useState(false);
  const { setSession } = useAuth();
  const navigate = useNavigate();

  const handle = (k: string) => (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    setForm((s) => ({ ...s, [k]: e.target.value }));
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    try {
      const payload: any = {
        login: form.login,
        email: form.email,
        password: form.password,
        fullName: form.fullName,
        phone: form.phone || null,
        role: form.role,
      };
      if (form.role === 'ROLE_PATIENT') {
        payload.birthDate = form.birthDate || null;
        payload.address = form.address || null;
        payload.insuranceNumber = form.insuranceNumber || null;
      } else {
        payload.specialization = form.specialization || 'Терапевт';
        payload.bio = form.bio || null;
      }
      const res = await authApi.register(payload);
      setSession(res);
      toast.success('Аккаунт создан');
      navigate('/');
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Ошибка регистрации');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-6">
      <motion.form
        initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.4 }}
        onSubmit={submit}
        className="card w-full max-w-2xl space-y-4"
      >
        <h2 className="text-2xl font-bold text-slate-800">Регистрация</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
          <div><label className="label">Логин</label><input className="input" required value={form.login} onChange={handle('login')} pattern="[A-Za-z0-9._-]{3,64}" title="3–64 символа: латиница, цифры, . _ -" /></div>
          <div><label className="label">Email</label><input className="input" type="email" required value={form.email} onChange={handle('email')} /></div>
          <div><label className="label">Пароль</label><input className="input" type="password" required minLength={6} value={form.password} onChange={handle('password')} /></div>
          <div><label className="label">ФИО</label><input className="input" required value={form.fullName} onChange={handle('fullName')} /></div>
          <div><label className="label">Телефон</label><input className="input" value={form.phone} onChange={handle('phone')} /></div>
          <div>
            <label className="label">Роль</label>
            <select className="input" value={form.role} onChange={handle('role')}>
              <option value="ROLE_PATIENT">Пациент</option>
              <option value="ROLE_DOCTOR">Врач</option>
            </select>
          </div>
        </div>

        {form.role === 'ROLE_PATIENT' && (
          <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div><label className="label">Дата рождения</label><input className="input" type="date" value={form.birthDate} onChange={handle('birthDate')} /></div>
            <div className="md:col-span-2"><label className="label">Адрес</label><input className="input" value={form.address} onChange={handle('address')} /></div>
            <div className="md:col-span-3"><label className="label">№ полиса</label><input className="input" value={form.insuranceNumber} onChange={handle('insuranceNumber')} /></div>
          </div>
        )}
        {form.role === 'ROLE_DOCTOR' && (
          <div className="grid grid-cols-1 gap-3">
            <div><label className="label">Специализация</label><input className="input" value={form.specialization} onChange={handle('specialization')} placeholder="Терапевт" /></div>
            <div>
              <label className="label">О себе</label>
              <textarea className="input" rows={3} value={form.bio} onChange={handle('bio')} />
            </div>
          </div>
        )}

        <button className="btn-primary w-full" type="submit" disabled={busy}>
          {busy ? 'Создаём…' : 'Зарегистрироваться'}
        </button>
        <div className="text-sm text-slate-500 text-center">
          Уже есть аккаунт? <Link className="text-brand-600 font-medium" to="/login">Войти</Link>
        </div>
      </motion.form>
    </div>
  );
}
