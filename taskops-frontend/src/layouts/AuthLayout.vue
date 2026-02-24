<template>
  <div class="auth-layout">
    <aside class="auth-side">
      <h1>TaskOps Cloud</h1>
      <p>企业任务协同平台</p>
      <ul>
        <li>统一网关鉴权</li>
        <li>微服务拆分</li>
        <li>可观测与权限控制</li>
      </ul>
    </aside>
    <main class="auth-main">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { RouterView } from "vue-router";
</script>
