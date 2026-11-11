<template>
  <div class="ai-assistant-root">
    <button v-show="!visible" class="ai-fab" type="button" @click="visible = true">
      <span class="fab-glow"></span>
      <el-icon :size="23"><MagicStick /></el-icon>
      <span class="fab-label">AI 旅行管家</span>
    </button>

    <el-drawer
      v-model="visible"
      :size="isMobile ? '100%' : '430px'"
      :with-header="false"
      class="ai-drawer"
      append-to-body
    >
      <div class="assistant-panel">
        <header class="assistant-header">
          <div class="header-main">
            <div class="avatar">
              <el-icon><MagicStick /></el-icon>
            </div>
            <div>
              <h3>AI 旅行管家</h3>
              <p>
                <span class="status-dot" :class="{ ok: health?.configured }"></span>
                {{ health?.configured ? `${health.model} · 平台数据增强` : 'AI 未配置 · 本地兜底' }}
              </p>
            </div>
          </div>
          <button class="close-btn" type="button" @click="visible = false">
            <el-icon><Close /></el-icon>
          </button>
        </header>

        <div ref="scrollRef" class="message-list">
          <div v-if="messages.length === 0" class="welcome">
            <div class="welcome-icon"><el-icon><Compass /></el-icon></div>
            <h4>想去哪儿？我来帮你查</h4>
            <p>我会结合平台在售线路、酒店和景点回答，推荐都可以直接点开预订。</p>
            <div class="quick-list">
              <button v-for="prompt in quickPrompts" :key="prompt" type="button" @click="sendPrompt(prompt)">
                {{ prompt }}
              </button>
            </div>
          </div>

          <div
            v-for="(message, index) in messages"
            :key="index"
            class="message"
            :class="message.role"
          >
            <div v-if="message.role === 'assistant'" class="assistant-avatar">
              <el-icon><MagicStick /></el-icon>
            </div>
            <div class="bubble-wrap">
              <div class="bubble">
                <div class="reply-text">{{ message.content }}</div>
                <div v-if="message.references?.length" class="reference-list">
                  <button
                    v-for="ref in message.references"
                    :key="ref.type + '-' + ref.id"
                    type="button"
                    class="reference-chip"
                    @click="goReference(ref)"
                  >
                    <span class="ref-name">{{ ref.name }}</span>
                    <span v-if="ref.price" class="ref-price">¥{{ ref.price }}</span>
                  </button>
                </div>
                <div v-if="message.meta?.fallback" class="fallback-badge">
                  本地兜底 · 未调用大模型
                </div>
              </div>
              <div class="message-time">{{ message.time }}</div>
            </div>
          </div>

          <div v-if="sending" class="message assistant">
            <div class="assistant-avatar"><el-icon><MagicStick /></el-icon></div>
            <div class="bubble-wrap">
              <div class="bubble typing">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="messages.length > 0" class="inline-prompts">
          <button v-for="prompt in followUpPrompts" :key="prompt" type="button" @click="sendPrompt(prompt)">
            {{ prompt }}
          </button>
        </div>

        <footer class="assistant-input">
          <el-input
            v-model="input"
            type="textarea"
            :rows="2"
            resize="none"
            maxlength="1000"
            show-word-limit
            placeholder="问线路、酒店、行程安排..."
            @keydown.enter.exact.prevent="send"
          />
          <div class="input-actions">
            <span class="input-hint">Enter 发送 · Shift+Enter 换行</span>
            <el-button type="primary" :loading="sending" :disabled="!input.trim()" @click="send">
              发送
              <el-icon class="send-icon"><Promotion /></el-icon>
            </el-button>
          </div>
        </footer>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { chatWithAi, getAiHealth } from '@/api/ai'

const router = useRouter()
const STORAGE_KEY = 'shiguang-ai-assistant-messages'
const MAX_HISTORY = 20

const visible = ref(false)
const sending = ref(false)
const input = ref('')
const messages = ref([])
const health = ref(null)
const scrollRef = ref(null)
const isMobile = ref(false)
const sessionId = ref(`web-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`)

const quickPrompts = [
  '桂林三天怎么玩？',
  '推荐 200 元以内的酒店',
  '学生党周末去哪儿',
  '帮我规划大理摄影行程'
]

const followUpPrompts = ['换成 4 天行程', '预算再低一点', '推荐附近景点']

function nowText() {
  return new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

function restoreMessages() {
  try {
    const cached = sessionStorage.getItem(STORAGE_KEY)
    if (cached) {
      const parsed = JSON.parse(cached)
      if (Array.isArray(parsed)) {
        messages.value = parsed.slice(-MAX_HISTORY)
      }
    }
  } catch {
    sessionStorage.removeItem(STORAGE_KEY)
  }
}

function persistMessages() {
  try {
    sessionStorage.setItem(STORAGE_KEY, JSON.stringify(messages.value.slice(-MAX_HISTORY)))
  } catch {
    // 会话存储不可用时不影响聊天
  }
}

function buildHistory() {
  return messages.value
    .filter(message => message.role === 'user' || message.role === 'assistant')
    .slice(-6)
    .map(message => ({ role: message.role, content: message.content }))
}

async function scrollToBottom() {
  await nextTick()
  if (scrollRef.value) {
    scrollRef.value.scrollTop = scrollRef.value.scrollHeight
  }
}

async function send() {
  const text = input.value.trim()
  if (!text || sending.value) {
    return
  }
  const history = buildHistory()
  messages.value.push({ role: 'user', content: text, time: nowText() })
  input.value = ''
  sending.value = true
  persistMessages()
  await scrollToBottom()

  try {
    const data = await chatWithAi({
      message: text,
      history,
      sessionId: sessionId.value
    })
    messages.value.push({
      role: 'assistant',
      content: data.reply,
      references: data.references || [],
      meta: data.meta || {},
      time: nowText()
    })
  } catch (error) {
    ElMessage.error(error?.message || 'AI 暂时无法回答，请稍后重试')
    messages.value.push({
      role: 'assistant',
      content: '抱歉，AI 服务暂时不可用。你可以先浏览平台线路，或稍后再试。',
      time: nowText()
    })
  } finally {
    sending.value = false
    persistMessages()
    await scrollToBottom()
  }
}

function sendPrompt(prompt) {
  input.value = prompt
  send()
}

function goReference(reference) {
  if (reference?.url) {
    visible.value = false
    router.push(reference.url)
  }
}

function handleOpenEvent() {
  visible.value = true
  scrollToBottom()
}

function handleResize() {
  isMobile.value = window.innerWidth < 640
}

onMounted(async () => {
  restoreMessages()
  handleResize()
  window.addEventListener('resize', handleResize)
  window.addEventListener('open-ai-assistant', handleOpenEvent)
  try {
    health.value = await getAiHealth()
  } catch {
    health.value = null
  }
  if (messages.value.length) {
    scrollToBottom()
  }

  // 演示/截图入口：/?ai=1 打开面板，/?ai=1&ask=... 自动提问。
  const params = new URLSearchParams(window.location.search)
  if (params.get('ai') === '1') {
    visible.value = true
  }
  const ask = params.get('ask')
  if (ask) {
    visible.value = true
    input.value = ask
    window.setTimeout(send, 500)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('open-ai-assistant', handleOpenEvent)
})
</script>

<style lang="scss" scoped>
$primary: #667eea;
$primary-dark: #5a67d8;
$ink: #1a1a2e;
$muted: #718096;
$line: #e8ecf4;

.ai-fab {
  position: fixed;
  right: 24px;
  bottom: 26px;
  z-index: 2100;
  display: inline-flex;
  align-items: center;
  gap: 9px;
  height: 52px;
  padding: 0 18px 0 15px;
  border: none;
  border-radius: 999px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 16px 34px -14px rgba(102, 126, 234, 0.9);
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 20px 40px -14px rgba(102, 126, 234, 1);
  }
}

.fab-glow {
  position: absolute;
  inset: -4px;
  border-radius: inherit;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.4), rgba(118, 75, 162, 0.35));
  filter: blur(10px);
  z-index: -1;
}

.fab-label {
  white-space: nowrap;
}

.assistant-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f7f9fd;
}

.assistant-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 18px 18px 16px;
  background: linear-gradient(125deg, #1f2455 0%, #3d3a8f 58%, #6d4aa8 100%);
  color: #fff;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 11px;

  h3 { margin: 0 0 3px; font-size: 16px; }
  p {
    margin: 0;
    display: flex;
    align-items: center;
    gap: 6px;
    color: rgba(255, 255, 255, 0.7);
    font-size: 11px;
  }
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 13px;
  display: grid;
  place-items: center;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.2);
  font-size: 20px;
}

.status-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #f6ad55;

  &.ok { background: #48bb78; }
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  cursor: pointer;
  display: grid;
  place-items: center;

  &:hover { background: rgba(255, 255, 255, 0.2); }
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 18px 16px 8px;
  display: flex;
  flex-direction: column;
  gap: 14px;
  scroll-behavior: smooth;
}

.welcome {
  margin: 20px 6px;
  padding: 24px 18px;
  border-radius: 18px;
  background: #fff;
  border: 1px solid $line;
  text-align: center;

  h4 { margin: 14px 0 6px; color: $ink; font-size: 17px; }
  p { margin: 0 auto; max-width: 290px; color: $muted; font-size: 12px; line-height: 1.7; }
}

.welcome-icon {
  width: 58px;
  height: 58px;
  margin: 0 auto;
  border-radius: 18px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #eef1ff, #f7ecff);
  color: $primary;
  font-size: 27px;
}

.quick-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 18px;

  button {
    width: 100%;
    padding: 10px 12px;
    border: 1px solid $line;
    border-radius: 10px;
    background: #fbfcff;
    color: #4a5568;
    font-size: 12px;
    text-align: left;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      border-color: rgba(102, 126, 234, 0.35);
      background: #f4f6ff;
      color: $primary-dark;
    }
  }
}

.message {
  display: flex;
  gap: 8px;
  align-items: flex-start;

  &.user {
    flex-direction: row-reverse;

    .bubble {
      background: linear-gradient(135deg, $primary, #6d5bd0);
      color: #fff;
      border-radius: 15px 4px 15px 15px;
    }

    .message-time { text-align: right; }
  }

  &.assistant .bubble {
    border-radius: 4px 15px 15px 15px;
  }
}

.assistant-avatar {
  width: 28px;
  height: 28px;
  flex: 0 0 28px;
  border-radius: 9px;
  display: grid;
  place-items: center;
  background: linear-gradient(135deg, #eef1ff, #f7ecff);
  color: $primary;
  font-size: 15px;
  margin-top: 2px;
}

.bubble-wrap {
  max-width: 86%;
  min-width: 0;
}

.bubble {
  padding: 11px 13px;
  background: #fff;
  border: 1px solid $line;
  color: #2d3748;
  font-size: 13px;
  line-height: 1.7;
  word-break: break-word;
}

.reply-text {
  white-space: pre-wrap;
}

.message-time {
  margin-top: 4px;
  color: #b0b7c3;
  font-size: 10px;
}

.reference-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
  padding-top: 9px;
  border-top: 1px dashed $line;
}

.reference-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  max-width: 100%;
  padding: 5px 8px;
  border: 1px solid #e2e8f6;
  border-radius: 8px;
  background: #f8faff;
  color: $primary-dark;
  font-size: 11px;
  cursor: pointer;

  &:hover { background: #eef2ff; }
}

.ref-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 150px;
}

.ref-price {
  color: #e53e3e;
  font-weight: 600;
  flex-shrink: 0;
}

.fallback-badge {
  display: inline-block;
  margin-top: 8px;
  padding: 4px 7px;
  border-radius: 6px;
  background: #fffaf0;
  color: #b7791f;
  font-size: 10px;
}

.typing {
  display: flex;
  gap: 4px;
  align-items: center;
  min-height: 38px;

  span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: #a3b1d6;
    animation: typing 1.2s infinite ease-in-out;

    &:nth-child(2) { animation-delay: 0.15s; }
    &:nth-child(3) { animation-delay: 0.3s; }
  }
}

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); opacity: 0.5; }
  30% { transform: translateY(-4px); opacity: 1; }
}

.inline-prompts {
  display: flex;
  gap: 7px;
  padding: 0 16px 8px;
  overflow-x: auto;

  button {
    flex-shrink: 0;
    padding: 6px 10px;
    border: 1px solid $line;
    border-radius: 999px;
    background: #fff;
    color: $muted;
    font-size: 11px;
    cursor: pointer;

    &:hover { border-color: rgba(102, 126, 234, 0.4); color: $primary-dark; }
  }
}

.assistant-input {
  padding: 10px 14px 14px;
  border-top: 1px solid $line;
  background: #fff;

  :deep(.el-textarea__inner) {
    border-radius: 12px;
    box-shadow: 0 0 0 1px $line;
    font-size: 13px;
    padding: 10px 12px;

    &:focus { box-shadow: 0 0 0 1px rgba(102, 126, 234, 0.55); }
  }
}

.input-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-top: 8px;

  .el-button {
    border: none;
    border-radius: 9px;
    background: linear-gradient(135deg, $primary, #764ba2);
  }
}

.input-hint {
  color: #b0b7c3;
  font-size: 10px;
}

.send-icon {
  margin-left: 4px;
}

:global(.ai-drawer .el-drawer__body) {
  padding: 0;
}

@media (max-width: 640px) {
  .ai-fab {
    right: 16px;
    bottom: 18px;
    width: 50px;
    height: 50px;
    padding: 0;
    justify-content: center;
  }

  .fab-label { display: none; }
  .bubble-wrap { max-width: 88%; }
}
</style>
