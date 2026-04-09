<template>
  <div class="page-stack">
    <!-- Hero Banner -->
    <section class="hero-banner hero-banner-soft">
      <div class="hero-content">
        <div class="hero-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          任务市场
        </div>
        <h1>任务大厅</h1>
        <p>按关键词、分类、优先级和状态筛选任务，点击任务卡片查看详情并完成接单。</p>
        <div class="hero-stats">
          <div class="hero-stat">
            <strong>{{ resultTotal }}</strong>
            <span>个任务</span>
          </div>
          <div class="hero-stat">
            <strong>{{ hotTasks.length }}</strong>
            <span>热门</span>
          </div>
        </div>
      </div>

      <div class="hero-aside">
        <div class="stat-mini glass-card">
          <span class="stat-mini-label">当前会员</span>
          <strong>{{ membershipLabel(membership.level) }}</strong>
          <p>剩余 {{ membership.remaining ?? 0 }} 个发布配额</p>
        </div>
        <div class="stat-mini glass-card">
          <span class="stat-mini-label">搜索结果</span>
          <strong>{{ resultTotal }}</strong>
          <p>当前条件下命中的任务数量</p>
        </div>
      </div>
    </section>

    <!-- Filter Bar -->
    <section class="filter-bar glass-card">
      <div class="filter-row">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索标题 / 描述 / 标签 / 地点"
          prefix-icon="Search"
          clearable
          @keyup.enter="loadTasks"
          class="search-input"
        />

        <el-select v-model="filters.status" placeholder="任务状态" clearable @change="loadTasks">
          <el-option label="待接单" value="OPEN" />
          <el-option label="已接单" value="TAKEN" />
          <el-option label="待验收" value="SUBMITTED" />
          <el-option label="已结算" value="SETTLED" />
          <el-option label="已取消" value="CANCELLED" />
        </el-select>

        <el-select v-model="filters.category" placeholder="任务分类" clearable @change="loadTasks">
          <el-option label="跑腿" value="ERRAND" />
          <el-option label="设计" value="DESIGN" />
          <el-option label="技术" value="TECH" />
          <el-option label="咨询" value="CONSULT" />
          <el-option label="综合" value="GENERAL" />
        </el-select>

        <el-select v-model="filters.priority" placeholder="优先级" clearable @change="loadTasks">
          <el-option label="P1 - 最高" :value="1" />
          <el-option label="P2" :value="2" />
          <el-option label="P3" :value="3" />
          <el-option label="P4" :value="4" />
          <el-option label="P5 - 最低" :value="5" />
        </el-select>

        <el-button type="primary" @click="loadTasks" :loading="loading">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </section>

    <!-- Error Alert -->
    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="true" @close="errorText = ''" />

    <!-- Main Content -->
    <section class="tasks-section">
      <!-- Task Grid -->
      <div class="tasks-grid" v-loading="loading">
        <div v-if="!loading && tasks.length === 0" class="empty-state">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="11" cy="11" r="8"/>
            <line x1="21" y1="21" x2="16.65" y2="16.65"/>
          </svg>
          <h3>暂无任务</h3>
          <p>当前没有符合条件的任务，请调整筛选条件</p>
        </div>

        <article
          v-for="task in tasks"
          :key="task.id"
          class="task-card hover-lift"
          :class="{ active: selectedTask?.id === task.id }"
          @click="handleSelectTask(task.id)"
        >
          <div class="task-card-header">
            <div class="task-category">
              <span class="category-tag">{{ taskCategoryLabel(task.category) }}</span>
              <span class="status-badge" :class="'status-' + task.status.toLowerCase()">{{ taskStatusLabel(task.status) }}</span>
            </div>
            <div class="task-reward">
              <strong>{{ formatMoney(task.rewardAmount) }}</strong>
            </div>
          </div>

          <h3 class="task-title">{{ task.title }}</h3>
          <p class="task-desc">{{ task.description }}</p>

          <div class="task-tags" v-if="splitTags(task.tags).length">
            <span v-for="tag in splitTags(task.tags)" :key="tag" class="task-tag">{{ tag }}</span>
          </div>

          <div class="task-meta">
            <div class="meta-item">
              <el-icon><Location /></el-icon>
              <span>{{ task.location || "线上交付" }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Clock /></el-icon>
              <span>{{ formatDateTime(task.deadline) }}</span>
            </div>
            <div class="meta-item">
              <el-icon><Star /></el-icon>
              <span>P{{ task.priority }}</span>
            </div>
          </div>

          <div class="task-actions" @click.stop>
            <el-button
              v-if="task.status === 'OPEN'"
              type="primary"
              size="small"
              :loading="acceptingId === task.id"
              @click="handleAccept(task.id)"
            >
              {{ acceptingId === task.id ? "接单中..." : "立即接单" }}
            </el-button>
            <el-button v-else type="info" size="small" disabled>
              {{ taskStatusLabel(task.status) }}
            </el-button>
            <el-button size="small" @click="handleSelectTask(task.id)">查看详情</el-button>
          </div>
        </article>
      </div>

      <!-- Hot Tasks Sidebar -->
      <aside class="hot-sidebar glass-card">
        <div class="sidebar-header">
          <h3>热门任务</h3>
          <RouterLink to="/tasks" class="view-all">查看全部</RouterLink>
        </div>
        <div class="hot-list">
          <article
            v-for="(item, index) in hotTasks.slice(0, 5)"
            :key="item.id"
            class="hot-item"
            @click="handleSelectTask(item.id)"
          >
            <div class="hot-rank" :class="{ gold: index === 0 }">{{ index + 1 }}</div>
            <div class="hot-content">
              <h4>{{ item.title }}</h4>
              <div class="hot-meta">
                <span>{{ taskCategoryLabel(item.category) }}</span>
                <strong>{{ formatMoney(item.rewardAmount) }}</strong>
              </div>
            </div>
          </article>
        </div>
      </aside>
    </section>

    <!-- Pagination -->
    <div class="pagination-wrapper" v-if="resultTotal > filters.size">
      <el-pagination
        v-model:current-page="filters.page"
        :page-size="filters.size"
        :total="resultTotal"
        layout="prev, pager, next, jumper"
        @current-change="loadTasks"
        background
      />
    </div>

    <!-- Task Detail Drawer -->
    <el-drawer
      v-model="drawerVisible"
      :title="selectedTask?.title || '任务详情'"
      size="480px"
      :before-close="handleDrawerClose"
    >
      <div v-if="detailLoading" class="drawer-loading">
        <el-skeleton :rows="8" animated />
      </div>
      <div v-else-if="selectedTask" class="task-detail">
        <div class="detail-header">
          <div class="detail-badges">
            <span class="category-tag">{{ taskCategoryLabel(selectedTask.category) }}</span>
            <span class="status-badge" :class="'status-' + selectedTask.status.toLowerCase()">
              {{ taskStatusLabel(selectedTask.status) }}
            </span>
            <span class="priority-tag">P{{ selectedTask.priority }}</span>
          </div>
          <h2>{{ selectedTask.title }}</h2>
        </div>

        <div class="detail-reward">
          <div class="reward-amount">
            <span class="reward-label">任务赏金</span>
            <strong>{{ formatMoney(selectedTask.rewardAmount) }}</strong>
          </div>
          <div class="reward-amount">
            <span class="reward-label">服务费</span>
            <span>{{ formatMoney(selectedTask.serviceFee) }}</span>
          </div>
          <div class="reward-amount highlight">
            <span class="reward-label">预计结算</span>
            <strong>{{ formatMoney(selectedTask.settleAmount || selectedTask.rewardAmount) }}</strong>
          </div>
        </div>

        <div class="detail-section">
          <h4>任务描述</h4>
          <p>{{ selectedTask.description }}</p>
        </div>

        <div class="detail-section">
          <h4>任务信息</h4>
          <div class="detail-info-grid">
            <div class="info-item">
              <span class="info-label">地点</span>
              <strong>{{ selectedTask.location || "线上交付" }}</strong>
            </div>
            <div class="info-item">
              <span class="info-label">联系方式</span>
              <strong>{{ selectedTask.contactInfo || "-" }}</strong>
            </div>
            <div class="info-item">
              <span class="info-label">发布时间</span>
              <strong>{{ formatDateTime(selectedTask.createdAt) }}</strong>
            </div>
            <div class="info-item">
              <span class="info-label">截止时间</span>
              <strong>{{ formatDateTime(selectedTask.deadline) }}</strong>
            </div>
            <div class="info-item">
              <span class="info-label">凭证要求</span>
              <strong>{{ selectedTask.proofRequired ? "需要提交凭证" : "无需凭证" }}</strong>
            </div>
            <div class="info-item">
              <span class="info-label">关联订单</span>
              <strong>{{ selectedTask.tradeOrderNo || "-" }}</strong>
            </div>
          </div>
        </div>

        <div class="detail-section" v-if="selectedTask.tags">
          <h4>标签</h4>
          <div class="detail-tags">
            <span v-for="tag in splitTags(selectedTask.tags)" :key="tag" class="task-tag">{{ tag }}</span>
          </div>
        </div>

        <div class="detail-actions">
          <el-button
            v-if="selectedTask.status === 'OPEN'"
            type="primary"
            size="large"
            :loading="acceptingId === selectedTask.id"
            @click="handleAccept(selectedTask.id)"
            class="accept-btn"
          >
            {{ acceptingId === selectedTask.id ? "接单中..." : "对这条任务接单" }}
          </el-button>
          <el-button type="primary" size="large" disabled v-else>
            当前不可接单
          </el-button>
          <RouterLink to="/tasks/create">
            <el-button size="large">去发布任务</el-button>
          </RouterLink>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { RouterLink } from "vue-router";
import { Search, Location, Clock, Star } from "@element-plus/icons-vue";
import { ElMessage } from "element-plus";
import { acceptTask, getHotTasks, getMembership, getTaskDetail, searchTasks } from "../api/task";
import { formatMoney, formatDateTime, membershipLabel, taskCategoryLabel, taskStatusLabel } from "../utils/format";

const loading = ref(false);
const detailLoading = ref(false);
const drawerVisible = ref(false);
const acceptingId = ref(null);
const errorText = ref("");
const tasks = ref([]);
const hotTasks = ref([]);
const selectedTask = ref(null);
const resultTotal = ref(0);
const membership = reactive({
  level: "",
  remaining: 0,
  usedTasks: 0,
  maxTasks: 0
});

const filters = reactive({
  keyword: "",
  status: "OPEN",
  category: "",
  priority: null,
  page: 1,
  size: 12
});

onMounted(async () => {
  await Promise.all([loadMembership(), loadHotTasks()]);
  await loadTasks();
});

function splitTags(tags) {
  if (!tags) return [];
  return String(tags).split(",").map((item) => item.trim()).filter(Boolean).slice(0, 4);
}

function resetFilters() {
  filters.keyword = "";
  filters.status = "OPEN";
  filters.category = "";
  filters.priority = null;
  filters.page = 1;
  loadTasks();
}

async function loadTasks() {
  loading.value = true;
  errorText.value = "";
  try {
    const payload = { ...filters };
    if (!payload.category) delete payload.category;
    if (!payload.status) delete payload.status;
    if (!payload.priority) delete payload.priority;

    const pageData = await searchTasks(payload);
    tasks.value = pageData.records || [];
    resultTotal.value = pageData.total || tasks.value.length;
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

async function loadHotTasks() {
  try {
    hotTasks.value = await getHotTasks();
  } catch (error) {
    if (!errorText.value) {
      errorText.value = error.message || "加载热门任务失败";
    }
  }
}

async function handleSelectTask(taskId) {
  drawerVisible.value = true;
  detailLoading.value = true;
  try {
    selectedTask.value = await getTaskDetail(taskId);
  } catch (error) {
    errorText.value = error.message || "加载任务详情失败";
  } finally {
    detailLoading.value = false;
  }
}

function handleDrawerClose() {
  drawerVisible.value = false;
}

async function handleAccept(taskId) {
  acceptingId.value = taskId;
  errorText.value = "";
  try {
    await acceptTask(taskId);
    ElMessage.success("接单成功！");
    await Promise.all([loadTasks(), loadHotTasks()]);
    if (selectedTask.value?.id === taskId) {
      await handleSelectTask(taskId);
    }
  } catch (error) {
    errorText.value = error.message || "接单失败";
  } finally {
    acceptingId.value = null;
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
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(240px, 0.7fr);
  gap: 24px;
  color: #fff7ef;
  background: linear-gradient(135deg, #204e58 0%, #24515f 42%, #97604f 100%);
  box-shadow: 0 16px 48px rgba(40, 32, 19, 0.15);
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
  background: rgba(255, 255, 255, 0.15);
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
  letter-spacing: 0.02em;
}

.hero-content p {
  font-size: 14px;
  line-height: 1.6;
  color: rgba(255, 247, 239, 0.8);
  margin: 0;
}

.hero-stats {
  display: flex;
  gap: 24px;
  margin-top: 8px;
}

.hero-stat {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.hero-stat strong {
  font-size: 28px;
  font-weight: 700;
}

.hero-stat span {
  font-size: 13px;
  color: rgba(255, 247, 239, 0.7);
}

.hero-aside {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-mini {
  padding: 16px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.stat-mini-label {
  display: block;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: rgba(255, 247, 239, 0.6);
  margin-bottom: 6px;
}

.stat-mini strong {
  display: block;
  font-size: 18px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 4px;
}

.stat-mini p {
  font-size: 11px;
  color: rgba(255, 247, 239, 0.5);
  margin: 0;
}

/* ========================
   Filter Bar
   ======================== */
.filter-bar {
  padding: 16px 20px;
  border-radius: 20px;
}

.filter-row {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 200px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 14px;
}

/* ========================
   Tasks Section
   ======================== */
.tasks-section {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 20px;
  align-items: start;
}

.tasks-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.task-card {
  padding: 20px;
  background: var(--surface);
  border-radius: 24px;
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-card:hover {
  border-color: rgba(30, 107, 102, 0.3);
}

.task-card.active {
  border-color: var(--accent);
  background: rgba(30, 107, 102, 0.05);
}

.task-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.task-category {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.category-tag {
  padding: 4px 10px;
  font-size: 11px;
  background: rgba(30, 107, 102, 0.1);
  color: var(--accent);
  border-radius: 999px;
  font-weight: 500;
}

.status-badge {
  padding: 4px 10px;
  font-size: 11px;
  border-radius: 999px;
  font-weight: 500;
}

.status-badge.status-open {
  background: rgba(29, 122, 95, 0.12);
  color: var(--success);
}

.status-badge.status-taken {
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
}

.status-badge.status-submitted {
  background: rgba(229, 191, 114, 0.15);
  color: #b8860b;
}

.status-badge.status-settled {
  background: rgba(29, 122, 95, 0.12);
  color: var(--success);
}

.status-badge.status-cancelled {
  background: rgba(173, 65, 54, 0.1);
  color: var(--danger);
}

.priority-tag {
  padding: 4px 10px;
  font-size: 11px;
  background: rgba(109, 116, 108, 0.1);
  color: var(--muted);
  border-radius: 999px;
}

.task-reward {
  text-align: right;
}

.task-reward strong {
  font-size: 18px;
  font-weight: 700;
  color: var(--brand-deep);
}

.task-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0;
  color: var(--text);
  line-height: 1.4;
}

.task-desc {
  font-size: 13px;
  color: var(--muted);
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.task-tag {
  padding: 3px 8px;
  font-size: 11px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid var(--line);
  color: var(--muted);
  border-radius: 6px;
}

.task-meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--muted);
}

.meta-item .el-icon {
  font-size: 14px;
}

.task-actions {
  display: flex;
  gap: 8px;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--line);
}

/* Hot Sidebar */
.hot-sidebar {
  padding: 20px;
  border-radius: 24px;
  position: sticky;
  top: 100px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.sidebar-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
}

.view-all {
  font-size: 12px;
  color: var(--accent);
  text-decoration: none;
}

.hot-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.hot-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.hot-item:hover {
  background: rgba(255, 255, 255, 0.8);
  transform: translateX(4px);
}

.hot-rank {
  width: 28px;
  height: 28px;
  min-width: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(109, 116, 108, 0.15);
  border-radius: 8px;
  font-weight: 700;
  font-size: 12px;
  color: var(--muted);
}

.hot-rank.gold {
  background: linear-gradient(135deg, #ffd700, #ffb347);
  color: #8B6914;
}

.hot-content h4 {
  font-size: 13px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--text);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.hot-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--muted);
}

.hot-meta strong {
  color: var(--brand-deep);
  font-weight: 600;
}

/* Empty State */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
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
  margin: 0;
  font-size: 14px;
}

/* Pagination */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 16px 0;
}

/* Drawer */
.drawer-loading {
  padding: 20px;
}

.task-detail {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.detail-header {
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.detail-badges {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.detail-header h2 {
  font-size: 22px;
  font-weight: 600;
  margin: 0;
  color: var(--text);
  line-height: 1.3;
}

.detail-reward {
  display: flex;
  gap: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 18px;
}

.reward-amount {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.reward-label {
  font-size: 11px;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.reward-amount strong {
  font-size: 20px;
  font-weight: 700;
  color: var(--text);
}

.reward-amount span {
  font-size: 14px;
  color: var(--muted);
}

.reward-amount.highlight {
  padding-left: 20px;
  border-left: 1px solid var(--line);
}

.reward-amount.highlight strong {
  color: var(--brand-deep);
}

.detail-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-section h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 0;
  color: var(--text);
}

.detail-section p {
  font-size: 14px;
  line-height: 1.7;
  color: var(--muted);
  margin: 0;
}

.detail-info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.info-item {
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
}

.info-label {
  display: block;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 4px;
}

.info-item strong {
  font-size: 13px;
  color: var(--text);
}

.detail-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-actions {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid var(--line);
}

.accept-btn {
  flex: 1;
}

.accept-btn,
.accept-btn + a {
  text-decoration: none;
}

/* Responsive */
@media (max-width: 1024px) {
  .tasks-section {
    grid-template-columns: 1fr;
  }

  .hot-sidebar {
    position: static;
  }

  .hero-banner {
    grid-template-columns: 1fr;
  }

  .filter-row {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    min-width: 100%;
  }
}
</style>
