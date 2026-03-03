//Entry point al aplicatiei
//configureaza
//- AuthProvider pentru contextul global de autentificare
//- BrowserRouter pt navigare intre pagini
//- Route-urile aplicatiei (public and protected)


import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';

// Auth pages
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';

// Admin pages 
import AdminDashboard from './pages/admin/AdminDashboard';
import UsersManagement from './pages/admin/UsersManagement';
import DevicesManagement from './pages/admin/DevicesManagement';

// Client pages
import ClientDashboard from './pages/client/ClientDashboard';

// Error page
import UnauthorizedPage from './pages/UnauthorizedPage';

function App() {
  return (

    <AuthProvider>
      <BrowserRouter>

        <Routes>
          
          {/* Public Routes - fara auth */}
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />
          
          {/* Admin Routes - DOAR pt admin */}
          <Route 
            path="/admin/dashboard" 
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <AdminDashboard />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/admin/users" 
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <UsersManagement />
              </ProtectedRoute>
            } 
          />
          
          <Route 
            path="/admin/devices" 
            element={
              <ProtectedRoute allowedRoles={['ADMIN']}>
                <DevicesManagement />
              </ProtectedRoute>
            } 
          />
          
          {/* Client Routes - doar pt client */}
          <Route 
            path="/client/dashboard" 
            element={
              <ProtectedRoute allowedRoles={['CLIENT']}>
                <ClientDashboard />
              </ProtectedRoute>
            } 
          />
          
          {/* Default  */}
          <Route path="/" element={<Navigate to="/login" replace />} />
          
          {/* Catch-all - orice alta ruta */}
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;