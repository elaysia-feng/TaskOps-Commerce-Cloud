<template>
  <section class="card auth-card">
    <h2>账号登录</h2>

    <div class="steps">
      <span :class="{ active: step === 1 }">1. 账号认证</span>
      <span :class="{ active: step === 2 }">2. 安全确认</span>
    </div>

    <form class="form-grid" v-if="step === 1" @submit.prevent="goNextStep">
      <label>
        用户名
        <input v-model.trim="form.username" required />
      </label>

      <label>
        密码
        <input v-model.trim="form.password" type="password" required />
      </label>

      <button class="btn">下一步</button>
      <p class="error" v-if="errorText">{{ errorText }}</p>
    </form>

    <form class="form-grid" v-else @submit.prevent="handleLogin">
      <p class="muted">即将登录账号：{{ form.username }}</p>
      <label>
        输入确认码：<strong>{{ challenge }}</strong>
        <input v-model.trim="confirmCode" placeholder="请输入上方确认码" required />
      </label>

      <div class="action-row">
        <button type="button" class="btn secondary" @click="step = 1">上一步</button>
        <button class="btn" :disabled="loading">{{ loading ? "登录中..." : "确认登录" }}</button>
      </div>

      <p class="error" v-if="errorText">{{ errorText }}</p>
    </form>
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { login } from "../api/auth";
import { setAuth } from "../utils/auth";

const router = useRouter();
const step = ref(1);
const loading = ref(false);
const errorText = ref("");
const confirmCode = ref("");
const challenge = ref(createChallenge());

const form = reactive({
  username: "",
  password: ""
});

function createChallenge() {
  return Math.random().toString(36).slice(2, 6).toUpperCase();
}

function goNextStep() {
  errorText.value = "";
  if (!form.username || !form.password) {
    errorText.value = "请输入用户名和密码";
    return;
  }
  confirmCode.value = "";
  challenge.value = createChallenge();
  step.value = 2;
}

async function handleLogin() {
  if (confirmCode.value.toUpperCase() !== challenge.value) {
    errorText.value = "确认码错误，请重试";
    return;
  }

  loading.value = true;
  errorText.value = "";
  try {
    const data = await login(form);
    setAuth({
      token: data.token,
      user: {
        userId: data.userId,
        username: data.username,
        roles: data.roles
      }
    });
    router.push("/dashboard");
  } catch (error) {
    errorText.value = error.message || "登录失败";
  } finally {
    loading.value = false;
  }
}
</script>
