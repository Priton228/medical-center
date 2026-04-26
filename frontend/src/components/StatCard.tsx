import { motion } from 'framer-motion';
import { ReactNode } from 'react';

interface Props {
  icon: ReactNode;
  label: string;
  value: number | string;
  hint?: string;
  delay?: number;
}

export default function StatCard({ icon, label, value, hint, delay = 0 }: Props) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 14 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay }}
      whileHover={{ y: -2 }}
      className="card flex items-start gap-3"
    >
      <div className="p-2.5 rounded-xl bg-gradient-to-br from-brand-500 to-brand-700 text-white shadow-soft">
        {icon}
      </div>
      <div className="min-w-0">
        <div className="text-xs text-slate-500 uppercase tracking-wide">{label}</div>
        <div className="text-2xl font-bold text-slate-900 mt-0.5">{value}</div>
        {hint && <div className="text-xs text-slate-400 mt-1">{hint}</div>}
      </div>
    </motion.div>
  );
}
