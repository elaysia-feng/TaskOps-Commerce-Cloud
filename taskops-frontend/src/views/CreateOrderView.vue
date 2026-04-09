<template>
  <div class="page-stack">
    <!-- Hero Banner -->
    <section class="hero-banner">
      <div class="hero-content">
        <div class="hero-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="1" y="4" width="22" height="16" rx="2" ry="2"/>
            <line x1="1" y1="10" x2="23" y2="10"/>
          </svg>
          Commerce
        </div>
        <h1>创建订单</h1>
        <p>选择商品后可以直接创建订单，创建成功会自动给出下一步去支付和查看订单的入口。</p>
      </div>
      <div class="hero-aside">
        <div class="stat-mini glass-card">
          <span class="stat-mini-label">当前选择</span>
          <strong>{{ currentProduct.label }}</strong>
          <p>SKU: {{ currentProduct.skuCode }}</p>
        </div>
        <div class="stat-mini glass-card">
          <span class="stat-mini-label">订单总额</span>
          <strong>{{ formatAmount(form.amount * form.quantity) }}</strong>
          <p>数量 {{ form.quantity }}</p>
        </div>
      </div>
    </section>

    <!-- Main Content -->
    <div class="content-grid">
      <!-- Order Form -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">Create Order</p>
            <h3>订单创建器</h3>
          </div>
        </div>

        <el-form :model="form" class="order-form" size="default" @submit.prevent="handleCreateOrder">
          <el-form-item label="选择商品">
            <el-select v-model="selectedSku" @change="syncForm" class="product-select">
              <el-option
                v-for="item in products"
                :key="item.skuCode"
                :label="`${item.label} · ${formatAmount(item.price)}`"
                :value="item.skuCode"
              >
                <div class="product-option">
                  <span class="product-name">{{ item.label }}</span>
                  <span class="product-price">{{ formatAmount(item.price) }}</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>

          <div class="product-info">
            <div class="info-row">
              <span class="info-label">商品类型</span>
              <span class="info-value">{{ typeLabel(currentProduct.type) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">默认金额</span>
              <span class="info-value">{{ formatAmount(currentProduct.price) }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">场景标签</span>
              <span class="product-badge">{{ currentProduct.badge }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">商品说明</span>
              <span class="info-value">{{ currentProduct.subtitle }}</span>
            </div>
          </div>

          <div class="form-row">
            <el-form-item label="数量" class="form-item-half">
              <el-input-number v-model="form.quantity" :min="1" :max="currentProduct.membership ? 1 : 99" :disabled="currentProduct.membership" />
            </el-form-item>
            <el-form-item label="金额" class="form-item-half">
              <el-input-number v-model="form.amount" :min="0.01" :precision="2" :step="0.01" />
            </el-form-item>
          </div>

          <el-button type="primary" native-type="submit" :loading="loading" class="create-btn">
            {{ loading ? "创建中..." : "创建订单" }}
          </el-button>
        </el-form>

        <el-alert v-if="errorText" :title="errorText" type="error" show-icon :closable="true" @close="errorText = ''" />
        <el-alert v-if="successText" :title="successText" type="success" show-icon :closable="true" @close="successText = ''" />

        <!-- Created Order -->
        <div v-if="createdOrder" class="created-order">
          <div class="order-success-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
              <polyline points="22 4 12 14.01 9 11.01"/>
            </svg>
          </div>
          <div class="order-detail">
            <h4>{{ createdOrder.orderNo }}</h4>
            <p>{{ statusLabel(createdOrder.status) }}</p>
          </div>
          <div class="order-amount">{{ formatAmount(createdOrder.amount) }}</div>
          <div class="order-actions">
            <RouterLink :to="`/payments?orderNo=${createdOrder.orderNo}`">
              <el-button type="primary">去支付</el-button>
            </RouterLink>
            <RouterLink to="/account/orders">
              <el-button>查看订单</el-button>
            </RouterLink>
          </div>
        </div>
      </section>

      <!-- Product Catalog -->
      <section class="content-card glass-card">
        <div class="card-header">
          <div>
            <p class="card-eyebrow">Catalog</p>
            <h3>商品目录</h3>
          </div>
        </div>

        <div class="catalog-grid">
          <article
            v-for="item in products"
            :key="item.skuCode"
            class="catalog-item hover-lift"
            :class="{ active: item.skuCode === selectedSku }"
            @click="pickProduct(item.skuCode)"
          >
            <div class="catalog-header">
              <strong>{{ item.label }}</strong>
              <span class="catalog-badge" :class="item.type.toLowerCase()">{{ item.badge }}</span>
            </div>
            <p class="catalog-desc">{{ item.subtitle }}</p>
            <div class="catalog-footer">
              <span class="catalog-sku">{{ item.skuCode }}</span>
              <strong class="catalog-price">{{ formatAmount(item.price) }}</strong>
            </div>
          </article>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { RouterLink } from "vue-router";
import { ElMessage } from "element-plus";
import { createOrder } from "../api/order";
import { orderStatusLabel } from "../utils/format";

const products = [
  {
    skuCode: "SKU_MEAL_001",
    label: "校园生活套餐",
    subtitle: "适合跑腿、资料整理与即时服务",
    type: "STOCK",
    badge: "高频",
    price: 99.9,
    membership: false
  },
  {
    skuCode: "SKU_MEAL_002",
    label: "创意执行套餐",
    subtitle: "偏设计、内容和轻技术类服务场景",
    type: "STOCK",
    badge: "进阶",
    price: 159.9,
    membership: false
  },
  {
    skuCode: "MEMBER_VIP",
    label: "VIP 会员",
    subtitle: "适合中等频率发布和协作",
    type: "MEMBERSHIP",
    badge: "会员",
    price: 19.9,
    membership: true
  },
  {
    skuCode: "MEMBER_SVIP",
    label: "SVIP 会员",
    subtitle: "适合高频任务分发和更大配额",
    type: "MEMBERSHIP",
    badge: "旗舰",
    price: 39.9,
    membership: true
  }
];

const loading = ref(false);
const errorText = ref("");
const successText = ref("");
const createdOrder = ref(null);
const selectedSku = ref(products[0].skuCode);

const form = reactive({
  quantity: 1,
  amount: products[0].price
});

const currentProduct = computed(() => products.find((item) => item.skuCode === selectedSku.value) || products[0]);

function syncForm() {
  form.amount = currentProduct.value.price;
  if (currentProduct.value.membership) {
    form.quantity = 1;
  }
}

function pickProduct(skuCode) {
  selectedSku.value = skuCode;
  syncForm();
}

function formatAmount(amount) {
  return `¥${Number(amount || 0).toFixed(2)}`;
}

function statusLabel(status) {
  return orderStatusLabel(status);
}

function typeLabel(type) {
  return type === "MEMBERSHIP" ? "会员商品" : "库存商品";
}

async function handleCreateOrder() {
  loading.value = true;
  errorText.value = "";
  successText.value = "";
  createdOrder.value = null;
  try {
    const data = await createOrder({
      skuCode: selectedSku.value,
      quantity: form.quantity,
      amount: form.amount
    });
    createdOrder.value = {
      orderNo: data.orderNo,
      status: data.status,
      skuCode: selectedSku.value,
      quantity: form.quantity,
      amount: form.amount
    };
    ElMessage.success(`订单创建成功：${data.orderNo}`);
  } catch (error) {
    errorText.value = error.message || "创建订单失败";
  } finally {
    loading.value = false;
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
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 16px 48px rgba(102, 126, 234, 0.3);
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
  line-height: 1.6;
  color: rgba(255, 255, 255, 0.85);
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
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
}

.stat-mini-label {
  display: block;
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: rgba(255, 255, 255, 0.7);
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
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
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

/* Order Form */
.order-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.order-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.product-select {
  width: 100%;
}

.product-option {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.product-name {
  font-weight: 500;
}

.product-price {
  color: var(--brand-deep);
  font-weight: 600;
}

.product-info {
  padding: 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-label {
  font-size: 12px;
  color: var(--muted);
}

.info-value {
  font-size: 13px;
  font-weight: 500;
  color: var(--text);
}

.product-badge {
  padding: 3px 10px;
  font-size: 11px;
  background: rgba(30, 107, 102, 0.1);
  color: var(--accent);
  border-radius: 999px;
  font-weight: 500;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-item-half {
  flex: 1;
}

.create-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
}

/* Created Order */
.created-order {
  margin-top: 20px;
  padding: 20px;
  background: rgba(29, 122, 95, 0.08);
  border: 1px solid rgba(29, 122, 95, 0.2);
  border-radius: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
  text-align: center;
}

.order-success-icon {
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(29, 122, 95, 0.15);
  border-radius: 50%;
  color: var(--success);
}

.order-detail h4 {
  font-size: 14px;
  font-weight: 600;
  margin: 0;
  color: var(--text);
}

.order-detail p {
  font-size: 12px;
  color: var(--success);
  margin: 4px 0 0;
}

.order-amount {
  font-size: 24px;
  font-weight: 700;
  color: var(--brand-deep);
}

.order-actions {
  display: flex;
  gap: 12px;
}

.order-actions a {
  text-decoration: none;
}

/* Catalog */
.catalog-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.catalog-item {
  padding: 18px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 20px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: all 0.25s;
}

.catalog-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(65, 42, 21, 0.1);
}

.catalog-item.active {
  border-color: var(--accent);
  background: rgba(30, 107, 102, 0.05);
}

.catalog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.catalog-header strong {
  font-size: 14px;
  color: var(--text);
}

.catalog-badge {
  padding: 3px 8px;
  font-size: 10px;
  border-radius: 999px;
  font-weight: 600;
}

.catalog-badge.stock {
  background: rgba(64, 158, 255, 0.12);
  color: #409eff;
}

.catalog-badge.membership {
  background: rgba(229, 191, 114, 0.15);
  color: #b8860b;
}

.catalog-desc {
  font-size: 12px;
  color: var(--muted);
  margin: 0 0 12px;
  line-height: 1.4;
}

.catalog-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.catalog-sku {
  font-size: 11px;
  color: var(--muted);
}

.catalog-price {
  font-size: 16px;
  font-weight: 700;
  color: var(--brand-deep);
}

/* Responsive */
@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }

  .hero-banner {
    grid-template-columns: 1fr;
  }

  .catalog-grid {
    grid-template-columns: 1fr;
  }
}
</style>
