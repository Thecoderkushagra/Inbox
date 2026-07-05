import api from '../api/axios';
import { ApiEndpoints } from '../constants';
import type { User, ApiResponse } from '../types';

export interface LoginRequest {
  email: string;
  password?: string;
  otp?: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface RegisterRequest {
  email: string;
  username: string;
  password?: string;
}

export const AuthService = {
  login: async (data: LoginRequest) => {
    const response = await api.post<ApiResponse<LoginResponse>>(ApiEndpoints.AUTH.LOGIN, data);
    return response.data;
  },

  register: async (data: RegisterRequest) => {
    const response = await api.post<ApiResponse<void>>(ApiEndpoints.AUTH.REGISTER, data);
    return response.data;
  },

  verifyOtp: async (email: string, otp: string) => {
    const response = await api.post<ApiResponse<LoginResponse>>('/auth/verify-otp', { email, otp });
    return response.data;
  },

  forgotPassword: async (email: string) => {
    const response = await api.post<ApiResponse<void>>('/auth/forgot-password', { email });
    return response.data;
  },

  resetPassword: async (token: string, password: string) => {
    const response = await api.post<ApiResponse<void>>('/auth/reset-password', { token, password });
    return response.data;
  },

  logout: async () => {
    const response = await api.post<ApiResponse<void>>(ApiEndpoints.AUTH.LOGOUT);
    return response.data;
  },

  refreshToken: async () => {
    const response = await api.post<ApiResponse<{ accessToken: string }>>(ApiEndpoints.AUTH.REFRESH);
    return response.data;
  },

  getCurrentUser: async () => {
    const response = await api.get<ApiResponse<User>>(ApiEndpoints.AUTH.ME);
    return response.data;
  },
};
