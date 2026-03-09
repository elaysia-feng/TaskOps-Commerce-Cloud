<template>
  <div class="order-shell">
    <section class="card">
      <p class="section-kicker">支付</p>
      <h1>支付已有订单</h1>
      <p class="muted">输入已有订单号后再查询和支付，不再和创建订单混在一起。</p>

      <form class="form-grid mt12" @submit.prevent="handleQueryOrder">
        <label>
          订单号
          <input v-model.trim="orderNo" placeholder="ORD..." required />
        </label>
        <div class="action-row action-row-start">
          <button class="btn secondary" :disabled="loadingQuery">
            {{ loadingQuery ? "查询中..." : "查询订单" }}
          </button>
          <button class="btn" type="button" :disabled="!canPay || loadingPay" @click="handleMockPay">
            {{ loadingPay ? "支付中..." : "模拟支付回调" }}
          </button>
        </div>
      </form>

      <p v-if="errorText" class="error mt12">{{ errorText }}</p>
      <p v-if="successText" class="success mt12">{{ successText }}</p>

      <article v-if="orderDetail" class="task-card mt12">
        <header>
          <h3>{{ orderDetail.orderNo }}</h3>
          <span class="tag">{{ statusLabel(orderDetail.status) }}</span>
        </header>
        <p class="muted">SKU：{{ orderDetail.skuCode }}</p>
        <p class="muted">数量：{{ orderDetail.quantity }}</p>
        <p class="muted">金额：{{ formatAmount(orderDetail.amount) }}</p>
        <p class="muted">用户 ID：{{ orderDetail.userId }}</p>
        <p v-if="orderDetail.status !== 'PENDING_PAY'" class="muted mt12">当前订单状态不可支付。</p>
      </article>
    </section>

    <section class="card helper-card">
      <p class="section-kicker">说明</p>
      <h2>推荐流程</h2>
      <ol class="helper-list mt12">
        <li>先在创建订单页面生成订单。</li>
        <li>将生成的订单号带到本页面。</li>
        <li>先查询订单状态，再按需触发支付回调。</li>
        <li>最后到我的订单页面查看完整结果。</li>
      </ol>
      <div class="action-row mt12 action-row-start">
        <RouterLink class="btn secondary" to="/orders/create">创建订单</RouterLink>
        <RouterLink class="btn secondary" to="/account/orders">我的订单</RouterLink>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref, watch } from "vue";
import { RouterLink, useRoute } from "vue-router";
import { getOrder } from "../api/order";
import { mockPayCallback } from "../api/pay";

const route = useRoute();
const loadingPay = ref(false);
const loadingQuery = ref(false);
const errorText = ref("");
const successText = ref("");
const orderNo = ref(route.query.orderNo || "");
const orderDetail = ref(null);

const canPay = computed(() => !!orderNo.value && orderDetail.value?.status === "PENDING_PAY");

watch(
  () => route.query.orderNo,
  (value) => {
    orderNo.value = value || "";
  }
);

function formatAmount(amount) {
  return `CNY ${Number(amount).toFixed(2)}`;
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

async function handleMockPay() {
  if (!orderNo.value) {
    errorText.value = "请先输入订单号";
    return;
  }
  if (!canPay.value) {
    errorText.value = "只有待支付订单才能支付";
    return;
  }
  loadingPay.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    await mockPayCallback(orderNo.value);
    successText.value = "支付回调已发送";
    await handleQueryOrder();
  } catch (error) {
    errorText.value = error.message || "支付回调失败";
  } finally {
    loadingPay.value = false;
  }
}

async function handleQueryOrder() {
  if (!orderNo.value) {
    errorText.value = "请先输入订单号";
    return;
  }
  loadingQuery.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    orderDetail.value = await getOrder(orderNo.value);
  } catch (error) {
    errorText.value = error.message || "查询订单失败";
  } finally {
    loadingQuery.value = false;
  }
}
</script>