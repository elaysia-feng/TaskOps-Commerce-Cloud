import http from "./http";

export function getWalletOverview(userId) {
  return http.get("/wallet/me", { params: { userId } });
}

export function getWalletFlows(params) {
  return http.get("/wallet/flows", { params });
}

export function applyWithdraw(userId, data) {
  return http.post("/wallet/withdraw", data, { params: { userId } });
}

export function getMyWithdraws(params) {
  return http.get("/wallet/withdraws", { params });
}

export function getWalletOverviewByAdmin(userId) {
  return http.get(`/admin/wallet/user/${userId}`);
}

export function getAllWithdraws(params) {
  return http.get("/admin/wallet/withdraws", { params });
}

export function approveWithdraw(withdrawId, auditBy) {
  return http.post(`/admin/wallet/withdraw/${withdrawId}/approve`, null, {
    params: auditBy ? { auditBy } : undefined
  });
}

export function rejectWithdraw(withdrawId, data, auditBy) {
  return http.post(`/admin/wallet/withdraw/${withdrawId}/reject`, data, {
    params: auditBy ? { auditBy } : undefined
  });
}