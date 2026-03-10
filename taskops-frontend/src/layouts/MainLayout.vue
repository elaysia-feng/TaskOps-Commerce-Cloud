<template>
  <div class="app-shell">
    <aside class="sidebar">
      <div class="sidebar-brand">
        <p class="eyebrow">TaskOps Cloud</p>
        <h2>工作台</h2>
      </div>
      <nav class="nav-list">
        <RouterLink to="/dashboard">首页总览</RouterLink>
        <RouterLink to="/tasks">任务大厅</RouterLink>
        <RouterLink to="/tasks/create">发布任务</RouterLink>
        <RouterLink to="/orders/create">创建订单</RouterLink>
        <RouterLink to="/payments">支付中心</RouterLink>
        <RouterLink to="/account/orders">我的订单</RouterLink>
      </nav>
    </aside>

    <div class="app-main">
      <header class="topbar">
        <div>
          <p class="topbar-label">当前登录</p>
          <strong>{{ username }}</strong>
        </div>
        <button class="btn ghost" @click="logout">退出登录</button>
      </header>
      <main class="page-body">
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