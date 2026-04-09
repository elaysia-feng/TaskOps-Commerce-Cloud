<template>
  <div class="login-view">
    <div class="login-header">
      <div class="login-icon">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
          <circle cx="12" cy="7" r="4"/>
        </svg>
      </div>
      <h1 class="login-title">欢迎回来</h1>
      <p class="login-subtitle">登录后即可使用任务、订单、支付、钱包和 AI 全部能力</p>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      class="login-form"
      @submit.prevent="handleLogin"
      size="large"
    >
      <el-form-item prop="username">
        <el-input
          v-model.trim="form.username"
          placeholder="请输入用户名"
          prefix-icon="User"
          clearable
          :disabled="loading"
        />
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          v-model.trim="form.password"
          type="password"
          placeholder="请输入密码"
          prefix-icon="Lock"
          show-password
          clearable
          :disabled="loading"
          @keyup.enter="handleLogin"
        />
      </el-form-item>

      <div class="form-options">
        <el-checkbox v-model="rememberMe">记住我</el-checkbox>
        <a href="#" class="forgot-link">忘记密码？</a>
      </div>

      <el-button
        type="primary"
        native-type="submit"
        :loading="loading"
        :disabled="loading"
        class="login-btn"
        round
      >
        {{ loading ? "登录中..." : "立即登录" }}
      </el-button>
    </el-form>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" class="error-alert" />

    <div class="login-footer">
      <span>还没有账号？</span>
      <RouterLink to="/register" class="register-link">去注册</RouterLink>
    </div>

    <div class="login-badges">
      <span class="badge">JWT 鉴权</span>
      <span class="badge">安全加密</span>
      <span class="badge">会话追踪</span>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from "element-plus";
import { reactive, ref } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { login } from "../api/auth";
import { setAuth } from "../utils/auth";

const router = useRouter();
const loading = ref(false);
const errorText = ref("");
const rememberMe = ref(false);
const formRef = ref(null);

const form = reactive({
  username: "",
  password: ""
});

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "用户名长度为 3-20 个字符", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 50, message: "密码至少 6 个字符", trigger: "blur" }
  ]
};

async function handleLogin() {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  loading.value = true;
  errorText.value = "";
  try {
    const data = await login(form);
    setAuth(data);
    ElMessage.success("登录成功，欢迎回来！");
    router.push("/dashboard");
  } catch (error) {
    errorText.value = error.message || "登录失败，请检查用户名和密码";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.login-header {
  text-align: center;
  margin-bottom: 8px;
}

.login-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--brand), var(--brand-deep));
  border-radius: 20px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(182, 90, 57, 0.3);
}

.login-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  color: var(--text);
  margin: 0 0 8px;
  letter-spacing: 0.02em;
}

.login-subtitle {
  font-size: 14px;
  color: var(--muted);
  margin: 0;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.login-form :deep(.el-input__wrapper) {
  padding: 6px 16px;
  border-radius: 18px;
  box-shadow: none;
  border: 1px solid rgba(71, 84, 78, 0.15);
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.2s;
}

.login-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(30, 107, 102, 0.3);
}

.login-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--accent);
  box-shadow: 0 0 0 4px rgba(30, 107, 102, 0.1);
}

.login-form :deep(.el-input__inner) {
  height: 44px;
  font-size: 15px;
}

.login-form :deep(.el-input__prefix) {
  color: var(--muted);
}

.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin: 8px 0 16px;
}

.form-options :deep(.el-checkbox__label) {
  font-size: 13px;
  color: var(--muted);
}

.forgot-link {
  font-size: 13px;
  color: var(--accent);
  text-decoration: none;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: var(--brand-deep);
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.05em;
  border: none;
  background: linear-gradient(135deg, var(--brand), var(--brand-deep)) !important;
  box-shadow: 0 6px 20px rgba(182, 90, 57, 0.3);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(182, 90, 57, 0.4);
}

.error-alert {
  border-radius: 16px;
}

.login-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  color: var(--muted);
}

.register-link {
  color: var(--accent);
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}

.register-link:hover {
  color: var(--brand-deep);
}

.login-badges {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 8px;
}

.badge {
  padding: 6px 12px;
  font-size: 11px;
  color: var(--muted);
  background: rgba(255, 255, 255, 0.7);
  border-radius: 999px;
  border: 1px solid rgba(71, 84, 78, 0.1);
}
</style>
