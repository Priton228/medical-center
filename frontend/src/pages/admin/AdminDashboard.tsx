import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { Activity, BookOpenCheck, CalendarCheck, Stethoscope, Users } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import StatCard from '@/components/StatCard';
import Loader from '@/components/Loader';
import { adminApi } from '@/api/endpoints';

export default function AdminDashboard() {
  const { data, isLoading } = useQuery({ queryKey: ['stats'], queryFn: () => adminApi.overview() });

  if (isLoading || !data) return (<><PageHeader title="Панель администратора" /><Loader /></>);

  const max = Math.max(1, ...data.appointmentsBySpecialization.map((s) => s.count));

  return (
    <>
      <PageHeader title="Панель администратора" subtitle="Сводка по системе и распределение нагрузки." />

      <div className="grid sm:grid-cols-2 lg:grid-cols-5 gap-4 mb-6">
        <StatCard icon={<Users size={20} />} label="Пользователи" value={data.totalUsers} delay={0.0} />
        <StatCard icon={<Stethoscope size={20} />} label="Врачи" value={data.totalDoctors} delay={0.05} />
        <StatCard icon={<Users size={20} />} label="Пациенты" value={data.totalPatients} delay={0.1} />
        <StatCard icon={<CalendarCheck size={20} />} label="Записи" value={data.totalAppointments}
          hint={`План: ${data.plannedAppointments} · Завершено: ${data.completedAppointments} · Отменено: ${data.cancelledAppointments}`} delay={0.15} />
        <StatCard icon={<BookOpenCheck size={20} />} label="Симптомы / диагнозы" value={`${data.totalSymptoms} / ${data.totalDiagnoses}`} delay={0.2} />
      </div>

      <motion.div initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} className="card">
        <div className="flex items-center gap-2 mb-4">
          <Activity className="text-brand-600" size={20} />
          <h3 className="font-semibold text-slate-800">Записи по специализациям</h3>
        </div>
        {data.appointmentsBySpecialization.length === 0 ? (
          <div className="text-sm text-slate-500">Пока нет данных.</div>
        ) : (
          <ul className="space-y-3">
            {data.appointmentsBySpecialization.map((s, i) => (
              <li key={s.specialization} className="flex items-center gap-3">
                <div className="w-40 text-sm text-slate-700 truncate">{s.specialization}</div>
                <div className="flex-1 bg-brand-50 rounded-full h-2.5 overflow-hidden">
                  <motion.div
                    className="h-full bg-gradient-to-r from-brand-500 to-brand-700"
                    initial={{ width: 0 }}
                    animate={{ width: `${(s.count / max) * 100}%` }}
                    transition={{ duration: 0.6, delay: i * 0.05 }}
                  />
                </div>
                <div className="w-10 text-right text-sm font-semibold text-slate-700">{s.count}</div>
              </li>
            ))}
          </ul>
        )}
      </motion.div>
    </>
  );
}
