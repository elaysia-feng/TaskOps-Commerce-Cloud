<template>
  <section class="card auth-card">
    <h1>注册</h1>
    <form class="form-grid" @submit.prevent="handleRegister">
      <label>
        用户名
        <input v-model.trim="form.username" required />
      </label>

      <label>
        昵称
        <input v-model.trim="form.nickname" required />
      </label>

      <label>
        密码（6-32位）
        <input v-model.trim="form.password" type="password" minlength="6" maxlength="32" required />
      </label>

      <button class="btn" :disabled="loading">{{ loading ? "注册中..." : "注册" }}</button>
      <p class="error" v-if="errorText">{{ errorText }}</p>
      <p class="success" v-if="successText">{{ successText }}</p>
    </form>
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import { register } from "../api/auth";

const loading = ref(false);
const errorText = ref("");
const successText = ref("");

const form = reactive({
  username: "",
  nickname: "",
  password: ""
});

async function handleRegister() {
  loading.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    await register(form);
    successText.value = "注册成功，请前往登录页面。";
    form.username = "";
    form.nickname = "";
    form.password = "";
  } catch (error) {
    errorText.value = error.message || "注册失败";
  } finally {
    loading.value = false;
  }
}
</script>
