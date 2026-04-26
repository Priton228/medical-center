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
      const { accessToken, userId, username, fullName, roles } = action.payload;
      state.token = accessToken;
      state.user = { userId, username, fullName, roles };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    },
    logout(state) {
      state.token = null;
      state.user = null;
      localStorage.removeItem(STORAGE_KEY);
    },
  },
});

export const { setSession, logout } = slice.actions;
export default slice.reducer;
