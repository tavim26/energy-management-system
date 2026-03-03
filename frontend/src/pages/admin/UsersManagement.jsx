import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from '../../components/layout/Navbar';
import { userService } from '../../services/userService';

function UsersManagement() {
  const navigate = useNavigate();
  
  // states
  const [users, setUsers] = useState([]);   //lista useri
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  

  //state ptr modale
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  
  // form data pentru modal create
  const [createFormData, setCreateFormData] = useState({
    username: '',
    password: '',
    role: 'CLIENT',
    fullName: '',
    address: ''
  });
  
  // Form data ptr modal edit
  const [editFormData, setEditFormData] = useState({
    fullName: '',
    address: ''
  });



  // incarca useri la mount
  useEffect(() => {
    loadUsers();
  }, []);


  // GET all users
  const loadUsers = async () => {
    try {

      setLoading(true);
      setError('');
      const data = await userService.getAllUsers();
      setUsers(data);

    } catch (err) {

      console.error('Error loading users:', err);
      setError('Failed to load users');

    } finally {
      setLoading(false);
    }
  };



  // CREATE user
  const handleCreateUser = async (e) => {
    e.preventDefault();
    
    try {
      setError('');
      
      // POST /api/users
      await userService.createUser(createFormData);
      
      await loadUsers();
      
      //reset form si close modal
      setShowCreateModal(false);
      setCreateFormData({
        username: '',
        password: '',
        role: 'CLIENT',
        fullName: '',
        address: ''
      });
      
      alert('User created successfully!');
      
    } catch (err) {
      console.error('Error creating user:', err);
      setError(err.response?.data?.message || 'Failed to create user');
    }
  };



  // EDIT user
  const handleEditUser = async (e) => {
    e.preventDefault();
    
    try {
      setError('');
      
      // PUT /api/users/{id}
      await userService.updateUser(selectedUser.id, editFormData);
      
      await loadUsers();
      
      // close modal
      setShowEditModal(false);
      setSelectedUser(null);
      
      alert('User updated successfully!');
      
    } catch (err) {
      console.error('Error updating user:', err);
      setError(err.response?.data?.message || 'Failed to update user');
    }
  };


  // DELETE user
  const handleDeleteUser = async (userId, username) => {

    const confirmed = window.confirm(
      `Are you sure you want to delete user "${username}"? This action cannot be undone.`
    );
    
    if (!confirmed) return;
    
    try {
      setError('');
      
      await userService.deleteUser(userId); // DELETE /api/users/{id}
      
      await loadUsers();
      
      alert('User deleted successfully!');
      
    } catch (err) {
      console.error('Error deleting user:', err);
      setError(err.response?.data?.message || 'Failed to delete user');
    }
  };



  // open edit modal with user existing data
  const openEditModal = (user) => {
    
    setSelectedUser(user);  //keep user pt referinta

    setEditFormData({
      fullName: user.fullName || '',
      address: user.address || ''
    });
    setShowEditModal(true);
  };




  // render UI
  return (
    <div className="min-h-screen bg-gray-100">
      <Navbar />

      <div className="container mx-auto p-6">
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-3xl font-bold text-gray-800">Users Management</h1>
            <p className="text-gray-600 mt-1">Manage all user accounts</p>
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
              className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded transition"
            >
              + Create User
            </button>
          </div>
        </div>

        {error && (
          <div className="bg-red-100 text-red-700 p-4 rounded-md mb-4">
            {error}
          </div>
        )}

        {/* tabel useri */}
        <div className="bg-white rounded-lg shadow-md overflow-hidden">
          {loading ? (
            <div className="p-8 text-center text-gray-600">
              Loading users...
            </div>
          ) : users.length === 0 ? (
            <div className="p-8 text-center text-gray-600">
              No users found
            </div>
          ) : (
            <table className="w-full">
              <thead className="bg-gray-50 border-b">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Username</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Role</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Full Name</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Address</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {users.map((user) => (
                  <tr key={user.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 text-sm text-gray-900">{user.id}</td>
                    <td className="px-6 py-4 text-sm text-gray-900 font-medium">{user.username}</td>
                    <td className="px-6 py-4 text-sm">
                      <span className={`px-2 py-1 rounded-full text-xs font-semibold ${
                        user.role === 'ADMIN' 
                          ? 'bg-purple-100 text-purple-800' 
                          : 'bg-blue-100 text-blue-800'
                      }`}>
                        {user.role}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-900">{user.fullName || '-'}</td>
                    <td className="px-6 py-4 text-sm text-gray-900">{user.address || '-'}</td>
                    <td className="px-6 py-4 text-sm">
                      <button
                        onClick={() => openEditModal(user)}
                        className="text-blue-600 hover:text-blue-800 mr-3 font-medium"
                      >
                        Edit
                      </button>
                      <button
                        onClick={() => handleDeleteUser(user.id, user.username)}
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
      </div>

      {/* Create User Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 w-full max-w-md">
            <h2 className="text-2xl font-bold mb-6">Create New User</h2>
            
            <form onSubmit={handleCreateUser}>

              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Username *</label>
                <input
                  type="text"
                  value={createFormData.username}
                  onChange={(e) => setCreateFormData({...createFormData, username: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                />
              </div>

              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Password *</label>
                <input
                  type="password"
                  value={createFormData.password}
                  onChange={(e) => setCreateFormData({...createFormData, password: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  required
                  minLength="6"
                />
              </div>

       
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Role *</label>
                <select
                  value={createFormData.role}
                  onChange={(e) => setCreateFormData({...createFormData, role: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="CLIENT">CLIENT</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>

           
              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Full Name</label>
                <input
                  type="text"
                  value={createFormData.fullName}
                  onChange={(e) => setCreateFormData({...createFormData, fullName: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

        
              <div className="mb-6">
                <label className="block text-gray-700 mb-2">Address</label>
                <input
                  type="text"
                  value={createFormData.address}
                  onChange={(e) => setCreateFormData({...createFormData, address: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

           
              <div className="flex gap-3">
                <button
                  type="submit"
                  className="flex-1 bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700"
                >
                  Create User
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

      {/* Edit User Modal */}
      {showEditModal && selectedUser && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 w-full max-w-md">
            <h2 className="text-2xl font-bold mb-6">Edit User</h2>
            
            <div className="mb-4 p-3 bg-gray-100 rounded">
              <p className="text-sm text-gray-600">Username: <span className="font-semibold">{selectedUser.username}</span></p>
              <p className="text-sm text-gray-600">Role: <span className="font-semibold">{selectedUser.role}</span></p>
              <p className="text-xs text-gray-500 mt-1">Note: Username and Role cannot be changed</p>
            </div>
            
            <form onSubmit={handleEditUser}>

              <div className="mb-4">
                <label className="block text-gray-700 mb-2">Full Name</label>
                <input
                  type="text"
                  value={editFormData.fullName}
                  onChange={(e) => setEditFormData({...editFormData, fullName: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="mb-6">
                <label className="block text-gray-700 mb-2">Address</label>
                <input
                  type="text"
                  value={editFormData.address}
                  onChange={(e) => setEditFormData({...editFormData, address: e.target.value})}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

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
    </div>
  );
}

export default UsersManagement;