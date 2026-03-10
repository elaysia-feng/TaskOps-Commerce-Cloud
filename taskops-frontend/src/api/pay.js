import http from "./http";

export function createPay(orderNo) {
  return http.post("/pay/create", null, { params: { orderNo } });
}

export function getPayDetail(orderNo) {
  return http.get(`/pay/${orderNo}`);
}

export function closePay(orderNo) {
  return http.post(`/pay/${orderNo}/close`);
}
