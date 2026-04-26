import SidebarLayout from '@/components/SidebarLayout';
import { Activity, BookOpenCheck, Home, Stethoscope, UserCog, Users } from 'lucide-react';

export default function AdminLayout() {
  return (
    <SidebarLayout
      title="Администратор"
      accent="Панель управления"
      items={[
        { to: '/admin', label: 'Обзор', icon: <Home size={18} />, end: true },
        { to: '/admin/users', label: 'Пользователи', icon: <UserCog size={18} /> },
        { to: '/admin/doctors', label: 'Врачи', icon: <Stethoscope size={18} /> },
        { to: '/admin/patients', label: 'Пациенты', icon: <Users size={18} /> },
        { to: '/admin/symptoms', label: 'Симптомы', icon: <Activity size={18} /> },
        { to: '/admin/diagnoses', label: 'Диагнозы', icon: <BookOpenCheck size={18} /> },
      ]}
    />
  );
}
