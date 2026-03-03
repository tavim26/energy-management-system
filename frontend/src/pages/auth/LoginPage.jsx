import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import apiClient from '../../services/api';

function LoginPage() {

  // STATES pentru formular (date ce se pot schimba)
  const [username, setUsername] = useState(''); //validare input username
  const [password, setPassword] = useState(''); 
  const [error, setError] = useState(''); 
  const [loading, setLoading] = useState(false);
  
  // hooks
  const { login } = useAuth(); // functia login din AuthContext
  const navigate = useNavigate();   //functie pt reddirect
  
  // logica login (submit formular)
  const handleSubmit = async (e) => {
    e.preventDefault(); // prevent refresh
    
    setError(''); 
    setLoading(true); 
    
    try {

      // POST /api/auth/login
      const response = await apiClient.post('/api/auth/login', {
        username: username,
        password: password
      });
      
      
      // salvare date in AuthContext
      login(response.data);
      
      // reddirect 
      if (response.data.role === 'ADMIN') 
      {
        navigate('/admin/dashboard'); 

      } 
      else 
      {
        navigate('/client/dashboard'); 
      }
      
    } catch (err) {
      
      setError('Wrong username or password!');
      console.error('Login error:', err);

    } finally {
      setLoading(false); 
    }
  };
  

  //render UI
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      {/* Login card */}
      <div className="bg-white p-8 rounded-lg shadow-md w-96">
        
        <h2 className="text-2xl font-bold mb-6 text-center">
          Energy Management System
        </h2>
        <h3 className="text-xl mb-6 text-center text-gray-600">Login</h3>
        
        {/* Formular */}
        <form onSubmit={handleSubmit}>
          
          <div className="mb-4">
            <label className="block text-gray-700 mb-2">Username</label>
            <input
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          
          <div className="mb-6">
            <label className="block text-gray-700 mb-2">Password</label>
            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          
          {/* Error message */}
          {error && (
            <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-md">
              {error}
            </div>
          )}
          
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
          >
            {loading ? 'Loading...' : 'Login'}
          </button>
        </form>
        
        {/* Link spre Register */}
        <p className="mt-4 text-center text-gray-600">
          Don't have an account?{' '}
          <a href="/register" className="text-blue-600 hover:underline">
            Register here
          </a>
        </p>
      </div>
    </div>
  );
}

export default LoginPage;