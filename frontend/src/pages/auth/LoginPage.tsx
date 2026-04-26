import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { HeartPulse, LogIn } from 'lucide-react';
import { authApi } from '@/api/endpoints';
import { useAuth } from '@/hooks/useAuth';
import toast from 'react-hot-toast';

export default function LoginPage() {
  const [login, setLogin] = useState('');
  const [password, setPassword] = useState('');
  const [busy, setBusy] = useState(false);
  const { setSession } = useAuth();
  const navigate = useNavigate();

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setBusy(true);
    try {
      const res = await authApi.login(login, password);
      setSession(res);
      toast.success(`Добро пожаловать, ${res.fullName}!`);
      navigate('/');
    } catch (err: any) {
      toast.error(err?.response?.data?.message || 'Ошибка входа');
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="min-h-screen grid lg:grid-cols-2">
      <motion.div
        initial={{ opacity: 0, x: -40 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.6 }}
        className="hidden lg:flex items-center justify-center bg-gradient-to-br from-brand-500 via-brand-600 to-brand-800 text-white p-12 relative overflow-hidden"
      >
        <div className="absolute -top-32 -left-32 w-96 h-96 bg-white/10 rounded-full blur-3xl" />
        <div className="absolute -bottom-40 -right-20 w-[28rem] h-[28rem] bg-brand-300/20 rounded-full blur-3xl" />
        <div className="relative max-w-md">
          <div className="flex items-center gap-3">
            <div className="bg-white/20 p-3 rounded-2xl backdrop-blur"><HeartPulse size={28} /></div>
            <div className="font-bold text-2xl">МедЦентр</div>
          </div>
          <h1 className="mt-10 text-4xl font-extrabold leading-tight">Современная запись на приём с подбором врача по симптомам</h1>
          <p className="mt-4 text-brand-100/90 text-lg">Опишите своё состояние — и система предложит подходящего специалиста и удобное время.</p>
          <ul className="mt-8 space-y-3 text-brand-100/90">
            <li>• Личный кабинет для пациентов, врачей и администраторов</li>
            <li>• Анализ симптомов и рекомендации специалиста</li>
            <li>• Управление расписанием и медкартами</li>
          </ul>
        </div>
      </motion.div>

      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.4 }}
        className="flex items-center justify-center p-6 lg:p-12"
      >
        <form onSubmit={submit} className="card w-full max-w-md space-y-4">
          <div>
            <h2 className="text-2xl font-bold text-slate-800">Вход в систему</h2>
            <p className="text-slate-500 text-sm mt-1">Введите данные учётной записи.</p>
          </div>
          <div>
            <label className="label">Логин</label>
            <input className="input" value={login} onChange={(e) => setLogin(e.target.value)} required autoFocus autoComplete="username" />
          </div>
          <div>
            <label className="label">Пароль</label>
            <input className="input" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
          </div>
          <button className="btn-primary w-full" type="submit" disabled={busy}>
            <LogIn size={18} />
            {busy ? 'Входим…' : 'Войти'}
          </button>
          <div className="text-sm text-slate-500 text-center">
            Нет аккаунта? <Link className="text-brand-600 font-medium" to="/register">Зарегистрироваться</Link>
          </div>
          <div className="text-xs text-slate-400 border-t pt-3 mt-2">
            Демо-учётки (пароль <b>password</b>): <code>admin</code>, <code>doctor.ivanov</code>, <code>patient.sidorov</code>
          </div>
        </form>
      </motion.div>
    </div>
  );
}
