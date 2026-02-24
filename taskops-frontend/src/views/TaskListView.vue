<template>
  <section class="card">
    <h1>任务列表</h1>

    <form class="inline-form" @submit.prevent="loadTasks(1)">
      <input v-model.trim="query.keyword" placeholder="关键词（标题/描述/技术栈）" />
      <select v-model="query.status">
        <option value="">全部状态</option>
        <option value="TODO">TODO</option>
        <option value="DOING">DOING</option>
        <option value="DONE">DONE</option>
      </select>
      <button class="btn">搜索</button>
    </form>

    <p v-if="errorText" class="error">{{ errorText }}</p>

    <div class="task-grid" v-if="tasks.length">
      <article class="task-card" v-for="item in tasks" :key="item.id">
        <header>
          <h3>{{ item.title }}</h3>
          <span class="tag">{{ item.status }}</span>
        </header>
        <p>{{ item.description }}</p>
        <p class="muted">技术栈：{{ item.techStack || "-" }}</p>
        <p class="muted">优先级：{{ item.priority }}</p>
        <p class="muted">进度：{{ item.progress }}%</p>
      </article>
    </div>

    <p v-else class="muted">暂无任务数据</p>

    <div class="pager">
      <button class="btn secondary" :disabled="loading || page <= 1" @click="loadTasks(page - 1)">上一页</button>
      <span>第 {{ page }} 页 / 共 {{ totalPage }} 页</span>
      <button class="btn secondary" :disabled="loading || page >= totalPage" @click="loadTasks(page + 1)">下一页</button>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { searchTasks } from "../api/task";

const loading = ref(false);
const errorText = ref("");
const tasks = ref([]);
const page = ref(1);
const size = 10;
const total = ref(0);

const query = reactive({
  keyword: "",
  status: ""
});

const totalPage = computed(() => Math.max(1, Math.ceil(total.value / size)));

async function loadTasks(targetPage = 1) {
  loading.value = true;
  errorText.value = "";
  try {
    const data = await searchTasks({
      keyword: query.keyword || null,
      status: query.status || null,
      page: targetPage,
      size
    });
    tasks.value = data.records || [];
    total.value = data.total || 0;
    page.value = targetPage;
  } catch (error) {
    errorText.value = error.message || "加载任务失败";
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  loadTasks(1);
});
</script>
