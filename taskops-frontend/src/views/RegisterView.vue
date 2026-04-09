<template>
  <div class="register-view">
    <div class="register-header">
      <div class="register-icon">
        <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
          <circle cx="8.5" cy="7" r="4"/>
          <line x1="20" y1="8" x2="20" y2="14"/>
          <line x1="23" y1="11" x2="17" y2="11"/>
        </svg>
      </div>
      <h1 class="register-title">创建账号</h1>
      <p class="register-subtitle">注册后即可进入统一工作台，完成任务协作和交易流程</p>
    </div>

    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      class="register-form"
      @submit.prevent="handleRegister"
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

      <el-form-item prop="nickname">
        <el-input
          v-model.trim="form.nickname"
          placeholder="请输入昵称"
          prefix-icon="UserFilled"
          clearable
          :disabled="loading"
        />
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          v-model.trim="form.password"
          type="password"
          placeholder="请输入密码（至少 6 位）"
          prefix-icon="Lock"
          show-password
          clearable
          :disabled="loading"
        />
      </el-form-item>

      <el-form-item prop="confirmPassword">
        <el-input
          v-model.trim="form.confirmPassword"
          type="password"
          placeholder="请确认密码"
          prefix-icon="Lock"
          show-password
          clearable
          :disabled="loading"
        />
      </el-form-item>

      <div class="form-notice">
        <el-checkbox v-model="agreeTerms">我已阅读并同意</el-checkbox>
        <a href="#" class="terms-link">《服务条款》</a>
      </div>

      <el-button
        type="primary"
        native-type="submit"
        :loading="loading"
        :disabled="loading || !agreeTerms"
        class="register-btn"
        round
      >
        {{ loading ? "注册中..." : "创建账号" }}
      </el-button>
    </el-form>

    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="false" class="error-alert" />
    <el-alert v-if="successText" :title="successText" type="success" show-icon :closable="false" class="success-alert" />

    <div class="register-footer">
      <span>已有账号？</span>
      <RouterLink to="/login" class="login-link">返回登录</RouterLink>
    </div>

    <div class="register-badges">
      <span class="badge">创建用户</span>
      <span class="badge">默认角色</span>
      <span class="badge">立即可用</span>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from "element-plus";
import { reactive, ref } from "vue";
import { RouterLink, useRouter } from "vue-router";
import { register } from "../api/auth";

const router = useRouter();
const loading = ref(false);
const errorText = ref("");
const successText = ref("");
const agreeTerms = ref(false);
const formRef = ref(null);

const form = reactive({
  username: "",
  password: "",
  confirmPassword: "",
  nickname: ""
});

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const rules = {
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 3, max: 20, message: "用户名长度为 3-20 个字符", trigger: "blur" }
  ],
  nickname: [
    { required: true, message: "请输入昵称", trigger: "blur" },
    { min: 2, max: 30, message: "昵称长度为 2-30 个字符", trigger: "blur" }
  ],
  password: [
    { required: true, message: "请输入密码", trigger: "blur" },
    { min: 6, max: 50, message: "密码至少 6 个字符", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认密码", trigger: "blur" },
    { validator: validateConfirmPassword, trigger: "blur" }
  ]
};

async function handleRegister() {
  if (!formRef.value) return;

  try {
    await formRef.value.validate();
  } catch {
    return;
  }

  loading.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    await register({
      username: form.username,
      password: form.password,
      nickname: form.nickname
    });
    ElMessage.success("注册成功，请登录！");
    setTimeout(() => {
      router.push("/login");
    }, 1500);
  } catch (error) {
    errorText.value = error.message || "注册失败，请稍后重试";
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.register-view {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.register-header {
  text-align: center;
  margin-bottom: 8px;
}

.register-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--accent), #2ea49b);
  border-radius: 20px;
  color: #fff;
  box-shadow: 0 8px 24px rgba(30, 107, 102, 0.3);
}

.register-title {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  color: var(--text);
  margin: 0 0 8px;
  letter-spacing: 0.02em;
}

.register-subtitle {
  font-size: 14px;
  color: var(--muted);
  margin: 0;
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.register-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.register-form :deep(.el-input__wrapper) {
  padding: 6px 16px;
  border-radius: 18px;
  box-shadow: none;
  border: 1px solid rgba(71, 84, 78, 0.15);
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.2s;
}

.register-form :deep(.el-input__wrapper:hover) {
  border-color: rgba(30, 107, 102, 0.3);
}

.register-form :deep(.el-input__wrapper.is-focus) {
  border-color: var(--accent);
  box-shadow: 0 0 0 4px rgba(30, 107, 102, 0.1);
}

.register-form :deep(.el-input__inner) {
  height: 44px;
  font-size: 15px;
}

.register-form :deep(.el-input__prefix) {
  color: var(--muted);
}

.form-notice {
  display: flex;
  align-items: center;
  gap: 4px;
  margin: 8px 0 16px;
  font-size: 13px;
  color: var(--muted);
}

.form-notice :deep(.el-checkbox__label) {
  font-size: 13px;
  color: var(--muted);
}

.terms-link {
  color: var(--accent);
  text-decoration: none;
  transition: color 0.2s;
}

.terms-link:hover {
  color: var(--brand-deep);
}

.register-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.05em;
  border: none;
  background: linear-gradient(135deg, var(--accent), #2ea49b) !important;
  box-shadow: 0 6px 20px rgba(30, 107, 102, 0.3);
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(30, 107, 102, 0.4);
}

.error-alert,
.success-alert {
  border-radius: 16px;
}

.register-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 14px;
  color: var(--muted);
}

.login-link {
  color: var(--accent);
  font-weight: 600;
  text-decoration: none;
  transition: color 0.2s;
}

.login-link:hover {
  color: var(--brand-deep);
}

.register-badges {
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
