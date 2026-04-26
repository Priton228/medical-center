import { ReactNode } from 'react';

export default function EmptyState({ title, description, icon }: { title: string; description?: string; icon?: ReactNode }) {
  return (
    <div className="card text-center text-slate-500 py-10">
      {icon && <div className="flex justify-center mb-3 text-brand-400">{icon}</div>}
      <div className="font-semibold text-slate-700">{title}</div>
      {description && <div className="text-sm mt-1">{description}</div>}
    </div>
  );
}
