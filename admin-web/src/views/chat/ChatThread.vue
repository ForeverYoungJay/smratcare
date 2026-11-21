<template>
  <div class="ct" :class="{ 'ct--compact': compact }">
    <div ref="scroller" class="ct-scroll" @scroll="onScroll">
      <div v-if="hasMore" class="ct-more"><a-button size="small" type="link" :loading="loadingOlder" @click="loadOlder">加载更早的消息</a-button></div>
      <a-empty v-if="loaded && !messages.length" description="还没有消息，发一条开始协同吧" />
      <template v-for="(m, i) in messages" :key="m.id">
        <div v-if="showDate(i)" class="ct-date"><span>{{ dayjs(m.createTime).format('YYYY年M月D日 dddd') }}</span></div>
        <div v-if="m.type === 'SYSTEM'" class="ct-system">{{ m.body }}</div>
        <div v-else class="ct-msg" :class="{ 'ct-msg--mine': m.senderId === myId }">
          <span class="ct-avatar">{{ (m.senderName || '?').slice(-1) }}</span>
          <div class="ct-bubble-wrap">
            <div class="ct-meta">{{ m.senderId === myId ? '我' : m.senderName }} · {{ dayjs(m.createTime).format('HH:mm') }}</div>
            <div v-if="quoteOf(m)" class="ct-quote">{{ quoteOf(m) }}</div>
            <div v-if="m.type === 'TASK_CARD' && m.task" class="ct-task">
              <b><CheckSquareOutlined /> 已关联任务：{{ m.task.title || '（无权查看）' }}</b>
              <span>任务 #{{ m.task.taskId }} · 负责人 {{ m.task.assigneeName || '-' }}{{ m.task.endTime ? ` · 截止 ${dayjs(m.task.endTime).format('MM-DD HH:mm')}` : '' }}</span>
              <span><a-tag :color="TASK_STATUS[m.task.status]?.color">{{ TASK_STATUS[m.task.status]?.label || m.task.status }}</a-tag></span>
              <a-button size="small" @click="router.push({ path: '/workbench/chat', query: { tab: 'tasks', taskId: m.task.taskId } })">查看任务进度 ↗</a-button>
            </div>
            <div v-else class="ct-bubble" :class="{ 'ct-bubble--recalled': m.recalled }">{{ m.recalled ? '消息已撤回' : m.body }}</div>
            <div v-if="!m.recalled" class="ct-actions">
              <span v-if="m.pending === 'sending'" class="ct-muted">发送中…</span>
              <template v-else-if="m.pending === 'failed'">
                <span class="ct-fail">发送失败</span><a @click="retry(m)">重发</a>
              </template>
              <template v-else-if="m.type === 'TEXT'">
                <a @click="openConvert(m)">＋ 转为待办</a>
                <a @click="replyTo = m">引用</a>
                <a v-if="m.senderId === myId && canRecall(m)" @click="recall(m)">撤回</a>
                <span v-if="Number(m.linkedTaskCount) > 0" class="ct-muted">已关联 {{ m.linkedTaskCount }} 个任务</span>
              </template>
            </div>
          </div>
        </div>
      </template>
    </div>

    <div class="ct-composer">
      <div v-if="replyTo" class="ct-replying">引用 {{ replyTo.senderName }}：{{ (replyTo.body || '').slice(0, 40) }} <a @click="replyTo = undefined">取消</a></div>
      <div class="ct-tools">
        <a @click="replyTo ? openConvert(replyTo) : message.info('请先在消息下方点「转为待办」或「引用」一条消息')"><CheckSquareOutlined /> 消息转待办</a>
        <span class="ct-muted">支持文本；附件、语音、视频暂未开通</span>
      </div>
      <div class="ct-input">
        <a-textarea v-model:value="draft" :auto-size="{ minRows: 1, maxRows: 4 }" :maxlength="2000" placeholder="输入消息，按 Enter 发送，Shift+Enter 换行"
          aria-label="输入消息" @keydown.enter.exact.prevent="send" />
        <a-button type="primary" :disabled="!draft.trim()" @click="send"><SendOutlined /> 发送</a-button>
      </div>
    </div>

    <ConvertTaskDrawer v-model:open="convertOpen" :message="convertSource" :conversation-title="conversationTitle" @created="onTaskCreated" />
  </div>
</template>

<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { CheckSquareOutlined, SendOutlined } from '@ant-design/icons-vue'
import { getMessages, markRead, recallMessage, sendMessage, type ChatMessage } from '../../api/chat'
import { useUserStore } from '../../stores/user'
import { TASK_STATUS, mergeMessages, newClientMessageId } from './chatModel'
import ConvertTaskDrawer from './ConvertTaskDrawer.vue'

const props = withDefaults(defineProps<{ conversationId: string; conversationTitle?: string; compact?: boolean }>(), { conversationTitle: '', compact: false })
const emit = defineEmits<{ changed: []; 'task-created': [] }>()

const PAGE = 30
const router = useRouter()
const myId = String(useUserStore().staffInfo?.id || '')
const messages = ref<ChatMessage[]>([])
const loaded = ref(false)
const hasMore = ref(false)
const loadingOlder = ref(false)
const draft = ref('')
const replyTo = ref<ChatMessage>()
const convertOpen = ref(false)
const convertSource = ref<ChatMessage>()
const scroller = ref<HTMLElement>()
let pollTimer: ReturnType<typeof setInterval> | undefined

const byId = (id?: string | null) => (id ? messages.value.find((m) => m.id === id) : undefined)
const quoteOf = (m: ChatMessage) => {
  const q = m.type === 'TEXT' ? byId(m.replyToId) : undefined
  return q ? `${q.senderName}：${q.recalled ? '消息已撤回' : (q.body || '').slice(0, 60)}` : ''
}
const showDate = (i: number) => i === 0 || !dayjs(messages.value[i].createTime).isSame(messages.value[i - 1].createTime, 'day')
const canRecall = (m: ChatMessage) => dayjs().diff(dayjs(m.createTime), 'second') < 120

function scrollToBottom() {
  nextTick(() => { if (scroller.value) scroller.value.scrollTop = scroller.value.scrollHeight })
}

async function markLatestRead() {
  const last = [...messages.value].reverse().find((m) => !m.id.startsWith('local-'))
  if (last) await markRead(props.conversationId, last.id).catch(() => {})
  emit('changed')
  window.dispatchEvent(new CustomEvent('chat-read'))
}

async function loadLatest() {
  loaded.value = false
  messages.value = []
  replyTo.value = undefined
  const list = await getMessages(props.conversationId, { limit: PAGE })
  messages.value = list
  hasMore.value = list.length === PAGE
  loaded.value = true
  scrollToBottom()
  await markLatestRead()
}

/** 断线、收到推送或定时：按游标补拉新消息，已有的按 ID 去重 */
async function fetchNewer() {
  const last = [...messages.value].reverse().find((m) => !m.id.startsWith('local-'))
  if (!last) return loadLatest()
  const atBottom = scroller.value ? scroller.value.scrollHeight - scroller.value.scrollTop - scroller.value.clientHeight < 80 : true
  // 重新拉最近一窗：同时拿到新消息、撤回和任务卡的最新状态
  const refreshed = await getMessages(props.conversationId, { limit: 100 })
  const hasNew = refreshed.some((r) => BigInt(r.id) > BigInt(last.id))
  messages.value = mergeMessages(messages.value.map((m) => refreshed.find((r) => r.id === m.id) || m), refreshed)
  if (hasNew) {
    if (atBottom) scrollToBottom()
    await markLatestRead()
  }
}

async function loadOlder() {
  const first = messages.value.find((m) => !m.id.startsWith('local-'))
  if (!first) return
  loadingOlder.value = true
  try {
    const list = await getMessages(props.conversationId, { beforeId: first.id, limit: PAGE })
    hasMore.value = list.length === PAGE
    messages.value = mergeMessages(messages.value, list)
  } finally {
    loadingOlder.value = false
  }
}

function onScroll() {
  if (scroller.value && scroller.value.scrollTop < 20 && hasMore.value && !loadingOlder.value) loadOlder()
}

async function deliver(local: ChatMessage) {
  local.pending = 'sending'
  try {
    const saved = await sendMessage(props.conversationId, { body: local.body || '', clientMessageId: local.clientMessageId!, replyToId: local.replyToId || undefined })
    messages.value = mergeMessages(messages.value, [saved])
    emit('changed')
  } catch {
    // 服务端未确认：保留在本地并标失败，用同一 clientMessageId 重发不会产生重复
    local.pending = 'failed'
  }
}

async function send() {
  const body = draft.value.trim()
  if (!body) return
  const local: ChatMessage = {
    id: `local-${Date.now()}`, conversationId: props.conversationId, senderId: myId, senderName: '我', type: 'TEXT', body,
    recalled: false, replyToId: replyTo.value?.id, clientMessageId: newClientMessageId(), createTime: dayjs().format('YYYY-MM-DDTHH:mm:ss'),
    linkedTaskCount: 0, pending: 'sending'
  }
  messages.value = [...messages.value, local]
  draft.value = ''
  replyTo.value = undefined
  scrollToBottom()
  await deliver(messages.value[messages.value.length - 1])
}

function retry(m: ChatMessage) {
  deliver(m)
}

async function recall(m: ChatMessage) {
  await recallMessage(m.id)
  message.success('已撤回')
  await fetchNewer()
}

function openConvert(m: ChatMessage) {
  convertSource.value = m
  convertOpen.value = true
}

async function onTaskCreated() {
  await fetchNewer()
  scrollToBottom()
  emit('task-created')
}

function onChatEvent(event: Event) {
  const detail = (event as CustomEvent).detail || {}
  if (!detail.conversationId || detail.conversationId === props.conversationId) fetchNewer().catch(() => {})
}

watch(() => props.conversationId, (id) => { if (id) loadLatest().catch(() => { loaded.value = true }) })
onMounted(() => {
  if (props.conversationId) loadLatest().catch(() => { loaded.value = true })
  window.addEventListener('chat-event', onChatEvent)
  // shortcut: 推送只是提醒，另每 15 秒按游标补拉一次，保证断线后也能补齐；以后可改为只在断线重连时补拉
  pollTimer = setInterval(() => { if (!document.hidden && props.conversationId) fetchNewer().catch(() => {}) }, 15000)
})
onBeforeUnmount(() => {
  window.removeEventListener('chat-event', onChatEvent)
  if (pollTimer) clearInterval(pollTimer)
})
defineExpose({ reload: loadLatest })
</script>

<style scoped>
.ct { display: flex; flex-direction: column; height: 100%; min-height: 0; }
.ct-scroll { flex: 1; min-height: 0; overflow-y: auto; padding: 16px 20px; }
.ct-more { text-align: center; }
.ct-date { text-align: center; margin: 10px 0; }
.ct-date span { padding: 2px 12px; border-radius: 999px; background: #eef3f0; color: #6b7a74; font-size: 12px; }
.ct-system { margin: 8px 0; text-align: center; color: #8a968f; font-size: 12px; }
.ct-msg { display: flex; gap: 10px; margin: 12px 0; }
.ct-msg--mine { flex-direction: row-reverse; }
.ct-avatar { flex: none; display: grid; place-items: center; width: 32px; height: 32px; border-radius: 50%; background: #e3f2ea; color: #1f6f55; font-weight: 600; }
.ct-bubble-wrap { display: grid; gap: 4px; max-width: min(560px, 78%); }
.ct-msg--mine .ct-bubble-wrap { justify-items: end; }
.ct-meta { color: #8a968f; font-size: 12px; }
.ct-bubble { padding: 10px 14px; border-radius: 12px; background: #fff; border: 1px solid #e3ebe6; white-space: pre-wrap; word-break: break-word; }
.ct-msg--mine .ct-bubble { background: #e3f4ea; border-color: #cfe9d9; }
.ct-bubble--recalled { color: #9aa59f; font-style: italic; }
.ct-quote { padding: 4px 10px; border-left: 3px solid #b9d9c7; background: #f5f9f7; color: #6b7a74; font-size: 12px; }
.ct-task { display: grid; gap: 6px; padding: 12px 14px; border-radius: 12px; border: 1px solid #cfe9d9; background: #f0f9f3; font-size: 13px; }
.ct-task b { color: #1f6f55; }
.ct-task span { color: #5c6f65; }
.ct-actions { display: flex; gap: 12px; font-size: 12px; }
.ct-actions a { color: #1f8a63; }
.ct-muted { color: #9aa59f; font-size: 12px; }
.ct-fail { color: #c0392b; }
.ct-composer { border-top: 1px solid #e6ece8; padding: 10px 16px 12px; background: #fbfcfb; }
.ct-replying { margin-bottom: 6px; color: #6b7a74; font-size: 12px; }
.ct-tools { display: flex; justify-content: space-between; gap: 12px; margin-bottom: 6px; font-size: 12px; }
.ct-tools a { color: #1f8a63; }
.ct-input { display: flex; gap: 10px; align-items: flex-end; }
.ct--compact .ct-scroll { padding: 12px; }
</style>
