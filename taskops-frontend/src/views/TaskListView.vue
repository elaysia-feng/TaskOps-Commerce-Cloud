<template>
  <section class="page-grid">
    <section class="panel">
      <div class="panel-head">
        <p class="eyebrow">任务大厅</p>
        <h1>接单任务列表</h1>
        <p class="muted">按赏金优先展示待接单任务，方便直接查看收益和截止时间。</p>
      </div>

      <form class="filter-grid task-filter-grid" @submit.prevent="loadTasks">
        <label>
          关键词
          <input v-model.trim="filters.keyword" placeholder="标题 / 描述 / 标签 / 地点" />
        </label>
        <label>
          状态
          <select v-model="filters.status">
            <option value="">默认只看待接单</option>
            <option value="OPEN">待接单</option>
            <option value="TAKEN">已接单</option>
            <option value="SUBMITTED">待验收</option>
            <option value="SETTLED">已结算</option>
            <option value="CANCELLED">已取消</option>
          </select>
        </label>
        <label>
          分类
          <select v-model="filters.category">
            <option value="">全部分类</option>
            <option value="ERRAND">跑腿</option>
            <option value="DESIGN">设计</option>
            <option value="TECH">技术</option>
            <option value="CONSULT">咨询</option>
            <option value="GENERAL">综合</option>
          </select>
        </label>
        <label>
          最低优先级
          <input v-model.number="filters.minPriority" type="number" min="1" max="5" />
        </label>
        <label>
          最高优先级
          <input v-model.number="filters.maxPriority" type="number" min="1" max="5" />
        </label>
        <button class="btn" :disabled="loading">{{ loading ? "加载中..." : "搜索任务" }}</button>
      </form>

      <p v-if="errorText" class="error">{{ errorText }}</p>

      <div class="list-grid" v-if="tasks.length">
        <article v-for="task in tasks" :key="task.id" class="list-card marketplace-card">
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
          <div class="meta-line task-meta-wrap">
            <span>服务费：{{ formatMoney(task.serviceFee) }}</span>
            <span>预计到手：{{ formatMoney(task.settleAmount) }}</span>
            <span>{{ task.proofRequired ? "需提交凭证" : "无需凭证" }}</span>
          </div>
          <div class="meta-line task-meta-wrap">
            <span>浏览：{{ task.viewCount || 0 }}</span>
            <span>评论：{{ task.commentCount || 0 }}</span>
            <span>单号：{{ task.tradeOrderNo || "-" }}</span>
          </div>
        </article>
      </div>
      <p v-else class="muted">当前没有符合条件的任务。</p>
    </section>

    <section class="panel">
      <div class="panel-head">
        <p class="eyebrow">发布额度</p>
        <h2>当前会员配额</h2>
      </div>
      <div class="meta-stack">
        <div class="meta-row"><span>会员等级</span><strong>{{ membership.level || "FREE" }}</strong></div>
        <div class="meta-row"><span>最大发布数</span><strong>{{ membership.maxTasks || 0 }}</strong></div>
        <div class="meta-row"><span>已使用</span><strong>{{ membership.usedTasks || 0 }}</strong></div>
        <div class="meta-row"><span>剩余可发</span><strong>{{ membership.remaining || 0 }}</strong></div>
      </div>
    </section>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { getMembership, searchTasks } from "../api/task";

const loading = ref(false);
const errorText = ref("");
const tasks = ref([]);
const membership = reactive({});
const filters = reactive({
  keyword: "",
  status: "",
  category: "",
  minPriority: null,
  maxPriority: null,
  page: 1,
  size: 10
});

onMounted(async () => {
  await Promise.all([loadTasks(), loadMembership()]);
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

async function loadTasks() {
  loading.value = true;
  errorText.value = "";
  try {
    const payload = { ...filters };
    if (!payload.status) {
      delete payload.status;
    }
    if (!payload.category) {
      delete payload.category;
    }
    tasks.value = (await searchTasks(payload)).records || [];
  } catch (error) {
    errorText.value = error.message || "加载任务失败";
  } finally {
    loading.value = false;
  }
}

async function loadMembership() {
  try {
    Object.assign(membership, await getMembership());
  } catch (error) {
    if (!errorText.value) {
      errorText.value = error.message || "加载会员配额失败";
    }
  }
}
</script>