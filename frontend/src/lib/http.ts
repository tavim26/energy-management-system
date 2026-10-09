import axios from 'axios';
import { API_URL } from '@/config';
import { readToken } from '@/features/auth/session';

export const http = axios.create({
  baseURL: API_URL,
  headers: { 'Content-Type': 'application/json' },
});

http.interceptors.request.use((config) => {
  const token = readToken();

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

let unauthorizedHandler: (() => void) | null = null;

// Called when the gateway rejects the stored token (for example after it expires)
export function setUnauthorizedHandler(handler: (() => void) | null): void {
  unauthorizedHandler = handler;
}

http.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401 && readToken()) {
      unauthorizedHandler?.();
    }

    return Promise.reject(error);
  },
);