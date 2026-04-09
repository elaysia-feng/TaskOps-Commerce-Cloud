<template>
  <div class="page-stack">
    <!-- Hero Banner -->
    <section class="hero-banner hero-banner-soft">
      <div class="hero-content">
        <div class="hero-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
          </svg>
          Execution
        </div>
        <h1>我的接单</h1>
        <p>接单后可在这里维护进度、填写完成说明并提交成果，等待发布方验收。</p>
      </div>
      <div class="hero-stats">
        <div class="hero-stat">
          <strong>{{ tasks.length }}</strong>
          <span>总任务数</span>
        </div>
        <div class="hero-stat">
          <strong>{{ takingCount }}</strong>
          <span>进行中</span>
        </div>
        <div class="hero-stat">
          <strong>{{ submittedCount }}</strong>
          <span>待验收</span>
        </div>
      </div>
    </section>

    <!-- Error Alert -->
    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="true" @close="errorText = ''" />

    <!-- Tasks Section -->
    <section class="tasks-section glass-card">
      <div class="section-header">
        <div>
          <p class="section-eyebrow">Accepted Tasks</p>
          <h3>任务执行面板</h3>
        </div>
        <el-button @click="loadTasks" :loading="loading" size="small">刷新</el-button>
      </div>

      <div v-if="loading" class="loading-state">
        <el-skeleton :rows="4" animated />
      </div>
      <div v-else-if="tasks.length" class="tasks-grid">
        <article v-for="task in tasks" :key="task.id" class="task-card hover-lift">
          <div class="task-header">
            <div>
              <h4>{{ task.title }}</h4>
              <span class="status-badge" :class="'status-' + task.status.toLowerCase()">
                {{ taskStatusLabel(task.status) }}
              </span>
            </div>
            <div class="task-reward">
              <strong>{{ formatMoney(task.settleAmount || task.rewardAmount) }}</strong>
              <span>结算金额</span>
            </div>
          </div>

          <p class="task-desc">{{ task.description }}</p>

          <div class="task-meta">
            <div class="meta-item">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              <span>截止：{{ formatDateTime(task.deadline) }}</span>
            </div>
            <div class="meta-item">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                <circle cx="12" cy="10" r="3"/>
              </svg>
              <span>{{ task.location || "线上交付" }}</span>
            </div>
          </div>

          <!-- Submit Form for TAKEN status -->
          <div v-if="task.status === 'TAKEN'" class="submit-section">
            <el-form :model="forms[task.id]" class="submit-form">
              <el-form-item>
                <el-input
                  v-model="forms[task.id].content"
                  type="textarea"
                  :rows="3"
                  placeholder="填写你完成任务的说明"
                />
              </el-form-item>
              <el-form-item>
                <el-input
                  v-model="forms[task.id].proofUrls"
                  placeholder="凭证链接（可选）"
                />
              </el-form-item>
              <el-button
                type="primary"
                :loading="submittingId === task.id"
                @click="handleSubmit(task)"
                class="submit-btn"
              >
                {{ submittingId === task.id ? "提交中..." : "提交任务成果" }}
              </el-button>
            </el-form>
          </div>

          <!-- Status Note for other statuses -->
          <div v-else class="status-note">
            <div class="status-icon" :class="task.status.toLowerCase()">
              <svg v-if="task.status === 'SUBMITTED'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10"/>
                <polyline points="12 6 12 12 16 14"/>
              </svg>
              <svg v-else-if="task.status === 'SETTLEMENT_PENDING'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="1" x2="12" y2="23"/>
                <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
              </svg>
              <svg v-else-if="task.status === 'SETTLED'" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
                <polyline points="22 4 12 14.01 9 11.01"/>
              </svg>
            </div>
            <p v-if="task.status === 'SUBMITTED'">成果已提交，等待发布方验收</p>
            <p v-else-if="task.status === 'SETTLEMENT_PENDING'">已通过验收，正在结算中</p>
            <p v-else-if="task.status === 'SETTLED'">任务已完成结算</p>
            <p v-else>当前状态无需操作</p>
          </div>
        </article>
      </div>
      <div v-else class="empty-state">
        <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="10"/>
          <polyline points="12 6 12 12 16 14"/>
        </svg>
        <h3>暂无接单记录</h3>
        <p>当前没有已接下的任务，去任务大厅看看吧</p>
        <RouterLink to="/tasks">
          <el-button type="primary">浏览任务大厅</el-button>
        </RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { RouterLink } from "vue-router";
import { ElMessage } from "element-plus";
import { getAcceptedTasks, submitTask } from "../api/task";
import { formatMoney, formatDateTime, taskStatusLabel } from "../utils/format";

const loading = ref(false);
const errorText = ref("");
const tasks = ref([]);
const forms = ref({});
const submittingId = ref(null);

const takingCount = computed(() => tasks.value.filter((item) => item.status === "TAKEN").length);
const submittedCount = computed(() => tasks.value.filter((item) => item.status === "SUBMITTED").length);
const settledCount = computed(() => tasks.value.filter((item) => item.status === "SETTLED").length);

onMounted(() => {
  loadTasks();
});

function createForm(taskId) {
  if (!forms.value[taskId]) {
    forms.value[taskId] = reactive({ content: "", proofUrls: "" });
  }
}

async function loadTasks() {
  loading.value = true;
  errorText.value = "";
  try {
    const pageData = await getAcceptedTasks({ page: 1, size: 20 });
    tasks.value = pageData.records || [];
    tasks.value.forEach((task) => createForm(task.id));
  } catch (error) {
    errorText.value = error.message || "加载我的接单任务失败";
  } finally {
    loading.value = false;
  }
}

async function handleSubmit(task) {
  createForm(task.id);
  const form = forms.value[task.id];
  if (!form.content) {
    errorText.value = "请先填写完成说明";
    return;
  }

  submittingId.value = task.id;
  errorText.value = "";
  try {
    await submitTask(task.id, {
      content: form.content,
      proofUrls: form.proofUrls || ""
    });
    form.content = "";
    form.proofUrls = "";
    ElMessage.success("任务成果已提交！");
    await loadTasks();
  } catch (error) {
    errorText.value = error.message || "提交任务失败";
  } finally {
    submittingId.value = null;
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
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  box-shadow: 0 16px 48px rgba(67, 233, 123, 0.25);
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
}

.hero-stats {
  display: flex;
  gap: 20px;
}

.hero-stat {
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  text-align: center;
}

.hero-stat strong {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #fff;
}

.hero-stat span {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.8);
}

/* ========================
   Tasks Section
   ======================== */
.tasks-section {
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

/* Tasks Grid */
.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(360px, 1fr));
  gap: 20px;
}

/* Task Card */
.task-card {
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 20px;
  border: 1px solid var(--line);
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.task-header h4 {
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

.status-badge.status-open { background: rgba(29, 122, 95, 0.12); color: var(--success); }
.status-badge.status-taken { background: rgba(64, 158, 255, 0.12); color: #409eff; }
.status-badge.status-submitted { background: rgba(229, 191, 114, 0.15); color: #b8860b; }
.status-badge.status-settlement_pending { background: rgba(229, 191, 114, 0.15); color: #b8860b; }
.status-badge.status-settled { background: rgba(29, 122, 95, 0.12); color: var(--success); }
.status-badge.status-rejected { background: rgba(173, 65, 54, 0.1); color: var(--danger); }
.status-badge.status-cancelled { background: rgba(173, 65, 54, 0.1); color: var(--danger); }

.task-reward {
  text-align: right;
}

.task-reward strong {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: var(--brand-deep);
}

.task-reward span {
  font-size: 11px;
  color: var(--muted);
}

.task-desc {
  font-size: 13px;
  color: var(--muted);
  line-height: 1.6;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-meta {
  display: flex;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--muted);
}

/* Submit Section */
.submit-section {
  padding: 16px;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 1px solid var(--line);
}

.submit-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.submit-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.submit-btn {
  width: 100%;
}

/* Status Note */
.status-note {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 14px;
}

.status-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.status-icon.submitted {
  background: rgba(229, 191, 114, 0.15);
  color: #b8860b;
}

.status-icon.settlement_pending {
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
}

.status-icon.settled {
  background: rgba(29, 122, 95, 0.12);
  color: var(--success);
}

.status-note p {
  font-size: 13px;
  color: var(--muted);
  margin: 0;
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

.empty-state a {
  text-decoration: none;
  margin-top: 8px;
}

/* Responsive */
@media (max-width: 1024px) {
  .hero-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .hero-stats {
    width: 100%;
    justify-content: space-between;
  }

  .tasks-grid {
    grid-template-columns: 1fr;
  }
}
</style>
