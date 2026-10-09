import { http } from '@/lib/http';
import type { Device, DeviceRequest } from './types';

export const devicesApi = {
  async list(): Promise<Device[]> {
    const { data } = await http.get<Device[]>('/api/devices');
    return data;
  },

  async listForUser(userId: number): Promise<Device[]> {
    const { data } = await http.get<Device[]>(`/api/devices/user/${userId}`);
    return data;
  },

  async create(request: DeviceRequest): Promise<Device> {
    const { data } = await http.post<Device>('/api/devices', request);
    return data;
  },

  async update(id: number, request: DeviceRequest): Promise<Device> {
    const { data } = await http.put<Device>(`/api/devices/${id}`, request);
    return data;
  },

  async remove(id: number): Promise<void> {
    await http.delete(`/api/devices/${id}`);
  },

  async assign(deviceId: number, userId: number): Promise<void> {
    await http.post('/api/devices/assign', { deviceId, userId });
  },

  async unassign(deviceId: number): Promise<void> {
    await http.delete(`/api/devices/${deviceId}/assignment`);
  },
};