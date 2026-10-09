import type { ReactNode } from 'react';
import { NavLink, Outlet } from 'react-router';
import { Gauge, LayoutDashboard, LogOut, PlugZap, Users } from 'lucide-react';
import { useAuth, useCurrentUser } from '@/features/auth/AuthContext';
import type { Role } from '@/features/auth/types';
import { cn } from '@/lib/cn';
import { Logo } from './Logo';

interface NavItem {
  to: string;
  label: string;
  icon: ReactNode;
  end?: boolean;
}

const navigation: Record<Role, NavItem[]> = {
  ADMIN: [
    { to: '/admin', label: 'Overview', icon: <LayoutDashboard className="size-4" aria-hidden />, end: true },
    { to: '/admin/devices', label: 'Devices', icon: <PlugZap className="size-4" aria-hidden /> },
    { to: '/admin/users', label: 'Users', icon: <Users className="size-4" aria-hidden /> },
  ],
  CLIENT: [{ to: '/dashboard', label: 'My energy', icon: <Gauge className="size-4" aria-hidden />, end: true }],
};

// Sidebar on large screens, top bar on small screens
export function AppLayout() {
  const user = useCurrentUser();
  const { logout } = useAuth();
  const items = navigation[user.role];

  return (
    <div className="min-h-screen lg:flex">
      <aside className="border-b border-line bg-surface lg:fixed lg:inset-y-0 lg:flex lg:w-60 lg:flex-col lg:border-b-0 lg:border-r">
        <div className="flex items-center justify-between px-4 py-4 lg:px-5 lg:py-6">
          <Logo />
          <button
            type="button"
            onClick={logout}
            className="flex items-center gap-1.5 rounded-md px-2 py-1.5 text-sm text-muted hover:bg-paper hover:text-ink lg:hidden"
          >
            <LogOut className="size-4" aria-hidden />
            Log out
          </button>
        </div>

        <nav aria-label="Main" className="flex gap-1 overflow-x-auto px-3 pb-3 lg:flex-1 lg:flex-col lg:pb-0">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-2.5 whitespace-nowrap rounded-md px-3 py-2 text-sm font-medium transition-colors',
                  isActive ? 'bg-brand-soft text-brand-strong' : 'text-muted hover:bg-paper hover:text-ink',
                )
              }
            >
              {item.icon}
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="hidden border-t border-line px-5 py-4 lg:block">
          <p className="truncate text-sm font-medium">{user.username}</p>
          <p className="text-xs text-muted">{user.role === 'ADMIN' ? 'Administrator' : 'Client'}</p>
          <button
            type="button"
            onClick={logout}
            className="mt-3 flex items-center gap-1.5 text-sm text-muted hover:text-ink"
          >
            <LogOut className="size-4" aria-hidden />
            Log out
          </button>
        </div>
      </aside>

      <main className="flex-1 px-4 py-6 sm:px-6 lg:ml-60 lg:px-10 lg:py-10">
        <div className="mx-auto max-w-6xl">
          <Outlet />
        </div>
      </main>
    </div>
  );
}