import { http } from '@/lib/http';
import type { LoginRequest, LoginResponse, RegisterRequest } from './types';

export const authApi = {
  async login(request: LoginRequest): Promise<LoginResponse> {
    const { data } = await http.post<LoginResponse>('/api/auth/login', request);
    return data;
  },

  async register(request: RegisterRequest): Promise<void> {
    await http.post('/api/auth/register', request);
  },
};