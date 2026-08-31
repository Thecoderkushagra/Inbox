import { ApiResponse, RefreshTokenResponse } from '@/types';
import {
  getAccessTokenCookie,
  getRefreshTokenCookie,
  setAccessTokenCookie,
  setRefreshTokenCookie,
  clearAuthCookies,
} from '@/utils/cookies';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || import.meta.env.VITE_API_URL || '';

export class ApiError extends Error {
  status: number;
  data: unknown;

  constructor(message: string, status: number, data?: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

export const formatErrorMessage = (resData: any, status: number): string => {
  // If validation errors list is present
  if (Array.isArray(resData?.errors) && resData.errors.length > 0) {
    return resData.errors.map((e: any) => e.message || e.defaultMessage || e).join(', ');
  }
  if (resData?.errors && typeof resData.errors === 'object') {
    const messages = Object.values(resData.errors).flat();
    if (messages.length > 0) {
      return messages.join(', ');
    }
  }

  const raw = resData?.message || resData?.error?.message || resData?.error || '';
  if (typeof raw === 'string' && raw.trim().length > 0) {
    if (raw.toLowerCase().includes('invalid credentials')) {
      return 'Incorrect username or password.';
    }
    if (raw.toLowerCase().includes('password must be strong') || raw.toLowerCase().includes('strongpassword')) {
      return 'Weak password: Must be at least 8 characters with 1 uppercase, 1 lowercase, 1 number, and 1 special symbol.';
    }
    if (raw.toLowerCase().includes('email is already in use')) {
      return 'An account with this email already exists.';
    }
    if (raw.toLowerCase().includes('username is already taken')) {
      return 'This username is already taken.';
    }
    if (raw.toLowerCase().includes('passwords do not match')) {
      return 'Passwords do not match.';
    }
    return raw;
  }

  if (status === 401) return 'Unauthorized. Please sign in again.';
  if (status === 403) return 'You do not have permission to perform this action.';
  if (status === 404) return 'The requested resource was not found.';
  if (status === 429) return 'Too many requests. Please slow down.';
  if (status >= 500) return 'Server error. Please try again in a few moments.';

  return 'An unexpected error occurred.';
};

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: unknown) => void;
  reject: (reason?: unknown) => void;
}> = [];

const processQueue = (error: Error | null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve();
    }
  });
  failedQueue = [];
};

export async function apiClient<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE_URL}${endpoint}`;
  
  // Read token from cookies (with localStorage fallback)
  const token = getAccessTokenCookie() || localStorage.getItem('access_token');

  const headers = new Headers(options.headers || {});
  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }
  if (token) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const response = await fetch(url, {
    ...options,
    headers,
  });

  if (response.status === 401 && !endpoint.includes('/api/v1/auth/')) {
    const refreshToken = getRefreshTokenCookie() || localStorage.getItem('refresh_token');
    if (refreshToken && !isRefreshing) {
      isRefreshing = true;
      try {
        const refreshRes = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken }),
        });

        if (refreshRes.ok) {
          const resJson = await refreshRes.json();
          const tokenData: RefreshTokenResponse = resJson.data || resJson;
          
          setAccessTokenCookie(tokenData.accessToken);
          setRefreshTokenCookie(tokenData.refreshToken);
          localStorage.setItem('access_token', tokenData.accessToken);
          localStorage.setItem('refresh_token', tokenData.refreshToken);
          processQueue(null);

          // Retry original request
          headers.set('Authorization', `Bearer ${tokenData.accessToken}`);
          const retryRes = await fetch(url, { ...options, headers });
          if (!retryRes.ok) {
            const retryErr = await retryRes.json().catch(() => ({}));
            throw new ApiError(formatErrorMessage(retryErr, retryRes.status), retryRes.status, retryErr);
          }
          const retryData: ApiResponse<T> = await retryRes.json();
          return (retryData.data !== undefined ? retryData.data : retryData) as T;
        } else {
          throw new Error('Refresh token invalid');
        }
      } catch (err) {
        processQueue(err as Error);
        clearAuthCookies();
        localStorage.removeItem('access_token');
        localStorage.removeItem('refresh_token');
        localStorage.removeItem('current_user');
        window.location.href = '/login';
        throw new ApiError('Session expired. Please log in again.', 401);
      } finally {
        isRefreshing = false;
      }
    } else if (isRefreshing) {
      await new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      });
      const newToken = getAccessTokenCookie() || localStorage.getItem('access_token');
      if (newToken) {
        headers.set('Authorization', `Bearer ${newToken}`);
      }
      const retryRes = await fetch(url, { ...options, headers });
      const retryData: ApiResponse<T> = await retryRes.json();
      return (retryData.data !== undefined ? retryData.data : retryData) as T;
    }
  }

  if (response.status === 204) {
    return {} as T;
  }

  const resData = await response.json().catch(() => ({}));

  if (!response.ok) {
    const message = formatErrorMessage(resData, response.status);
    throw new ApiError(message, response.status, resData);
  }

  return (resData.data !== undefined ? resData.data : resData) as T;
}
