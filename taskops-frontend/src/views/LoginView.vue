<template>
  <section class="panel auth-card">
    <div class="panel-head">
      <p class="eyebrow">登录</p>
      <h1>进入工作台</h1>
      <p class="muted">使用 `auth-service` 中的账号登录系统。</p>
    </div>

    <form class="stack" @submit.prevent="handleLogin">
      <label>
        用户名
        <input v-model.trim="form.username" required />
      </label>
      <label>
        密码
        <input v-model.trim="form.password" type="password" required />
      </label>
      <button class="btn" :disabled="loading">{{ loading ? "登录中..." : "登录" }}</button>
    </form>

    <p v-if="errorText" class="error">{{ errorText }}</p>
    <p class="muted auth-switch">
      还没有账号？
      <RouterLink to="/register">去注册</RouterLink>
    </p>
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { login } from "../api/auth";
import { setAuth } from "../utils/auth";

const router = useRouter();
const loading = ref(false);
const errorText = ref("");
const form = reactive({
  username: "",
  password: ""
});

async function handleLogin() {
  loading.value = true;
  errorText.value = "";
  try {
    const data = await login(form);
    setAuth(data);
    router.push("/dashboard");
  } catch (error) {
    errorText.value = error.message || "登录失败";
  } finally {
    loading.value = false;
  }
}
</script>