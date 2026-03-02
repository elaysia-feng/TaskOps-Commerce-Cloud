import http from "./http";

export function mockPayCallback(orderNo) {
  return http.post("/pay/callback/mock", { orderNo });
}
