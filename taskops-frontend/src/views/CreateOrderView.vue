<template>
  <section class="page-grid">
    <section class="panel">
      <div class="panel-head">
        <p class="eyebrow">订单</p>
        <h1>创建订单</h1>
        <p class="muted">当前订单创建逻辑仍然对接仓库中的实时 `order-service` 接口。</p>
      </div>

      <form class="stack" @submit.prevent="handleCreateOrder">
        <label>
          商品
          <select v-model="selectedSku" @change="syncForm">
            <option v-for="item in products" :key="item.skuCode" :value="item.skuCode">
              {{ item.label }}
            </option>
          </select>
        </label>

        <div class="product-box">
          <div class="meta-row"><span>SKU 编码</span><strong>{{ currentProduct.skuCode }}</strong></div>
          <div class="meta-row"><span>商品类型</span><strong>{{ currentProduct.type }}</strong></div>
          <div class="meta-row"><span>默认金额</span><strong>{{ formatAmount(currentProduct.price) }}</strong></div>
        </div>

        <div class="split-grid">
          <label>
            数量
            <input v-model.number="form.quantity" type="number" min="1" :disabled="currentProduct.membership" />
          </label>
          <label>
            金额
            <input v-model.number="form.amount" type="number" min="0.01" step="0.01" />
          </label>
        </div>

        <button class="btn" :disabled="loading">{{ loading ? "创建中..." : "创建订单" }}</button>
      </form>

      <p v-if="errorText" class="error">{{ errorText }}</p>
      <p v-if="successText" class="success">{{ successText }}</p>

      <article v-if="createdOrder" class="panel nested-panel">
        <header class="card-head">
          <h3>{{ createdOrder.orderNo }}</h3>
          <span class="tag">{{ statusLabel(createdOrder.status) }}</span>
        </header>
        <div class="meta-stack">
          <div class="meta-row"><span>SKU</span><strong>{{ createdOrder.skuCode }}</strong></div>
          <div class="meta-row"><span>数量</span><strong>{{ createdOrder.quantity }}</strong></div>
          <div class="meta-row"><span>金额</span><strong>{{ formatAmount(createdOrder.amount) }}</strong></div>
        </div>
        <div class="button-row">
          <RouterLink class="btn ghost" :to="`/payments?orderNo=${createdOrder.orderNo}`">去支付</RouterLink>
          <RouterLink class="btn ghost" to="/account/orders">我的订单</RouterLink>
        </div>
      </article>
    </section>

    <section class="panel">
      <div class="panel-head">
        <p class="eyebrow">商品目录</p>
        <h2>可选商品</h2>
      </div>
      <div class="list-grid">
        <article v-for="item in products" :key="item.skuCode" class="list-card">
          <header>
            <h3>{{ item.label }}</h3>
            <span class="tag">{{ typeLabel(item.type) }}</span>
          </header>
          <p class="muted">{{ item.skuCode }}</p>
          <p class="muted">{{ formatAmount(item.price) }}</p>
          <button class="btn ghost" @click="pickProduct(item.skuCode)">使用该商品</button>
        </article>
      </div>
    </section>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { RouterLink } from "vue-router";
import { createOrder } from "../api/order";

const products = [
  { skuCode: "SKU_MEAL_001", label: "套餐 A", type: "STOCK", price: 99.9, membership: false },
  { skuCode: "SKU_MEAL_002", label: "套餐 B", type: "STOCK", price: 159.9, membership: false },
  { skuCode: "MEMBER_VIP", label: "VIP 会员", type: "MEMBERSHIP", price: 19.9, membership: true },
  { skuCode: "MEMBER_SVIP", label: "SVIP 会员", type: "MEMBERSHIP", price: 39.9, membership: true }
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
  return `¥${Number(amount).toFixed(2)}`;
}

function statusLabel(status) {
  const map = {
    PENDING_PAY: "待支付",
    PAID: "已支付",
    DONE: "已完成",
    CANCELLED: "已取消"
  };
  return map[status] || status;
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
    successText.value = `订单创建成功：${data.orderNo}`;
  } catch (error) {
    errorText.value = error.message || "创建订单失败";
  } finally {
    loading.value = false;
  }
}
</script>