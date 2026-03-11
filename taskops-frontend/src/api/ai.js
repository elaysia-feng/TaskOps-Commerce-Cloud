import axios from "axios";

const aiHttp = axios.create({
  baseURL: import.meta.env.VITE_AI_API_BASE_URL || "/ai-api",
  timeout: 15000
});

aiHttp.interceptors.response.use(
  (response) => {
    const payload = response.data;
    if (payload && payload.code === 0) {
      return payload.data;
    }
    return Promise.reject(new Error(payload?.message || "AI 请求失败"));
  },
  (error) => Promise.reject(error)
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

export function sendAiChat(data) {
  return aiHttp.post("/ai-proxy/chat", data);
}