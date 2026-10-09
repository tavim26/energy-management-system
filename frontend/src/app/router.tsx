import { createBrowserRouter } from 'react-router';
import { AppLayout } from '@/components/layout/AppLayout';
import { AdminOverviewPage } from '@/features/admin/AdminOverviewPage';
import { GuestOnly, HomeRedirect, RequireRole } from '@/features/auth/guards';
import { LoginPage } from '@/features/auth/LoginPage';
import { RegisterPage } from '@/features/auth/RegisterPage';
import { ClientDashboardPage } from '@/features/client/ClientDashboardPage';
import { DevicesPage } from '@/features/devices/DevicesPage';
import { UsersPage } from '@/features/users/UsersPage';

export const router = createBrowserRouter([
  { path: '/', element: <HomeRedirect /> },
  {
    element: <GuestOnly />,
    children: [
      { path: '/login', element: <LoginPage /> },
      { path: '/register', element: <RegisterPage /> },
    ],
  },
  {
    element: <RequireRole role="ADMIN" />,
    children: [
      {
        path: '/admin',
        element: <AppLayout />,
        children: [
          { index: true, element: <AdminOverviewPage /> },
          { path: 'devices', element: <DevicesPage /> },
          { path: 'users', element: <UsersPage /> },
        ],
      },
    ],
  },
  {
    element: <RequireRole role="CLIENT" />,
    children: [
      {
        path: '/dashboard',
        element: <AppLayout />,
        children: [{ index: true, element: <ClientDashboardPage /> }],
      },
    ],
  },
  { path: '*', element: <HomeRedirect /> },
]);