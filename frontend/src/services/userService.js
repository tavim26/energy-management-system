import apiClient from './api';

export const userService = {
 
  async getAllUsers() {
    const response = await apiClient.get('/api/users');
    return response.data;
  },

  async getUserById(userId) {
    const response = await apiClient.get(`/api/users/${userId}`);
    return response.data;
  },

  async getUserByUsername(username) {
    const response = await apiClient.get(`/api/users/username/${username}`);
    return response.data;
  },

  async createUser(userData) {
    const response = await apiClient.post('/api/auth/users', userData);
    return response.data;
  },

  async updateUser(userId, userData) {
    const response = await apiClient.put(`/api/users/${userId}`, userData);
    return response.data;
  },

  async deleteUser(userId) {
    await apiClient.delete(`/api/users/${userId}`);
  }
};