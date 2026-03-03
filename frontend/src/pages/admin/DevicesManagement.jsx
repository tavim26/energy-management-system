import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../components/layout/Navbar';
import { deviceService } from '../../services/deviceService';
import { userService } from '../../services/userService';

function DevicesManagement() {
  const navigate = useNavigate();
  
  // STATE pentru devices
  const [devices, setDevices] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // STATE pentru useri (la assign)
  const [users, setUsers] = useState([]);
  
  // ARRAY cu asocieri device-user
  const [deviceAssignments, setDeviceAssignments] = useState([]);
  
  // STATE pentru modals
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showAssignModal, setShowAssignModal] = useState(false);
  const [selectedDevice, setSelectedDevice] = useState(null);
  
  // Form data pentru create
  const [createFormData, setCreateFormData] = useState({
    name: '',
    maxConsumption: ''
  });
  
  // Form data pentru edit
  const [editFormData, setEditFormData] = useState({
    name: '',
    maxConsumption: ''
  });
  
  // State pentru assign
  const [selectedUserId, setSelectedUserId] = useState('');

  // load users (pentru assign)
  const loadUsers = async () => {
    try {
      const data = await userService.getAllUsers();
      setUsers(data);
    } catch (err) {
      console.error('Error loading users:', err);
    }
  };

  const loadDeviceAssignments = useCallback(() => {
    const assignments = [];
    
    // Itereaza prin devices (deja incarcate cu getAllDevices)
    devices.forEach(device => {
      // Daca device-ul e alocat unui user (userId != null)
      if (device.userId) {
        // Gaseste user-ul corespunzator
        const user = users.find(u => u.id === device.userId);
        
        if (user) {
          assignments.push({
            deviceId: device.id,
            deviceName: device.name,
            deviceMaxConsumption: device.maxConsumption,
            userId: user.id,
            username: user.username,
            userRole: user.role
          });
        }
      }
    });
    
    setDeviceAssignments(assignments);
    
  }, [devices, users]); // Dependencies: devices si users

  const loadDevices = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const data = await deviceService.getAllDevices(); // GET /api/devices (UN SINGUR REQUEST!)
      setDevices(data);
      
    } catch (err) {
      console.error('Error loading devices:', err);
      setError('Failed to load devices');
    } finally {
      setLoading(false);
    }
  }, []); // NU mai depinde de loadDeviceAssignments

  useEffect(() => {
    const initData = async () => {
      await loadUsers();     // Incarca users
      await loadDevices();   // Incarca devices
    };
    
    initData();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // Run once la mount

  useEffect(() => {
    if (devices.length > 0 && users.length > 0) {
      loadDeviceAssignments();
    }
  }, [devices, users, loadDeviceAssignments]);

  // CREATE device
  const handleCreateDevice = async (e) => {
    e.preventDefault();
    
    try {
      setError('');
      
      // conversie maxConsumption la numar
      const deviceData = {
        name: createFormData.name,
        maxConsumption: parseFloat(createFormData.maxConsumption)
      };
      
      await deviceService.createDevice(deviceData); // POST /api/devices
      
      // reload lista
      await loadDevices();
      
      // Inchidem modal si resetam form
      setShowCreateModal(false);
      setCreateFormData({
        name: '',
        maxConsumption: ''
      });
      
      alert('Device created successfully!');
      
    } catch (err) {
      console.error('Error creating device:', err);
      setError(err.response?.data?.message || 'Failed to create device');
    }
  };

  // EDIT device
  const handleEditDevice = async (e) => {
    e.preventDefault();
    
    try {
      setError('');
      
      const deviceData = {
        name: editFormData.name,
        maxConsumption: parseFloat(editFormData.maxConsumption)
      };
      
      await deviceService.updateDevice(selectedDevice.id, deviceData);
      
      // Reincarcam lista
      await loadDevices();
      
      // Inchidem modal
      setShowEditModal(false);
      setSelectedDevice(null);
      
      alert('Device updated successfully!');
      
    } catch (err) {
      console.error('Error updating device:', err);
      setError(err.response?.data?.message || 'Failed to update device');
    }
  };

  // DELETE device
  const handleDeleteDevice = async (deviceId, deviceName) => {
    // Confirm
    const confirmed = window.confirm(
      `Are you sure you want to delete device "${deviceName}"? This will also remove all assignments.`
    );
    
    if (!confirmed) return;
    
    try {
      setError('');
      
      await deviceService.deleteDevice(deviceId);
      
      // Reincarcam lista
      await loadDevices();
      
      alert('Device deleted successfully!');
      
    } catch (err) {
      console.error('Error deleting device:', err);
      setError(err.response?.data?.message || 'Failed to delete device');
    }
  };

  // open EDIT modal + populate form
  const openEditModal = (device) => {
    setSelectedDevice(device);
    setEditFormData({
      name: device.name,
      maxConsumption: device.maxConsumption.toString()
    });
    setShowEditModal(true);
  };

  // open ASSIGN modal
  const openAssignModal = (device) => {
    setSelectedDevice(device);
    setSelectedUserId('');
    setShowAssignModal(true);
  };

  // ASSIGN device to user
  const handleAssignDevice = async () => {
    if (!selectedUserId) {
      alert('Please select a user!');
      return;
    }
    
    try {
      setError('');
      
      await deviceService.assignDevice(parseInt(selectedUserId), selectedDevice.id);
      
      // reload devices si asocierile
      await loadDevices();
      
      // close modal
      setShowAssignModal(false);
      setSelectedDevice(null);
      setSelectedUserId('');
      
      alert('Device assigned successfully!');
      
    } catch (err) {
      console.error('Error assigning device:', err);
      
      // Verificam daca e eroare de "device already assigned"
      if (err.response?.status === 400) {
        setError('This device is already assigned to a user!');
      } else {
        setError(err.response?.data?.message || 'Failed to assign device');
      }
    }
  };

  // Render UI
  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />

      <div className="container mx-auto p-6">
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Devices Management</h1>
            <p className="text-gray-600 mt-1">Manage all devices and assignments</p>
          </div>
          
          <div className="flex gap-3">
            <button
              onClick={() => navigate('/admin/dashboard')}
              className="bg-gray-500 hover:bg-gray-600 text-white px-4 py-2 rounded transition"
            >
              Back to Dashboard
            </button>
            
            <button
              onClick={() => setShowCreateModal(true)}
              className="bg-green-600 hover:bg-green-700 text-white px-4 py-2 rounded transition"
            >
              + Create Device
            </button>
          </div>
        </div>

        {/* Error message */}
        {error && (
          <div className="bg-red-100 text-red-700 p-4 rounded-md mb-4">
            {error}
          </div>
        )}

        {/* Devices table */}
        <div className="bg-white rounded-lg shadow-md overflow-hidden mb-8">
          <div className="px-6 py-4 bg-gray-50 border-b">
            <h2 className="text-xl font-bold text-gray-800">All Devices</h2>
          </div>
          
          {loading ? (
            <div className="p-8 text-center text-gray-600">
              Loading devices...
            </div>
          ) : devices.length === 0 ? (
            <div className="p-8 text-center text-gray-600">
              No devices found
            </div>
          ) : (
            <table className="w-full">
              <thead className="bg-gray-50 border-b">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Name</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Max Consumption (W)</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Assigned To</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {devices.map((device) => (
                  <tr key={device.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm text-gray-900">{device.id}</td>
                    <td className="px-6 py-4 text-sm text-gray-900 font-medium">{device.name}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">{device.maxConsumption}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      {deviceAssignments.filter(a => a.deviceId === device.id).length > 0 ? (
                        <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs font-semibold">
                          {deviceAssignments.filter(a => a.deviceId === device.id).length} user(s)
                        </span>
                      ) : (
                        <span className="text-gray-400 italic">Not assigned</span>
                      )}
                    </td>
                    <td className="px-6 py-4 text-sm">
                      <button
                        onClick={() => openAssignModal(device)}
                        className="text-green-600 hover:text-green-800 mr-3 font-medium"
                      >
                        Assign
                      </button>
                      <button
                        onClick={() => openEditModal(device)}
                        className="text-blue-600 hover:text-blue-800 mr-3 font-medium"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDeleteDevice(device.id, device.name)}
                        className="text-red-600 hover:text-red-800 font-medium"
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {/* DEVICE-USER ASSOCIATIONS TABLE */}
        <div className="bg-white rounded-lg shadow-md overflow-hidden">
          <div className="px-6 py-4 bg-gray-50 border-b">
            <h2 className="text-xl font-bold text-gray-800">Device-User Associations</h2>
          </div>
          
          {deviceAssignments.length === 0 ? (
            <div className="p-8 text-center text-gray-600">
              No devices assigned yet
            </div>
          ) : (
            <table className="w-full">
              <thead className="bg-gray-50 border-b">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Device ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Device Name</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Max Consumption (W)</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Assigned To User</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {deviceAssignments.map((assignment, index) => (
                  <tr key={index} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm text-gray-900">{assignment.deviceId}</td>
                    <td className="px-6 py-4 text-sm text-gray-900 font-medium">{assignment.deviceName}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">{assignment.deviceMaxConsumption}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">
                      <span className="px-2 py-1 bg-blue-100 text-blue-800 rounded-full text-xs font-semibold">
                        {assignment.username}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>

      {/* CREATE DEVICE MODAL */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 w-full max-w-md">
            <h2 className="text-2xl font-bold mb-6">Create New Device</h2>
            
            <form onSubmit={handleCreateDevice}>
              {/* Device Name */}
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Device Name *</label>
                <input
                  type="text"
                  value={createFormData.name}
                  onChange={(e) => setCreateFormData({...createFormData, name: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
                  placeholder="e.g. Smart Thermostat"
                  required
                />
              </div>

              {/* Max Consumption */}
              <div className="mb-6">
                <label className="block text-gray-700 mb-2">Max Consumption (Watts) *</label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={createFormData.maxConsumption}
                  onChange={(e) => setCreateFormData({...createFormData, maxConsumption: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
                  placeholder="e.g. 150.5"
                  required
                />
              </div>

              {/* Buttons */}
              <div className="flex gap-3">
                <button
                  type="submit"
                  className="flex-1 bg-green-600 text-white py-2 rounded-md hover:bg-green-700"
                >
                  Create Device
                </button>
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="flex-1 bg-gray-300 text-gray-700 py-2 rounded-md hover:bg-gray-400"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* EDIT DEVICE MODAL */}
      {showEditModal && selectedDevice && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 w-full max-w-md">
            <h2 className="text-2xl font-bold mb-6">Edit Device</h2>
            
            <form onSubmit={handleEditDevice}>
              {/* Device Name */}
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Device Name *</label>
                <input
                  type="text"
                  value={editFormData.name}
                  onChange={(e) => setEditFormData({...editFormData, name: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>

              {/* Max Consumption */}
              <div className="mb-6">
                <label className="block text-gray-700 mb-2">Max Consumption (Watts) *</label>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  value={editFormData.maxConsumption}
                  onChange={(e) => setEditFormData({...editFormData, maxConsumption: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>

              {/* Buttons */}
              <div className="flex gap-3">
                <button
                  type="submit"
                  className="flex-1 bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700"
                >
                  Save Changes
                </button>
                <button
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  className="flex-1 bg-gray-300 text-gray-700 py-2 rounded-md hover:bg-gray-400"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ASSIGN DEVICE MODAL */}
      {showAssignModal && selectedDevice && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 w-full max-w-md">
            <h2 className="text-2xl font-bold mb-6">Assign Device to User</h2>
            
            {/* Device info */}
            <div className="mb-6 p-4 bg-gray-100 rounded">
              <p className="text-sm text-gray-600">Device: <span className="font-semibold">{selectedDevice.name}</span></p>
              <p className="text-sm text-gray-600">Max Consumption: <span className="font-semibold">{selectedDevice.maxConsumption}W</span></p>
            </div>
            
            {/* Select User */}
            <div className="mb-6">
              <label className="block text-gray-700 mb-2">Select User *</label>
              <select
                value={selectedUserId}
                onChange={(e) => setSelectedUserId(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500"
              >
                <option value="">-- Select a user --</option>
                {users.filter(user => user.role === 'CLIENT').map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.username} ({user.role})
                  </option>
                ))}
              </select>
            </div>

            {/* Error in modal */}
            {error && (
              <div className="mb-4 p-3 bg-red-100 text-red-700 rounded-md text-sm">
                {error}
              </div>
            )}

            {/* Buttons */}
            <div className="flex gap-3">
              <button
                onClick={handleAssignDevice}
                className="flex-1 bg-green-600 text-white py-2 rounded-md hover:bg-green-700"
              >
                Assign Device
              </button>
              <button
                onClick={() => {
                  setShowAssignModal(false);
                  setError('');
                }}
                className="flex-1 bg-gray-300 text-gray-700 py-2 rounded-md hover:bg-gray-400"
              >
                Cancel
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default DevicesManagement;