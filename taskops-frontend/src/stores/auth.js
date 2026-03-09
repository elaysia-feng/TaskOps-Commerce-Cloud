import { defineStore } from "pinia";
import { clearAuth, getToken, getUser, setAuth } from "../utils/auth";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: getToken(),
    user: getUser()
  }),
  getters: {
    isLogin: (state) => Boolean(state.token)
  },
  actions: {
    refreshFromLocal() {
      this.token = getToken();
      this.user = getUser();
    },
    loginSuccess(payload) {
      setAuth(payload);
      this.refreshFromLocal();
    },
    logout() {
      clearAuth();
      this.token = "";
      this.user = null;
    }
  }
});
