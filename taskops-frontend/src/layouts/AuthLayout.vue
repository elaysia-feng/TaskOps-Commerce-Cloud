<template>
  <div class="auth-shell">
    <!-- Animated Background -->
    <div class="auth-bg">
      <div class="auth-bg-gradient"></div>
      <div class="auth-bg-grid"></div>
      <div class="auth-bg-shapes">
        <div class="auth-shape auth-shape--1"></div>
        <div class="auth-shape auth-shape--2"></div>
        <div class="auth-shape auth-shape--3"></div>
        <div class="auth-shape auth-shape--4"></div>
      </div>
      <div class="auth-bg-particles">
        <span v-for="n in 12" :key="n" class="particle" :style="{ '--i': n }"></span>
      </div>
    </div>

    <!-- Left Branding Panel -->
    <aside class="auth-aside">
      <div class="auth-brand">
        <div class="auth-logo">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <path d="M24 4L44 14V34L24 44L4 34V14L24 4Z" stroke="currentColor" stroke-width="2.5" fill="none"/>
            <path d="M24 14L34 19V29L24 34L14 29V19L24 14Z" fill="currentColor"/>
          </svg>
        </div>
        <h1 class="auth-title">TaskOps Cloud</h1>
        <p class="auth-subtitle">把任务协作做成一个顺手、顺眼、能闭环的工作台</p>
      </div>

      <div class="auth-features">
        <div class="feature-item stagger-item" style="animation-delay: 0.1s">
          <div class="feature-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div class="feature-text">
            <strong>任务全链路</strong>
            <span>发布、接单、验收、结算、提现</span>
          </div>
        </div>
        <div class="feature-item stagger-item" style="animation-delay: 0.2s">
          <div class="feature-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
              <line x1="1" y1="10" x2="23" y2="10"/>
            </svg>
          </div>
          <div class="feature-text">
            <strong>交易闭环</strong>
            <span>订单、支付、钱包一站式管理</span>
          </div>
        </div>
        <div class="feature-item stagger-item" style="animation-delay: 0.3s">
          <div class="feature-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <div class="feature-text">
            <strong>AI 助手</strong>
            <span>会话记忆与流式对话</span>
          </div>
        </div>
      </div>

      <div class="auth-metrics stagger-item" style="animation-delay: 0.4s">
        <div class="metric-item">
          <strong>All In One</strong>
          <span>6 大能力域</span>
        </div>
        <div class="metric-divider"></div>
        <div class="metric-item">
          <strong>User + Admin</strong>
          <span>双角色视角</span>
        </div>
      </div>

      <blockquote class="auth-quote stagger-item" style="animation-delay: 0.5s">
        "从发布一个任务，到验收、结算、提现，全流程在一个界面里就能走完。"
      </blockquote>
    </aside>

    <!-- Right Auth Form Panel -->
    <main class="auth-main">
      <div class="auth-card glass-card">
        <RouterView v-slot="{ Component }">
          <Transition name="fade" mode="out-in">
            <component :is="Component" />
          </Transition>
        </RouterView>
      </div>
    </main>
  </div>
</template>

<script setup>
import { RouterView } from "vue-router";
</script>

<style scoped>
.auth-shell {
  display: grid;
  grid-template-columns: 1fr 1fr;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
}

/* ========================
   Animated Background
   ======================== */
.auth-bg {
  position: fixed;
  inset: 0;
  z-index: 0;
}

.auth-bg-gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #1a1a2e 100%);
}

.auth-bg-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

.auth-bg-shapes {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.auth-shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float-shape 20s ease-in-out infinite;
}

.auth-shape--1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.4), transparent 70%);
  top: -150px;
  left: -100px;
  animation-delay: 0s;
}

.auth-shape--2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(118, 75, 162, 0.4), transparent 70%);
  top: 40%;
  right: -100px;
  animation-delay: -5s;
}

.auth-shape--3 {
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(237, 100, 166, 0.3), transparent 70%);
  bottom: -100px;
  left: 30%;
  animation-delay: -10s;
}

.auth-shape--4 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(79, 172, 254, 0.3), transparent 70%);
  bottom: 20%;
  left: -50px;
  animation-delay: -15s;
}

@keyframes float-shape {
  0%, 100% {
    transform: translate(0, 0) scale(1) rotate(0deg);
  }
  25% {
    transform: translate(30px, -40px) scale(1.1) rotate(5deg);
  }
  50% {
    transform: translate(-20px, 30px) scale(0.95) rotate(-5deg);
  }
  75% {
    transform: translate(40px, 20px) scale(1.05) rotate(3deg);
  }
}

/* Particles */
.auth-bg-particles {
  position: absolute;
  inset: 0;
  overflow: hidden;
}

.particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  left: calc(var(--i) * 8%);
  top: calc(50% + sin(var(--i) * 30deg) * 30%);
  animation: particle-float calc(8s + var(--i) * 0.5s) ease-in-out infinite;
  animation-delay: calc(var(--i) * -0.3s);
}

@keyframes particle-float {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-100px) scale(1.5);
    opacity: 0.8;
  }
}

/* ========================
   Left Panel
   ======================== */
.auth-aside {
  position: relative;
  z-index: 1;
  padding: 60px 52px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff7ef;
  background: linear-gradient(135deg, rgba(26, 26, 46, 0.8), rgba(22, 33, 62, 0.6));
}

.auth-brand {
  margin-bottom: 48px;
}

.auth-logo {
  width: 72px;
  height: 72px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.3), rgba(118, 75, 162, 0.3));
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 22px;
  color: #fff;
  margin-bottom: 24px;
  backdrop-filter: blur(10px);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

.auth-title {
  font-family: var(--font-display);
  font-size: 42px;
  font-weight: 700;
  letter-spacing: 0.02em;
  margin-bottom: 12px;
  background: linear-gradient(135deg, #fff, rgba(255, 255, 255, 0.8));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.auth-subtitle {
  font-size: 16px;
  color: rgba(255, 247, 239, 0.7);
  line-height: 1.6;
  max-width: 380px;
}

/* Features */
.auth-features {
  display: flex;
  flex-direction: column;
  gap: 20px;
  margin-bottom: 40px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 18px;
  backdrop-filter: blur(10px);
  transition: all 0.3s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(8px);
}

.feature-icon {
  width: 44px;
  height: 44px;
  min-width: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.4), rgba(118, 75, 162, 0.4));
  border-radius: 14px;
  color: #fff;
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.feature-text strong {
  font-size: 15px;
  font-weight: 600;
  color: #fff;
}

.feature-text span {
  font-size: 12px;
  color: rgba(255, 247, 239, 0.6);
}

/* Metrics */
.auth-metrics {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 20px;
  margin-bottom: 32px;
}

.metric-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metric-item strong {
  font-size: 18px;
  font-weight: 700;
  color: var(--gold);
}

.metric-item span {
  font-size: 11px;
  color: rgba(255, 247, 239, 0.6);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.metric-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.15);
}

/* Quote */
.auth-quote {
  font-family: var(--font-display);
  font-size: 15px;
  font-style: italic;
  color: rgba(255, 247, 239, 0.7);
  line-height: 1.7;
  padding-left: 20px;
  border-left: 2px solid rgba(229, 191, 114, 0.5);
  margin: 0;
}

/* ========================
   Right Panel
   ======================== */
.auth-main {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: rgba(244, 237, 227, 0.95);
}

.auth-card {
  width: 100%;
  max-width: 480px;
  padding: 48px;
  border-radius: 36px;
  animation: rise 0.6s ease;
}

@keyframes rise {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ========================
   Responsive
   ======================== */
@media (max-width: 1024px) {
  .auth-shell {
    grid-template-columns: 1fr;
  }

  .auth-aside {
    display: none;
  }

  .auth-main {
    padding: 24px;
  }

  .auth-card {
    max-width: 100%;
    padding: 32px;
  }
}
</style>
