<template>
  <div class="page-stack">
    <!-- Hero Balance Card -->
    <section class="balance-hero glass-card">
      <div class="balance-content">
        <div class="balance-label">
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
            <line x1="1" y1="10" x2="23" y2="10"/>
          </svg>
          我的钱包
        </div>
        <div class="balance-amount">
          <span class="currency">¥</span>
          <strong>{{ formatMoney(overview.availableAmount) }}</strong>
        </div>
        <p class="balance-caption">
          冻结金额：{{ formatMoney(overview.frozenAmount) }} ·
          钱包状态：{{ walletStatusLabel(overview.walletStatus) }}
        </p>
      </div>
      <div class="balance-stats">
        <div class="balance-stat">
          <span class="stat-value">{{ formatMoney(overview.totalIncome) }}</span>
          <span class="stat-label">累计收入</span>
        </div>
        <div class="balance-stat">
          <span class="stat-value">{{ formatMoney(overview.totalExpense) }}</span>
          <span class="stat-label">累计支出</span>
        </div>
        <div class="balance-stat">
          <span class="stat-value">{{ formatMoney(overview.totalWithdraw) }}</span>
          <span class="stat-label">累计提现</span>
        </div>
      </div>
    </section>

    <!-- Alerts -->
    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="true" @close="errorText = ''" />
    <el-alert v-if="successText" :title="successText" type="success" show-icon :closable="true" @close="successText = ''" />

    <!-- Main Content Grid -->
    <div class="content-grid">
      <!-- Withdraw Section -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">Withdraw</p>
            <h3>发起提现</h3>
          </div>
        </div>

        <el-form :model="withdrawForm" class="withdraw-form" size="default" @submit.prevent="handleWithdraw">
          <el-form-item>
            <template #label><span>提现金额</span></template>
            <el-input-number v-model="withdrawForm.amount" :min="0.01" :precision="2" :step="0.01" />
          </el-form-item>
          <el-form-item label="账户类型">
            <el-select v-model="withdrawForm.accountType" placeholder="选择账户类型">
              <el-option label="支付宝" value="ALIPAY" />
              <el-option label="银行转账" value="BANK" />
            </el-select>
          </el-form-item>
          <el-form-item label="账号">
            <el-input v-model.trim="withdrawForm.accountNo" placeholder="输入收款账号" />
          </el-form-item>
          <el-form-item label="收款人">
            <el-input v-model.trim="withdrawForm.accountName" placeholder="输入账户姓名" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model.trim="withdrawForm.remark" type="textarea" :rows="2" placeholder="可选，补充打款说明" />
          </el-form-item>
          <el-button type="primary" native-type="submit" :loading="submittingWithdraw" class="withdraw-btn">
            {{ submittingWithdraw ? "提交中..." : "发起提现" }}
          </el-button>
        </el-form>
      </section>

      <!-- Withdraw Records -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">Withdraw Records</p>
            <h3>我的提现记录</h3>
          </div>
          <el-button size="small" @click="loadWithdraws">刷新</el-button>
        </div>

        <div v-if="withdrawPage.records.length" class="records-list">
          <article v-for="item in withdrawPage.records" :key="item.id" class="record-item">
            <div class="record-header">
              <strong>{{ item.withdrawNo }}</strong>
              <span class="status-badge" :class="'status-' + item.status.toLowerCase()">
                {{ withdrawStatusLabel(item.status) }}
              </span>
            </div>
            <div class="record-details">
              <span>金额：{{ formatMoney(item.amount) }}</span>
              <span>账户：{{ item.accountType }} / {{ item.accountName }}</span>
            </div>
            <div class="record-time">
              <span>创建：{{ formatDateTime(item.createdAt) }}</span>
              <span v-if="item.auditAt">审核：{{ formatDateTime(item.auditAt) }}</span>
            </div>
            <p v-if="item.rejectReason" class="reject-reason">驳回原因：{{ item.rejectReason }}</p>
          </article>
        </div>
        <div v-else class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
            <line x1="1" y1="10" x2="23" y2="10"/>
          </svg>
          <p>还没有提现记录</p>
        </div>
      </section>
    </div>

    <!-- Flow Section -->
    <section class="flow-section glass-card">
      <div class="card-header">
        <div>
          <p class="card-eyebrow">Flows</p>
          <h3>钱包流水</h3>
        </div>
        <div class="flow-filters">
          <el-select v-model="flowFilters.flowType" placeholder="流水类型" clearable @change="loadFlows" size="small">
            <el-option label="全部" value="" />
            <el-option label="收入" value="INCOME" />
            <el-option label="支出" value="EXPENSE" />
            <el-option label="提现" value="WITHDRAW" />
            <el-option label="冻结" value="FREEZE" />
            <el-option label="解冻" value="UNFREEZE" />
          </el-select>
          <el-button size="small" @click="loadFlows">刷新</el-button>
        </div>
      </div>

      <div v-if="flowPage.records.length" class="flow-list">
        <article v-for="item in flowPage.records" :key="item.flowNo" class="flow-item">
          <div class="flow-icon" :class="item.direction === 'IN' ? 'income' : 'expense'">
            <svg v-if="item.direction === 'IN'" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="19" x2="12" y2="5"/>
              <polyline points="5 12 12 5 19 12"/>
            </svg>
            <svg v-else width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="5" x2="12" y2="19"/>
              <polyline points="19 12 12 19 5 12"/>
            </svg>
          </div>
          <div class="flow-content">
            <div class="flow-header">
              <strong>{{ item.flowNo }}</strong>
              <span class="flow-amount" :class="item.direction === 'IN' ? 'income' : 'expense'">
                {{ item.direction === 'IN' ? '+' : '-' }}{{ formatMoney(item.amount) }}
              </span>
            </div>
            <div class="flow-meta">
              <span>{{ flowTypeLabel(item.flowType) }}</span>
              <span>业务：{{ item.bizType || '-' }}</span>
            </div>
            <div class="flow-balance">
              <span>余额：{{ formatMoney(item.balanceAfter) }}</span>
              <span>{{ formatDateTime(item.createdAt) }}</span>
            </div>
          </div>
        </article>
      </div>
      <div v-else class="empty-state">
        <p>当前没有流水记录</p>
      </div>
    </section>

    <!-- Admin Section -->
    <section v-if="showAdminPanel" class="admin-section glass-card">
      <div class="card-header">
        <div>
          <p class="card-eyebrow">Admin Wallet</p>
          <h3>管理员审核中心</h3>
        </div>
      </div>

      <div class="admin-content">
        <!-- User Lookup -->
        <div class="admin-panel">
          <h4>查询指定用户钱包</h4>
          <div class="lookup-form">
            <el-input v-model.trim="auditUserId" placeholder="输入用户 ID" size="small" />
            <el-button size="small" @click="loadAuditOverview">查询</el-button>
          </div>
          <div v-if="auditOverview.availableAmount !== undefined" class="audit-overview">
            <div class="overview-item">
              <span>可用余额</span>
              <strong>{{ formatMoney(auditOverview.availableAmount) }}</strong>
            </div>
            <div class="overview-item">
              <span>冻结金额</span>
              <strong>{{ formatMoney(auditOverview.frozenAmount) }}</strong>
            </div>
            <div class="overview-item">
              <span>累计收入</span>
              <strong>{{ formatMoney(auditOverview.totalIncome) }}</strong>
            </div>
            <div class="overview-item">
              <span>钱包状态</span>
              <strong>{{ walletStatusLabel(auditOverview.walletStatus) }}</strong>
            </div>
          </div>
        </div>

        <!-- Withdraw Audit -->
        <div class="admin-panel">
          <div class="panel-header">
            <h4>全部提现申请</h4>
            <el-button size="small" @click="loadAdminWithdraws">刷新</el-button>
          </div>

          <div v-if="adminWithdrawPage.records.length" class="audit-list">
            <article v-for="item in adminWithdrawPage.records" :key="item.id" class="audit-item">
              <div class="audit-header">
                <strong>{{ item.withdrawNo }}</strong>
                <span class="status-badge" :class="'status-' + item.status.toLowerCase()">
                  {{ withdrawStatusLabel(item.status) }}
                </span>
              </div>
              <div class="audit-details">
                <span>金额：{{ formatMoney(item.amount) }}</span>
                <span>账户：{{ item.accountType }} / {{ item.accountName }}</span>
              </div>
              <p class="audit-time">创建：{{ formatDateTime(item.createdAt) }}</p>
              <div class="audit-actions" v-if="item.status === 'PENDING'">
                <el-input v-model.trim="auditReasons[item.id]" placeholder="驳回原因" size="small" />
                <el-button size="small" type="success" @click="handleApprove(item.id)">通过</el-button>
                <el-button size="small" type="danger" @click="handleReject(item.id)">驳回</el-button>
              </div>
            </article>
          </div>
          <div v-else class="empty-state">
            <p>当前没有待审核提现记录</p>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import {
  approveWithdraw,
  applyWithdraw,
  getAllWithdraws,
  getMyWithdraws,
  getWalletFlows,
  getWalletOverview,
  getWalletOverviewByAdmin,
  rejectWithdraw
} from "../api/wallet";
import { getUser } from "../utils/auth";
import {
  flowDirectionLabel,
  flowTypeLabel,
  formatDateTime,
  formatMoney,
  isAdmin,
  walletStatusLabel,
  withdrawStatusLabel
} from "../utils/format";

const currentUser = getUser() || {};
const currentUserId = currentUser.userId;
const showAdminPanel = isAdmin(currentUser);
const errorText = ref("");
const successText = ref("");
const submittingWithdraw = ref(false);
const overview = reactive({});
const auditOverview = reactive({});
const flowPage = reactive({ records: [], total: 0, pageNum: 1, pageSize: 10 });
const withdrawPage = reactive({ records: [], total: 0, pageNum: 1, pageSize: 10 });
const adminWithdrawPage = reactive({ records: [], total: 0, pageNum: 1, pageSize: 10 });
const flowFilters = reactive({ flowType: "" });
const withdrawForm = reactive({
  amount: 20,
  accountType: "ALIPAY",
  accountNo: "",
  accountName: "",
  remark: ""
});
const auditUserId = ref(String(currentUserId || ""));
const auditReasons = ref({});

onMounted(() => {
  loadWalletData();
  if (showAdminPanel) {
    loadAuditOverview();
    loadAdminWithdraws();
  }
});

async function loadWalletData() {
  if (!currentUserId) {
    errorText.value = "当前登录信息里没有 userId，无法加载钱包。";
    return;
  }
  await Promise.allSettled([loadOverview(), loadFlows(), loadWithdraws()]);
}

async function loadOverview() {
  try {
    Object.assign(overview, await getWalletOverview(currentUserId));
  } catch (error) {
    errorText.value = error.message || "加载钱包总览失败";
  }
}

async function loadFlows() {
  try {
    const data = await getWalletFlows({
      userId: currentUserId,
      pageNum: flowPage.pageNum,
      pageSize: flowPage.pageSize,
      flowType: flowFilters.flowType || undefined
    });
    Object.assign(flowPage, data || {});
  } catch (error) {
    errorText.value = error.message || "加载钱包流水失败";
  }
}

async function loadWithdraws() {
  try {
    const data = await getMyWithdraws({
      userId: currentUserId,
      pageNum: withdrawPage.pageNum,
      pageSize: withdrawPage.pageSize
    });
    Object.assign(withdrawPage, data || {});
  } catch (error) {
    errorText.value = error.message || "加载提现记录失败";
  }
}

async function handleWithdraw() {
  submittingWithdraw.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    await applyWithdraw(currentUserId, { ...withdrawForm });
    ElMessage.success("提现申请已提交！");
    withdrawForm.amount = 20;
    withdrawForm.accountNo = "";
    withdrawForm.accountName = "";
    withdrawForm.remark = "";
    await loadWalletData();
  } catch (error) {
    errorText.value = error.message || "发起提现失败";
  } finally {
    submittingWithdraw.value = false;
  }
}

async function loadAuditOverview() {
  if (!auditUserId.value) return;
  try {
    Object.assign(auditOverview, await getWalletOverviewByAdmin(auditUserId.value));
  } catch (error) {
    errorText.value = error.message || "加载管理员钱包查询失败";
  }
}

async function loadAdminWithdraws() {
  try {
    const data = await getAllWithdraws({
      pageNum: adminWithdrawPage.pageNum,
      pageSize: adminWithdrawPage.pageSize
    });
    Object.assign(adminWithdrawPage, data || {});
  } catch (error) {
    errorText.value = error.message || "加载管理员提现审核列表失败";
  }
}

async function handleApprove(withdrawId) {
  try {
    await approveWithdraw(withdrawId, currentUserId);
    ElMessage.success("提现申请已通过！");
    await Promise.allSettled([loadAdminWithdraws(), loadWalletData(), loadAuditOverview()]);
  } catch (error) {
    errorText.value = error.message || "通过提现失败";
  }
}

async function handleReject(withdrawId) {
  if (!auditReasons.value[withdrawId]) {
    errorText.value = "请先填写驳回原因";
    return;
  }
  try {
    await rejectWithdraw(withdrawId, { reason: auditReasons.value[withdrawId] }, currentUserId);
    ElMessage.success("提现申请已驳回");
    auditReasons.value[withdrawId] = "";
    await Promise.allSettled([loadAdminWithdraws(), loadWalletData(), loadAuditOverview()]);
  } catch (error) {
    errorText.value = error.message || "驳回提现失败";
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
   Balance Hero
   ======================== */
.balance-hero {
  padding: 32px;
  border-radius: 28px;
  background: linear-gradient(135deg, #1f5f63 0%, #21444b 50%, #2d3a4a 100%);
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.balance-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.balance-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: rgba(255, 247, 239, 0.8);
}

.balance-amount {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.currency {
  font-size: 24px;
  font-weight: 600;
  opacity: 0.8;
}

.balance-amount strong {
  font-size: 48px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.balance-caption {
  font-size: 13px;
  color: rgba(255, 247, 239, 0.6);
  margin: 0;
}

.balance-stats {
  display: flex;
  gap: 32px;
}

.balance-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 24px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(10px);
}

.stat-value {
  font-size: 18px;
  font-weight: 700;
  color: #fff;
}

.stat-label {
  font-size: 11px;
  color: rgba(255, 247, 239, 0.6);
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

/* ========================
   Content Cards
   ======================== */
.content-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.content-card {
  padding: 24px;
  border-radius: 24px;
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

/* Withdraw Form */
.withdraw-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.withdraw-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.withdraw-btn {
  margin-top: 8px;
}

/* Records List */
.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-item {
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  border: 1px solid var(--line);
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.record-header strong {
  font-size: 13px;
  color: var(--text);
}

.status-badge {
  padding: 4px 10px;
  font-size: 11px;
  border-radius: 999px;
  font-weight: 500;
}

.status-badge.status-pending {
  background: rgba(229, 191, 114, 0.15);
  color: #b8860b;
}

.status-badge.status-approved,
.status-badge.status-paid {
  background: rgba(29, 122, 95, 0.12);
  color: var(--success);
}

.status-badge.status-rejected {
  background: rgba(173, 65, 54, 0.1);
  color: var(--danger);
}

.record-details {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 4px;
}

.record-time {
  display: flex;
  gap: 16px;
  font-size: 11px;
  color: var(--muted);
}

.reject-reason {
  font-size: 12px;
  color: var(--danger);
  margin: 8px 0 0;
}

/* Flow Section */
.flow-section {
  padding: 24px;
  border-radius: 24px;
}

.flow-filters {
  display: flex;
  gap: 12px;
}

.flow-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.flow-item {
  display: flex;
  gap: 14px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  border: 1px solid var(--line);
}

.flow-icon {
  width: 40px;
  height: 40px;
  min-width: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
}

.flow-icon.income {
  background: rgba(29, 122, 95, 0.12);
  color: var(--success);
}

.flow-icon.expense {
  background: rgba(173, 65, 54, 0.1);
  color: var(--danger);
}

.flow-content {
  flex: 1;
  overflow: hidden;
}

.flow-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 6px;
}

.flow-header strong {
  font-size: 13px;
  color: var(--text);
}

.flow-amount {
  font-size: 14px;
  font-weight: 700;
}

.flow-amount.income {
  color: var(--success);
}

.flow-amount.expense {
  color: var(--danger);
}

.flow-meta,
.flow-balance {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 4px;
}

/* Admin Section */
.admin-section {
  padding: 24px;
  border-radius: 24px;
}

.admin-content {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.admin-panel {
  padding: 20px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 18px;
}

.admin-panel h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 16px;
  color: var(--text);
}

.lookup-form {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.audit-overview {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.overview-item {
  padding: 12px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 12px;
  text-align: center;
}

.overview-item span {
  display: block;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 4px;
}

.overview-item strong {
  font-size: 14px;
  color: var(--text);
}

.audit-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.audit-item {
  padding: 14px;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 14px;
}

.audit-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.audit-header strong {
  font-size: 13px;
}

.audit-details {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 4px;
}

.audit-time {
  font-size: 11px;
  color: var(--muted);
  margin: 0 0 8px;
}

.audit-actions {
  display: flex;
  gap: 8px;
  align-items: center;
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

/* Responsive */
@media (max-width: 1024px) {
  .balance-hero {
    flex-direction: column;
    align-items: flex-start;
    gap: 20px;
  }

  .balance-stats {
    width: 100%;
    justify-content: space-between;
  }

  .content-grid,
  .admin-content {
    grid-template-columns: 1fr;
  }

  .flow-list {
    grid-template-columns: 1fr;
  }
}
</style>
