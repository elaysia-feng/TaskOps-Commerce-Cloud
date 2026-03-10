<template>
  <div class="auth-shell">
    <aside class="auth-aside">
      <p class="eyebrow">TaskOps Cloud</p>
      <h1>任务、订单、支付一体化协同。</h1>
      <p class="auth-copy">
        当前前端已经对齐仓库里的真实微服务路由，覆盖认证、任务、订单、支付和后台聚合能力。
      </p>
      <ul class="auth-points">
        <li>JWT 登录与路由鉴权</li>
        <li>任务发布配额与会员视图</li>
        <li>订单创建、支付中心与我的订单</li>
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