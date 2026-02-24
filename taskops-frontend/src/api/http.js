import axios from "axios";
import { clearAuth, getToken } from "../utils/auth";

const http = axios.create({
  baseURL: "/api",
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload && payload.code === 0) {
      return payload.data;
    }
    const msg = payload?.message || "请求失败";
    return Promise.reject(new Error(msg));
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuth();
    }
    return Promise.reject(error);
  }
);

export default http;
