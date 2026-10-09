import axios from 'axios';

// The backend returns errors as { status, message, timestamp }
export function getErrorMessage(error: unknown, fallback = 'Something went wrong. Please try again.'): string {
  if (axios.isAxiosError(error)) {
    if (!error.response) {
      return 'The server cannot be reached. Check that the backend is running.';
    }

    const data = error.response.data as { message?: unknown } | undefined;

    if (data && typeof data.message === 'string' && data.message.length > 0) {
      return data.message;
    }
  }

  return fallback;
}