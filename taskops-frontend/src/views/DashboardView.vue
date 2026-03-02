<template>
  <section class="card">
    <h1>控制台</h1>
    <p class="muted">登录后可直接在左侧切换模块进行联调与压测。</p>

    <div class="kpi-grid">
      <article>
        <h3>鉴权</h3>
        <p>Gateway JWT</p>
      </article>
      <article>
        <h3>角色</h3>
        <p>{{ roleText }}</p>
      </article>
      <article>
        <h3>链路</h3>
        <p>Order -> Pay -> Order</p>
      </article>
    </div>

    <div class="task-grid mt12">
      <article class="task-card">
        <header>
          <h3>今日建议</h3>
          <span class="tag">Sentinel</span>
        </header>
        <p>优先验证登录、下单、支付接口的限流与熔断策略。</p>
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