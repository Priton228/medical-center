import { useQuery } from '@tanstack/react-query';
import { motion } from 'framer-motion';
import { CalendarRange, ClipboardList, Stethoscope, Users } from 'lucide-react';
import PageHeader from '@/components/PageHeader';
import StatCard from '@/components/StatCard';
import StatusBadge from '@/components/StatusBadge';
import Loader from '@/components/Loader';
import { appointmentsApi, doctorsApi } from '@/api/endpoints';
import { formatDateTime } from '@/utils/format';

export default function DoctorDashboard() {
  const me = useQuery({ queryKey: ['d-me'], queryFn: () => doctorsApi.me() });
  const upcoming = useQuery({ queryKey: ['d-upcoming'], queryFn: () => appointmentsApi.upcoming() });

  return (
    <>
      <PageHeader
        title={`Здравствуйте, ${me.data?.fullName ?? ''}`}
        subtitle={me.data ? `${me.data.specialization} · кабинет ${me.data.roomNumber ?? '—'}` : 'Загрузка профиля…'}
      />

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
        <StatCard icon={<CalendarRange size={20} />} label="Часы приёма"
          value={me.data ? `${me.data.workStart?.slice(0, 5)}–${me.data.workEnd?.slice(0, 5)}` : '—'} delay={0} />
        <StatCard icon={<ClipboardList size={20} />} label="Предстоящих записей" value={upcoming.data?.length ?? 0} delay={0.05} />
        <StatCard icon={<Users size={20} />} label="Пациентов сегодня"
          value={(upcoming.data ?? []).filter((a) => new Date(a.appointmentDate).toDateString() === new Date().toDateString()).length} delay={0.1} />
        <StatCard icon={<Stethoscope size={20} />} label="Доступность" value={me.data?.available ? 'Активен' : 'Закрыт'} delay={0.15} />
      </div>

      <motion.div initial={{ opacity: 0, y: 14 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.35 }} className="card">
        <h3 className="font-semibold text-slate-800 mb-3">Ближайшие приёмы</h3>
        {upcoming.isLoading ? <Loader /> :
          (upcoming.data ?? []).length === 0 ? <div className="text-sm text-slate-500">Записей не запланировано.</div> :
          <ul className="divide-y divide-brand-50">
            {upcoming.data!.slice(0, 8).map((a) => (
              <li key={a.id} className="py-3 flex items-center justify-between">
                <div>
                  <div className="font-medium text-slate-800">{a.patientFullName}</div>
                  <div className="text-xs text-slate-500">{formatDateTime(a.appointmentDate)} · {a.notes ?? '—'}</div>
                </div>
                <StatusBadge status={a.status} />
              </li>
            ))}
          </ul>}
      </motion.div>
    </>
  );
}
