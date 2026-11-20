<template>
  <a-drawer :open="open" :width="width" :title="`快捷聊天${totalUnread ? ` · ${totalUnread} 条未读` : ''}`" :body-style="{ padding: 0, display: 'flex', flexDirection: 'column' }" @close="emit('update:open', false)">
    <div class="qd-top">
      <a-input-search v-model:value="keyword" placeholder="搜索会话" allow-clear size="small" />
      <div class="qd-chips">
        <button v-for="c in shown" :key="c.id" type="button" class="qd-chip" :class="{ 'qd-chip--on': c.id === activeId }" @click="activeId = c.id">
          {{ c.title }}<a-badge v-if="Number(c.unread) > 0" :count="Number(c.unread)" :offset="[4, -2]" />
        </button>
      </div>
      <a-empty v-if="loaded && !conversations.length" description="还没有会话，进入完整聊天发起" :image-style="{ height: '32px' }" />
    </div>
    <div class="qd-thread">
      <ChatThread v-if="activeId" :key="activeId" :conversation-id="activeId" :conversation-title="active?.title" compact @changed="load" />
    </div>
    <template #footer>
      <a-space style="float: right">
        <a-button @click="emit('update:open', false)">关闭</a-button>
        <a-button type="primary" @click="goFull">进入完整聊天 ↗</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getConversations, type ChatConversation } from '../../api/chat'
import ChatThread from './ChatThread.vue'

const props = defineProps<{ open: boolean }>()
const emit = defineEmits<{ 'update:open': [value: boolean] }>()
const router = useRouter()
const conversations = ref<ChatConversation[]>([])
const loaded = ref(false)
const activeId = ref('')
const keyword = ref('')
const width = Math.min(480, window.innerWidth)

const shown = computed(() => conversations.value.filter((c) => !keyword.value || c.title.includes(keyword.value)).slice(0, 12))
const active = computed(() => conversations.value.find((c) => c.id === activeId.value))
const totalUnread = computed(() => conversations.value.reduce((sum, c) => sum + Number(c.unread || 0), 0))

async function load() {
  conversations.value = await getConversations()
  loaded.value = true
  if (!activeId.value && conversations.value.length) activeId.value = conversations.value[0].id
}

function goFull() {
  emit('update:open', false)
  router.push({ path: '/workbench/chat', query: activeId.value ? { c: activeId.value } : {} })
}

watch(() => props.open, (open) => { if (open) load().catch(() => { loaded.value = true }) })
</script>

<style scoped>
.qd-top { padding: 12px 16px 8px; border-bottom: 1px solid #e6ece8; }
.qd-chips { display: flex; gap: 8px; overflow-x: auto; padding: 10px 0 4px; }
.qd-chip { flex: none; padding: 6px 12px; border: 1px solid #e3ebe6; border-radius: 999px; background: #fff; cursor: pointer; font-size: 13px; }
.qd-chip--on { border-color: #1f9d74; background: #eef8f3; color: #1f6f55; }
.qd-thread { flex: 1; min-height: 0; }
</style>
