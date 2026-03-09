<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div>
        <p class="hero-tag">TaskOps Cloud</p>
        <div class="logo">控制台</div>
      </div>
      <RouterLink to="/dashboard">首页总览</RouterLink>
      <RouterLink to="/tasks">任务列表</RouterLink>
      <RouterLink to="/tasks/create">创建任务</RouterLink>
      <RouterLink to="/orders/create">创建订单</RouterLink>
      <RouterLink to="/payments">订单支付</RouterLink>
      <RouterLink to="/account/orders">我的订单</RouterLink>
    </aside>

    <div class="content-wrap">
      <header class="admin-topbar">
        <div class="welcome">你好，{{ username }}</div>
        <button class="btn secondary" @click="logout">退出登录</button>
      </header>
      <main class="content-main">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { RouterLink, RouterView, useRouter } from "vue-router";
import { clearAuth, getUser } from "../utils/auth";

const router = useRouter();
const username = computed(() => getUser()?.username || "用户");

function logout() {
  clearAuth();
  router.push("/login");
}
</script>