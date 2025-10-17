import axios from 'axios';
import { useAuthStore } from '@/stores/auth';

const serverURL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const apiClient = axios.create({
  baseURL: `${serverURL}/api/v1`,
});

apiClient.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  const token = authStore.accessToken;

  if (token) {
    config.headers.Authorization = `Bearer ${ token }`;
  }

  return config;
}, (error) => {
  return Promise.reject(error);
})

export default apiClient;