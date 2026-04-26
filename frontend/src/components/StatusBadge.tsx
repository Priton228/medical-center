import clsx from 'clsx';
import type { AppointmentStatus } from '@/types';
import { statusLabel } from '@/utils/format';

export default function StatusBadge({ status }: { status: AppointmentStatus }) {
  return (
    <span className={clsx(
      'inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium border',
      `badge-status-${status}`
    )}>
      {statusLabel(status)}
    </span>
  );
}
