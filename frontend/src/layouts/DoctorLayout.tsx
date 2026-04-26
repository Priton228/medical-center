import SidebarLayout from '@/components/SidebarLayout';
import { CalendarRange, ClipboardList, Home, NotebookText } from 'lucide-react';

export default function DoctorLayout() {
  return (
    <SidebarLayout
      title="Врач"
      accent="Рабочее место"
      items={[
        { to: '/doctor', label: 'Обзор', icon: <Home size={18} />, end: true },
        { to: '/doctor/schedule', label: 'Расписание', icon: <CalendarRange size={18} /> },
        { to: '/doctor/appointments', label: 'Записи на приём', icon: <ClipboardList size={18} /> },
        { to: '/doctor/records', label: 'Медкарты', icon: <NotebookText size={18} /> },
      ]}
    />
  );
}
