import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import apiClient from '../../services/api';

function RegisterPage() {


  //state - un singur obiect pentru toate campurile
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    fullName: '',
    address: ''
  });
  
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const navigate = useNavigate();
  

  //handle input change
  const handleChange = (e) => {
    setFormData({
      ...formData, // pastreaza val existente
      [e.target.name]: e.target.value // update doar campul modificat
    });
  };

  
  // trimitere date la backend
  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    
    try {
      // trm POST catre backend
      await apiClient.post('/api/auth/register', formData);   //asteapta pana se compelteaza requestul
      
      // reddirect la login in caz de succes
      navigate('/login');
      
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed!');
      console.error('Register error:', err);
    } finally {
      setLoading(false);
    }
  };
  

  
  //render UI
  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      {/* Register card */}
      <div className="bg-white p-8 rounded-lg shadow-md w-96">
        
        <h2 className="text-2xl font-bold mb-6 text-center">
          Energy Management System
        </h2>
        <h3 className="text-xl mb-6 text-center text-gray-600">Register</h3>
        
        {/* Formular */}
        <form onSubmit={handleSubmit}>
          
          <div className="mb-4">
            <label className="block text-gray-700 mb-2">Username</label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>
          
          <div className="mb-4">
            <label className="block text-gray-700 mb-2">Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              required
              minLength="6"
            />
          </div>
          
          
          
          <div className="mb-4">
            <label className="block text-gray-700 mb-2">Full Name</label>
            <input
              type="text"
              name="fullName"
              value={formData.fullName}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          <div className="mb-6">
            <label className="block text-gray-700 mb-2">Address</label>
            <input
              type="text"
              name="address"
              value={formData.address}
              onChange={handleChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          {error && (
            <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-md">
              {error}
            </div>
          )}
          
          {/* Submit button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700 disabled:bg-gray-400"
          >
            {loading ? 'Loading...' : 'Register'}
          </button>
        </form>
        
        {/* Link to Login */}
        <p className="mt-4 text-center text-gray-600">
          Already have an account?{' '}
          <a href="/login" className="text-blue-600 hover:underline">
            Login here
          </a>
        </p>
      </div>
    </div>
  );
}

export default RegisterPage;