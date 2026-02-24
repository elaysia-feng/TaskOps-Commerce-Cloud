<template>
  <section class="card">
    <h1>控制台</h1>
    <p class="muted">登录后进入业务工作台，可在左侧切换模块。</p>
    <div class="kpi-grid">
      <article>
        <h3>鉴权模式</h3>
        <p>Gateway JWT</p>
      </article>
      <article>
        <h3>当前角色</h3>
        <p>{{ roleText }}</p>
      </article>
      <article>
        <h3>建议操作</h3>
        <p>前往任务列表或创建新任务</p>
      </article>
    </div>
  </section>
</template>

<script setup>
import { computed } from "vue";
import { getUser } from "../utils/auth";

const roleText = computed(() => {
  const roles = getUser()?.roles;
  return Array.isArray(roles) && roles.length ? roles.join(",") : "USER";
});
</script>
