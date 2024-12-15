import axios from 'axios';

const API_URL = 'http://localhost:8080/api/auth/';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export const login = async (data: LoginRequest) => {
  const response = await axios.post(API_URL + 'login', data);
  if (response.data.token) {
    localStorage.setItem('token', response.data.token);
    localStorage.setItem('refreshToken', response.data.refreshToken);
    localStorage.setItem('user', JSON.stringify({
      username: response.data.username,
      roles: response.data.roles
    }));
  }
  return response;
};

export const register = async (data: RegisterRequest) => {
  return axios.post(API_URL + 'register', data);
};

export const logout = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('user');
};

export const setupAxiosInterceptors = (token: string) => {
  axios.interceptors.request.use(
    (config) => {
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    },
    (error) => {
      return Promise.reject(error);
    }
  );
}; 