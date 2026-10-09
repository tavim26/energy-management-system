export type Role = 'ADMIN' | 'CLIENT';

export interface SessionUser {
  userId: number;
  username: string;
  role: Role;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse extends SessionUser {
  token: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  fullName?: string;
  address?: string;
}