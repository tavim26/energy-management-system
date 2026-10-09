import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { setUnauthorizedHandler } from '@/lib/http';
import { authApi } from './api';
import { clearSession, readSession, writeSession } from './session';
import type { LoginRequest, Role, SessionUser } from './types';

interface AuthContextValue {
  user: SessionUser | null;
  login: (request: LoginRequest) => Promise<SessionUser>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient();
  const [user, setUser] = useState<SessionUser | null>(() => readSession()?.user ?? null);

  const logout = useCallback(() => {
    clearSession();
    // Cached data belongs to the previous user and must not be shown to the next one
    queryClient.clear();
    setUser(null);
  }, [queryClient]);

  const login = useCallback(async (request: LoginRequest) => {
    const response = await authApi.login(request);
    const sessionUser: SessionUser = {
      userId: response.userId,
      username: response.username,
      role: response.role,
    };

    writeSession({ token: response.token, user: sessionUser });
    setUser(sessionUser);
    return sessionUser;
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      logout();
      toast.error('Your session has expired. Please log in again.');
    });

    return () => setUnauthorizedHandler(null);
  }, [logout]);

  const value = useMemo(() => ({ user, login, logout }), [user, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }

  return context;
}

// Logged-in users always need a session; this returns it without null checks
export function useCurrentUser(): SessionUser {
  const { user } = useAuth();

  if (!user) {
    throw new Error('useCurrentUser must be used on a page that requires login');
  }

  return user;
}

export function homePathFor(role: Role): string {
  return role === 'ADMIN' ? '/admin' : '/dashboard';
}