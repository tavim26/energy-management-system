import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

function UnauthorizedPage() 
{
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  
  const handleGoBack = () => {

    // reddirect spre dashboard-ul corespunzator (based on role)
    if (user?.role === 'ADMIN') 
    {
      navigate('/admin/dashboard');
    } 
    else if (user?.role === 'CLIENT') 
    {
      navigate('/client/dashboard');
    } 
    else 
    {
      navigate('/login');
    }

  };
  
  // logout user si reddirect la login
  const handleLogout = () => {
    logout();
    navigate('/login');
  };
  

  // Render UI
  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-md max-w-md w-full text-center">
        {/* Icon */}
        <div className="mb-6">
          <svg 
            className="w-24 h-24 text-red-500 mx-auto" 
            fill="none" 
            stroke="currentColor" 
            viewBox="0 0 24 24"
          >
            <path 
              strokeLinecap="round" 
              strokeLinejoin="round" 
              strokeWidth="2" 
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        </div>
        
        <h1 className="text-3xl font-bold text-gray-800 mb-4">
          Access Denied
        </h1>
        
        <p className="text-gray-600 mb-6">
          You don't have permission to access this page.
        </p>
        
        {/* User info */}
        {user && (
          <div className="bg-gray-100 p-4 rounded mb-6">
            <p className="text-sm text-gray-600">
              Logged in as: <span className="font-semibold">{user.username}</span>
            </p>
            <p className="text-sm text-gray-600">
              Role: <span className="font-semibold">{user.role}</span>
            </p>
          </div>
        )}
        
        {/* Buttons */}
        <div className="space-y-3">
          <button
            onClick={handleGoBack}
            className="w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700 transition"
          >
            Go to Dashboard
          </button>
          
          <button
            onClick={handleLogout}
            className="w-full bg-gray-500 text-white py-2 px-4 rounded hover:bg-gray-600 transition"
          >
            Logout
          </button>
        </div>
      </div>
    </div>
  );
}

export default UnauthorizedPage;