import { Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';

export default function HomeRedirect() {
  const { user, primaryRole } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (primaryRole === 'ROLE_ADMIN') return <Navigate to="/admin" replace />;
  if (primaryRole === 'ROLE_DOCTOR') return <Navigate to="/doctor" replace />;
  return <Navigate to="/patient" replace />;
}
