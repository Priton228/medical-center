import SidebarLayout from '@/components/SidebarLayout';
import { Activity, CalendarCheck, FileHeart, Home, Stethoscope, UserCircle } from 'lucide-react';

export default function PatientLayout() {
  return (
    <SidebarLayout
      title="Пациент"
      accent="Личный кабинет"
      items={[
        { to: '/patient', label: 'Обзор', icon: <Home size={18} />, end: true },
        { to: '/patient/doctors', label: 'Врачи', icon: <Stethoscope size={18} /> },
        { to: '/patient/appointments', label: 'Мои записи', icon: <CalendarCheck size={18} /> },
        { to: '/patient/symptoms', label: 'Анализ симптомов', icon: <Activity size={18} /> },
        { to: '/patient/records', label: 'Медкарта', icon: <FileHeart size={18} /> },
        { to: '/patient/profile', label: 'Профиль', icon: <UserCircle size={18} /> },
      ]}
    />
  );
}
