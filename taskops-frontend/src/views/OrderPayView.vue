<template>
  <section class="card">
    <h1>下单与支付</h1>

    <form class="order-form" @submit.prevent="handleCreateOrder">
      <label>
        SKU 编码
        <input v-model.trim="form.skuCode" placeholder="SKU-1001" required />
      </label>
      <label>
        数量
        <input v-model.number="form.quantity" type="number" min="1" required />
      </label>
      <label>
        金额
        <input v-model.number="form.amount" type="number" min="0.01" step="0.01" required />
      </label>
      <button class="btn" :disabled="loadingCreate">{{ loadingCreate ? "下单中..." : "创建订单" }}</button>
    </form>

    <div class="action-row mt12">
      <button class="btn secondary" :disabled="!orderNo || loadingPay" @click="handleMockPay">
        {{ loadingPay ? "回调中..." : "模拟支付回调" }}
      </button>
      <button class="btn secondary" :disabled="!orderNo || loadingQuery" @click="handleQueryOrder">
        {{ loadingQuery ? "查询中..." : "查询订单状态" }}
      </button>
    </div>

    <p class="error" v-if="errorText">{{ errorText }}</p>
    <p class="success" v-if="successText">{{ successText }}</p>

    <article class="task-card mt12" v-if="orderDetail">
      <header>
        <h3>订单号：{{ orderDetail.orderNo }}</h3>
        <span class="tag">{{ orderDetail.status }}</span>
      </header>
      <p class="muted">用户ID：{{ orderDetail.userId }}</p>
      <p class="muted">SKU：{{ orderDetail.skuCode }}</p>
      <p class="muted">数量：{{ orderDetail.quantity }}</p>
      <p class="muted">金额：{{ orderDetail.amount }}</p>
    </article>
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import { createOrder, getOrder } from "../api/order";
import { mockPayCallback } from "../api/pay";

const loadingCreate = ref(false);
const loadingPay = ref(false);
const loadingQuery = ref(false);
const errorText = ref("");
const successText = ref("");
const orderNo = ref("");
const orderDetail = ref(null);

const form = reactive({
  skuCode: "SKU-1001",
  quantity: 1,
  amount: 99.9
});

async function handleCreateOrder() {
  loadingCreate.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    const data = await createOrder({
      skuCode: form.skuCode,
      quantity: form.quantity,
      amount: form.amount
    });
    orderNo.value = data.orderNo;
    successText.value = `下单成功：${data.orderNo}，当前状态：${data.status}`;
    await handleQueryOrder();
  } catch (error) {
    errorText.value = error.message || "创建订单失败";
  } finally {
    loadingCreate.value = false;
  }
}

async function handleMockPay() {
  if (!orderNo.value) {
    errorText.value = "请先创建订单";
    return;
  }
  loadingPay.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    await mockPayCallback(orderNo.value);
    successText.value = "支付回调请求已发送";
    await handleQueryOrder();
  } catch (error) {
    errorText.value = error.message || "支付回调失败";
  } finally {
    loadingPay.value = false;
  }
}

async function handleQueryOrder() {
  if (!orderNo.value) {
    errorText.value = "请先创建订单";
    return;
  }
  loadingQuery.value = true;
  errorText.value = "";
  try {
    orderDetail.value = await getOrder(orderNo.value);
  } catch (error) {
    errorText.value = error.message || "查询订单失败";
  } finally {
    loadingQuery.value = false;
  }
}
</script>
