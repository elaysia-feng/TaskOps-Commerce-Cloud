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
