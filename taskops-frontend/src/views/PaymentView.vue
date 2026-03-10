<template>
  <section class="page-grid">
    <section class="panel">
      <div class="panel-head">
        <p class="eyebrow">支付</p>
        <h1>支付中心</h1>
        <p class="muted">
          当前后端提供支付单创建、查询和关闭能力，前端暂不直接模拟第三方支付成功回调。
        </p>
      </div>

      <form class="stack" @submit.prevent="handleCreate">
        <label>
          订单号
          <input v-model.trim="orderNo" placeholder="ORD..." required />
        </label>
        <div class="button-row">
          <button class="btn" :disabled="loadingCreate">{{ loadingCreate ? "创建中..." : "创建支付单" }}</button>
          <button class="btn ghost" type="button" :disabled="loadingQuery || !orderNo" @click="handleQuery">
            {{ loadingQuery ? "查询中..." : "查询支付单" }}
          </button>
          <button class="btn ghost" type="button" :disabled="loadingClose || !orderNo" @click="handleClose">
            {{ loadingClose ? "关闭中..." : "关闭支付单" }}
          </button>
        </div>
      </form>

      <p v-if="errorText" class="error">{{ errorText }}</p>
      <p v-if="successText" class="success">{{ successText }}</p>

      <article v-if="paymentDetail" class="panel nested-panel">
        <header class="card-head">
          <h3>{{ paymentDetail.payNo }}</h3>
          <span class="tag">{{ paymentStatusLabel(paymentDetail.status) }}</span>
        </header>
        <div class="meta-stack">
          <div class="meta-row"><span>订单号</span><strong>{{ paymentDetail.orderNo }}</strong></div>
          <div class="meta-row"><span>支付渠道</span><strong>{{ paymentDetail.channel || "-" }}</strong></div>
          <div class="meta-row"><span>支付金额</span><strong>{{ formatAmount(paymentDetail.amount) }}</strong></div>
          <div class="meta-row"><span>标题</span><strong>{{ paymentDetail.subject || "-" }}</strong></div>
          <div class="meta-row"><span>第三方流水号</span><strong>{{ paymentDetail.thirdTradeNo || "-" }}</strong></div>
        </div>
      </article>
    </section>

    <section class="panel">
      <div class="panel-head">
        <p class="eyebrow">流程说明</p>
        <h2>当前支付流程</h2>
      </div>
      <ol class="helper-list">
        <li>先在订单页创建订单。</li>
        <li>使用返回的订单号创建支付单。</li>
        <li>在本页查询支付单状态。</li>
        <li>如果订单不再需要支付，可在本页关闭支付单。</li>
      </ol>
      <p class="muted">
        如果后续在 `pay-service` 重新补上通知接口，这里可以继续增加模拟支付成功按钮。
      </p>
    </section>
  </section>
</template>

<script setup>
import { ref, watch } from "vue";
import { useRoute } from "vue-router";
import { closePay, createPay, getPayDetail } from "../api/pay";

const route = useRoute();
const orderNo = ref(route.query.orderNo || "");
const loadingCreate = ref(false);
const loadingQuery = ref(false);
const loadingClose = ref(false);
const errorText = ref("");
const successText = ref("");
const paymentDetail = ref(null);

watch(
  () => route.query.orderNo,
  (value) => {
    orderNo.value = value || "";
  }
);

function formatAmount(amount) {
  return `¥${Number(amount || 0).toFixed(2)}`;
}

function paymentStatusLabel(status) {
  const map = {
    CREATED: "已创建",
    WAIT_BUYER_PAY: "待支付",
    CLOSED: "已关闭",
    SUCCESS: "已支付",
    TRADE_SUCCESS: "支付成功"
  };
  return map[status] || status;
}

async function handleCreate() {
  loadingCreate.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    paymentDetail.value = await createPay(orderNo.value);
    successText.value = `支付单已创建：${paymentDetail.value.payNo}`;
  } catch (error) {
    errorText.value = error.message || "创建支付单失败";
  } finally {
    loadingCreate.value = false;
  }
}

async function handleQuery() {
  loadingQuery.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    paymentDetail.value = await getPayDetail(orderNo.value);
    successText.value = `当前支付状态：${paymentStatusLabel(paymentDetail.value.status)}`;
  } catch (error) {
    errorText.value = error.message || "查询支付单失败";
  } finally {
    loadingQuery.value = false;
  }
}

async function handleClose() {
  loadingClose.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    await closePay(orderNo.value);
    successText.value = "支付单已关闭";
    paymentDetail.value = await getPayDetail(orderNo.value);
  } catch (error) {
    errorText.value = error.message || "关闭支付单失败";
  } finally {
    loadingClose.value = false;
  }
}
</script>