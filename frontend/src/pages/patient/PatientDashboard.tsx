import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { CalendarCheck, FileHeart, HeartPulse, Stethoscope } from 'lucide-react';
import { Link } from 'react-router-dom';
import PageHeader from '@/components/PageHeader';
import StatCard from '@/components/StatCard';
import StatusBadge from '@/components/StatusBadge';
import Loader from '@/components/Loader';
import { appointmentsApi, recommendationsApi } from '@/api/endpoints';
import { useAuth } from '@/hooks/useAuth';
import { formatDateTime } from '@/utils/format';

export default function PatientDashboard() {
  const { user } = useAuth();
  const upcoming = useQuery({ queryKey: ['p-upcoming'], queryFn: () => appointmentsApi.upcoming() });
  const history = useQuery({ queryKey: ['p-rec-history'], queryFn: () => recommendationsApi.history() });

  return (
    <>
      <PageHeader
        title={`Здравствуйте, ${user?.fullName?.split(' ')[1] ?? user?.fullName}!`}
        subtitle="Ваш медицинский центр на расстоянии одного клика."
      />

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard icon={<CalendarCheck size={20} />} label="Предстоящих приёмов" value={upcoming.data?.length ?? 0} delay={0.0} />
        <StatCard icon={<HeartPulse size={20} />} label="Анализов симптомов" value={history.data?.length ?? 0} delay={0.05} />
        <StatCard icon={<Stethoscope size={20} />} label="Записаться к врачу" value="Перейти" hint="Подберите подходящего специалиста" delay={0.1} />
        <StatCard icon={<FileHeart size={20} />} label="Медкарта" value="Открыть" hint="История приёмов и назначения" delay={0.15} />
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }} className="card">
          <div className="flex items-center justify-between mb-3">
            <h3 className="font-semibold text-slate-800">Ближайшие приёмы</h3>
            <Link className="text-sm text-brand-600 font-medium" to="/patient/appointments">Все →</Link>
          </div>
          {upcoming.isLoading ? <Loader /> :
            (upcoming.data ?? []).length === 0 ? <div className="text-slate-500 text-sm">Записей нет.</div> :
            <ul className="space-y-3">
              {(upcoming.data ?? []).slice(0, 5).map((a) => (
                <li key={a.id} className="flex items-center justify-between border-b border-brand-50 last:border-0 pb-3 last:pb-0">
                  <div>
                    <div className="font-medium text-slate-800">{a.doctorFullName}</div>
                    <div className="text-xs text-slate-500">{a.doctorSpecialization} · {formatDateTime(a.appointmentDate)}</div>
                  </div>
                  <StatusBadge status={a.status} />
                </li>
              ))}
            </ul>}
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 12 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35, delay: 0.05 }} className="card">
          <div className="flex items-center justify-between mb-3">
            <h3 className="font-semibold text-slate-800">Последние рекомендации</h3>
            <Link className="text-sm text-brand-600 font-medium" to="/patient/symptoms">К анализу →</Link>
          </div>
          {history.isLoading ? <Loader /> :
            (history.data ?? []).length === 0 ? <div className="text-slate-500 text-sm">Анализы ещё не проводились.</div> :
            <ul className="space-y-3">
              {(history.data ?? []).slice(0, 5).map((r) => (
                <li key={r.id} className="flex items-center justify-between border-b border-brand-50 last:border-0 pb-3 last:pb-0">
                  <div>
                    <div className="font-medium text-slate-800">{r.diagnosisName ?? 'Совпадений не найдено'}</div>
                    <div className="text-xs text-slate-500">
                      {r.recommendedDoctorName ? `Рекомендовано: ${r.recommendedDoctorName}` : '—'} · {formatDateTime(r.createdAt)}
                    </div>
                  </div>
                  <span className="chip">{r.confidence}%</span>
                </li>
              ))}
            </ul>}
        </motion.div>
      </div>
    </>
  );
}
