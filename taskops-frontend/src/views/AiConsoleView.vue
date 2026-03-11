<template>
  <section class="page-grid ai-page-grid">
    <section class="panel ai-hero-panel">
      <p class="eyebrow">AI 模块</p>
      <h1>通过 ai-proxy-service 流式转发 Spring AI</h1>
      <p class="muted">
        会话列表和历史消息仍然走普通接口，聊天消息改成通过 WebClient 流式转发。
      </p>
      <div class="button-row ai-hero-actions">
        <button class="btn" @click="handleCreateSession" :disabled="creatingSession">新建会话</button>
        <button class="btn ghost" @click="loadSessions" :disabled="loadingSessions">刷新会话</button>
      </div>
      <p v-if="pageError" class="error">{{ pageError }}</p>
    </section>

    <section class="panel nested-panel ai-status-panel">
      <p class="eyebrow">当前状态</p>
      <div class="stats-grid ai-mini-stats">
        <article class="list-card stat-card">
          <p class="stat-label">会话类型</p>
          <strong>{{ sessionType }}</strong>
          <span>当前查询和创建都使用这个类型</span>
        </article>
        <article class="list-card stat-card">
          <p class="stat-label">当前 ChatId</p>
          <strong>{{ activeChatId || '-' }}</strong>
          <span>流式发送和查看历史都会使用它</span>
        </article>
      </div>
    </section>

    <section class="panel ai-shell-panel">
      <div class="split-grid ai-split-grid">
        <section class="nested-panel ai-column">
          <div class="panel-head">
            <div>
              <p class="eyebrow">会话列表</p>
              <h2>历史会话</h2>
            </div>
            <label class="ai-inline-label">
              <span>Type</span>
              <input v-model="sessionType" @change="loadSessions" placeholder="chat" />
            </label>
          </div>
          <div class="stack ai-session-stack">
            <button
              v-for="item in sessions"
              :key="item"
              class="ai-session-item"
              :class="{ active: item === activeChatId }"
              @click="selectSession(item)"
            >
              {{ item }}
            </button>
            <p v-if="!sessions.length" class="muted">当前还没有会话，先点击“新建会话”。</p>
          </div>
        </section>

        <section class="nested-panel ai-column">
          <div class="panel-head">
            <div>
              <p class="eyebrow">流式聊天</p>
              <h2>发送消息</h2>
            </div>
            <button class="btn ghost" @click="loadCurrentHistory" :disabled="!activeChatId || loadingHistory">
              查看当前历史
            </button>
          </div>
          <div class="stack">
            <label>
              ChatId
              <input v-model="activeChatId" placeholder="先新建或选择一个会话" />
            </label>
            <label>
              Prompt
              <textarea v-model="prompt" rows="5" placeholder="例如：解释一下 OpenFeign 和 WebClient 在这个项目里的分工"></textarea>
            </label>
            <div class="button-row">
              <button class="btn" @click="handleSend" :disabled="sending">开始流式输出</button>
              <button class="btn ghost" @click="clearMessages">清空消息</button>
            </div>
          </div>
          <div class="stack ai-message-stack">
            <article v-for="(item, index) in messages" :key="`${item.role}-${index}`" class="list-card ai-message-card" :class="item.role">
              <div class="meta-row">
                <strong>{{ item.role === 'user' ? '我' : 'AI' }}</strong>
                <span>{{ item.role }}</span>
              </div>
              <p class="muted ai-prewrap">{{ item.content }}</p>
            </article>
            <p v-if="!messages.length" class="muted">这里会显示通过代理服务流式返回的对话内容。</p>
          </div>
        </section>
      </div>
    </section>

    <section class="panel ai-history-panel">
      <div class="panel-head">
        <div>
          <p class="eyebrow">历史消息</p>
          <h2>当前会话 Memory</h2>
        </div>
      </div>
      <div class="list-grid ai-history-grid">
        <article v-for="(item, index) in historyMessages" :key="`${item.role}-${index}`" class="list-card ai-history-card" :class="item.role">
          <div class="meta-row">
            <strong>{{ item.role }}</strong>
          </div>
          <p class="muted ai-prewrap">{{ item.content }}</p>
        </article>
        <p v-if="!historyMessages.length" class="muted">当前没有历史消息，发送一条消息后再查看。</p>
      </div>
    </section>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { createAiSession, getAiSessionHistory, getAiSessions } from "../api/ai";

const sessionType = ref("chat");
const sessions = ref([]);
const activeChatId = ref("");
const prompt = ref("");
const messages = ref([]);
const historyMessages = ref([]);
const loadingSessions = ref(false);
const loadingHistory = ref(false);
const creatingSession = ref(false);
const sending = ref(false);
const pageError = ref("");

onMounted(loadSessions);

async function loadSessions() {
  loadingSessions.value = true;
  pageError.value = "";
  try {
    sessions.value = await getAiSessions(sessionType.value || "chat");
  } catch (error) {
    pageError.value = error.message || "加载 AI 会话失败";
  } finally {
    loadingSessions.value = false;
  }
}

async function handleCreateSession() {
  creatingSession.value = true;
  pageError.value = "";
  try {
    const data = await createAiSession(sessionType.value || "chat");
    activeChatId.value = data.chatId || "";
    messages.value = [];
    historyMessages.value = [];
    await loadSessions();
  } catch (error) {
    pageError.value = error.message || "创建 AI 会话失败";
  } finally {
    creatingSession.value = false;
  }
}

async function loadCurrentHistory() {
  if (!activeChatId.value) {
    pageError.value = "请先选择或创建一个会话";
    return;
  }
  loadingHistory.value = true;
  pageError.value = "";
  try {
    historyMessages.value = await getAiSessionHistory(activeChatId.value);
  } catch (error) {
    pageError.value = error.message || "加载会话历史失败";
  } finally {
    loadingHistory.value = false;
  }
}

async function handleSend() {
  if (!activeChatId.value) {
    pageError.value = "请先新建会话或从左侧选择会话";
    return;
  }
  if (!prompt.value.trim()) {
    pageError.value = "请输入消息内容";
    return;
  }

  const currentPrompt = prompt.value.trim();
  const assistant = { role: "assistant", content: "" };
  messages.value.push({ role: "user", content: currentPrompt });
  messages.value.push(assistant);
  prompt.value = "";
  sending.value = true;
  pageError.value = "";

  try {
    const response = await fetch(
      `/ai-api/ai-proxy/stream?prompt=${encodeURIComponent(currentPrompt)}&chatId=${encodeURIComponent(activeChatId.value)}`,
      { method: "GET", headers: { Accept: "text/html;charset=utf-8" } }
    );

    if (!response.ok || !response.body) {
      throw new Error(`流式请求失败: ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");

    while (true) {
      const { done, value } = await reader.read();
      if (done) {
        break;
      }
      assistant.content += decoder.decode(value, { stream: true });
      messages.value = [...messages.value];
    }

    assistant.content += decoder.decode();
    messages.value = [...messages.value];
    await Promise.all([loadSessions(), loadCurrentHistory()]);
  } catch (error) {
    pageError.value = error.message || "AI 调用失败";
  } finally {
    sending.value = false;
  }
}

function clearMessages() {
  messages.value = [];
}

function selectSession(chatId) {
  activeChatId.value = chatId;
  loadCurrentHistory();
}
</script>