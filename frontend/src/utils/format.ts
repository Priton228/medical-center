export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '—';
  return d.toLocaleString('ru-RU', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

export function formatDate(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '—';
  return d.toLocaleDateString('ru-RU', { day: '2-digit', month: '2-digit', year: 'numeric' });
}

export function statusLabel(status: string): string {
  switch (status) {
    case 'PLANNED':   return 'Запланирован';
    case 'CONFIRMED': return 'Подтверждён';
    case 'COMPLETED': return 'Завершён';
    case 'CANCELLED': return 'Отменён';
    default: return status;
  }
}

export function roleLabel(role: string): string {
  switch (role) {
    case 'ROLE_PATIENT': return 'Пациент';
    case 'ROLE_DOCTOR':  return 'Врач';
    case 'ROLE_ADMIN':   return 'Администратор';
    default: return role;
  }
}
