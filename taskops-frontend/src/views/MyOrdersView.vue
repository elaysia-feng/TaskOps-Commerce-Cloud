<template>
  <section class="panel">
    <div class="panel-head">
      <p class="eyebrow">订单</p>
      <h1>我的订单</h1>
      <p class="muted">读取 `GET /api/orders/mine`，并允许取消待支付订单。</p>
    </div>

    <div class="button-row">
      <button class="btn" @click="loadOrders" :disabled="loading">{{ loading ? "刷新中..." : "刷新订单" }}</button>
      <RouterLink class="btn ghost" to="/orders/create">创建订单</RouterLink>
    </div>

    <p v-if="errorText" class="error">{{ errorText }}</p>
    <p v-if="successText" class="success">{{ successText }}</p>

    <div class="list-grid" v-if="orders.length">
      <article v-for="item in orders" :key="item.orderNo" class="list-card">
        <header>
          <h3>{{ item.orderNo }}</h3>
          <span class="tag">{{ statusLabel(item.status) }}</span>
        </header>
        <div class="meta-stack">
          <div class="meta-row"><span>SKU</span><strong>{{ item.skuCode }}</strong></div>
          <div class="meta-row"><span>数量</span><strong>{{ item.quantity }}</strong></div>
          <div class="meta-row"><span>金额</span><strong>{{ formatAmount(item.amount) }}</strong></div>
        </div>
        <div class="button-row">
          <RouterLink class="btn ghost" :to="`/payments?orderNo=${item.orderNo}`">去支付</RouterLink>
          <button
            class="btn ghost"
            :disabled="item.status !== 'PENDING_PAY' || cancelingOrderNo === item.orderNo"
            @click="handleCancel(item.orderNo)"
          >
            {{ cancelingOrderNo === item.orderNo ? "取消中..." : "取消订单" }}
          </button>
        </div>
      </article>
    </div>
    <p v-else class="muted">当前还没有订单。</p>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import { cancelOrder, listMyOrders } from "../api/order";

const orders = ref([]);
const loading = ref(false);
const errorText = ref("");
const successText = ref("");
const cancelingOrderNo = ref("");

onMounted(loadOrders);

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

async function loadOrders() {
  loading.value = true;
  errorText.value = "";
  try {
    orders.value = await listMyOrders();
  } catch (error) {
    errorText.value = error.message || "加载订单失败";
  } finally {
    loading.value = false;
  }
}

async function handleCancel(orderNo) {
  cancelingOrderNo.value = orderNo;
  errorText.value = "";
  successText.value = "";
  try {
    await cancelOrder(orderNo);
    successText.value = `订单已取消：${orderNo}`;
    await loadOrders();
  } catch (error) {
    errorText.value = error.message || "取消订单失败";
  } finally {
    cancelingOrderNo.value = "";
  }
}
</script>