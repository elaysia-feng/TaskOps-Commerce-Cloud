<template>
  <div class="page-stack">
    <!-- Hero Banner -->
    <section class="hero-banner hero-banner-soft">
      <div class="hero-content">
        <div class="hero-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <path d="M16 10a4 4 0 0 1-8 0"/>
          </svg>
          Orders
        </div>
        <h1>我的订单</h1>
        <p>查看订单详情、状态流转和待支付订单。</p>
        <div class="order-stats">
          <div class="stat-item">
            <strong>{{ orders.length }}</strong>
            <span>全部订单</span>
          </div>
          <div class="stat-item">
            <strong>{{ pendingCount }}</strong>
            <span>待支付</span>
          </div>
          <div class="stat-item">
            <strong>{{ paidCount }}</strong>
            <span>已支付</span>
          </div>
          <div class="stat-item">
            <strong>{{ doneCount }}</strong>
            <span>已完成</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Alerts -->
    <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="true" @close="errorText = ''" />
    <el-alert v-if="successText" :title="successText" type="success" show-icon :closable="true" @close="successText = ''" />

    <!-- Main Content -->
    <div class="content-grid">
      <!-- Orders List -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">List</p>
            <h3>订单列表</h3>
          </div>
          <div class="header-actions">
            <RouterLink to="/orders/create">
              <el-button type="primary" size="small">新建订单</el-button>
            </RouterLink>
            <el-button size="small" @click="loadOrders" :loading="loading">刷新</el-button>
          </div>
        </div>

        <div v-if="orders.length" class="orders-grid" v-loading="loading">
          <article
            v-for="item in orders"
            :key="item.orderNo"
            class="order-card hover-lift"
            :class="{ active: selectedOrder?.orderNo === item.orderNo }"
            @click="openOrder(item.orderNo)"
          >
            <div class="order-header">
              <strong>{{ item.orderNo }}</strong>
              <span class="status-badge" :class="'status-' + item.status.toLowerCase()">
                {{ orderStatusLabel(item.status) }}
              </span>
            </div>
            <div class="order-details">
              <span class="order-sku">{{ item.skuCode }}</span>
              <span class="order-amount">{{ formatAmount(item.amount) }}</span>
            </div>
            <div class="order-actions" @click.stop>
              <RouterLink :to="`/payments?orderNo=${item.orderNo}`">
                <el-button size="small" type="primary" plain>去支付</el-button>
              </RouterLink>
              <el-button
                size="small"
                type="warning"
                plain
                :disabled="item.status !== 'PENDING_PAY' || cancelingOrderNo === item.orderNo"
                @click="handleCancel(item.orderNo)"
              >
                {{ cancelingOrderNo === item.orderNo ? "取消中..." : "取消" }}
              </el-button>
            </div>
          </article>
        </div>
        <div v-else class="empty-state">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M6 2L3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z"/>
            <line x1="3" y1="6" x2="21" y2="6"/>
            <path d="M16 10a4 4 0 0 1-8 0"/>
          </svg>
          <h3>暂无订单</h3>
          <p>当前还没有订单，去创建第一个订单吧</p>
          <RouterLink to="/orders/create">
            <el-button type="primary">创建订单</el-button>
          </RouterLink>
        </div>
      </section>

      <!-- Order Detail -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">Detail</p>
            <h3>订单详情</h3>
          </div>
        </div>

        <div v-if="loadingDetail" class="detail-loading">
          <el-skeleton :rows="6" animated />
        </div>
        <div v-else-if="selectedOrder" class="order-detail">
          <div class="detail-header">
            <div>
              <h4>{{ selectedOrder.orderNo }}</h4>
              <span class="status-badge" :class="'status-' + selectedOrder.status.toLowerCase()">
                {{ orderStatusLabel(selectedOrder.status) }}
              </span>
            </div>
            <div class="detail-amount">{{ formatAmount(selectedOrder.amount) }}</div>
          </div>

          <div class="detail-grid">
            <div class="detail-item">
              <span class="item-label">SKU</span>
              <strong>{{ selectedOrder.skuCode }}</strong>
            </div>
            <div class="detail-item">
              <span class="item-label">数量</span>
              <strong>{{ selectedOrder.quantity }}</strong>
            </div>
            <div class="detail-item">
              <span class="item-label">用户 ID</span>
              <strong>{{ selectedOrder.userId || "-" }}</strong>
            </div>
            <div class="detail-item">
              <span class="item-label">状态</span>
              <strong>{{ orderStatusLabel(selectedOrder.status) }}</strong>
            </div>
            <div class="detail-item full-width">
              <span class="item-label">创建时间</span>
              <strong>{{ formatDateTime(selectedOrder.createdAt) }}</strong>
            </div>
            <div class="detail-item full-width">
              <span class="item-label">更新时间</span>
              <strong>{{ formatDateTime(selectedOrder.updatedAt) }}</strong>
            </div>
          </div>

          <div class="detail-actions" v-if="selectedOrder.status === 'PENDING_PAY'">
            <RouterLink :to="`/payments?orderNo=${selectedOrder.orderNo}`">
              <el-button type="primary" size="large">进入支付中心</el-button>
            </RouterLink>
          </div>
        </div>
        <div v-else class="empty-detail">
          <p>从左侧选择一条订单查看详情</p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import { ElMessage } from "element-plus";
import { cancelOrder, getOrder, listMyOrders } from "../api/order";
import { formatDateTime, orderStatusLabel } from "../utils/format";

const orders = ref([]);
const selectedOrder = ref(null);
const loading = ref(false);
const loadingDetail = ref(false);
const errorText = ref("");
const successText = ref("");
const cancelingOrderNo = ref("");

const pendingCount = computed(() => orders.value.filter((item) => item.status === "PENDING_PAY").length);
const paidCount = computed(() => orders.value.filter((item) => item.status === "PAID").length);
const doneCount = computed(() => orders.value.filter((item) => item.status === "DONE").length);

onMounted(loadOrders);

function formatAmount(amount) {
  return `¥${Number(amount || 0).toFixed(2)}`;
}

async function loadOrders() {
  loading.value = true;
  errorText.value = "";
  try {
    orders.value = await listMyOrders();
    if (orders.value.length && !selectedOrder.value) {
      await openOrder(orders.value[0].orderNo);
    }
  } catch (error) {
    errorText.value = error.message || "加载订单失败";
  } finally {
    loading.value = false;
  }
}

async function openOrder(orderNo) {
  loadingDetail.value = true;
  errorText.value = "";
  try {
    selectedOrder.value = await getOrder(orderNo);
  } catch (error) {
    errorText.value = error.message || "加载订单详情失败";
  } finally {
    loadingDetail.value = false;
  }
}

async function handleCancel(orderNo) {
  cancelingOrderNo.value = orderNo;
  errorText.value = "";
  successText.value = "";
  try {
    await cancelOrder(orderNo);
    ElMessage.success(`订单已取消：${orderNo}`);
    await loadOrders();
  } catch (error) {
    errorText.value = error.message || "取消订单失败";
  } finally {
    cancelingOrderNo.value = "";
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
  display: flex;
  justify-content: space-between;
  align-items: center;
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

.order-stats {
  display: flex;
  gap: 24px;
  margin-top: 8px;
}

.stat-item {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.stat-item strong {
  font-size: 24px;
  font-weight: 700;
}

.stat-item span {
  font-size: 13px;
  color: rgba(255, 247, 239, 0.7);
}

/* Content Grid */
.content-grid {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 20px;
  align-items: start;
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

.header-actions {
  display: flex;
  gap: 8px;
}

.header-actions a {
  text-decoration: none;
}

/* Orders Grid */
.orders-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.order-card {
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 18px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.25s;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.order-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(65, 42, 21, 0.1);
}

.order-card.active {
  border-color: var(--accent);
  background: rgba(30, 107, 102, 0.05);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-header strong {
  font-size: 13px;
  color: var(--text);
  word-break: break-all;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  font-size: 11px;
  border-radius: 999px;
  font-weight: 500;
  white-space: nowrap;
}

.status-badge.status-pending_pay {
  background: rgba(229, 191, 114, 0.15);
  color: #b8860b;
}

.status-badge.status-paid,
.status-badge.status-done {
  background: rgba(29, 122, 95, 0.12);
  color: var(--success);
}

.status-badge.status-cancelled {
  background: rgba(173, 65, 54, 0.1);
  color: var(--danger);
}

.order-details {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-sku {
  font-size: 12px;
  color: var(--muted);
}

.order-amount {
  font-size: 18px;
  font-weight: 700;
  color: var(--brand-deep);
}

.order-actions {
  display: flex;
  gap: 8px;
}

.order-actions a {
  text-decoration: none;
}

/* Order Detail */
.detail-loading {
  padding: 20px;
}

.order-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--line);
}

.detail-header h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 6px;
  color: var(--text);
  word-break: break-all;
}

.detail-amount {
  font-size: 24px;
  font-weight: 700;
  color: var(--brand-deep);
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.detail-item {
  padding: 12px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
}

.detail-item.full-width {
  grid-column: span 2;
}

.item-label {
  display: block;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 4px;
}

.detail-item strong {
  font-size: 13px;
  color: var(--text);
}

.detail-actions {
  padding-top: 16px;
  border-top: 1px solid var(--line);
}

.detail-actions a {
  text-decoration: none;
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

.empty-detail {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 40px;
  color: var(--muted);
  font-size: 14px;
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

  .order-stats {
    flex-wrap: wrap;
  }

  .orders-grid {
    grid-template-columns: 1fr;
  }
}
</style>
