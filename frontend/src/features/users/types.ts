import type { Role } from '@/features/auth/types';

// username and role are null when the account no longer exists in the Authorization Service
export interface User {
  id: number;
  username: string | null;
  role: Role | null;
  fullName: string | null;
  address: string | null;
}

export interface CreateUserRequest {
  username: string;
  password: string;
  role: Role;
  fullName?: string;
  address?: string;
}

export interface UpdateUserRequest {
  fullName: string;
  address: string;
}