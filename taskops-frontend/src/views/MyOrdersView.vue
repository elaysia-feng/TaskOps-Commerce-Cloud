<template>
  <section class="card">
    <div class="page-head">
      <div>
        <p class="section-kicker">账户</p>
        <h1>我的订单</h1>
        <p class="muted">查看个人订单历史、当前状态，并可取消待支付订单。</p>
      </div>
      <div class="action-row action-row-start">
        <RouterLink class="btn secondary" to="/orders/create">创建订单</RouterLink>
        <RouterLink class="btn secondary" to="/payments">订单支付</RouterLink>
      </div>
    </div>

    <p v-if="errorText" class="error mt12">{{ errorText }}</p>
    <p v-if="successText" class="success mt12">{{ successText }}</p>

    <div class="action-row mt12 action-row-start">
      <button class="btn secondary" :disabled="loading" @click="loadOrders">
        {{ loading ? "刷新中..." : "刷新订单" }}
      </button>
    </div>

    <div v-if="orders.length" class="task-grid mt12">
      <article v-for="item in orders" :key="item.orderNo" class="task-card">
        <header>
          <h3>{{ item.orderNo }}</h3>
          <span class="tag">{{ statusLabel(item.status) }}</span>
        </header>
        <p class="muted">SKU：{{ item.skuCode }}</p>
        <p class="muted">数量：{{ item.quantity }}</p>
        <p class="muted">金额：{{ formatAmount(item.amount) }}</p>
        <p class="muted">创建时间：{{ item.createdAt || "-" }}</p>
        <div class="action-row mt12 action-row-start">
          <RouterLink v-if="item.status === 'PENDING_PAY'" class="btn secondary" :to="`/payments?orderNo=${item.orderNo}`">
            去支付
          </RouterLink>
          <span v-else class="muted">当前状态不可支付</span>
          <button
            v-if="item.status === 'PENDING_PAY'"
            class="btn secondary"
            :disabled="cancelingOrderNo === item.orderNo"
            @click="handleCancel(item.orderNo)"
          >
            {{ cancelingOrderNo === item.orderNo ? "取消中..." : "取消订单" }}
          </button>
        </div>
      </article>
    </div>

    <div v-else class="empty-state mt12">
      <h3>暂时还没有订单</h3>
      <p class="muted">先去创建订单页面生成第一笔订单。</p>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { RouterLink } from "vue-router";
import { cancelOrder, listMyOrders } from "../api/order";

const loading = ref(false);
const cancelingOrderNo = ref("");
const errorText = ref("");
const successText = ref("");
const orders = ref([]);

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

onMounted(loadOrders);
</script>