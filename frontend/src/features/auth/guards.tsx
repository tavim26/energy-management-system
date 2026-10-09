import { Navigate, Outlet } from 'react-router';
import { homePathFor, useAuth } from './AuthContext';
import type { Role } from './types';

// Only lets through users with the given role; others go to the login page or to their own home page
export function RequireRole({ role }: { role: Role }) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (user.role !== role) {
    return <Navigate to={homePathFor(user.role)} replace />;
  }

  return <Outlet />;
}

// Login and registration pages are only for visitors who are not logged in
export function GuestOnly() {
  const { user } = useAuth();

  return user ? <Navigate to={homePathFor(user.role)} replace /> : <Outlet />;
}

export function HomeRedirect() {
  const { user } = useAuth();

  return <Navigate to={user ? homePathFor(user.role) : '/login'} replace />;
}