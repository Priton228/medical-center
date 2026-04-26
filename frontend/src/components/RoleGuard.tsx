import { Navigate, Outlet } from 'react-router-dom';
import { ReactNode } from 'react';
import { useAuth } from '@/hooks/useAuth';
import type { Role } from '@/types';

interface Props {
  role: Role;
  children?: ReactNode;
}

export default function RoleGuard({ role, children }: Props) {
  const { user, hasRole } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (!hasRole(role)) return <Navigate to="/" replace />;
  return <>{children ?? <Outlet />}</>;
}
