<template>
  <div class="admin-layout">
    <aside class="sidebar">
      <div class="logo">TaskOps</div>
      <RouterLink to="/dashboard">控制台</RouterLink>
      <RouterLink to="/tasks">任务列表</RouterLink>
      <RouterLink to="/tasks/create">创建任务</RouterLink>
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
