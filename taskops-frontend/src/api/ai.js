import axios from "axios";
import { clearAuth, getToken } from "../utils/auth";

const aiHttp = axios.create({
  baseURL: import.meta.env.VITE_AI_API_BASE_URL || "/ai-api",
  timeout: 20000
});

aiHttp.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

aiHttp.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload && payload.code === 0) {
      return payload.data;
    }
    return Promise.reject(new Error(payload?.message || "AI 请求失败"));
  },
  (error) => {
    if (error.response?.status === 401) {
      clearAuth();
    }
    return Promise.reject(error);
  }
);

export function getAiSessions(type = "chat") {
  return aiHttp.get(`/ai-proxy/history/${type}`);
}

export function createAiSession(type = "chat") {
  return aiHttp.post("/ai-proxy/session", null, { params: { type } });
}

export function getAiSessionHistory(chatId) {
  return aiHttp.get(`/ai-proxy/history/session/${chatId}`);
}

export function listAiMemories() {
  return aiHttp.get("/ai-proxy/memory");
}

export function listSessionMemories(chatId) {
  return aiHttp.get(`/ai-proxy/memory/session/${chatId}`);
}

export function saveAiMemory(data) {
  return aiHttp.post("/ai-proxy/memory", data);
}

export function updateSessionMemorySelection(data) {
  return aiHttp.put("/ai-proxy/memory/select", data);
}

export function deleteAiMemory(memoryId) {
  return aiHttp.delete(`/ai-proxy/memory/${memoryId}`);
}

export function prepareAiChat(data) {
  return aiHttp.post("/ai-proxy/chat/prepare", data);
}