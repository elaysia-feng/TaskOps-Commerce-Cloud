<template>
  <section class="page-grid">
    <section class="panel">
      <div class="panel-head">
        <p class="eyebrow">待我验收</p>
        <h1>待验收任务</h1>
        <p class="muted">发布方在这里进行通过或驳回处理。</p>
      </div>

      <p v-if="errorText" class="error">{{ errorText }}</p>

      <div v-if="loading" class="muted">加载中...</div>
      <div v-else-if="tasks.length" class="list-grid">
        <article v-for="task in tasks" :key="task.id" class="list-card marketplace-card">
          <header>
            <div>
              <h3>{{ task.title }}</h3>
              <p class="muted">待验收</p>
            </div>
            <div class="price-box">
              <strong>{{ formatMoney(task.settleAmount || task.rewardAmount) }}</strong>
              <span>结算金额</span>
            </div>
          </header>

          <p class="muted clamp">{{ task.description }}</p>
          <div class="meta-line task-meta-wrap">
            <span>接单人：{{ task.acceptorId || '-' }}</span>
            <span>提交时间：{{ formatDateTime(task.submittedAt) }}</span>
          </div>

          <div class="review-box">
            <label>
              驳回原因
              <textarea v-model.trim="rejectReasons[task.id]" rows="3" placeholder="通过时可不填，驳回时必填"></textarea>
            </label>
            <div class="action-row">
              <button class="btn" :disabled="approvingId === task.id || rejectingId === task.id" @click="handleApprove(task.id)">
                {{ approvingId === task.id ? '处理中...' : '验收通过' }}
              </button>
              <button class="btn ghost" :disabled="approvingId === task.id || rejectingId === task.id" @click="handleReject(task.id)">
                {{ rejectingId === task.id ? '处理中...' : '驳回重做' }}
              </button>
            </div>
          </div>
        </article>
      </div>
      <p v-else class="muted">当前没有待验收任务。</p>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { approveTask, getReviewTasks, rejectTask } from '../api/task';

const loading = ref(false);
const errorText = ref('');
const tasks = ref([]);
const rejectReasons = ref({});
const approvingId = ref(null);
const rejectingId = ref(null);

onMounted(() => {
  loadTasks();
});

function formatMoney(amount) {
  return `¥${Number(amount || 0).toFixed(2)}`;
}

function formatDateTime(value) {
  if (!value) {
    return '未设置';
  }
  return String(value).replace('T', ' ').slice(0, 16);
}

async function loadTasks() {
  loading.value = true;
  errorText.value = '';
  try {
    const pageData = await getReviewTasks({ page: 1, size: 20 });
    tasks.value = pageData.records || [];
  } catch (error) {
    errorText.value = error.message || '加载待验收任务失败';
  } finally {
    loading.value = false;
  }
}

async function handleApprove(taskId) {
  approvingId.value = taskId;
  errorText.value = '';
  try {
    await approveTask(taskId);
    await loadTasks();
  } catch (error) {
    errorText.value = error.message || '验收通过失败';
  } finally {
    approvingId.value = null;
  }
}

async function handleReject(taskId) {
  const reason = rejectReasons.value[taskId];
  if (!reason) {
    errorText.value = '请先填写驳回原因';
    return;
  }

  rejectingId.value = taskId;
  errorText.value = '';
  try {
    await rejectTask(taskId, { reason });
    rejectReasons.value[taskId] = '';
    await loadTasks();
  } catch (error) {
    errorText.value = error.message || '驳回任务失败';
  } finally {
    rejectingId.value = null;
  }
}
</script>