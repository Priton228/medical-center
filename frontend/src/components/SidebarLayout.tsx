import { ReactNode } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { LogOut } from 'lucide-react';
import { useAuth } from '@/hooks/useAuth';
import { roleLabel } from '@/utils/format';
import clsx from 'clsx';

export interface NavItem {
  to: string;
  label: string;
  icon: ReactNode;
  end?: boolean;
}

interface Props {
  items: NavItem[];
  title: string;
  accent: string;
}

export default function SidebarLayout({ items, title, accent }: Props) {
  const { user, logout, primaryRole } = useAuth();
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex">
      <motion.aside
        initial={{ x: -40, opacity: 0 }}
        animate={{ x: 0, opacity: 1 }}
        transition={{ duration: 0.4 }}
        className="w-64 hidden md:flex flex-col bg-gradient-to-b from-brand-700 via-brand-600 to-brand-500 text-white shadow-soft"
      >
        <div className="px-5 py-6 border-b border-white/15">
          <div className="text-xs uppercase tracking-widest text-brand-100/80">МедЦентр</div>
          <div className="text-xl font-bold mt-1">{title}</div>
          <div className="text-sm text-brand-100/80 mt-1">{accent}</div>
        </div>

        <nav className="flex-1 px-3 py-4 space-y-1">
          {items.map((it) => (
            <NavLink
              key={it.to}
              to={it.to}
              end={it.end}
              className={({ isActive }) =>
                clsx(
                  'flex items-center gap-3 px-3 py-2 rounded-xl transition-all duration-200',
                  isActive
                    ? 'bg-white/20 backdrop-blur shadow-soft text-white'
                    : 'text-brand-50/90 hover:bg-white/10 hover:text-white'
                )
              }
            >
              {it.icon}
              <span className="text-sm font-medium">{it.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="px-4 py-4 border-t border-white/15 text-sm">
          <div className="font-semibold truncate">{user?.fullName}</div>
          <div className="text-brand-100/80 text-xs">{primaryRole && roleLabel(primaryRole)}</div>
          <button
            type="button"
            className="mt-3 w-full flex items-center justify-center gap-2 px-3 py-2 rounded-xl bg-white/15 hover:bg-white/25 transition"
            onClick={() => { logout(); navigate('/login'); }}
          >
            <LogOut size={16} /> Выйти
          </button>
        </div>
      </motion.aside>

      <main className="flex-1 min-w-0">
        <header className="md:hidden flex items-center justify-between bg-white/80 backdrop-blur px-4 py-3 border-b border-brand-100">
          <div>
            <div className="text-xs text-brand-600 uppercase">МедЦентр · {title}</div>
            <div className="font-semibold text-slate-800 text-sm">{user?.fullName}</div>
          </div>
          <button className="btn-ghost px-2 py-1 text-xs" onClick={() => { logout(); navigate('/login'); }}>
            <LogOut size={14} /> Выйти
          </button>
        </header>
        <div className="p-6 lg:p-8 max-w-7xl mx-auto animate-fade-in">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
