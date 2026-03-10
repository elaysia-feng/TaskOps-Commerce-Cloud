<template>
  <section class="panel page-narrow">
    <div class="panel-head">
      <p class="eyebrow">发布任务</p>
      <h1>创建悬赏任务</h1>
      <p class="muted">发布任务时直接填写赏金、地点、截止时间和交付要求。</p>
    </div>

    <form class="stack" @submit.prevent="handleSubmit">
      <label>
        任务标题
        <input v-model.trim="form.title" required />
      </label>
      <label>
        任务描述
        <textarea v-model.trim="form.description" rows="5" required />
      </label>
      <div class="split-grid">
        <label>
          任务分类
          <select v-model="form.category">
            <option value="ERRAND">跑腿</option>
            <option value="DESIGN">设计</option>
            <option value="TECH">技术</option>
            <option value="CONSULT">咨询</option>
            <option value="GENERAL">综合</option>
          </select>
        </label>
        <label>
          优先级
          <select v-model.number="form.priority">
            <option :value="1">1 - 最高</option>
            <option :value="2">2</option>
            <option :value="3">3</option>
            <option :value="4">4</option>
            <option :value="5">5 - 最低</option>
          </select>
        </label>
      </div>
      <div class="split-grid">
        <label>
          任务赏金
          <input v-model.number="form.rewardAmount" type="number" min="0.01" step="0.01" required />
        </label>
        <label>
          平台服务费
          <input v-model.number="form.serviceFee" type="number" min="0" step="0.01" />
        </label>
      </div>
      <div class="split-grid">
        <label>
          任务地点
          <input v-model.trim="form.location" placeholder="如：东区菜鸟驿站 / 线上交付" />
        </label>
        <label>
          联系方式
          <input v-model.trim="form.contactInfo" placeholder="微信 / QQ / 手机号" />
        </label>
      </div>
      <div class="split-grid">
        <label>
          截止时间
          <input v-model="form.deadline" type="datetime-local" />
        </label>
        <label>
          标签
          <input v-model.trim="form.techStack" placeholder="跑腿, 快递, 校园" />
        </label>
      </div>
      <label>
        凭证要求
        <select v-model="proofRequiredText">
          <option value="true">需要提交完成凭证</option>
          <option value="false">不需要提交凭证</option>
        </select>
      </label>
      <button class="btn" :disabled="loading">{{ loading ? "创建中..." : "发布任务" }}</button>
    </form>

    <div class="product-box mt12">
      <div class="meta-row"><span>预估到手金额</span><strong>{{ estimatedSettleAmount }}</strong></div>
      <div class="meta-row"><span>任务状态</span><strong>待接单</strong></div>
      <div class="meta-row"><span>凭证要求</span><strong>{{ proofRequiredText === "true" ? "需要" : "不需要" }}</strong></div>
    </div>

    <p v-if="errorText" class="error">{{ errorText }}</p>
    <p v-if="successText" class="success">{{ successText }}</p>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { createTask } from "../api/task";

const loading = ref(false);
const errorText = ref("");
const successText = ref("");
const proofRequiredText = ref("true");
const form = reactive({
  title: "",
  description: "",
  techStack: "",
  priority: 3,
  category: "ERRAND",
  rewardAmount: 12.5,
  serviceFee: 0.5,
  location: "",
  contactInfo: "",
  deadline: "",
  proofRequired: true
});

const estimatedSettleAmount = computed(() => {
  const reward = Number(form.rewardAmount || 0);
  const fee = Number(form.serviceFee || 0);
  return `¥${Math.max(0, reward - fee).toFixed(2)}`;
});

function normalizeDateTime(value) {
  return value ? `${value}:00` : null;
}

async function handleSubmit() {
  loading.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    const taskId = await createTask({
      ...form,
      proofRequired: proofRequiredText.value === "true",
      deadline: normalizeDateTime(form.deadline)
    });
    successText.value = `任务发布成功：${taskId}`;
    form.title = "";
    form.description = "";
    form.techStack = "";
    form.priority = 3;
    form.category = "ERRAND";
    form.rewardAmount = 12.5;
    form.serviceFee = 0.5;
    form.location = "";
    form.contactInfo = "";
    form.deadline = "";
    proofRequiredText.value = "true";
  } catch (error) {
    errorText.value = error.message || "发布任务失败";
  } finally {
    loading.value = false;
  }
}
</script>