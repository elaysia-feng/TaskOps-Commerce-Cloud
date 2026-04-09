<template>
  <div class="page-stack">
    <!-- Hero Banner -->
    <section class="hero-banner hero-banner-soft">
      <div class="hero-content">
        <div class="hero-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19"/>
            <line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          Task Studio
        </div>
        <h1>发布与管理</h1>
        <p>创建任务、切换会员等级，并管理我发布的任务。</p>
      </div>
      <div class="hero-stats">
        <div class="hero-stat">
          <span class="stat-label">当前会员</span>
          <strong>{{ membershipLabel(membership.level) }}</strong>
          <span>{{ membership.usedTasks || 0 }} / {{ membership.maxTasks || 0 }} 已使用</span>
        </div>
        <div class="hero-stat">
          <span class="stat-label">预估结算</span>
          <strong>{{ estimatedSettleAmount }}</strong>
          <span>赏金 - 服务费</span>
        </div>
      </div>
    </section>

    <!-- Error/Success Alerts -->
    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="true" @close="errorText = ''" />
    <el-alert v-if="successText" :title="successText" type="success" show-icon :closable="true" @close="successText = ''" />

    <!-- Main Content -->
    <div class="content-grid">
      <!-- Create Task Form -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">Create</p>
            <h3>发布新任务</h3>
          </div>
        </div>

        <el-form :model="form" class="task-form" size="default" @submit.prevent="handleSubmit">
          <el-form-item label="任务标题">
            <el-input v-model.trim="form.title" placeholder="例如：帮我取快递并送到宿舍" />
          </el-form-item>

          <el-form-item label="任务描述">
            <el-input v-model.trim="form.description" type="textarea" :rows="4" placeholder="补充交付要求、时限和注意事项" />
          </el-form-item>

          <div class="form-row">
            <el-form-item label="任务分类" class="form-item-half">
              <el-select v-model="form.category">
                <el-option label="跑腿" value="ERRAND" />
                <el-option label="设计" value="DESIGN" />
                <el-option label="技术" value="TECH" />
                <el-option label="咨询" value="CONSULT" />
                <el-option label="综合" value="GENERAL" />
              </el-select>
            </el-form-item>
            <el-form-item label="优先级" class="form-item-half">
              <el-select v-model.number="form.priority">
                <el-option label="P1 - 最高" :value="1" />
                <el-option label="P2" :value="2" />
                <el-option label="P3 - 默认" :value="3" />
                <el-option label="P4" :value="4" />
                <el-option label="P5 - 最低" :value="5" />
              </el-select>
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="任务赏金" class="form-item-half">
              <el-input-number v-model="form.rewardAmount" :min="0.01" :precision="2" :step="0.01" />
            </el-form-item>
            <el-form-item label="平台服务费" class="form-item-half">
              <el-input-number v-model="form.serviceFee" :min="0" :precision="2" :step="0.01" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="任务地点" class="form-item-half">
              <el-input v-model.trim="form.location" placeholder="如：东区菜鸟驿站 / 线上交付" />
            </el-form-item>
            <el-form-item label="联系方式" class="form-item-half">
              <el-input v-model.trim="form.contactInfo" placeholder="微信 / QQ / 手机号" />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item label="截止时间" class="form-item-half">
              <el-date-picker v-model="form.deadline" type="datetime" placeholder="选择截止时间" style="width: 100%" />
            </el-form-item>
            <el-form-item label="标签" class="form-item-half">
              <el-input v-model.trim="form.tags" placeholder="跑腿, 快递, 校园" />
            </el-form-item>
          </div>

          <el-form-item label="凭证要求">
            <el-switch v-model="form.proofRequired" active-text="需要提交凭证" inactive-text="无需凭证" />
          </el-form-item>

          <div class="form-actions">
            <el-button type="primary" native-type="submit" :loading="loading" class="submit-btn">
              {{ loading ? "发布中..." : "发布任务" }}
            </el-button>
            <el-button @click="resetForm" :disabled="loading">重置表单</el-button>
          </div>
        </el-form>
      </section>

      <!-- Membership Plans & Published Tasks -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">Membership</p>
            <h3>发布配额方案</h3>
          </div>
        </div>

        <div class="plans-grid">
          <article
            v-for="plan in plans"
            :key="plan.level"
            class="plan-card"
            :class="{ active: membership.level === plan.level }"
          >
            <div class="plan-header">
              <strong>{{ plan.title }}</strong>
              <span class="plan-badge">{{ plan.maxTasks }} 条</span>
            </div>
            <p class="plan-desc">{{ plan.description }}</p>
            <el-button
              size="small"
              :type="membership.level === plan.level ? 'info' : 'primary'"
              :disabled="switchingLevel === plan.level || membership.level === plan.level"
              @click="handleSwitch(plan.level)"
            >
              {{ switchingLevel === plan.level ? "切换中..." : membership.level === plan.level ? "当前方案" : `切换到 ${plan.title}` }}
            </el-button>
          </article>
        </div>

        <el-divider />

        <div class="card-header">
          <div>
            <p class="card-eyebrow">Published</p>
            <h3>我发布的任务</h3>
          </div>
          <el-button size="small" @click="loadPublishedTasks">刷新</el-button>
        </div>

        <div v-if="publishedTasks.length" class="published-list">
          <article
            v-for="task in publishedTasks"
            :key="task.id"
            class="published-item hover-lift"
            :class="{ active: selectedTask?.id === task.id }"
            @click="selectPublishedTask(task.id)"
          >
            <div class="item-header">
              <strong>{{ task.title }}</strong>
              <span class="status-badge" :class="'status-' + task.status.toLowerCase()">
                {{ taskStatusLabel(task.status) }}
              </span>
            </div>
            <p class="item-desc">{{ task.description }}</p>
            <div class="item-meta">
              <span>{{ taskCategoryLabel(task.category) }}</span>
              <strong>{{ formatMoney(task.rewardAmount) }}</strong>
            </div>
          </article>
        </div>
        <div v-else class="empty-state">
          <p>还没有已发布任务</p>
        </div>
      </section>
    </div>

    <!-- Selected Task Detail -->
    <section v-if="selectedTask" class="detail-section glass-card">
      <div class="detail-header">
        <div>
          <p class="card-eyebrow">Task Detail</p>
          <h3>{{ selectedTask.title }}</h3>
        </div>
        <span class="status-badge" :class="'status-' + selectedTask.status.toLowerCase()">
          {{ taskStatusLabel(selectedTask.status) }}
        </span>
      </div>

      <div class="detail-grid">
        <div class="detail-item">
          <span class="item-label">赏金</span>
          <strong>{{ formatMoney(selectedTask.rewardAmount) }}</strong>
        </div>
        <div class="detail-item">
          <span class="item-label">结算金额</span>
          <strong>{{ formatMoney(selectedTask.settleAmount || selectedTask.rewardAmount) }}</strong>
        </div>
        <div class="detail-item">
          <span class="item-label">地点</span>
          <strong>{{ selectedTask.location || "线上交付" }}</strong>
        </div>
        <div class="detail-item">
          <span class="item-label">联系信息</span>
          <strong>{{ selectedTask.contactInfo || "-" }}</strong>
        </div>
        <div class="detail-item">
          <span class="item-label">接单人</span>
          <strong>{{ selectedTask.acceptorId || "-" }}</strong>
        </div>
        <div class="detail-item">
          <span class="item-label">驳回原因</span>
          <strong>{{ selectedTask.rejectReason || "-" }}</strong>
        </div>
      </div>

      <p class="detail-desc">{{ selectedTask.description }}</p>

      <div class="cancel-section">
        <el-input v-model.trim="cancelReason" type="textarea" :rows="2" placeholder="取消原因（可选）" />
        <el-button
          type="warning"
          :disabled="!taskCanCancel(selectedTask.status) || cancelingTask"
          @click="handleCancelSelected"
          class="cancel-btn"
        >
          {{ cancelingTask ? "取消中..." : taskCanCancel(selectedTask.status) ? "取消任务" : "当前不可取消" }}
        </el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { cancelTask, createTask, getMembership, getPublishedTasks, getTaskDetail, switchMembership } from "../api/task";
import {
  formatMoney,
  formatDateTime,
  membershipLabel,
  taskCanCancel,
  taskCategoryLabel,
  taskStatusLabel
} from "../utils/format";

const loading = ref(false);
const cancelingTask = ref(false);
const switchingLevel = ref("");
const errorText = ref("");
const successText = ref("");
const cancelReason = ref("");
const membership = reactive({
  level: "",
  remaining: 0,
  usedTasks: 0,
  maxTasks: 0
});
const publishedTasks = ref([]);
const selectedTask = ref(null);

const form = reactive({
  title: "",
  description: "",
  tags: "",
  priority: 3,
  category: "ERRAND",
  rewardAmount: 18.8,
  serviceFee: 0.8,
  location: "",
  contactInfo: "",
  deadline: "",
  proofRequired: true
});

const plans = [
  { level: "FREE", title: "免费版", maxTasks: 20, description: "适合体验与轻量发布。" },
  { level: "VIP", title: "VIP", maxTasks: 50, description: "适合稳定发布和多人协作。" },
  { level: "SVIP", title: "SVIP", maxTasks: 100, description: "适合高频任务分发和更大配额。" }
];

const estimatedSettleAmount = computed(() => {
  const reward = Number(form.rewardAmount || 0);
  const fee = Number(form.serviceFee || 0);
  return formatMoney(Math.max(0, reward - fee));
});

onMounted(async () => {
  await Promise.all([loadMembership(), loadPublishedTasks()]);
});

function normalizeDateTime(value) {
  return value ? `${value}:00` : null;
}

function resetForm() {
  form.title = "";
  form.description = "";
  form.tags = "";
  form.priority = 3;
  form.category = "ERRAND";
  form.rewardAmount = 18.8;
  form.serviceFee = 0.8;
  form.location = "";
  form.contactInfo = "";
  form.deadline = "";
  form.proofRequired = true;
}

async function loadMembership() {
  try {
    Object.assign(membership, await getMembership());
  } catch (error) {
    errorText.value = error.message || "加载会员配额失败";
  }
}

async function loadPublishedTasks() {
  try {
    const data = await getPublishedTasks({ page: 1, size: 8 });
    publishedTasks.value = data.records || [];
    if (publishedTasks.value.length && !selectedTask.value) {
      await selectPublishedTask(publishedTasks.value[0].id);
    }
  } catch (error) {
    errorText.value = error.message || "加载已发布任务失败";
  }
}

async function selectPublishedTask(taskId) {
  try {
    selectedTask.value = await getTaskDetail(taskId);
    cancelReason.value = "";
  } catch (error) {
    errorText.value = error.message || "加载任务详情失败";
  }
}

async function handleSubmit() {
  loading.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    const taskId = await createTask({
      ...form,
      deadline: normalizeDateTime(form.deadline)
    });
    ElMessage.success(`任务发布成功！`);
    resetForm();
    selectedTask.value = null;
    await Promise.all([loadMembership(), loadPublishedTasks()]);
    await selectPublishedTask(taskId);
  } catch (error) {
    errorText.value = error.message || "发布任务失败";
  } finally {
    loading.value = false;
  }
}

async function handleSwitch(level) {
  switchingLevel.value = level;
  errorText.value = "";
  successText.value = "";
  try {
    await switchMembership(level);
    ElMessage.success(`已切换到 ${membershipLabel(level)}`);
    await loadMembership();
  } catch (error) {
    errorText.value = error.message || "切换会员失败";
  } finally {
    switchingLevel.value = "";
  }
}

async function handleCancelSelected() {
  if (!selectedTask.value) return;

  cancelingTask.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    await cancelTask(selectedTask.value.id, { reason: cancelReason.value || undefined });
    ElMessage.success("任务已取消");
    cancelReason.value = "";
    const currentId = selectedTask.value.id;
    selectedTask.value = null;
    await Promise.all([loadMembership(), loadPublishedTasks()]);
  } catch (error) {
    errorText.value = error.message || "取消任务失败";
  } finally {
    cancelingTask.value = false;
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
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  box-shadow: 0 16px 48px rgba(79, 172, 254, 0.25);
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
  gap: 16px;
}

.hero-stat {
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  text-align: center;
  min-width: 140px;
}

.hero-stat .stat-label {
  display: block;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 4px;
}

.hero-stat strong {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 2px;
}

.hero-stat span {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
}

/* ========================
   Content Grid
   ======================== */
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.content-card {
  padding: 24px;
  border-radius: 24px;
}

.card-header {
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

/* Task Form */
.task-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.task-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-item-half {
  flex: 1;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.submit-btn {
  flex: 1;
}

/* Plans Grid */
.plans-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.plan-card {
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.plan-card.active {
  border-color: var(--accent);
  background: rgba(30, 107, 102, 0.05);
}

.plan-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.plan-header strong {
  font-size: 14px;
  color: var(--text);
}

.plan-badge {
  padding: 3px 8px;
  font-size: 10px;
  background: rgba(30, 107, 102, 0.1);
  color: var(--accent);
  border-radius: 999px;
  font-weight: 500;
}

.plan-desc {
  font-size: 12px;
  color: var(--muted);
  margin: 0 0 12px;
  line-height: 1.4;
}

.plan-card .el-button {
  width: 100%;
}

/* Published List */
.published-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.published-item {
  padding: 14px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 14px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.2s;
}

.published-item:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: translateX(4px);
}

.published-item.active {
  border-color: var(--accent);
  background: rgba(30, 107, 102, 0.05);
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.item-header strong {
  font-size: 13px;
  color: var(--text);
}

.status-badge {
  display: inline-block;
  padding: 3px 8px;
  font-size: 10px;
  border-radius: 999px;
  font-weight: 500;
}

.status-badge.status-open { background: rgba(29, 122, 95, 0.12); color: var(--success); }
.status-badge.status-taken { background: rgba(64, 158, 255, 0.12); color: #409eff; }
.status-badge.status-submitted { background: rgba(229, 191, 114, 0.15); color: #b8860b; }
.status-badge.status-settled { background: rgba(29, 122, 95, 0.12); color: var(--success); }
.status-badge.status-cancelled { background: rgba(173, 65, 54, 0.1); color: var(--danger); }

.item-desc {
  font-size: 12px;
  color: var(--muted);
  margin: 0 0 8px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--muted);
}

.item-meta strong {
  color: var(--brand-deep);
  font-weight: 600;
}

/* Detail Section */
.detail-section {
  padding: 24px;
  border-radius: 24px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.detail-header h3 {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--text);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.detail-item {
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
}

.detail-item .item-label {
  display: block;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 4px;
}

.detail-item strong {
  font-size: 13px;
  color: var(--text);
}

.detail-desc {
  font-size: 14px;
  color: var(--muted);
  line-height: 1.7;
  margin: 0 0 20px;
}

.cancel-section {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.cancel-section .el-textarea {
  flex: 1;
}

.cancel-btn {
  white-space: nowrap;
}

/* Empty State */
.empty-state {
  padding: 30px;
  text-align: center;
  color: var(--muted);
  font-size: 13px;
}

/* Responsive */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .hero-banner {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .hero-stats {
    width: 100%;
    justify-content: space-between;
  }

  .plans-grid {
    grid-template-columns: 1fr;
  }

  .detail-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .form-row {
    flex-direction: column;
  }
}
</style>
