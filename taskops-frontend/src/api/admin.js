import http from "./http";

export function getDashboard(loginLogLimit = 10) {
  return http.get("/admin/dashboard", { params: { loginLogLimit } });
}
