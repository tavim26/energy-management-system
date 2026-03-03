import apiClient from './api';

export const deviceService = {
  // toate device uri pt un user
  async getUserDevices(userId) {
    const response = await apiClient.get(`/api/devices/user/${userId}`);
    return response.data;
  },

  // toate dispozitivele
  async getAllDevices() {
    const response = await apiClient.get('/api/devices');
    return response.data;
  },

  
  async getDeviceById(deviceId) {
    const response = await apiClient.get(`/api/devices/${deviceId}`);
    return response.data;
  },



  // operatii CRUD pe device uri

  async createDevice(deviceData) {
    const response = await apiClient.post('/api/devices', deviceData);
    return response.data;
  },

  async updateDevice(deviceId, deviceData) {
    const response = await apiClient.put(`/api/devices/${deviceId}`, deviceData);
    return response.data;
  },

  async deleteDevice(deviceId) {
    await apiClient.delete(`/api/devices/${deviceId}`);
  },

  async assignDevice(userId, deviceId) {
    const response = await apiClient.post('/api/devices/assign', {
      userId: userId,
      deviceId: deviceId
    });
    return response.data;
  },

};