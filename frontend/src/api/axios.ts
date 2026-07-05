import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { StorageKeys } from '../constants';
import { getItem, setItem } from '../utils/storageUtils';
import { isTokenExpired } from '../utils/jwtUtils';

const api: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

api.interceptors.request.use(
  (config) => {
    const token = getItem(StorageKeys.ACCESS_TOKEN);
    if (token) {
      if (!isTokenExpired(token)) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      // If expired, let it go through or handle refresh synchronously
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    // Centralized error interceptor
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const res = await axios.post(
          `${api.defaults.baseURL}/auth/refresh`,
          {},
          { withCredentials: true }
        );
        const newToken = res.data?.data?.accessToken;
        if (newToken) {
          setItem(StorageKeys.ACCESS_TOKEN, newToken);
          api.defaults.headers.common['Authorization'] = `Bearer ${newToken}`;
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
          return api(originalRequest);
        }
      } catch (refreshError) {
        // Logout or handle refresh failure
        localStorage.removeItem(StorageKeys.ACCESS_TOKEN);
        window.location.href = '/login';
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);

export default api;
