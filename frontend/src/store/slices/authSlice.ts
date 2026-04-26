import { createSlice, PayloadAction } from '@reduxjs/toolkit';
import type { AuthUser, JwtResponse } from '@/types';

interface AuthState {
  token: string | null;
  user: AuthUser | null;
}

const STORAGE_KEY = 'medcenter.auth.v1';

function readInitial(): AuthState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (raw) return JSON.parse(raw) as AuthState;
  } catch {
    /* ignore */
  }
  return { token: null, user: null };
}

const slice = createSlice({
  name: 'auth',
  initialState: readInitial(),
  reducers: {
    setSession(state, action: PayloadAction<JwtResponse>) {
      const { accessToken, userId, login, fullName, avatarUrl, roles } = action.payload;
      state.token = accessToken;
      state.user = { userId, login, fullName, avatarUrl: avatarUrl ?? null, roles };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    },
    updateProfile(state, action: PayloadAction<Partial<AuthUser>>) {
      if (!state.user) return;
      state.user = { ...state.user, ...action.payload };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    },
    logout(state) {
      state.token = null;
      state.user = null;
      localStorage.removeItem(STORAGE_KEY);
    },
  },
});

export const { setSession, updateProfile, logout } = slice.actions;
export default slice.reducer;
