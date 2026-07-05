import { createContext, useState, useEffect, useCallback } from 'react';
import type { ReactNode } from 'react';
import type { User } from '../types';
import { AuthService } from '../services/AuthService';
import { StorageKeys } from '../constants';
import { getItem, setItem } from '../utils/storageUtils';

export interface AuthContextType {
  user: User | null;
  token: string | null;
  loading: boolean;
  login: (token: string, user: User) => void;
  logout: () => Promise<void>;
  updateUser: (user: User) => void;
  restoreSession: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  const login = useCallback((newToken: string, newUser: User) => {
    setItem(StorageKeys.ACCESS_TOKEN, newToken);
    setToken(newToken);
    setUser(newUser);
  }, []);

  const logout = useCallback(async () => {
    try {
      await AuthService.logout();
    } catch (_e) {
      // Proceed with local logout even if server fails
    } finally {
      localStorage.removeItem(StorageKeys.ACCESS_TOKEN);
      setToken(null);
      setUser(null);
      window.location.href = '/login';
    }
  }, []);

  const updateUser = useCallback((updatedUser: User) => {
    setUser(updatedUser);
  }, []);

  const restoreSession = useCallback(async () => {
    try {
      setLoading(true);
      const savedToken = getItem(StorageKeys.ACCESS_TOKEN);
      if (!savedToken) {
        setLoading(false);
        return;
      }
      
      const response = await AuthService.getCurrentUser();
      if (response.success) {
        setToken(savedToken);
        setUser(response.data);
      }
    } catch (_error) {
      localStorage.removeItem(StorageKeys.ACCESS_TOKEN);
      setToken(null);
      setUser(null);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let mounted = true;
    const initSession = async () => {
      try {
        const savedToken = getItem(StorageKeys.ACCESS_TOKEN);
        if (!savedToken) {
          if (mounted) setLoading(false);
          return;
        }
        
        const response = await AuthService.getCurrentUser();
        if (response.success && mounted) {
          setToken(savedToken);
          setUser(response.data);
        }
      } catch (_error) {
        localStorage.removeItem(StorageKeys.ACCESS_TOKEN);
        if (mounted) {
          setToken(null);
          setUser(null);
        }
      } finally {
        if (mounted) setLoading(false);
      }
    };
    
    initSession();
    return () => { mounted = false; };
  }, []);

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout, updateUser, restoreSession }}>
      {children}
    </AuthContext.Provider>
  );
}
