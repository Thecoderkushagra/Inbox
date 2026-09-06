import { create } from 'zustand';
import toast from 'react-hot-toast';
import { User, UserProfile, RoleType } from '@/types';
import { authApi, usersApi } from '@/api';
import {
  getAccessTokenCookie,
  getRefreshTokenCookie,
  setAccessTokenCookie,
  setRefreshTokenCookie,
  clearAuthCookies,
} from '@/utils/cookies';

interface AuthState {
  user: User | null;
  profile: UserProfile | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  login: (identifier: string, pass: string, silentToast?: boolean) => Promise<void>;
  register: (username: string, email: string, pass: string, confirmPassword?: string) => Promise<void>;
  logout: () => Promise<void>;
  fetchProfile: () => Promise<void>;
  updateProfile: (data: Partial<UserProfile>) => Promise<void>;
  uploadAvatar: (file: File) => Promise<string>;
  clearError: () => void;
  initialize: () => void;
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  profile: null,
  isAuthenticated: !!(getAccessTokenCookie() || localStorage.getItem('access_token')),
  isLoading: false,
  error: null,

  initialize: () => {
    const savedUser = localStorage.getItem('current_user');
    const token = getAccessTokenCookie() || localStorage.getItem('access_token');
    if (token && savedUser) {
      try {
        set({ user: JSON.parse(savedUser), isAuthenticated: true });
        get().fetchProfile();
      } catch {
        clearAuthCookies();
        localStorage.removeItem('current_user');
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        set({ user: null, isAuthenticated: false });
      }
    }
  },

  login: async (identifier, password, silentToast = false) => {
    set({ isLoading: true, error: null });
    try {
      const res = await authApi.login(identifier, password);
      
      // Store in cookies & localStorage
      setAccessTokenCookie(res.accessToken);
      setRefreshTokenCookie(res.refreshToken);
      localStorage.setItem('access_token', res.accessToken);
      localStorage.setItem('refresh_token', res.refreshToken);

      const user: User = {
        id: res.userId,
        email: res.email,
        username: res.username,
        roles: (res.roles || ['USER']) as RoleType[],
      };
      localStorage.setItem('current_user', JSON.stringify(user));
      set({ user, isAuthenticated: true, isLoading: false, error: null });

      if (!silentToast) {
        toast.success('Logged in successfully!');
      }

      get().fetchProfile();
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Login failed';
      set({ error: message, isLoading: false });
      toast.error(message);
      throw err;
    }
  },

  register: async (username, email, password, confirmPassword) => {
    set({ isLoading: true, error: null });
    try {
      await authApi.register(username, email, password, confirmPassword);
      toast.success('Account created successfully!');

      // Instant activation: automatically log in
      await get().login(username, password, true);
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Registration failed';
      set({ error: message, isLoading: false });
      toast.error(message);
      throw err;
    }
  },

  logout: async () => {
    const refreshToken = getRefreshTokenCookie() || localStorage.getItem('refresh_token');
    if (refreshToken) {
      try {
        await authApi.logout(refreshToken);
      } catch (err) {
        console.error('Logout error:', err);
      }
    }
    clearAuthCookies();
    localStorage.removeItem('access_token');
    localStorage.removeItem('refresh_token');
    localStorage.removeItem('current_user');
    set({ user: null, profile: null, isAuthenticated: false, error: null });
    toast.success('Logged out successfully');
  },

  fetchProfile: async () => {
    try {
      const profile = await usersApi.getMyProfile();
      set({ profile });
    } catch (err) {
      console.warn('Could not fetch profile:', err);
    }
  },

  updateProfile: async (data) => {
    set({ isLoading: true });
    try {
      const updated = await usersApi.updateMyProfile(data);
      set({ profile: updated, isLoading: false });
      toast.success('Profile updated successfully!');
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Update failed';
      set({ error: message, isLoading: false });
      toast.error(message);
      throw err;
    }
  },

  uploadAvatar: async (file: File) => {
    set({ isLoading: true });
    try {
      const updated = await usersApi.uploadAvatar(file);
      set({ profile: updated, isLoading: false });
      toast.success('Avatar uploaded successfully!');
      return updated.avatarUrl || '';
    } catch (err: unknown) {
      const message = err instanceof Error ? err.message : 'Failed to upload avatar';
      set({ error: message, isLoading: false });
      toast.error(message);
      throw err;
    }
  },

  clearError: () => set({ error: null }),
}));
