import React, { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { getToken } from '../api/client';
import * as authService from '../services/authService';
import { getCurrentUser } from '../services/userService';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [token, setTokenState] = useState(getToken());
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(Boolean(getToken()));

  const refreshUser = useCallback(async () => {
    try {
      setUser(await getCurrentUser());
    } catch {
      // The axios interceptor clears an invalid token and redirects to /login.
      setUser(null);
    }
  }, []);

  useEffect(() => {
    if (!token) {
      setUser(null);
      setLoading(false);
      return;
    }
    setLoading(true);
    refreshUser().finally(() => setLoading(false));
  }, [token, refreshUser]);

  const login = async (credentials) => {
    const result = await authService.signIn(credentials);
    setTokenState(result.token);
    setUser({ id: result.id, username: result.username, email: result.email });
    return result;
  };

  const register = async (values) => {
    const result = await authService.signUp(values);
    setTokenState(result.token);
    setUser({ id: result.id, username: result.username, email: result.email });
    return result;
  };

  const logout = () => {
    authService.signOut();
    setTokenState(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ token, user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used inside AuthProvider');
  return context;
}
