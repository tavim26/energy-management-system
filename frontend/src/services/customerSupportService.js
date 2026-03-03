import apiClient from './api';

export const customerSupportService = {
  // Trimite mesaj la chatbot
  async sendMessage(userId, message) {
    const response = await apiClient.post('/api/support/message', {
      userId: userId,
      message: message,
    });
    return response.data;
  },
};