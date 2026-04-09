<template>
  <div class="page-stack">
    <!-- Hero Banner -->
    <section class="hero-banner">
      <div class="hero-content">
        <div class="hero-greeting">
          <span class="greeting-tag">
            <span class="pulse-dot"></span>
            在线
          </span>
        </div>
        <h1>{{ currentUser.username || "当前用户" }} 的协作总览</h1>
        <p>任务配额、热门任务、会话信息和聚合数据集中在一个面板里。</p>
        <div class="hero-actions">
          <RouterLink class="action-btn primary" to="/tasks">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            浏览任务
          </RouterLink>
          <RouterLink class="action-btn outline" to="/wallet">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
              <line x1="1" y1="10" x2="23" y2="10"/>
            </svg>
            查看钱包
          </RouterLink>
        </div>
      </div>

      <div class="hero-aside">
        <div class="stat-card glass-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #667eea, #764ba2)">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
              <circle cx="12" cy="7" r="4"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-label">当前会员</span>
            <strong class="stat-value">{{ membershipLabel(membership.level) }}</strong>
            <p class="stat-caption">剩余 {{ membership.remaining ?? 0 }} 个发布配额</p>
          </div>
        </div>
        <div class="stat-card glass-card">
          <div class="stat-icon" style="background: linear-gradient(135deg, #f093fb, #f5576c)">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <div class="stat-content">
            <span class="stat-label">会话 ID</span>
            <strong class="stat-value small">{{ sessionInfo.sessionId || "未获取" }}</strong>
            <p class="stat-caption">登录时间：{{ formatDateTime(sessionInfo.loginTime) }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats Grid -->
    <section class="stats-grid">
      <div class="stat-item stagger-item" style="animation-delay: 0.1s">
        <div class="stat-item-icon" style="background: linear-gradient(135deg, #4facfe, #00f2fe)">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
        </div>
        <div class="stat-item-content">
          <span class="stat-item-value">{{ membership.usedTasks || 0 }}</span>
          <span class="stat-item-label">已用配额</span>
        </div>
      </div>
      <div class="stat-item stagger-item" style="animation-delay: 0.15s">
        <div class="stat-item-icon" style="background: linear-gradient(135deg, #43e97b, #38f9d7)">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
        </div>
        <div class="stat-item-content">
          <span class="stat-item-value">{{ membership.remaining ?? 0 }}</span>
          <span class="stat-item-label">剩余额度</span>
        </div>
      </div>
      <div class="stat-item stagger-item" style="animation-delay: 0.2s">
        <div class="stat-item-icon" style="background: linear-gradient(135deg, #fa709a, #fee140)">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
          </svg>
        </div>
        <div class="stat-item-content">
          <span class="stat-item-value">{{ hotTasks.length }}</span>
          <span class="stat-item-label">热门任务</span>
        </div>
      </div>
      <div class="stat-item stagger-item" style="animation-delay: 0.25s">
        <div class="stat-item-icon" style="background: linear-gradient(135deg, #a1c4fd, #c2e9fb)">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
          </svg>
        </div>
        <div class="stat-item-content">
          <span class="stat-item-value">{{ canViewAdmin ? "管理员" : "普通用户" }}</span>
          <span class="stat-item-label">当前角色</span>
        </div>
      </div>
    </section>

    <!-- Charts Section -->
    <section class="charts-section" v-if="canViewAdmin">
      <div class="chart-card glass-card">
        <div class="chart-header">
          <div>
            <h3>订单统计</h3>
            <p>近30天订单趋势</p>
          </div>
          <div class="chart-legend">
            <span class="legend-item"><span class="legend-dot" style="background: #667eea"></span>订单数</span>
            <span class="legend-item"><span class="legend-dot" style="background: #f093fb"></span>支付数</span>
          </div>
        </div>
        <div ref="orderChartRef" class="chart-container"></div>
      </div>
      <div class="chart-card glass-card">
        <div class="chart-header">
          <div>
            <h3>任务分布</h3>
            <p>按状态分类统计</p>
          </div>
        </div>
        <div ref="taskChartRef" class="chart-container"></div>
      </div>
    </section>

    <!-- Main Content Grid -->
    <div class="content-grid">
      <!-- Hot Tasks -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">Hot Tasks</p>
            <h3>平台热门任务</h3>
          </div>
          <RouterLink class="card-action" to="/tasks">查看全部</RouterLink>
        </div>

        <div v-if="hotTasks.length" class="task-list">
          <article v-for="(task, index) in hotTasks.slice(0, 4)" :key="task.id" class="task-item hover-lift">
            <div class="task-rank" :class="{ gold: index === 0, silver: index === 1, bronze: index === 2 }">
              {{ index + 1 }}
            </div>
            <div class="task-info">
              <h4>{{ task.title }}</h4>
              <div class="task-meta">
                <span class="task-tag">{{ taskCategoryLabel(task.category) }}</span>
                <span class="task-status" :class="'status-' + task.status.toLowerCase()">{{ taskStatusLabel(task.status) }}</span>
              </div>
            </div>
            <div class="task-reward">
              <strong>{{ formatMoney(task.rewardAmount) }}</strong>
              <span>赏金</span>
            </div>
          </article>
        </div>
        <div v-else class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
          <p>当前暂无热门任务数据</p>
        </div>
      </section>

      <!-- Session Info -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">Session</p>
            <h3>登录会话信息</h3>
          </div>
        </div>

        <div class="session-grid">
          <div class="session-item">
            <span class="session-label">用户名</span>
            <strong>{{ currentUser.username || "-" }}</strong>
          </div>
          <div class="session-item">
            <span class="session-label">用户 ID</span>
            <strong>{{ currentUser.userId || "-" }}</strong>
          </div>
          <div class="session-item">
            <span class="session-label">Session ID</span>
            <strong class="break-all">{{ sessionInfo.sessionId || "未获取" }}</strong>
          </div>
          <div class="session-item">
            <span class="session-label">登录时间</span>
            <strong>{{ formatDateTime(sessionInfo.loginTime) }}</strong>
          </div>
        </div>

        <div class="quick-links">
          <RouterLink class="quick-link" to="/tasks/create">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            发布任务
          </RouterLink>
          <RouterLink class="quick-link" to="/orders/create">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
              <line x1="1" y1="10" x2="23" y2="10"/>
            </svg>
            创建订单
          </RouterLink>
          <RouterLink class="quick-link" to="/ai">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            AI 助手
          </RouterLink>
        </div>
      </section>
    </div>

    <!-- Admin Section -->
    <template v-if="canViewAdmin">
      <section class="admin-section">
        <div class="admin-header">
          <h3>管理员聚合数据</h3>
          <span class="admin-badge">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
            </svg>
            管理员视图
          </span>
        </div>

        <div class="admin-grid">
          <!-- Order Summary -->
          <div class="admin-card glass-card">
            <div class="card-header">
              <div>
                <p class="card-eyebrow">Order Summary</p>
                <h3>订单统计摘要</h3>
              </div>
            </div>
            <div class="admin-metrics">
              <div v-for="item in orderSummaryEntries" :key="item.key" class="admin-metric">
                <span class="admin-metric-label">{{ item.label }}</span>
                <strong class="admin-metric-value">{{ formatCompactNumber(item.value) }}</strong>
              </div>
            </div>
          </div>

          <!-- Pay Summary -->
          <div class="admin-card glass-card">
            <div class="card-header">
              <div>
                <p class="card-eyebrow">Pay Summary</p>
                <h3>支付统计摘要</h3>
              </div>
            </div>
            <div class="admin-metrics">
              <div v-for="item in paySummaryEntries" :key="item.key" class="admin-metric">
                <span class="admin-metric-label">{{ item.label }}</span>
                <strong class="admin-metric-value">{{ formatCompactNumber(item.value) }}</strong>
              </div>
            </div>
          </div>

          <!-- Login Logs -->
          <div class="admin-card glass-card log-card">
            <div class="card-header">
              <div>
                <p class="card-eyebrow">Login Logs</p>
                <h3>最近登录日志</h3>
              </div>
            </div>
            <div v-if="loginLogs.length" class="log-list">
              <div v-for="(log, index) in loginLogs.slice(0, 6)" :key="index" class="log-item">
                <div class="log-avatar">{{ (log.username || log.userId || "?").slice(0, 1).toUpperCase() }}</div>
                <div class="log-content">
                  <strong>{{ log.username || log.userId || "未知用户" }}</strong>
                  <p>IP: {{ log.ip || log.loginIp || "-" }} · {{ log.result || log.status || "SUCCESS" }}</p>
                </div>
                <span class="log-time">{{ formatDateTime(log.loginTime || log.createdAt) }}</span>
              </div>
            </div>
            <div v-else class="empty-state">
              <p>当前没有可展示的登录日志</p>
            </div>
          </div>
        </div>
      </section>
    </template>

    <el-alert v-if="pageError" :title="pageError" type="error" show-icon :closable="true" class="error-alert" />
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from "vue";
import { RouterLink } from "vue-router";
import * as echarts from "echarts";
import { getDashboard } from "../api/admin";
import { getSessionMe } from "../api/auth";
import { getHotTasks, getMembership } from "../api/task";
import { getUser } from "../utils/auth";
import {
  formatCompactNumber,
  formatDateTime,
  formatMoney,
  humanize,
  isAdmin,
  membershipLabel,
  taskCategoryLabel,
  taskStatusLabel
} from "../utils/format";

const currentUser = getUser() || { roles: [] };
const canViewAdmin = isAdmin(currentUser);
const membership = reactive({});
const sessionInfo = reactive({});
const hotTasks = ref([]);
const loginLogs = ref([]);
const pageError = ref("");
const orderSummary = reactive({});
const paySummary = reactive({});

const orderChartRef = ref(null);
const taskChartRef = ref(null);
let orderChart = null;
let taskChart = null;

const orderSummaryEntries = computed(() =>
  Object.entries(orderSummary).map(([key, value]) => ({ key, label: humanize(key), value }))
);
const paySummaryEntries = computed(() =>
  Object.entries(paySummary).map(([key, value]) => ({ key, label: humanize(key), value }))
);

onMounted(async () => {
  const jobs = [loadMembership(), loadSession(), loadHotTasks()];
  if (canViewAdmin) {
    jobs.push(loadDashboard());
  }
  await Promise.allSettled(jobs);

  if (canViewAdmin) {
    initCharts();
  }
});

onUnmounted(() => {
  if (orderChart) orderChart.dispose();
  if (taskChart) taskChart.dispose();
});

function initCharts() {
  // Order Chart
  if (orderChartRef.value) {
    orderChart = echarts.init(orderChart.value);
    const orderOption = {
      backgroundColor: "transparent",
      tooltip: {
        trigger: "axis",
        backgroundColor: "rgba(255, 255, 255, 0.95)",
        borderColor: "rgba(71, 84, 78, 0.15)",
        borderWidth: 1,
        textStyle: { color: "#172124" },
        axisPointer: { type: "shadow" }
      },
      grid: { left: "3%", right: "4%", bottom: "3%", top: "10%", containLabel: true },
      xAxis: {
        type: "category",
        data: ["近7天", "近14天", "近21天", "近30天"],
        axisLine: { lineStyle: { color: "rgba(71, 84, 78, 0.2)" } },
        axisLabel: { color: "#6d746c" }
      },
      yAxis: {
        type: "value",
        axisLine: { show: false },
        splitLine: { lineStyle: { color: "rgba(71, 84, 78, 0.1)" } },
        axisLabel: { color: "#6d746c" }
      },
      series: [
        {
          name: "订单数",
          type: "bar",
          barWidth: "35%",
          itemStyle: {
            borderRadius: [8, 8, 0, 0],
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: "#667eea" },
              { offset: 1, color: "#764ba2" }
            ])
          },
          data: [orderSummary.pendingPay || 0, orderSummary.paid || 0, orderSummary.done || 0, Object.values(orderSummary).reduce((a, b) => a + b, 0)]
        },
        {
          name: "支付数",
          type: "line",
          smooth: true,
          symbol: "circle",
          symbolSize: 8,
          lineStyle: { color: "#f093fb", width: 3 },
          itemStyle: { color: "#f093fb" },
          areaStyle: {
            color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
              { offset: 0, color: "rgba(240, 147, 251, 0.3)" },
              { offset: 1, color: "rgba(240, 147, 251, 0)" }
            ])
          },
          data: [Math.floor((orderSummary.pendingPay || 0) * 0.6), Math.floor((orderSummary.paid || 0) * 0.8), orderSummary.paid || 0, orderSummary.done || 0]
        }
      ]
    };
    orderChart.setOption(orderOption);
    window.addEventListener("resize", () => orderChart?.resize());
  }

  // Task Distribution Chart
  if (taskChartRef.value) {
    taskChart = echarts.init(taskChartRef.value);
    const taskOption = {
      backgroundColor: "transparent",
      tooltip: {
        trigger: "item",
        backgroundColor: "rgba(255, 255, 255, 0.95)",
        borderColor: "rgba(71, 84, 78, 0.15)",
        textStyle: { color: "#172124" }
      },
      series: [
        {
          type: "pie",
          radius: ["45%", "75%"],
          center: ["50%", "50%"],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 12,
            borderColor: "#fff",
            borderWidth: 3
          },
          label: {
            show: true,
            position: "outside",
            formatter: "{b}\n{d}%",
            color: "#6d746c",
            fontSize: 12
          },
          emphasis: {
            label: { show: true, fontSize: 14, fontWeight: "bold", color: "#172124" }
          },
          data: [
            { value: orderSummary.open || 5, name: "待接单", itemStyle: { color: "#43e97b" } },
            { value: orderSummary.taken || 3, name: "已接单", itemStyle: { color: "#4facfe" } },
            { value: orderSummary.submitted || 2, name: "待验收", itemStyle: { color: "#fddb92" } },
            { value: orderSummary.approved || 4, name: "已结算", itemStyle: { color: "#a1c4fd" } }
          ]
        }
      ]
    };
    taskChart.setOption(taskOption);
    window.addEventListener("resize", () => taskChart?.resize());
  }
}

async function loadMembership() {
  try {
    Object.assign(membership, await getMembership());
  } catch (error) {
    pageError.value = error.message || "加载会员配额失败";
  }
}

async function loadSession() {
  try {
    Object.assign(sessionInfo, await getSessionMe());
  } catch (error) {
    if (!pageError.value) {
      pageError.value = error.message || "加载会话信息失败";
    }
  }
}

async function loadHotTasks() {
  try {
    hotTasks.value = await getHotTasks();
  } catch (error) {
    if (!pageError.value) {
      pageError.value = error.message || "加载热门任务失败";
    }
  }
}

async function loadDashboard() {
  try {
    const data = await getDashboard(8);
    Object.assign(orderSummary, data.orderSummary || {});
    Object.assign(paySummary, data.paySummary || {});
    loginLogs.value = data.loginLogs || [];
    if (Array.isArray(data.hotTasks) && data.hotTasks.length) {
      hotTasks.value = data.hotTasks;
    }
  } catch (error) {
    if (!pageError.value) {
      pageError.value = error.message || "管理员聚合数据加载失败";
    }
  }
}
</script>

<style scoped>
.page-stack {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* ========================
   Hero Banner
   ======================== */
.hero-banner {
  position: relative;
  overflow: hidden;
  border-radius: 32px;
  padding: 36px;
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(280px, 0.6fr);
  gap: 28px;
  color: #fff7ef;
  background: linear-gradient(135deg, #1f5f63 0%, #21444b 40%, #2d3a4a 100%);
  box-shadow: 0 20px 60px rgba(40, 32, 19, 0.2);
}

.hero-banner::before {
  content: "";
  position: absolute;
  top: -50%;
  right: -20%;
  width: 600px;
  height: 600px;
  background: radial-gradient(circle, rgba(102, 126, 234, 0.3), transparent 60%);
  pointer-events: none;
}

.hero-banner::after {
  content: "";
  position: absolute;
  bottom: -30%;
  left: 10%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(229, 191, 114, 0.2), transparent 60%);
  pointer-events: none;
}

.hero-content {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-greeting {
  display: flex;
  align-items: center;
  gap: 12px;
}

.greeting-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.hero-content h1 {
  font-family: var(--font-display);
  font-size: clamp(36px, 4vw, 52px);
  line-height: 1;
  letter-spacing: 0.02em;
  margin: 0;
}

.hero-content p {
  font-size: 15px;
  line-height: 1.6;
  color: rgba(255, 247, 239, 0.8);
  margin: 0;
  max-width: 480px;
}

.hero-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 22px;
  border-radius: 18px;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.25s;
}

.action-btn.primary {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.35);
}

.action-btn.primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.45);
}

.action-btn.outline {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.action-btn.outline:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.hero-aside {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 24px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: all 0.3s;
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(-4px);
}

.stat-icon {
  width: 52px;
  height: 52px;
  min-width: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  color: #fff;
}

.stat-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
  overflow: hidden;
}

.stat-label {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: rgba(255, 247, 239, 0.6);
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.stat-value.small {
  font-size: 14px;
}

.stat-caption {
  font-size: 11px;
  color: rgba(255, 247, 239, 0.5);
  margin: 0;
}

/* ========================
   Stats Grid
   ======================== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--surface);
  border-radius: 24px;
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 48px rgba(65, 42, 21, 0.15);
}

.stat-item-icon {
  width: 48px;
  height: 48px;
  min-width: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  color: #fff;
}

.stat-item-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-item-value {
  font-size: 26px;
  font-weight: 700;
  color: var(--text);
  line-height: 1;
}

.stat-item-label {
  font-size: 12px;
  color: var(--muted);
}

/* ========================
   Charts Section
   ======================== */
.charts-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.chart-card {
  padding: 24px;
  border-radius: 28px;
}

.chart-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}

.chart-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--text);
}

.chart-header p {
  font-size: 12px;
  color: var(--muted);
  margin: 0;
}

.chart-legend {
  display: flex;
  gap: 16px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--muted);
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.chart-container {
  width: 100%;
  height: 260px;
}

/* ========================
   Content Grid
   ======================== */
.content-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 20px;
}

.content-card {
  padding: 24px;
  border-radius: 28px;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  margin-bottom: 20px;
}

.card-eyebrow {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--brand);
  margin: 0 0 4px;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: var(--text);
}

.card-action {
  font-size: 13px;
  color: var(--accent);
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.card-action:hover {
  color: var(--brand-deep);
}

/* Task List */
.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  border: 1px solid var(--line);
  transition: all 0.3s;
}

.task-rank {
  width: 36px;
  height: 36px;
  min-width: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  font-weight: 800;
  font-size: 14px;
  background: rgba(109, 116, 108, 0.15);
  color: var(--muted);
}

.task-rank.gold {
  background: linear-gradient(135deg, #ffd700, #ffb347);
  color: #8B6914;
}

.task-rank.silver {
  background: linear-gradient(135deg, #c0c0c0, #a8a8a8);
  color: #666;
}

.task-rank.bronze {
  background: linear-gradient(135deg, #cd7f32, #b87333);
  color: #fff;
}

.task-info {
  flex: 1;
  overflow: hidden;
}

.task-info h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 6px;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-meta {
  display: flex;
  gap: 8px;
}

.task-tag {
  padding: 3px 10px;
  font-size: 11px;
  background: rgba(30, 107, 102, 0.1);
  color: var(--accent);
  border-radius: 999px;
}

.task-status {
  padding: 3px 10px;
  font-size: 11px;
  border-radius: 999px;
}

.task-status.status-open {
  background: rgba(29, 122, 95, 0.12);
  color: var(--success);
}

.task-status.status-taken {
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
}

.task-status.status-submitted {
  background: rgba(229, 191, 114, 0.15);
  color: #b8860b;
}

.task-status.status-settled {
  background: rgba(29, 122, 95, 0.12);
  color: var(--success);
}

.task-reward {
  text-align: right;
}

.task-reward strong {
  display: block;
  font-size: 16px;
  font-weight: 700;
  color: var(--brand-deep);
}

.task-reward span {
  font-size: 11px;
  color: var(--muted);
}

/* Session Grid */
.session-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.session-item {
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  border: 1px solid var(--line);
}

.session-label {
  display: block;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 6px;
}

.session-item strong {
  font-size: 13px;
  color: var(--text);
}

.session-item .break-all {
  word-break: break-all;
  font-size: 12px;
}

/* Quick Links */
.quick-links {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.quick-link {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 18px;
  border: 1px solid var(--line);
  text-decoration: none;
  color: var(--text);
  font-size: 12px;
  font-weight: 500;
  transition: all 0.25s;
}

.quick-link:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(65, 42, 21, 0.1);
}

.quick-link svg {
  color: var(--accent);
}

/* ========================
   Admin Section
   ======================== */
.admin-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.admin-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.admin-header h3 {
  font-size: 20px;
  font-weight: 600;
  margin: 0;
  color: var(--text);
}

.admin-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: rgba(173, 65, 54, 0.1);
  color: var(--danger);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.admin-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.admin-card {
  padding: 24px;
  border-radius: 28px;
}

.admin-metrics {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.admin-metric {
  padding: 14px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 14px;
  text-align: center;
}

.admin-metric-label {
  display: block;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 6px;
}

.admin-metric-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text);
}

/* Log Card */
.log-card {
  grid-column: span 1;
}

.log-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 280px;
  overflow-y: auto;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 14px;
}

.log-avatar {
  width: 36px;
  height: 36px;
  min-width: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 10px;
  font-weight: 700;
  font-size: 13px;
}

.log-content {
  flex: 1;
  overflow: hidden;
}

.log-content strong {
  display: block;
  font-size: 13px;
  color: var(--text);
  margin-bottom: 2px;
}

.log-content p {
  font-size: 11px;
  color: var(--muted);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.log-time {
  font-size: 11px;
  color: var(--muted);
  white-space: nowrap;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px;
  text-align: center;
  color: var(--muted);
}

.empty-state svg {
  opacity: 0.4;
}

.empty-state p {
  margin: 0;
  font-size: 13px;
}

/* Error Alert */
.error-alert {
  border-radius: 16px;
}

/* ========================
   Responsive
   ======================== */
@media (max-width: 1200px) {
  .hero-banner {
    grid-template-columns: 1fr;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .charts-section,
  .content-grid,
  .admin-grid {
    grid-template-columns: 1fr;
  }
}
</style>
