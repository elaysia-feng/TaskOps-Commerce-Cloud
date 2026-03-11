import http from "./http";

export function searchTasks(data) {
  return http.post("/tasks/search", data);
}

export function createTask(data) {
  return http.post("/tasks", data);
}

export function getHotTasks() {
  return http.get("/tasks/hot");
}

export function getTaskDetail(id) {
  return http.get(`/tasks/${id}`);
}

export function getMembership() {
  return http.get("/tasks/membership/me");
}

export function switchMembership(level) {
  return http.put(`/tasks/membership/me/${level}`);
}

export function acceptTask(id) {
  return http.post(`/tasks/${id}/accept`);
}

export function submitTask(id, data) {
  return http.post(`/tasks/${id}/submit`, data);
}

export function approveTask(id) {
  return http.post(`/tasks/${id}/approve`);
}

export function rejectTask(id, data) {
  return http.post(`/tasks/${id}/reject`, data);
}

export function getPublishedTasks(params) {
  return http.get("/tasks/mine/published", { params });
}

export function getAcceptedTasks(params) {
  return http.get("/tasks/mine/accept", { params });
}

export function getReviewTasks(params) {
  return http.get("/tasks/mine/review", { params });
}