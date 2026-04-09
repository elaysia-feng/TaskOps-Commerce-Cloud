<template>
  <div class="app-shell">
    <!-- Animated Background Shapes -->
    <div class="bg-shapes" aria-hidden="true">
      <div class="bg-shape bg-shape--1"></div>
      <div class="bg-shape bg-shape--2"></div>
      <div class="bg-shape bg-shape--3"></div>
    </div>

    <!-- Sidebar -->
    <aside class="sidebar">
      <!-- Brand Header -->
      <div class="sidebar-header">
        <div class="brand-logo">
          <div class="brand-icon">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
              <path d="M14 2L26 8V20L14 26L2 20V8L14 2Z" stroke="currentColor" stroke-width="2" fill="none"/>
              <path d="M14 10L20 13V19L14 22L8 19V13L14 10Z" fill="currentColor"/>
            </svg>
          </div>
          <div class="brand-text">
            <p class="brand-name">TaskOps</p>
            <p class="brand-tagline">Cloud Studio</p>
          </div>
        </div>
        <button class="sidebar-toggle" @click="sidebarCollapsed = !sidebarCollapsed" :title="sidebarCollapsed ? '展开' : '收起'">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
            <path v-if="sidebarCollapsed" d="M6 3l5 5-5 5"/>
            <path v-else d="M10 3L5 8l5 5"/>
          </svg>
        </button>
      </div>

      <!-- Navigation Groups -->
      <nav class="nav-container">
        <div v-for="group in navGroups" :key="group.label" class="nav-group" :class="{ collapsed: sidebarCollapsed }">
          <p class="nav-group-label" v-show="!sidebarCollapsed">{{ group.label }}</p>
          <div class="nav-list">
            <RouterLink
              v-for="(item, index) in group.items"
              :key="item.to"
              :to="item.to"
              class="nav-item hover-lift"
              :style="{ animationDelay: `${index * 0.05}s` }"
              :title="sidebarCollapsed ? item.label : ''"
            >
              <span class="nav-icon" :style="{ background: item.iconBg, color: item.iconColor }">
                {{ item.icon }}
              </span>
              <Transition name="fade">
                <span v-if="!sidebarCollapsed" class="nav-content">
                  <strong>{{ item.label }}</strong>
                  <small>{{ item.description }}</small>
                </span>
              </Transition>
            </RouterLink>
          </div>
        </div>
      </nav>

      <!-- User Footer -->
      <div class="sidebar-footer glass-card--dark" :class="{ collapsed: sidebarCollapsed }">
        <div class="user-info" v-if="!sidebarCollapsed">
          <div class="avatar-badge">{{ initials }}</div>
          <div class="user-details">
            <strong>{{ username }}</strong>
            <p class="user-role">{{ roleText }}</p>
          </div>
        </div>
        <button class="logout-btn" @click="logout" :title="sidebarCollapsed ? '退出登录' : ''">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          <span v-if="!sidebarCollapsed">退出</span>
        </button>
      </div>
    </aside>

    <!-- Main Content Area -->
    <div class="app-main" :class="{ expanded: sidebarCollapsed }">
      <!-- Topbar with Breadcrumbs -->
      <header class="topbar">
        <div class="topbar-left">
          <nav class="breadcrumb" aria-label="Breadcrumb">
            <RouterLink to="/dashboard" class="breadcrumb-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
              </svg>
            </RouterLink>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-current">{{ route.meta.eyebrow || '工作台' }}</span>
          </nav>
          <div class="page-title">
            <h2>{{ route.meta.title || '工作台' }}</h2>
            <p class="page-description">{{ route.meta.description || '统一任务协作与支付结算面板' }}</p>
          </div>
        </div>

        <div class="topbar-right">
          <div class="quick-actions">
            <RouterLink class="action-btn primary" to="/tasks/create">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19"/>
                <line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
              发布任务
            </RouterLink>
          </div>
          <div class="user-menu glass-card">
            <div class="avatar-badge small">{{ initials }}</div>
            <div class="user-menu-info">
              <strong>{{ username }}</strong>
              <p>{{ roleText }}</p>
            </div>
          </div>
        </div>
      </header>

      <!-- Page Body with Transition -->
      <main class="page-body">
        <RouterView v-slot="{ Component, route: currentRoute }">
          <Transition name="page" mode="out-in">
            <component :is="Component" :key="currentRoute.path" />
          </Transition>
        </RouterView>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from "vue";
import { RouterLink, RouterView, useRoute, useRouter } from "vue-router";
import { clearAuth, getUser } from "../utils/auth";
import { isAdmin } from "../utils/format";

const router = useRouter();
const route = useRoute();
const sidebarCollapsed = ref(false);

const user = computed(() => getUser() || { roles: [] });
const username = computed(() => user.value.username || "用户");
const initials = computed(() => username.value.slice(0, 1).toUpperCase());
const isAdminUser = computed(() => isAdmin(user.value));
const roleText = computed(() => (isAdminUser.value ? "管理员" : "普通用户"));

const navGroups = computed(() => [
  {
    label: "总览与任务",
    items: [
      {
        to: "/dashboard",
        icon: "DK",
        iconBg: "linear-gradient(135deg, #667eea, #764ba2)",
        iconColor: "#fff",
        label: "总览看板",
        description: "热门任务与数据聚合"
      },
      {
        to: "/tasks",
        icon: "TK",
        iconBg: "linear-gradient(135deg, #f093fb, #f5576c)",
        iconColor: "#fff",
        label: "任务大厅",
        description: "筛选任务并接单"
      },
      {
        to: "/tasks/create",
        icon: "ST",
        iconBg: "linear-gradient(135deg, #4facfe, #00f2fe)",
        iconColor: "#fff",
        label: "发布与管理",
        description: "发布任务、切换会员"
      },
      {
        to: "/tasks/accepted",
        icon: "GO",
        iconBg: "linear-gradient(135deg, #43e97b, #38f9d7)",
        iconColor: "#fff",
        label: "我的接单",
        description: "提交成果并跟进状态"
      },
      {
        to: "/tasks/review",
        icon: "RV",
        iconBg: "linear-gradient(135deg, #fa709a, #fee140)",
        iconColor: "#fff",
        label: "待我验收",
        description: "通过或驳回提交"
      }
    ]
  },
  {
    label: "交易与账户",
    items: [
      {
        to: "/orders/create",
        icon: "OD",
        iconBg: "linear-gradient(135deg, #a18cd1, #fbc2eb)",
        iconColor: "#fff",
        label: "创建订单",
        description: "从目录快速发起订单"
      },
      {
        to: "/payments",
        icon: "PY",
        iconBg: "linear-gradient(135deg, #ff9a9e, #fecfef)",
        iconColor: "#fff",
        label: "支付中心",
        description: "支付单创建与查询"
      },
      {
        to: "/account/orders",
        icon: "MY",
        iconBg: "linear-gradient(135deg, #a1c4fd, #c2e9fb)",
        iconColor: "#fff",
        label: "我的订单",
        description: "订单详情与取消"
      },
      {
        to: "/wallet",
        icon: "WL",
        iconBg: "linear-gradient(135deg, #fddb92, #d1fdff)",
        iconColor: "#fff",
        label: "钱包中心",
        description: "余额、提现与审核"
      }
    ]
  },
  {
    label: "智能助手",
    items: [
      {
        to: "/ai",
        icon: "AI",
        iconBg: "linear-gradient(135deg, #667eea, #764ba2)",
        iconColor: "#fff",
        label: "AI 助手",
        description: "会话、记忆与流式对话"
      }
    ]
  }
]);

function logout() {
  clearAuth();
  router.push("/login");
}
</script>

<style scoped>
.app-shell {
  display: grid;
  grid-template-columns: 280px 1fr;
  min-height: 100vh;
  position: relative;
}

/* ========================
   Sidebar
   ======================== */
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: 280px;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  color: #eef7f4;
  background: linear-gradient(180deg, rgba(14, 27, 30, 0.98), rgba(20, 34, 37, 0.95));
  backdrop-filter: blur(20px);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  z-index: 100;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.sidebar.collapsed {
  width: 80px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 8px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-logo {
  display: flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
}

.brand-icon {
  width: 44px;
  height: 44px;
  min-width: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--gold), #f6d8a4);
  color: #1d2528;
  border-radius: 14px;
  box-shadow: 0 4px 16px rgba(229, 191, 114, 0.3);
}

.brand-text {
  overflow: hidden;
  transition: opacity 0.2s;
}

.sidebar.collapsed .brand-text {
  opacity: 0;
  width: 0;
}

.brand-name {
  font-size: 18px;
  font-weight: 800;
  color: #fff;
  letter-spacing: 0.02em;
  white-space: nowrap;
}

.brand-tagline {
  font-size: 11px;
  color: rgba(238, 247, 244, 0.6);
  letter-spacing: 0.08em;
  text-transform: uppercase;
  white-space: nowrap;
}

.sidebar-toggle {
  width: 28px;
  height: 28px;
  min-width: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.06);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  color: rgba(238, 247, 244, 0.7);
  cursor: pointer;
  transition: all 0.2s;
}

.sidebar-toggle:hover {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

/* ========================
   Navigation
   ======================== */
.nav-container {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding-right: 4px;
}

.nav-container::-webkit-scrollbar {
  width: 4px;
}

.nav-container::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

.nav-group {
  margin-bottom: 24px;
}

.nav-group-label {
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: rgba(238, 247, 244, 0.45);
  padding: 0 12px;
  margin-bottom: 10px;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
}

.nav-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid transparent;
  color: rgba(238, 247, 244, 0.8);
  text-decoration: none;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(229, 191, 114, 0.2);
  transform: translateX(4px);
}

.nav-item.router-link-active {
  background: rgba(255, 255, 255, 0.1);
  border-color: rgba(229, 191, 114, 0.3);
  color: #fff;
}

.nav-icon {
  width: 38px;
  height: 38px;
  min-width: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-weight: 800;
  font-size: 12px;
  letter-spacing: 0.02em;
  transition: transform 0.2s;
}

.nav-item:hover .nav-icon {
  transform: scale(1.05);
}

.nav-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
  flex: 1;
}

.nav-content strong {
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
}

.nav-content small {
  font-size: 11px;
  color: rgba(238, 247, 244, 0.5);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* Collapsed state */
.nav-group.collapsed .nav-item {
  justify-content: center;
  padding: 12px;
}

.nav-group.collapsed .nav-icon {
  margin: 0;
}

/* ========================
   Sidebar Footer
   ======================== */
.sidebar-footer {
  border-radius: 20px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s;
}

.sidebar-footer.collapsed {
  padding: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.sidebar-footer.collapsed .user-info {
  display: none;
}

.avatar-badge {
  width: 42px;
  height: 42px;
  min-width: 42px;
  background: linear-gradient(135deg, var(--accent), #2ea49b);
  color: #fff;
  font-weight: 800;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  box-shadow: 0 4px 12px rgba(30, 107, 102, 0.3);
}

.avatar-badge.small {
  width: 36px;
  height: 36px;
  min-width: 36px;
  font-size: 14px;
  border-radius: 12px;
}

.user-details strong {
  display: block;
  font-size: 14px;
  color: #fff;
  white-space: nowrap;
}

.user-role {
  font-size: 11px;
  color: rgba(238, 247, 244, 0.5);
  margin-top: 2px;
}

.user-menu-info strong {
  display: block;
  font-size: 13px;
  color: var(--text);
  white-space: nowrap;
}

.user-menu-info p {
  font-size: 11px;
  color: var(--muted);
  margin-top: 2px;
  white-space: nowrap;
}

.logout-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px;
  border-radius: 14px;
  background: rgba(173, 65, 54, 0.15);
  border: 1px solid rgba(173, 65, 54, 0.2);
  color: rgba(238, 121, 108, 0.9);
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(173, 65, 54, 0.25);
  color: #ef7a6d;
  transform: translateY(-1px);
}

.sidebar-footer.collapsed .logout-btn span {
  display: none;
}

/* ========================
   Main Content
   ======================== */
.app-main {
  margin-left: 280px;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.app-main.expanded {
  margin-left: 80px;
}

/* ========================
   Topbar
   ======================== */
.topbar {
  position: sticky;
  top: 0;
  z-index: 50;
  padding: 20px 32px;
  background: rgba(244, 237, 227, 0.85);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(71, 84, 78, 0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
}

.topbar-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--muted);
}

.breadcrumb-item {
  display: flex;
  align-items: center;
  color: var(--muted);
  transition: color 0.2s;
}

.breadcrumb-item:hover {
  color: var(--accent);
}

.breadcrumb-separator {
  color: rgba(109, 116, 108, 0.4);
}

.breadcrumb-current {
  color: var(--text);
  font-weight: 500;
}

.page-title h2 {
  font-family: var(--font-display);
  font-size: 32px;
  font-weight: 700;
  color: var(--text);
  letter-spacing: 0.02em;
  margin: 0;
}

.page-description {
  font-size: 13px;
  color: var(--muted);
  margin: 4px 0 0;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.quick-actions {
  display: flex;
  gap: 10px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  border-radius: 16px;
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s;
}

.action-btn.primary {
  background: linear-gradient(135deg, var(--brand), var(--brand-deep));
  color: #fff8f3;
  box-shadow: 0 4px 16px rgba(182, 90, 57, 0.25);
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(182, 90, 57, 0.35);
}

.user-menu {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 18px;
}

/* ========================
   Page Body
   ======================== */
.page-body {
  padding: 28px 32px;
  flex: 1;
}

/* ========================
   Responsive
   ======================== */
@media (max-width: 1200px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .sidebar.open {
    transform: translateX(0);
  }

  .app-main {
    margin-left: 0;
  }

  .app-main.expanded {
    margin-left: 0;
  }
}
</style>
