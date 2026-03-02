import http from "./http";

export function createOrder(data) {
  return http.post("/orders", data);
}

export function getOrder(orderNo) {
  return http.get(`/orders/${orderNo}`);
}

export function cancelOrder(orderNo) {
  return http.post(`/orders/${orderNo}/cancel`);
}
