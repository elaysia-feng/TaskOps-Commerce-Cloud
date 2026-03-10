<template>
  <section class="page-grid">
    <div class="panel hero-panel">
      <p class="eyebrow">平台总览</p>
      <h1>当前任务接单平台概览</h1>
      <p class="muted">
        前端已经切换为接单平台视角，任务大厅和热门任务都会优先展示高赏金任务。
      </p>
    </div>

    <div class="stats-grid">
      <article class="panel stat-card">
        <p class="stat-label">会员等级</p>
        <strong>{{ membership.level || "免费" }}</strong>
        <span>{{ membership.usedTasks || 0 }} / {{ membership.maxTasks || 0 }} 个发布额度已使用</span>
      </article>
      <article class="panel stat-card">
        <p class="stat-label">剩余额度</p>
        <strong>{{ membership.remaining ?? 0 }}</strong>
        <span>当前还能继续发布的任务数</span>
      </article>
      <article class="panel stat-card">
        <p class="stat-label">订单汇总</p>
        <strong>{{ orderSummary.total ?? "-" }}</strong>
        <span>管理端可见时展示汇总信息</span>
      </article>
      <article class="panel stat-card">
        <p class="stat-label">支付汇总</p>
        <strong>{{ paySummary.total ?? "-" }}</strong>
        <span>当前支付服务聚合数据</span>
      </article>
    </div>

    <section class="panel">
      <div class="panel-head">
        <p class="eyebrow">热门任务</p>
        <h2>高赏金优先</h2>
      </div>
      <div v-if="hotTasks.length" class="list-grid">
        <article v-for="task in hotTasks" :key="task.id" class="list-card marketplace-card">
          <header>
            <div>
              <h3>{{ task.title }}</h3>
              <p class="muted">{{ categoryLabel(task.category) }} · {{ statusLabel(task.status) }}</p>
            </div>
            <div class="price-box">
              <strong>{{ formatMoney(task.rewardAmount) }}</strong>
              <span>赏金</span>
            </div>
          </header>
          <p class="muted clamp">{{ task.description }}</p>
          <div class="meta-line task-meta-wrap">
            <span>地点：{{ task.location || "线上交付" }}</span>
            <span>优先级：P{{ task.priority }}</span>
            <span>截止：{{ formatDateTime(task.deadline) }}</span>
          </div>
        </article>
      </div>
      <p v-else class="muted">暂无热门任务数据。</p>
    </section>

    <section class="panel" v-if="dashboardError">
      <p class="eyebrow">提示</p>
      <h2>管理信息不可用</h2>
      <p class="muted">{{ dashboardError }}</p>
    </section>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { getDashboard } from "../api/admin";
import { getMembership, getHotTasks } from "../api/task";

const membership = reactive({});
const hotTasks = ref([]);
const orderSummary = reactive({});
const paySummary = reactive({});
const dashboardError = ref("");

onMounted(async () => {
  await Promise.all([loadMembership(), loadHotTasks(), loadDashboard()]);
});

function formatMoney(amount) {
  return `¥${Number(amount || 0).toFixed(2)}`;
}

function formatDateTime(value) {
  if (!value) {
    return "未设置";
  }
  return String(value).replace("T", " ").slice(0, 16);
}

function statusLabel(status) {
  const map = {
    OPEN: "待接单",
    TAKEN: "已接单",
    SUBMITTED: "待验收",
    APPROVED: "已验收",
    REJECTED: "已驳回",
    SETTLED: "已结算",
    CANCELLED: "已取消"
  };
  return map[status] || status;
}

function categoryLabel(category) {
  const map = {
    ERRAND: "跑腿",
    DESIGN: "设计",
    TECH: "技术",
    CONSULT: "咨询",
    GENERAL: "综合"
  };
  return map[category] || (category || "综合");
}

async function loadMembership() {
  try {
    Object.assign(membership, await getMembership());
  } catch (error) {
    dashboardError.value = error.message || "加载会员配额失败。";
  }
}

async function loadHotTasks() {
  try {
    hotTasks.value = await getHotTasks();
  } catch (error) {
    if (!dashboardError.value) {
      dashboardError.value = error.message || "加载热门任务失败。";
    }
  }
}

async function loadDashboard() {
  try {
    const data = await getDashboard(10);
    Object.assign(orderSummary, data.orderSummary || {});
    Object.assign(paySummary, data.paySummary || {});
    if (Array.isArray(data.hotTasks) && data.hotTasks.length) {
      hotTasks.value = data.hotTasks;
    }
  } catch (error) {
    if (!dashboardError.value) {
      dashboardError.value = "当前用户没有管理权限，已自动降级展示普通看板。";
    }
  }
}
</script>