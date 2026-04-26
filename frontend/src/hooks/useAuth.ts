import { useDispatch, useSelector } from 'react-redux';
import type { AppDispatch, RootState } from '@/store';
import { logout, setSession, updateProfile } from '@/store/slices/authSlice';
import type { AuthUser, Role } from '@/types';

export function useAuth() {
  const auth = useSelector((s: RootState) => s.auth);
  const dispatch = useDispatch<AppDispatch>();

  const hasRole = (role: Role) => auth.user?.roles.includes(role) ?? false;
  const primaryRole: Role | undefined = auth.user?.roles[0];

  return {
    ...auth,
    hasRole,
    primaryRole,
    setSession: (...args: Parameters<typeof setSession>) => dispatch(setSession(...args)),
    updateProfile: (patch: Partial<AuthUser>) => dispatch(updateProfile(patch)),
    logout: () => dispatch(logout()),
  };
}
