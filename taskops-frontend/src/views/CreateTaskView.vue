<template>
  <section class="card">
    <h1>创建任务</h1>
    <form class="form-grid" @submit.prevent="handleCreate">
      <label>
        标题
        <input v-model.trim="form.title" required />
      </label>

      <label>
        描述
        <textarea v-model.trim="form.description" required rows="4"></textarea>
      </label>

      <label>
        技术栈
        <input v-model.trim="form.techStack" placeholder="Spring Boot, Redis" />
      </label>

      <label>
        优先级（1-5，数字越小优先级越高）
        <input v-model.number="form.priority" type="number" min="1" max="5" required />
      </label>

      <button class="btn" :disabled="loading">{{ loading ? "提交中..." : "创建" }}</button>
      <p class="error" v-if="errorText">{{ errorText }}</p>
      <p class="success" v-if="successText">{{ successText }}</p>
    </form>
  </section>
</template>

<script setup>
import { reactive, ref } from "vue";
import { createTask } from "../api/task";

const loading = ref(false);
const errorText = ref("");
const successText = ref("");

const form = reactive({
  title: "",
  description: "",
  techStack: "",
  priority: 3
});

async function handleCreate() {
  loading.value = true;
  errorText.value = "";
  successText.value = "";
  try {
    const id = await createTask(form);
    successText.value = `任务创建成功，ID: ${id}`;
    form.title = "";
    form.description = "";
    form.techStack = "";
    form.priority = 3;
  } catch (error) {
    errorText.value = error.message || "创建任务失败";
  } finally {
    loading.value = false;
  }
}
</script>
