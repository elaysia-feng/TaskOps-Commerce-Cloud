const historyTypeInput = document.getElementById("historyType");
const reloadBtn = document.getElementById("reloadBtn");
const createBtn = document.getElementById("createBtn");
const sessionList = document.getElementById("sessionList");
const currentChatId = document.getElementById("currentChatId");
const statusText = document.getElementById("statusText");
const chatIdInput = document.getElementById("chatId");
const promptInput = document.getElementById("prompt");
const historyBtn = document.getElementById("historyBtn");
const sendBtn = document.getElementById("sendBtn");
const chatForm = document.getElementById("chatForm");
const messages = document.getElementById("messages");
const historyMessages = document.getElementById("historyMessages");

function setStatus(text) {
    statusText.textContent = text;
}

function syncChatId() {
    currentChatId.textContent = chatIdInput.value.trim() || "-";
}

function createMessage(role, content) {
    const article = document.createElement("article");
    article.className = `message ${role}`;
    const roleNode = document.createElement("div");
    roleNode.className = "message-role";
    roleNode.textContent = role;
    const bubble = document.createElement("div");
    bubble.className = "bubble";
    bubble.textContent = content;
    article.append(roleNode, bubble);
    messages.appendChild(article);
    messages.scrollTop = messages.scrollHeight;
}

function renderEmpty(container, text) {
    container.innerHTML = "";
    const empty = document.createElement("div");
    empty.className = "empty";
    empty.textContent = text;
    container.appendChild(empty);
}

function renderSessions(items) {
    sessionList.innerHTML = "";
    const selected = chatIdInput.value.trim();

    if (!items.length) {
        renderEmpty(sessionList, "当前没有会话。先点击“新会话”。");
        return;
    }

    items.forEach((item) => {
        const button = document.createElement("button");
        button.type = "button";
        button.className = "session-item";
        if (item === selected) {
            button.classList.add("active");
        }
        button.textContent = item;
        button.addEventListener("click", async () => {
            chatIdInput.value = item;
            syncChatId();
            renderSessions(items);
            setStatus("Session selected");
            await loadSessionHistory();
        });
        sessionList.appendChild(button);
    });
}

function renderHistory(items) {
    historyMessages.innerHTML = "";

    if (!items.length) {
        renderEmpty(historyMessages, "当前 ChatId 还没有历史消息。发送一条消息后再查看。")
        return;
    }

    items.forEach((item) => {
        const entry = document.createElement("article");
        entry.className = `history-entry ${item.role || "unknown"}`;
        const role = document.createElement("div");
        role.className = "message-role";
        role.textContent = item.role || "unknown";
        const content = document.createElement("div");
        content.className = "bubble";
        content.textContent = item.content || "";
        entry.append(role, content);
        historyMessages.appendChild(entry);
    });
}

async function loadSessions() {
    setStatus("Loading sessions");
    const type = historyTypeInput.value.trim() || "chat";
    const response = await fetch(`/api/ai-proxy/history/${encodeURIComponent(type)}`);
    const result = await response.json();
    renderSessions(result.data || []);
    setStatus("Sessions loaded");
}

async function createSession() {
    setStatus("Creating session");
    const type = historyTypeInput.value.trim() || "chat";
    const response = await fetch(`/api/ai-proxy/session?type=${encodeURIComponent(type)}`, { method: "POST" });
    const result = await response.json();
    const chatId = result.data?.chatId || "";
    chatIdInput.value = chatId;
    syncChatId();
    messages.innerHTML = "";
    createMessage("assistant", "代理服务已经创建新会话。现在发送消息，验证 Feign 调用是否成功。");
    renderHistory([]);
    await loadSessions();
    setStatus("Session created");
}

async function loadSessionHistory() {
    const chatId = chatIdInput.value.trim();
    if (!chatId) {
        renderHistory([]);
        setStatus("Missing ChatId");
        return;
    }

    setStatus("Loading history");
    const response = await fetch(`/api/ai-proxy/history/session/${encodeURIComponent(chatId)}`);
    const result = await response.json();
    renderHistory(result.data || []);
    setStatus("History loaded");
}

async function sendChat(prompt, chatId) {
    const response = await fetch("/api/ai-proxy/chat", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ prompt, chatId })
    });
    const result = await response.json();
    return result.data?.content || "";
}

chatForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    const prompt = promptInput.value.trim();
    const chatId = chatIdInput.value.trim();

    if (!chatId) {
        createMessage("assistant", "请先点击“新会话”，或选择左侧已有会话。")
        setStatus("Missing ChatId");
        return;
    }
    if (!prompt) {
        setStatus("Prompt required");
        promptInput.focus();
        return;
    }

    createMessage("user", prompt);
    promptInput.value = "";
    sendBtn.disabled = true;
    setStatus("Calling proxy");

    try {
        const content = await sendChat(prompt, chatId);
        createMessage("assistant", content || "代理服务返回了空内容");
        await loadSessions();
        await loadSessionHistory();
        setStatus("Proxy call completed");
    } catch (error) {
        createMessage("assistant", `调用失败：${error.message}`);
        setStatus("Proxy call failed");
    } finally {
        sendBtn.disabled = false;
    }
});

reloadBtn.addEventListener("click", loadSessions);
createBtn.addEventListener("click", createSession);
historyBtn.addEventListener("click", loadSessionHistory);
chatIdInput.addEventListener("input", syncChatId);
promptInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter" && !event.shiftKey) {
        event.preventDefault();
        chatForm.requestSubmit();
    }
});

syncChatId();
renderEmpty(historyMessages, "当前 ChatId 还没有历史消息。发送一条消息后再查看。");
loadSessions();