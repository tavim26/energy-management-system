import { http } from '@/lib/http';
import type { CreateUserRequest, UpdateUserRequest, User } from './types';

export const usersApi = {
  async list(): Promise<User[]> {
    const { data } = await http.get<User[]>('/api/users');
    return data;
  },

  // Accounts are created by the Authorization Service, which also stores the password
  async create(request: CreateUserRequest): Promise<void> {
    await http.post('/api/auth/users', request);
  },

  async update(id: number, request: UpdateUserRequest): Promise<User> {
    const { data } = await http.put<User>(`/api/users/${id}`, request);
    return data;
  },

  async remove(id: number): Promise<void> {
    await http.delete(`/api/users/${id}`);
  },
};