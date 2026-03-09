<template>
  <div class="order-shell">
    <section class="card">
      <p class="section-kicker">订单</p>
      <h1>创建订单</h1>
      <p class="muted">先创建订单，再到独立的支付页面完成支付回调。</p>

      <form class="form-grid mt12" @submit.prevent="handleCreateOrder">
        <label>
          商品
          <select v-model="selectedSku" @change="handleSkuChange">
            <option v-for="item in products" :key="item.skuCode" :value="item.skuCode">
              {{ item.label }}
            </option>
          </select>
        </label>

        <div class="product-brief">
          <div>
            <span class="muted">SKU 编码</span>
            <strong>{{ currentProduct.skuCode }}</strong>
          </div>
          <div>
            <span class="muted">商品类型</span>
            <strong>{{ currentProduct.kind }}</strong>
          </div>
          <div>
            <span class="muted">建议价格</span>
            <strong>{{ formatAmount(currentProduct.price) }}</strong>
          </div>
        </div>

        <div class="form-split">
          <label>
            数量
            <input
              v-model.number="form.quantity"
              type="number"
              min="1"
              :max="currentProduct.membership ? 1 : 99"
              :disabled="currentProduct.membership"
              required
            />
          </label>
          <label>
            金额
            <input v-model.number="form.amount" type="number" min="0.01" step="0.01" required />
          </label>
        </div>

        <button class="btn" :disabled="loadingCreate">
          {{ loadingCreate ? "创建中..." : "创建订单" }}
        </button>
      </form>

      <p v-if="errorText" class="error mt12">{{ errorText }}</p>
      <p v-if="successText" class="success mt12">{{ successText }}</p>

      <article v-if="createdOrder" class="task-card mt12">
        <header>
          <h3>{{ createdOrder.orderNo }}</h3>
          <span class="tag">{{ createdOrder.status }}</span>
        </header>
        <p class="muted">SKU：{{ createdOrder.skuCode }}</p>
        <p class="muted">数量：{{ createdOrder.quantity }}</p>
        <p class="muted">金额：{{ formatAmount(createdOrder.amount) }}</p>
        <div class="action-row mt12 action-row-start">
          <RouterLink class="btn secondary" :to="`/payments?orderNo=${createdOrder.orderNo}`">前往支付</RouterLink>
          <RouterLink class="btn secondary" to="/account/orders">查看我的订单</RouterLink>
        </div>
      </article>
    </section>

    <section class="card catalog-card">
      <p class="section-kicker">商品目录</p>
      <h2>可选商品</h2>
      <div class="catalog-list mt12">
        <article v-for="item in products" :key="item.skuCode" class="task-card catalog-item">
          <header>
            <h3>{{ item.label }}</h3>
            <span class="tag">{{ item.membership ? "会员" : "库存商品" }}</span>
          </header>
          <p class="muted">{{ item.skuCode }}</p>
          <p class="muted">价格：{{ formatAmount(item.price) }}</p>
          <button class="btn secondary" type="button" @click="selectProduct(item.skuCode)">使用该商品</button>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { RouterLink } from "vue-router";
import { createOrder } from "../api/order";

const products = [
  { skuCode: "SKU_MEAL_001", label: "套餐 A", kind: "库存商品", price: 99.9, membership: false },
  { skuCode: "SKU_MEAL_002", label: "套餐 B", kind: "库存商品", price: 159.9, membership: false },
  { skuCode: "MEMBER_VIP", label: "VIP 会员", kind: "会员商品", price: 19.9, membership: true },
  { skuCode: "MEMBER_SVIP", label: "SVIP 会员", kind: "会员商品", price: 39.9, membership: true }
];

const loadingCreate = ref(false);
const errorText = ref("");
const successText = ref("");
const createdOrder = ref(null);
const selectedSku = ref(products[0].skuCode);
const form = reactive({
  quantity: 1,
  amount: products[0].price
});

const currentProduct = computed(() => products.find((item) => item.skuCode === selectedSku.value) || products[0]);

function formatAmount(amount) {
  return `CNY ${Number(amount).toFixed(2)}`;
}

function syncFormFromProduct() {
  form.amount = currentProduct.value.price;
  if (currentProduct.value.membership) {
    form.quantity = 1;
  }
}

function handleSkuChange() {
  syncFormFromProduct();
}

function selectProduct(skuCode) {
  selectedSku.value = skuCode;
  syncFormFromProduct();
}

async function handleCreateOrder() {
  loadingCreate.value = true;
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
    loadingCreate.value = false;
  }
}
</script>