<template>
  <div class="page-stack">
    <!-- Hero Banner -->
    <section class="hero-banner">
      <div class="hero-content">
        <div class="hero-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
          </svg>
          Review
        </div>
        <h1>待我验收</h1>
        <p>当任务被提交后，发布方可在这里查看成果说明，并执行通过或驳回操作。</p>
      </div>
      <div class="hero-stat-card glass-card">
        <span class="stat-label">待验收</span>
        <strong>{{ tasks.length }}</strong>
        <span>当前等待处理</span>
      </div>
    </section>

    <!-- Error Alert -->
    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="true" @close="errorText = ''" />

    <!-- Tasks Section -->
    <section class="review-section glass-card">
      <div class="section-header">
        <div>
          <p class="section-eyebrow">Review Queue</p>
          <h3>任务验收面板</h3>
        </div>
        <el-button @click="loadTasks" :loading="loading" size="small">刷新</el-button>
      </div>

      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="4" animated />
      </div>
      <div v-else-if="tasks.length" class="review-grid">
        <article v-for="task in tasks" :key="task.id" class="review-card">
          <div class="review-header">
            <div class="review-info">
              <h4>{{ task.title }}</h4>
              <span class="status-badge status-submitted">待验收</span>
            </div>
            <div class="review-reward">
              <strong>{{ formatMoney(task.settleAmount || task.rewardAmount) }}</strong>
              <span>结算金额</span>
            </div>
          </div>

          <p class="review-desc">{{ task.description }}</p>

          <div class="review-meta">
            <div class="meta-item">
              <span class="meta-label">接单人</span>
              <strong>{{ task.acceptorId || "-" }}</strong>
            </div>
            <div class="meta-item">
              <span class="meta-label">提交时间</span>
              <strong>{{ formatDateTime(task.submittedAt) }}</strong>
            </div>
            <div class="meta-item">
              <span class="meta-label">截止时间</span>
              <strong>{{ formatDateTime(task.deadline) }}</strong>
            </div>
          </div>

          <div class="reject-reason">
            <el-input
              v-model="rejectReasons[task.id]"
              type="textarea"
              :rows="2"
              placeholder="填写驳回原因（通过时可不填）"
            />
          </div>

          <div class="review-actions">
            <el-button
              type="success"
              :loading="approvingId === task.id"
              @click="handleApprove(task.id)"
              class="approve-btn"
            >
              <el-icon><Check /></el-icon>
              {{ approvingId === task.id ? "处理中..." : "验收通过" }}
            </el-button>
            <el-button
              type="warning"
              :loading="rejectingId === task.id"
              @click="handleReject(task.id)"
              :disabled="!rejectReasons[task.id]"
            >
              <el-icon><Close /></el-icon>
              {{ rejectingId === task.id ? "处理中..." : "驳回重做" }}
            </el-button>
          </div>
        </article>
      </div>
      <div v-else class="empty-state">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
          <polyline points="22 4 12 14.01 9 11.01"/>
        </svg>
        <h3>暂无待验收任务</h3>
        <p>当前没有需要验收的任务，很好！</p>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage } from "element-plus";
import { Check, Close } from "@element-plus/icons-vue";
import { approveTask, getReviewTasks, rejectTask } from "../api/task";
import { formatMoney, formatDateTime } from "../utils/format";

const loading = ref(false);
const errorText = ref("");
const tasks = ref([]);
const rejectReasons = ref({});
const approvingId = ref(null);
const rejectingId = ref(null);

onMounted(() => {
  loadTasks();
});

async function loadTasks() {
  loading.value = true;
  errorText.value = "";
  try {
    const pageData = await getReviewTasks({ page: 1, size: 20 });
    tasks.value = pageData.records || [];
  } catch (error) {
    errorText.value = error.message || "加载待验收任务失败";
  } finally {
    loading.value = false;
  }
}

async function handleApprove(taskId) {
  approvingId.value = taskId;
  errorText.value = "";
  try {
    await approveTask(taskId);
    ElMessage.success("任务验收通过！");
    await loadTasks();
  } catch (error) {
    errorText.value = error.message || "验收通过失败";
  } finally {
    approvingId.value = null;
  }
}

async function handleReject(taskId) {
  if (!rejectReasons.value[taskId]) {
    errorText.value = "请先填写驳回原因";
    return;
  }

  rejectingId.value = taskId;
  errorText.value = "";
  try {
    await rejectTask(taskId, { reason: rejectReasons.value[taskId] });
    ElMessage.success("任务已驳回");
    rejectReasons.value[taskId] = "";
    await loadTasks();
  } catch (error) {
    errorText.value = error.message || "驳回任务失败";
  } finally {
    rejectingId.value = null;
  }
}
</script>

<style scoped>
.page-stack {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* ========================
   Hero Banner
   ======================== */
.hero-banner {
  position: relative;
  overflow: hidden;
  border-radius: 28px;
  padding: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff7ef;
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  box-shadow: 0 16px 48px rgba(250, 112, 154, 0.25);
}

.hero-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  width: fit-content;
}

.hero-content h1 {
  font-family: var(--font-display);
  font-size: 40px;
  font-weight: 700;
  margin: 0;
}

.hero-content p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  max-width: 500px;
}

.hero-stat-card {
  padding: 24px 32px;
  border-radius: 20px;
  text-align: center;
  background: rgba(255, 255, 255, 0.9);
}

.hero-stat-card .stat-label {
  display: block;
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 8px;
}

.hero-stat-card strong {
  display: block;
  font-size: 48px;
  font-weight: 700;
  color: var(--brand-deep);
  line-height: 1;
  margin-bottom: 4px;
}

.hero-stat-card span {
  font-size: 12px;
  color: var(--muted);
}

/* ========================
   Review Section
   ======================== */
.review-section {
  padding: 24px;
  border-radius: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.section-eyebrow {
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
  color: var(--brand);
  margin: 0 0 4px;
}

.section-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0;
  color: var(--text);
}

/* Loading */
.loading-state {
  padding: 20px;
}

/* Review Grid */
.review-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

/* Review Card */
.review-card {
  padding: 24px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 20px;
  border: 2px solid rgba(229, 191, 114, 0.3);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.review-info h4 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 8px;
  color: var(--text);
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  border-radius: 999px;
  font-weight: 500;
}

.status-badge.status-submitted {
  background: rgba(229, 191, 114, 0.15);
  color: #b8860b;
}

.review-reward {
  text-align: right;
}

.review-reward strong {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--brand-deep);
}

.review-reward span {
  font-size: 11px;
  color: var(--muted);
}

.review-desc {
  font-size: 14px;
  color: var(--muted);
  line-height: 1.6;
  margin: 0;
}

.review-meta {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.meta-item {
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  text-align: center;
}

.meta-label {
  display: block;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 4px;
}

.meta-item strong {
  font-size: 12px;
  color: var(--text);
}

.reject-reason {
  margin-top: 8px;
}

.review-actions {
  display: flex;
  gap: 12px;
}

.approve-btn {
  flex: 1;
}

/* Empty State */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 40px;
  text-align: center;
  color: var(--muted);
}

.empty-state svg {
  opacity: 0.4;
  color: var(--success);
}

.empty-state h3 {
  font-size: 18px;
  margin: 0;
  color: var(--text);
}

.empty-state p {
  font-size: 14px;
  margin: 0;
}

/* Responsive */
@media (max-width: 1024px) {
  .hero-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .review-grid {
    grid-template-columns: 1fr;
  }

  .review-meta {
    grid-template-columns: 1fr;
  }
}
</style>
