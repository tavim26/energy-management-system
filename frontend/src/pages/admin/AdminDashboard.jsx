import React from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../components/layout/Navbar';
import { useAuth } from '../../context/AuthContext';

function AdminDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate(); //ptr redirect


  // render UI
  return (
    <div className="min-h-screen bg-gray-100">
      {/* Navbar */}
      <Navbar />

      <div className="container mx-auto p-6">
        {/* Welcome message */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h1 className="text-3xl font-bold text-gray-800">
            Admin Dashboard
          </h1>
          <p className="text-gray-600 mt-2">
            Welcome, {user?.username}!
          </p>
        </div>

        {/* Action cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          
          {/* Manage Users */}
          <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-xl transition cursor-pointer"
               onClick={() => navigate('/admin/users')}>

            <div className="flex items-center mb-4">
              <div className="bg-blue-100 p-3 rounded-full">
                <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              </div>
            </div>
            <h2 className="text-xl font-bold text-gray-800 mb-2">
              Manage Users
            </h2>
            <p className="text-gray-600">
              Create, edit, and delete user accounts
            </p>

          </div>



          {/* Manage Devices */}
          <div className="bg-white rounded-lg shadow-md p-6 hover:shadow-xl transition cursor-pointer"
               onClick={() => navigate('/admin/devices')}>

            <div className="flex items-center mb-4">
              <div className="bg-green-100 p-3 rounded-full">
                <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 3v2m6-2v2M9 19v2m6-2v2M5 9H3m2 6H3m18-6h-2m2 6h-2M7 19h10a2 2 0 002-2V7a2 2 0 00-2-2H7a2 2 0 00-2 2v10a2 2 0 002 2zM9 9h6v6H9V9z" />
                </svg>
              </div>
            </div>
            <h2 className="text-xl font-bold text-gray-800 mb-2">
              Manage Devices
            </h2>
            <p className="text-gray-600">
              Create, edit, delete, and assign devices
            </p>

          </div>

        </div>
      </div>
    </div>
  );
}

export default AdminDashboard;