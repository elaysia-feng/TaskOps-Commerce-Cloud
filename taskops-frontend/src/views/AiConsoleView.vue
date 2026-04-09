<template>
  <div class="ai-console">
    <!-- Sessions Sidebar -->
    <aside class="sessions-panel glass-card">
      <div class="panel-header">
        <h3>会话列表</h3>
        <el-button type="primary" size="small" circle @click="handleCreateSession" :loading="creatingSession">
          <el-icon><Plus /></el-icon>
        </el-button>
      </div>

      <div class="session-type-select">
        <el-select v-model="sessionType" size="small" @change="loadSessions">
          <el-option label="Chat" value="chat" />
          <el-option label="Task" value="task" />
          <el-option label="Analysis" value="analysis" />
        </el-select>
      </div>

      <div class="sessions-list" v-loading="loadingSessions">
        <div
          v-for="chatId in sessions"
          :key="chatId"
          class="session-item"
          :class="{ active: chatId === activeChatId }"
          @click="selectSession(chatId)"
        >
          <div class="session-avatar">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <div class="session-content">
            <strong>{{ chatId.slice(0, 12) }}...</strong>
            <span>{{ chatId === activeChatId ? "当前会话" : "点击选择" }}</span>
          </div>
        </div>

        <div v-if="!loadingSessions && sessions.length === 0" class="empty-sessions">
          <p>暂无会话</p>
          <el-button size="small" @click="handleCreateSession">创建新会话</el-button>
        </div>
      </div>
    </aside>

    <!-- Chat Area -->
    <main class="chat-area">
      <!-- Chat Header -->
      <div class="chat-header">
        <div class="chat-header-info">
          <h3>AI 助手</h3>
          <span class="chat-status">
            <span class="status-dot"></span>
            {{ sending ? "AI 正在输入..." : "在线" }}
          </span>
        </div>
        <div class="chat-header-actions">
          <el-button size="small" @click="clearMessages" :disabled="streamMessages.length === 0">
            清空对话
          </el-button>
        </div>
      </div>

      <!-- Messages -->
      <div class="messages-container" ref="messagesContainer">
        <div v-if="streamMessages.length === 0 && !sending" class="welcome-message">
          <div class="welcome-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
          </div>
          <h4>开始与 AI 对话</h4>
          <p>输入消息开始对话，支持流式输出和长期记忆</p>
        </div>

        <div
          v-for="(msg, index) in streamMessages"
          :key="index"
          class="message-wrapper"
          :class="msg.role"
        >
          <div class="message-avatar" v-if="msg.role === 'assistant'">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 8-8 8 8 0 0 1-8 8zm-1-13h2v6h-2zm0 8h2v2h-2"/>
            </svg>
          </div>
          <div class="message-bubble">
            <pre class="message-text">{{ msg.content || (msg.role === 'assistant' && sending && index === streamMessages.length - 1 ? '思考中...' : '') }}</pre>
          </div>
          <div class="message-avatar user" v-if="msg.role === 'user'">
            {{ usernameInitials }}
          </div>
        </div>

        <div v-if="sending && streamMessages[streamMessages.length - 1]?.role === 'user'" class="message-wrapper assistant">
          <div class="message-avatar">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="currentColor">
              <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2zm0 18a8 8 0 1 1 8-8 8 8 0 0 1-8 8zm-1-13h2v6h-2zm0 8h2v2h-2"/>
            </svg>
          </div>
          <div class="message-bubble typing">
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
            <span class="typing-dot"></span>
          </div>
        </div>
      </div>

      <!-- Input Area -->
      <div class="input-area">
        <div class="input-wrapper">
          <el-input
            v-model="prompt"
            type="textarea"
            :rows="2"
            placeholder="输入消息，AI 将实时回复... (Shift+Enter 换行，Enter 发送)"
            :disabled="sending"
            @keydown.enter.exact.prevent="handleSend"
            resize="none"
          />
          <el-button
            type="primary"
            size="large"
            :loading="sending"
            @click="handleSend"
            class="send-btn"
          >
            <el-icon><Promotion /></el-icon>
          </el-button>
        </div>
        <p class="input-hint">当前 ChatId: {{ activeChatId || "未选择（发送时自动创建）" }}</p>
      </div>
    </main>

    <!-- Memory Panel -->
    <aside class="memory-panel glass-card">
      <div class="panel-header">
        <h3>长期记忆</h3>
        <el-badge :value="memories.length" :max="99" />
      </div>

      <el-tabs v-model="memoryTab" class="memory-tabs">
        <el-tab-pane label="记忆列表" name="list">
          <div class="memory-list" v-loading="loadingMemories">
            <div
              v-for="memory in mergedMemories"
              :key="memory.id"
              class="memory-item"
              :class="{ active: Number(memory.selected) === 1 }"
            >
              <div class="memory-header">
                <strong>{{ memory.memoryKey || `MEM-${memory.id}` }}</strong>
                <span class="memory-type">{{ memory.memoryType || "profile" }}</span>
              </div>
              <p class="memory-content">{{ memory.memorySummary || memory.memoryContent }}</p>
              <div class="memory-meta">
                <span>重要性: {{ memory.importanceScore ?? 50 }}</span>
                <span v-if="Number(memory.selected) === 1" class="memory-active">已启用</span>
              </div>
              <div class="memory-actions">
                <el-button size="small" @click="editMemory(memory)">编辑</el-button>
                <el-button
                  size="small"
                  :type="Number(memory.selected) === 1 ? 'warning' : 'primary'"
                  :disabled="!activeChatId"
                  @click="toggleMemory(memory)"
                >
                  {{ Number(memory.selected) === 1 ? "取消" : "启用" }}
                </el-button>
              </div>
            </div>

            <div v-if="!loadingMemories && memories.length === 0" class="empty-memory">
              <p>暂无记忆</p>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="创建记忆" name="create">
          <el-form :model="memoryForm" class="memory-form" size="small">
            <el-form-item>
              <el-input v-model="memoryForm.memoryKey" placeholder="记忆键 (如: user-preference)" />
            </el-form-item>
            <el-form-item>
              <el-select v-model="memoryForm.memoryType" placeholder="记忆类型">
                <el-option label="profile" value="profile" />
                <el-option label="project" value="project" />
                <el-option label="habit" value="habit" />
                <el-option label="preference" value="preference" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-input-number v-model="memoryForm.importanceScore" :min="1" :max="100" />
            </el-form-item>
            <el-form-item>
              <el-input v-model="memoryForm.memorySummary" placeholder="记忆摘要" />
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="memoryForm.memoryContent"
                type="textarea"
                :rows="4"
                placeholder="记忆内容..."
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                :loading="savingMemory"
                @click="handleSaveMemory"
                class="save-memory-btn"
              >
                {{ memoryForm.id ? "更新记忆" : "保存记忆" }}
              </el-button>
              <el-button @click="resetMemoryForm">清空</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </aside>

    <!-- Error Alert -->
    <el-alert v-if="pageError" :title="pageError" type="error" show-icon :closable="true" @close="pageError = ''" class="error-toast" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch, nextTick } from "vue";
import { ElMessage } from "element-plus";
import { Plus, Promotion } from "@element-plus/icons-vue";
import {
  createAiSession,
  deleteAiMemory,
  getAiSessionHistory,
  getAiSessions,
  listAiMemories,
  listSessionMemories,
  saveAiMemory,
  updateSessionMemorySelection
} from "../api/ai";
import { getToken, getUser } from "../utils/auth";
import { formatDateTime } from "../utils/format";

const user = getUser() || {};
const usernameInitials = (user.username || "U").slice(0, 1).toUpperCase();

const sessionType = ref("chat");
const sessions = ref([]);
const activeChatId = ref("");
const prompt = ref("");
const streamMessages = ref([]);
const historyMessages = ref([]);
const memories = ref([]);
const sessionMemories = ref([]);
const loadingSessions = ref(false);
const loadingHistory = ref(false);
const loadingMemories = ref(false);
const creatingSession = ref(false);
const sending = ref(false);
const savingMemory = ref(false);
const pageError = ref("");
const memoryTab = ref("list");
const messagesContainer = ref(null);

const memoryForm = reactive({
  id: null,
  memoryKey: "",
  memoryContent: "",
  memorySummary: "",
  memoryType: "profile",
  importanceScore: 50,
  selectedByDefault: 0
});

const sessionSelectionMap = computed(() =>
  new Map(sessionMemories.value.map((item) => [item.id, Number(item.selected ?? item.selectedByDefault ?? 0)]))
);

const mergedMemories = computed(() =>
  memories.value.map((memory) => ({
    ...memory,
    selected: sessionSelectionMap.value.has(memory.id)
      ? sessionSelectionMap.value.get(memory.id)
      : Number(memory.selected ?? memory.selectedByDefault ?? 0)
  }))
);

watch(streamMessages, () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight;
    }
  });
}, { deep: true });

onMounted(() => {
  reloadAll();
});

async function reloadAll() {
  await Promise.allSettled([loadSessions(), loadMemories()]);
  if (activeChatId.value) {
    await Promise.allSettled([loadCurrentHistory(), loadSessionMemories()]);
  }
}

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

async function loadMemories() {
  loadingMemories.value = true;
  try {
    memories.value = await listAiMemories();
  } catch (error) {
    pageError.value = error.message || "加载长期记忆失败";
  } finally {
    loadingMemories.value = false;
  }
}

async function loadCurrentHistory() {
  if (!activeChatId.value) {
    historyMessages.value = [];
    return;
  }
  loadingHistory.value = true;
  try {
    historyMessages.value = await getAiSessionHistory(activeChatId.value);
  } catch (error) {
    pageError.value = error.message || "加载会话历史失败";
  } finally {
    loadingHistory.value = false;
  }
}

async function loadSessionMemories() {
  if (!activeChatId.value) {
    sessionMemories.value = [];
    return;
  }
  try {
    sessionMemories.value = await listSessionMemories(activeChatId.value);
  } catch (error) {
    pageError.value = error.message || "加载会话记忆失败";
  }
}

async function handleCreateSession() {
  creatingSession.value = true;
  pageError.value = "";
  try {
    const data = await createAiSession(sessionType.value || "chat");
    activeChatId.value = data.chatId || "";
    streamMessages.value = [];
    await Promise.allSettled([loadSessions(), loadCurrentHistory(), loadSessionMemories()]);
    ElMessage.success("会话创建成功！");
  } catch (error) {
    pageError.value = error.message || "创建 AI 会话失败";
  } finally {
    creatingSession.value = false;
  }
}

async function selectSession(chatId) {
  activeChatId.value = chatId;
  await Promise.allSettled([loadCurrentHistory(), loadSessionMemories()]);
}

async function handleSend() {
  if (!prompt.value.trim()) {
    pageError.value = "请输入消息内容";
    return;
  }

  sending.value = true;
  pageError.value = "";
  const currentPrompt = prompt.value.trim();
  const assistant = { role: "assistant", content: "" };
  streamMessages.value.push({ role: "user", content: currentPrompt });
  streamMessages.value.push(assistant);
  prompt.value = "";

  try {
    const prepared = await prepareAiChat({
      prompt: currentPrompt,
      chatId: activeChatId.value || undefined
    });
    activeChatId.value = prepared.chatId || activeChatId.value;

    const token = getToken();
    const response = await fetch(
      `/ai-api/ai-proxy/stream?prompt=${encodeURIComponent(currentPrompt)}&chatId=${encodeURIComponent(activeChatId.value)}`,
      {
        method: "GET",
        headers: {
          Accept: "text/event-stream",
          ...(token ? { Authorization: `Bearer ${token}` } : {})
        }
      }
    );

    if (!response.ok || !response.body) {
      throw new Error(`流式请求失败: ${response.status}`);
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");

    while (true) {
      const { done, value } = await reader.read();
      if (done) break;
      assistant.content += decoder.decode(value, { stream: true });
      streamMessages.value = [...streamMessages.value];
    }

    assistant.content += decoder.decode();
    streamMessages.value = [...streamMessages.value];
    await Promise.allSettled([loadSessions(), loadCurrentHistory(), loadSessionMemories()]);
  } catch (error) {
    pageError.value = error.message || "AI 调用失败";
    streamMessages.value = streamMessages.value.filter((m) => m !== assistant);
  } finally {
    sending.value = false;
  }
}

async function prepareAiChat(data) {
  const token = getToken();
  const response = await fetch("/ai-api/ai-proxy/chat/prepare", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: JSON.stringify(data)
  });
  return response.json();
}

function clearMessages() {
  streamMessages.value = [];
}

function resetMemoryForm() {
  memoryForm.id = null;
  memoryForm.memoryKey = "";
  memoryForm.memoryContent = "";
  memoryForm.memorySummary = "";
  memoryForm.memoryType = "profile";
  memoryForm.importanceScore = 50;
  memoryForm.selectedByDefault = 0;
  memoryTab.value = "list";
}

function editMemory(memory) {
  memoryForm.id = memory.id;
  memoryForm.memoryKey = memory.memoryKey || "";
  memoryForm.memoryContent = memory.memoryContent || "";
  memoryForm.memorySummary = memory.memorySummary || "";
  memoryForm.memoryType = memory.memoryType || "profile";
  memoryForm.importanceScore = memory.importanceScore ?? 50;
  memoryForm.selectedByDefault = Number(memory.selectedByDefault ?? 0);
  memoryTab.value = "create";
}

async function handleSaveMemory() {
  savingMemory.value = true;
  pageError.value = "";
  try {
    await saveAiMemory({
      id: memoryForm.id,
      memoryKey: memoryForm.memoryKey,
      memoryContent: memoryForm.memoryContent,
      memorySummary: memoryForm.memorySummary,
      memoryType: memoryForm.memoryType,
      importanceScore: Number(memoryForm.importanceScore || 50),
      selectedByDefault: Number(memoryForm.selectedByDefault || 0)
    });
    ElMessage.success("记忆保存成功！");
    resetMemoryForm();
    await Promise.allSettled([loadMemories(), loadSessionMemories()]);
  } catch (error) {
    pageError.value = error.message || "保存记忆失败";
  } finally {
    savingMemory.value = false;
  }
}

async function handleDeleteMemory(memoryId) {
  try {
    await deleteAiMemory(memoryId);
    if (memoryForm.id === memoryId) {
      resetMemoryForm();
    }
    await Promise.allSettled([loadMemories(), loadSessionMemories()]);
    ElMessage.success("记忆已删除");
  } catch (error) {
    pageError.value = error.message || "删除记忆失败";
  }
}

async function toggleMemory(memory) {
  if (!activeChatId.value) {
    pageError.value = "请先选择一个会话";
    return;
  }

  try {
    await updateSessionMemorySelection({
      chatId: activeChatId.value,
      memoryId: memory.id,
      selected: Number(memory.selected) === 1 ? 0 : 1
    });
    await loadSessionMemories();
  } catch (error) {
    pageError.value = error.message || "切换会话记忆失败";
  }
}
</script>

<style scoped>
.ai-console {
  display: grid;
  grid-template-columns: 260px 1fr 300px;
  gap: 20px;
  height: calc(100vh - 160px);
  min-height: 600px;
}

/* ========================
   Panels
   ======================== */
.sessions-panel,
.memory-panel {
  padding: 20px;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.panel-header h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0;
  color: var(--text);
}

/* Sessions */
.session-type-select {
  margin-bottom: 8px;
}

.sessions-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.2s;
  background: rgba(255, 255, 255, 0.5);
}

.session-item:hover {
  background: rgba(255, 255, 255, 0.8);
}

.session-item.active {
  background: rgba(30, 107, 102, 0.1);
  border: 1px solid rgba(30, 107, 102, 0.2);
}

.session-avatar {
  width: 36px;
  height: 36px;
  min-width: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 10px;
}

.session-content {
  overflow: hidden;
}

.session-content strong {
  display: block;
  font-size: 13px;
  color: var(--text);
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-content span {
  font-size: 11px;
  color: var(--muted);
}

.empty-sessions {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  text-align: center;
  color: var(--muted);
}

.empty-sessions p {
  margin: 0;
  font-size: 13px;
}

/* ========================
   Chat Area
   ======================== */
.chat-area {
  display: flex;
  flex-direction: column;
  background: var(--surface);
  border-radius: 24px;
  border: 1px solid var(--line);
  box-shadow: var(--shadow);
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.5);
}

.chat-header-info h3 {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--text);
}

.chat-status {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--muted);
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

/* Messages */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.welcome-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: var(--muted);
}

.welcome-icon {
  width: 80px;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1), rgba(118, 75, 162, 0.1));
  border-radius: 24px;
  margin-bottom: 16px;
  color: var(--accent);
}

.welcome-message h4 {
  font-size: 18px;
  margin: 0 0 8px;
  color: var(--text);
}

.welcome-message p {
  font-size: 13px;
  margin: 0;
}

.message-wrapper {
  display: flex;
  gap: 12px;
  max-width: 85%;
}

.message-wrapper.user {
  flex-direction: row-reverse;
  margin-left: auto;
}

.message-avatar {
  width: 36px;
  height: 36px;
  min-width: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  border-radius: 12px;
}

.message-avatar.user {
  background: linear-gradient(135deg, var(--brand), var(--brand-deep));
}

.message-bubble {
  padding: 14px 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid var(--line);
  max-width: 100%;
}

.message-wrapper.user .message-bubble {
  background: linear-gradient(135deg, var(--brand), var(--brand-deep));
  color: #fff;
  border: none;
}

.message-text {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}

.typing {
  display: flex;
  gap: 4px;
  padding: 16px 20px;
}

.typing-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
  animation: typing-bounce 1.4s ease-in-out infinite;
}

.typing-dot:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing-bounce {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-6px); }
}

/* Input Area */
.input-area {
  padding: 16px 20px;
  border-top: 1px solid var(--line);
  background: rgba(255, 255, 255, 0.5);
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-wrapper :deep(.el-textarea__inner) {
  border-radius: 16px;
  padding: 12px 16px;
  resize: none;
}

.send-btn {
  width: 48px;
  height: 48px;
  border-radius: 14px !important;
  flex-shrink: 0;
}

.input-hint {
  font-size: 11px;
  color: var(--muted);
  margin: 8px 0 0;
}

/* ========================
   Memory Panel
   ======================== */
.memory-tabs {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.memory-tabs :deep(.el-tabs__content) {
  flex: 1;
  overflow-y: auto;
}

.memory-tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
}

.memory-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.memory-item {
  padding: 14px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 16px;
  border: 1px solid var(--line);
  transition: all 0.2s;
}

.memory-item:hover {
  background: rgba(255, 255, 255, 0.9);
}

.memory-item.active {
  border-color: var(--accent);
  background: rgba(30, 107, 102, 0.05);
}

.memory-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.memory-header strong {
  font-size: 13px;
  color: var(--text);
}

.memory-type {
  padding: 2px 8px;
  font-size: 10px;
  background: rgba(30, 107, 102, 0.1);
  color: var(--accent);
  border-radius: 999px;
}

.memory-content {
  font-size: 12px;
  color: var(--muted);
  margin: 0 0 8px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.memory-meta {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--muted);
  margin-bottom: 8px;
}

.memory-active {
  color: var(--success);
  font-weight: 500;
}

.memory-actions {
  display: flex;
  gap: 8px;
}

.empty-memory {
  text-align: center;
  padding: 20px;
  color: var(--muted);
}

.empty-memory p {
  margin: 0;
  font-size: 13px;
}

.memory-form :deep(.el-form-item) {
  margin-bottom: 12px;
}

.save-memory-btn {
  width: 100%;
}

/* Error Toast */
.error-toast {
  position: fixed;
  bottom: 24px;
  right: 24px;
  max-width: 400px;
  z-index: 1000;
}

/* Responsive */
@media (max-width: 1200px) {
  .ai-console {
    grid-template-columns: 1fr;
    height: auto;
  }

  .sessions-panel,
  .memory-panel {
    max-height: 300px;
  }

  .chat-area {
    min-height: 500px;
  }
}
</style>
