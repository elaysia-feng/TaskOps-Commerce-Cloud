<template>
  <section class="panel auth-card">
    <div class="panel-head">
      <p class="eyebrow">注册</p>
      <h1>创建账号</h1>
      <p class="muted">先在 `auth-service` 中注册账号，再进入系统登录。</p>
    </div>

    <form class="stack" @submit.prevent="handleRegister">
      <label>
        用户名
        <input v-model.trim="form.username" required />
      </label>
      <label>
        密码
        <input v-model.trim="form.password" type="password" required />
      </label>
      <label>
        昵称
        <input v-model.trim="form.nickname" required />
      </label>
      <button class="btn" :disabled="loading">{{ loading ? "注册中..." : "注册" }}</button>
    </form>

    <p v-if="errorText" class="error">{{ errorText }}</p>
    <p v-if="successText" class="success">{{ successText }}</p>
    <p class="muted auth-switch">
      已经有账号？
      <RouterLink to="/login">返回登录</RouterLink>
    </p>
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import { RouterLink } from "vue-router";
import { register } from "../api/auth";

const loading = ref(false);
const errorText = ref("");
const successText = ref("");
const form = reactive({
  username: "",
  password: "",
  nickname: ""
});

async function handleRegister() {
  loading.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    await register(form);
    successText.value = "注册成功，现在可以登录了。";
    form.username = "";
    form.password = "";
    form.nickname = "";
  } catch (error) {
    errorText.value = error.message || "注册失败";
  } finally {
    loading.value = false;
  }
}
</script>