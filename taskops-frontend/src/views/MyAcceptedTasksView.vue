<template>
  <section class="page-grid">
    <section class="panel">
      <div class="panel-head">
        <p class="eyebrow">我的接单</p>
        <h1>我接下的任务</h1>
        <p class="muted">在这里提交完成说明，等待发布方验收。</p>
      </div>

      <p v-if="errorText" class="error">{{ errorText }}</p>

      <div v-if="loading" class="muted">加载中...</div>
      <div v-else-if="tasks.length" class="list-grid">
        <article v-for="task in tasks" :key="task.id" class="list-card marketplace-card">
          <header>
            <div>
              <h3>{{ task.title }}</h3>
              <p class="muted">{{ statusLabel(task.status) }}</p>
            </div>
            <div class="price-box">
              <strong>{{ formatMoney(task.settleAmount || task.rewardAmount) }}</strong>
              <span>预计到手</span>
            </div>
          </header>

          <p class="muted clamp">{{ task.description }}</p>
          <div class="meta-line task-meta-wrap">
            <span>截止：{{ formatDateTime(task.deadline) }}</span>
            <span>地点：{{ task.location || '线上交付' }}</span>
          </div>

          <div v-if="task.status === 'TAKEN'" class="submit-box">
            <label>
              完成说明
              <textarea v-model.trim="forms[task.id].content" rows="4" placeholder="填写你完成任务的说明"></textarea>
            </label>
            <label>
              凭证链接
              <input v-model.trim="forms[task.id].proofUrls" placeholder="可选，后续接入 MinIO / OSS" />
            </label>
            <button class="btn" :disabled="submittingId === task.id" @click="handleSubmit(task)">
              {{ submittingId === task.id ? '提交中...' : '提交任务' }}
            </button>
          </div>

          <p v-else-if="task.status === 'SUBMITTED'" class="muted">已提交，等待发布方验收。</p>
          <p v-else-if="task.status === 'SETTLEMENT_PENDING'" class="muted">已通过验收，正在结算。</p>
          <p v-else-if="task.status === 'SETTLED'" class="muted">任务已结算完成。</p>
          <p v-else class="muted">当前状态不可提交。</p>
        </article>
      </div>
      <p v-else class="muted">当前没有接单记录。</p>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { getAcceptedTasks, submitTask } from '../api/task';

const loading = ref(false);
const errorText = ref('');
const tasks = ref([]);
const forms = ref({});
const submittingId = ref(null);

onMounted(() => {
  loadTasks();
});

function createForm(taskId) {
  if (!forms.value[taskId]) {
    forms.value[taskId] = { content: '', proofUrls: '' };
  }
}

function formatMoney(amount) {
  return `¥${Number(amount || 0).toFixed(2)}`;
}

function formatDateTime(value) {
  if (!value) {
    return '未设置';
  }
  return String(value).replace('T', ' ').slice(0, 16);
}

function statusLabel(status) {
  const map = {
    TAKEN: '进行中',
    SUBMITTED: '待验收',
    SETTLEMENT_PENDING: '结算中',
    SETTLED: '已结算',
    CANCELLED: '已取消'
  };
  return map[status] || status;
}

async function loadTasks() {
  loading.value = true;
  errorText.value = '';
  try {
    const pageData = await getAcceptedTasks({ page: 1, size: 20 });
    tasks.value = pageData.records || [];
    tasks.value.forEach((task) => createForm(task.id));
  } catch (error) {
    errorText.value = error.message || '加载我的接单任务失败';
  } finally {
    loading.value = false;
  }
}

async function handleSubmit(task) {
  createForm(task.id);
  const form = forms.value[task.id];
  if (!form.content) {
    errorText.value = '请先填写完成说明';
    return;
  }

  submittingId.value = task.id;
  errorText.value = '';
  try {
    await submitTask(task.id, {
      content: form.content,
      proofUrls: form.proofUrls || ''
    });
    form.content = '';
    form.proofUrls = '';
    await loadTasks();
  } catch (error) {
    errorText.value = error.message || '提交任务失败';
  } finally {
    submittingId.value = null;
  }
}
</script>