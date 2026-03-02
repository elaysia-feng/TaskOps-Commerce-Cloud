<template>
  <div class="auth-layout">
    <aside class="auth-side">
      <p class="hero-tag">TaskOps Platform</p>
      <h1>Build Reliable Microservice Workflows</h1>
      <p>面向任务、订单、支付链路的一体化联调工作台。</p>
      <ul>
        <li>Gateway + JWT 统一鉴权</li>
        <li>Sentinel 流控与降级</li>
        <li>多服务链路快速验证</li>
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