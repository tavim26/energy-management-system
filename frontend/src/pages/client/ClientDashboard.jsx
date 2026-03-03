import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { deviceService } from '../../services/deviceService';
import Navbar from '../../components/layout/Navbar';
import CustomerSupport from '../../components/CustomerSupport';
import OverconsumptionNotification from '../../components/OverconsumptionNotification';

function ClientDashboard() {
  const { user } = useAuth();

  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    loadDevices();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const loadDevices = async () => {
    try {
      setLoading(true);
      const data = await deviceService.getUserDevices(user.userId);
      setDevices(data);
    } catch (err) {
      console.error('Error loading devices:', err);
      setError('Failed to load devices');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-100">
      {/* WebSocket Notifications */}
      <OverconsumptionNotification />

      {/* Navbar */}
      <Navbar />

      <div className="container mx-auto p-6">
        {/* Welcome message */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h1 className="text-3xl font-bold text-gray-800">
            Welcome, {user?.username}!
          </h1>
          <p className="text-gray-600 mt-2">These are your devices</p>
        </div>

        {/* Devices section */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">My Devices</h2>

          {loading && (
            <div className="text-center py-8">
              <p className="text-gray-600">Loading devices...</p>
            </div>
          )}

          {error && (
            <div className="bg-red-100 text-red-700 p-4 rounded-md mb-4">
              {error}
            </div>
          )}

          {!loading && !error && devices.length === 0 && (
            <div className="text-center py-8">
              <p className="text-gray-600">No devices assigned yet</p>
            </div>
          )}

          {!loading && !error && devices.length > 0 && (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {devices.map((device) => (
                <div
                  key={device.id}
                  className="border border-gray-300 rounded-lg p-4 hover:shadow-lg transition"
                >
                  <h3 className="text-lg font-semibold text-gray-800 mb-2">
                    {device.name}
                  </h3>

                  <div className="text-sm text-gray-600">
                    <p className="mb-1">
                      <span className="font-semibold">Max Consumption:</span>{' '}
                      {device.maxConsumption} W
                    </p>
                  </div>

                  <div className="mt-3">
                    <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-xs font-semibold">
                      Active
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Customer Support Chat */}
        <CustomerSupport />
      </div>
    </div>
  );
}

export default ClientDashboard;