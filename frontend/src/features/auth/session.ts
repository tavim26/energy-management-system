import type { SessionUser } from './types';

const STORAGE_KEY = 'energy-management.session';

interface StoredSession {
  token: string;
  user: SessionUser;
}

export function readSession(): StoredSession | null {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);

    if (!raw) {
      return null;
    }

    const session = JSON.parse(raw) as StoredSession;

    if (isExpired(session.token)) {
      clearSession();
      return null;
    }

    return session;
  } catch {
    return null;
  }
}

export function readToken(): string | null {
  return readSession()?.token ?? null;
}

export function writeSession(session: StoredSession): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
  } catch {
    // Storage can be unavailable (private mode); the session then lasts until the page is reloaded
  }
}

export function clearSession(): void {
  try {
    localStorage.removeItem(STORAGE_KEY);
  } catch {
    // Nothing to clear
  }
}

// A JWT is "header.payload.signature"; the payload is base64url-encoded JSON with an "exp" field in seconds
function isExpired(token: string): boolean {
  try {
    const payload = token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const { exp } = JSON.parse(atob(payload)) as { exp?: number };
    return typeof exp === 'number' && exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}