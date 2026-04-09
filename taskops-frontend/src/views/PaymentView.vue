<template>
  <div class="page-stack">
    <!-- Hero Banner -->
    <section class="hero-banner hero-banner-soft">
      <div class="hero-content">
        <div class="hero-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
            <line x1="1" y1="10" x2="23" y2="10"/>
          </svg>
          Payments
        </div>
        <h1>支付中心</h1>
        <p>使用订单号创建支付单、查询状态或关闭支付单。</p>
      </div>
      <div class="hero-aside">
        <div class="stat-mini glass-card">
          <span class="stat-mini-label">当前订单号</span>
          <strong>{{ orderNo || "未填写" }}</strong>
          <p>可直接从订单页跳转过来</p>
        </div>
      </div>
    </section>

    <!-- Alerts -->
    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="true" @close="errorText = ''" />
    <el-alert v-if="successText" :title="successText" type="success" show-icon :closable="true" @close="successText = ''" />

    <!-- Main Content -->
    <div class="content-grid">
      <!-- Payment Actions -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">Action</p>
            <h3>支付操作</h3>
          </div>
        </div>

        <el-form class="pay-form" @submit.prevent="handleCreate">
          <el-form-item label="订单号">
            <el-input v-model.trim="orderNo" placeholder="ORD..." clearable />
          </el-form-item>
          <div class="action-buttons">
            <el-button type="primary" @click="handleCreate" :loading="loadingCreate" :disabled="!orderNo">
              {{ loadingCreate ? "创建中..." : "创建支付单" }}
            </el-button>
            <el-button @click="handleQuery" :loading="loadingQuery" :disabled="!orderNo">
              {{ loadingQuery ? "查询中..." : "查询支付单" }}
            </el-button>
            <el-button type="warning" @click="handleClose" :loading="loadingClose" :disabled="!orderNo">
              {{ loadingClose ? "关闭中..." : "关闭支付单" }}
            </el-button>
          </div>
        </el-form>

        <!-- Payment Detail -->
        <div v-if="paymentDetail" class="payment-detail">
          <div class="payment-header">
            <div class="payment-info">
              <h4>{{ paymentDetail.payNo }}</h4>
              <span class="status-badge" :class="'status-' + paymentDetail.status.toLowerCase()">
                {{ payStatusLabel(paymentDetail.status) }}
              </span>
            </div>
            <div class="payment-amount">{{ formatAmount(paymentDetail.amount) }}</div>
          </div>

          <div class="payment-grid">
            <div class="payment-item">
              <span class="item-label">订单号</span>
              <strong>{{ paymentDetail.orderNo }}</strong>
            </div>
            <div class="payment-item">
              <span class="item-label">支付渠道</span>
              <strong>{{ paymentDetail.channel || "-" }}</strong>
            </div>
            <div class="payment-item">
              <span class="item-label">标题</span>
              <strong>{{ paymentDetail.subject || "-" }}</strong>
            </div>
            <div class="payment-item">
              <span class="item-label">第三方流水号</span>
              <strong>{{ paymentDetail.thirdTradeNo || "-" }}</strong>
            </div>
            <div class="payment-item">
              <span class="item-label">买家标识</span>
              <strong>{{ paymentDetail.buyerId || "-" }}</strong>
            </div>
            <div class="payment-item">
              <span class="item-label">支付时间</span>
              <strong>{{ formatDateTime(paymentDetail.payTime) }}</strong>
            </div>
          </div>

          <div v-if="payParamsEntries.length" class="pay-params">
            <h5>支付参数</h5>
            <div class="params-list">
              <div v-for="item in payParamsEntries" :key="item.key" class="param-item">
                <span class="param-key">{{ item.key }}</span>
                <span class="param-value">{{ item.value }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Payment Flow -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">Flow</p>
            <h3>支付流程说明</h3>
          </div>
        </div>

        <div class="flow-steps">
          <div class="flow-step">
            <div class="step-number">1</div>
            <div class="step-content">
              <strong>创建订单</strong>
              <p>先在订单页创建业务订单，获得唯一订单号。</p>
            </div>
          </div>
          <div class="flow-step">
            <div class="step-number">2</div>
            <div class="step-content">
              <strong>创建支付单</strong>
              <p>在本页创建支付单，会返回支付单号以及支付参数。</p>
            </div>
          </div>
          <div class="flow-step">
            <div class="step-number">3</div>
            <div class="step-content">
              <strong>查询支付状态</strong>
              <p>随时可根据订单号查询支付单是否已关闭或已成功。</p>
            </div>
          </div>
          <div class="flow-step">
            <div class="step-number">4</div>
            <div class="step-content">
              <strong>关闭支付单</strong>
              <p>如订单不再支付，可以直接在本页关闭支付单。</p>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { closePay, createPay, getPayDetail } from "../api/pay";
import { formatDateTime, payStatusLabel } from "../utils/format";

const route = useRoute();
const orderNo = ref("");
const loadingCreate = ref(false);
const loadingQuery = ref(false);
const loadingClose = ref(false);
const errorText = ref("");
const successText = ref("");
const paymentDetail = ref(null);

const payParamsEntries = computed(() =>
  Object.entries(paymentDetail.value?.payParams || {}).map(([key, value]) => ({ key, value }))
);

watch(
  () => route.query.orderNo,
  async (value) => {
    orderNo.value = value || "";
    if (value) {
      await handleQuery();
    }
  },
  { immediate: true }
);

function formatAmount(amount) {
  return `¥${Number(amount || 0).toFixed(2)}`;
}

async function handleCreate() {
  if (!orderNo.value) return;
  loadingCreate.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    paymentDetail.value = await createPay(orderNo.value);
    ElMessage.success(`支付单已创建：${paymentDetail.value.payNo}`);
  } catch (error) {
    errorText.value = error.message || "创建支付单失败";
  } finally {
    loadingCreate.value = false;
  }
}

async function handleQuery() {
  if (!orderNo.value) return;
  loadingQuery.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    paymentDetail.value = await getPayDetail(orderNo.value);
    ElMessage.success(`当前支付状态：${payStatusLabel(paymentDetail.value.status)}`);
  } catch (error) {
    errorText.value = error.message || "查询支付单失败";
  } finally {
    loadingQuery.value = false;
  }
}

async function handleClose() {
  if (!orderNo.value) return;
  loadingClose.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    await closePay(orderNo.value);
    ElMessage.success("支付单已关闭");
    paymentDetail.value = await getPayDetail(orderNo.value);
  } catch (error) {
    errorText.value = error.message || "关闭支付单失败";
  } finally {
    loadingClose.value = false;
  }
}
</script>

<style scoped>
.page-stack {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Hero Banner */
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
}

.hero-content p {
  font-size: 14px;
  color: rgba(255, 247, 239, 0.8);
  margin: 0;
}

.hero-aside {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-mini {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
}

.stat-mini-label {
  display: block;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: rgba(255, 247, 239, 0.7);
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
  color: rgba(255, 247, 239, 0.6);
  margin: 0;
}

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
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

/* Pay Form */
.pay-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.pay-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.action-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

/* Payment Detail */
.payment-detail {
  margin-top: 24px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  border: 1px solid var(--line);
}

.payment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.payment-info h4 {
  font-size: 14px;
  margin: 0 0 6px;
  color: var(--text);
  word-break: break-all;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  border-radius: 999px;
  font-weight: 500;
}

.status-badge.status-created {
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
}

.status-badge.status-wait_buyer_pay {
  background: rgba(229, 191, 114, 0.15);
  color: #b8860b;
}

.status-badge.status-success {
  background: rgba(29, 122, 95, 0.12);
  color: var(--success);
}

.status-badge.status-closed {
  background: rgba(173, 65, 54, 0.1);
  color: var(--danger);
}

.payment-amount {
  font-size: 24px;
  font-weight: 700;
  color: var(--brand-deep);
}

.payment-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.payment-item {
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
}

.item-label {
  display: block;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 4px;
}

.payment-item strong {
  font-size: 12px;
  color: var(--text);
  word-break: break-all;
}

.pay-params {
  margin-top: 16px;
}

.pay-params h5 {
  font-size: 13px;
  font-weight: 600;
  margin: 0 0 12px;
  color: var(--text);
}

.params-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.param-item {
  display: flex;
  gap: 12px;
  padding: 10px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 10px;
  font-size: 12px;
}

.param-key {
  color: var(--muted);
  min-width: 80px;
}

.param-value {
  color: var(--text);
  word-break: break-all;
}

/* Flow Steps */
.flow-steps {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.flow-step {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
}

.step-number {
  width: 36px;
  height: 36px;
  min-width: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--accent), #2ea49b);
  color: #fff;
  border-radius: 12px;
  font-weight: 700;
  font-size: 14px;
}

.step-content strong {
  display: block;
  font-size: 14px;
  color: var(--text);
  margin-bottom: 4px;
}

.step-content p {
  font-size: 12px;
  color: var(--muted);
  margin: 0;
  line-height: 1.5;
}

/* Responsive */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .hero-banner {
    grid-template-columns: 1fr;
  }

  .payment-grid {
    grid-template-columns: 1fr;
  }
}
</style>
