<template>
  <PageContainer title="协同消息" sub-title="跨部门沟通 · 在线群聊 · 消息转待办 · 全程留痕">
    <template #extra>
      <a-space>
        <a-button v-if="tab === 'chat'" @click="setTab('tasks')"><CheckSquareOutlined /> 聊天待办</a-button>
        <a-button v-else @click="setTab('chat')"><MessageOutlined /> 回到聊天</a-button>
        <a-button type="primary" @click="createOpen = true"><PlusOutlined /> 发起会话</a-button>
      </a-space>
    </template>

    <ChatTasksView v-if="tab === 'tasks'" :highlight="String(route.query.taskId || '')" @open-chat="openConversation" />

    <div v-else class="cw">
      <aside class="cw-list" :class="{ 'cw-list--hidden': narrow && activeId }">
        <div class="cw-list__head"><b>最近会话</b><a-button type="text" aria-label="发起会话" @click="createOpen = true"><PlusOutlined /></a-button></div>
        <a-input-search v-model:value="search" placeholder="搜索会话或消息内容" allow-clear @search="runSearch" />
        <a-segmented v-model:value="filter" :options="FILTERS" block size="small" style="margin: 10px 0" />
        <div v-if="searchHits.length" class="cw-hits">
          <div class="cw-muted">消息搜索结果（仅限你所在的会话）<a @click="searchHits = []">清除</a></div>
          <button v-for="hit in searchHits" :key="hit.id" type="button" class="cw-hit" @click="openConversation(hit.conversationId)">
            <b>{{ hit.senderName }}</b><span>{{ hit.body }}</span>
          </button>
        </div>
        <a-empty v-if="loaded && !visibleConversations.length" description="还没有会话，点 ＋ 发起单聊或群聊" />
        <button v-for="c in visibleConversations" :key="c.id" type="button" class="cw-conv" :class="{ 'cw-conv--on': c.id === activeId }" @click="openConversation(c.id)">
          <span class="cw-avatar" :class="c.type === 'GROUP' ? 'cw-avatar--group' : ''">{{ avatarText(c.title, c.type) }}</span>
          <span class="cw-conv__main">
            <b>{{ c.title }}</b>
            <small>{{ c.lastMessagePreview || '暂无消息' }}</small>
          </span>
          <span class="cw-conv__side">
            <small>{{ c.lastMessageAt ? shortTime(c.lastMessageAt) : '' }}</small>
            <a-badge v-if="Number(c.unread) > 0" :count="Number(c.unread)" />
          </span>
        </button>
      </aside>

      <section class="cw-main" :class="{ 'cw-main--hidden': narrow && !activeId }">
        <template v-if="active">
          <header class="cw-main__head">
            <a-button v-if="narrow" type="link" @click="activeId = ''">← 会话</a-button>
            <div><b>{{ active.title }}</b><small v-if="active.type === 'GROUP'"> （{{ members.length }} 人）</small></div>
          </header>
          <ChatThread :key="active.id" :conversation-id="active.id" :conversation-title="active.title" @changed="loadConversations" @task-created="loadSide" />
        </template>
        <a-empty v-else class="cw-empty" description="选择左侧会话开始沟通" />
      </section>

      <aside v-if="active && !narrow" class="cw-side">
        <h4>{{ active.type === 'GROUP' ? '群组信息' : '会话信息' }}</h4>
        <div class="cw-box">{{ active.title }}<div class="cw-muted">只有会话成员能查看消息；被移出后立即失去访问权</div></div>
        <h4>{{ active.type === 'GROUP' ? `群成员 (${members.length})` : '成员' }}
          <a v-if="canManage" class="cw-link" @click="manageOpen = true">管理</a></h4>
        <div class="cw-members"><span v-for="m in members" :key="m.staffId" class="cw-chip">{{ m.name }}<small v-if="m.role === 'OWNER'"> 群主</small></span></div>
        <h4>会话内待办 <small>{{ convTasks.length }}</small></h4>
        <a-empty v-if="!convTasks.length" description="暂无" :image-style="{ height: '28px' }" />
        <div v-for="t in convTasks.slice(0, 4)" :key="t.taskId" class="cw-box">
          <b>{{ t.title }}</b>
          <div class="cw-muted">{{ t.assigneeName }} · <a-tag :color="TASK_STATUS[t.status]?.color">{{ TASK_STATUS[t.status]?.label }}</a-tag></div>
          <a-progress :percent="t.status === 'DONE' ? 100 : t.status === 'SUBMITTED' ? 75 : 40" :show-info="false" size="small" />
        </div>
        <a-button block @click="setTab('tasks')">查看全部聊天待办 ↗</a-button>
        <div class="cw-privacy"><b>隐私提醒</b>涉及长者健康、身份和财务信息，请在有权限的业务系统内处理，不要在聊天里大段复制。紧急情况请立即执行应急预案。</div>
      </aside>
    </div>

    <a-modal v-model:open="createOpen" title="发起会话" :confirm-loading="creating" @ok="createConv">
      <a-form layout="vertical">
        <a-form-item label="类型">
          <a-radio-group v-model:value="createForm.type"><a-radio value="DIRECT">单聊</a-radio><a-radio value="GROUP">群聊</a-radio></a-radio-group>
        </a-form-item>
        <a-form-item v-if="createForm.type === 'GROUP'" label="群名称" required><a-input v-model:value="createForm.title" :maxlength="100" placeholder="如：护理后勤协同群" /></a-form-item>
        <a-form-item v-if="createForm.type === 'GROUP'" label="按部门拉人">
          <a-select :value="undefined" show-search :filter-option="false" :options="departmentOptions" :loading="deptAdding || departmentLoading"
            placeholder="选择部门，加入该部门全部在职同事（可再逐个移除）" @search="searchDepartments" @focus="() => searchDepartments('')" @select="(d: unknown) => addDepartment(String(d), 'create')" />
        </a-form-item>
        <a-form-item label="成员" required>
          <a-select v-model:value="createForm.memberIds" :mode="createForm.type === 'GROUP' ? 'multiple' : undefined" show-search :filter-option="false"
            :options="staffOptions" :loading="staffLoading" placeholder="搜索同事姓名" @search="searchStaff" @focus="() => !staffOptions.length && searchStaff('')" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal v-model:open="manageOpen" title="管理群成员" :confirm-loading="creating" @ok="saveMembers">
      <a-form layout="vertical">
        <a-form-item label="添加成员">
          <a-select v-model:value="addIds" mode="multiple" show-search :filter-option="false" :options="staffOptions" :loading="staffLoading" @search="searchStaff" />
        </a-form-item>
        <a-form-item v-if="true" label="按部门拉人">
          <a-select :value="undefined" show-search :filter-option="false" :options="departmentOptions" :loading="deptAdding || departmentLoading"
            placeholder="选择部门，加入该部门全部在职同事（可再逐个移除）" @search="searchDepartments" @focus="() => searchDepartments('')" @select="(d: unknown) => addDepartment(String(d), 'manage')" />
        </a-form-item>
        <a-form-item label="移出成员">
          <a-checkbox-group v-model:value="removeIds" :options="members.filter((m) => m.role !== 'OWNER').map((m) => ({ value: m.staffId, label: m.name }))" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { CheckSquareOutlined, MessageOutlined, PlusOutlined } from '@ant-design/icons-vue'
import PageContainer from '../../components/PageContainer.vue'
import { createConversation, getChatTasks, getConversations, getMembers, manageMembers, searchMessages, type ChatConversation, type ChatMember, type ChatMessage, type ChatTaskRow } from '../../api/chat'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { useDepartmentOptions } from '../../composables/useDepartmentOptions'
import { getStaffOptionPage } from '../../api/rbac'
import { TASK_STATUS, avatarText } from './chatModel'
import ChatThread from './ChatThread.vue'
import ChatTasksView from './ChatTasksView.vue'

const FILTERS = [{ label: '全部会话', value: 'all' }, { label: '群聊', value: 'GROUP' }, { label: '单聊', value: 'DIRECT' }]
const route = useRoute()
const router = useRouter()
const { staffOptions, staffLoading, searchStaff, ensureSelectedStaff } = useStaffOptions({ pageSize: 50 })
const { departmentOptions, departmentLoading, searchDepartments } = useDepartmentOptions({ pageSize: 100 })
const deptAdding = ref(false)

const tab = computed(() => (route.query.tab === 'tasks' ? 'tasks' : 'chat'))
const conversations = ref<ChatConversation[]>([])
const loaded = ref(false)
const activeId = ref(String(route.query.c || ''))
const filter = ref('all')
const search = ref('')
const searchHits = ref<ChatMessage[]>([])
const members = ref<ChatMember[]>([])
const convTasks = ref<ChatTaskRow[]>([])
const createOpen = ref(false)
const manageOpen = ref(false)
const creating = ref(false)
const createForm = reactive({ type: 'GROUP' as 'DIRECT' | 'GROUP', title: '', memberIds: [] as string[] | string })
const addIds = ref<string[]>([])
const removeIds = ref<string[]>([])
const narrow = ref(window.innerWidth < 900)

const visibleConversations = computed(() => conversations.value
  .filter((c) => filter.value === 'all' || c.type === filter.value)
  .filter((c) => !search.value || c.title.includes(search.value)))
const active = computed(() => conversations.value.find((c) => c.id === activeId.value))
const canManage = computed(() => active.value?.type === 'GROUP' && ['OWNER', 'ADMIN'].includes(active.value.myRole || ''))

const shortTime = (t: string) => (dayjs(t).isSame(dayjs(), 'day') ? dayjs(t).format('HH:mm') : dayjs(t).format('MM-DD'))

function setTab(next: 'chat' | 'tasks') {
  router.replace({ query: { ...route.query, tab: next === 'tasks' ? 'tasks' : undefined, taskId: undefined } })
}

async function loadConversations() {
  conversations.value = await getConversations()
  loaded.value = true
  if (!activeId.value && !narrow.value && conversations.value.length) activeId.value = conversations.value[0].id
}

async function loadSide() {
  if (!active.value) return
  const [m, t] = await Promise.allSettled([getMembers(active.value.id), getChatTasks({ view: 'involved' })])
  members.value = m.status === 'fulfilled' ? m.value : []
  convTasks.value = t.status === 'fulfilled' ? t.value.filter((x) => x.conversationId === active.value?.id) : []
}

function openConversation(id: string) {
  activeId.value = id
  searchHits.value = []
  if (tab.value === 'tasks') setTab('chat')
}

async function runSearch() {
  if (search.value.trim().length < 2) { searchHits.value = []; return }
  searchHits.value = await searchMessages(search.value.trim())
}

async function createConv() {
  const ids = Array.isArray(createForm.memberIds) ? createForm.memberIds : createForm.memberIds ? [createForm.memberIds] : []
  if (!ids.length) return message.warning('请选择成员')
  if (createForm.type === 'GROUP' && !createForm.title.trim()) return message.warning('请填写群名称')
  creating.value = true
  try {
    const { id } = await createConversation({ type: createForm.type, title: createForm.title.trim(), memberIds: ids })
    createOpen.value = false
    Object.assign(createForm, { type: 'GROUP', title: '', memberIds: [] })
    await loadConversations()
    openConversation(String(id))
  } finally {
    creating.value = false
  }
}

// shortcut: 每个部门最多取 300 人，超大部门再改为服务端按部门展开
async function addDepartment(departmentId: string, target: 'create' | 'manage') {
  deptAdding.value = true
  try {
    const page = await getStaffOptionPage({ departmentId, activeOnly: true, pageNo: 1, pageSize: 300 })
    const existing = new Set(target === 'manage' ? members.value.map((m) => m.staffId) : [])
    const ids = page.records.filter((s) => !existing.has(String(s.id))).map((s) => {
      ensureSelectedStaff(s.id, s.realName)
      return String(s.id)
    })
    const current = target === 'create' ? (Array.isArray(createForm.memberIds) ? createForm.memberIds : []) : addIds.value
    const merged = [...new Set([...current, ...ids])]
    if (target === 'create') createForm.memberIds = merged
    else addIds.value = merged
    message.success(ids.length ? `已加入 ${merged.length - current.length} 位同事` : '该部门没有可加入的在职同事')
  } finally {
    deptAdding.value = false
  }
}

async function saveMembers() {
  if (!active.value) return
  creating.value = true
  try {
    await manageMembers(active.value.id, { add: addIds.value, remove: removeIds.value })
    message.success('群成员已更新')
    manageOpen.value = false
    addIds.value = []
    removeIds.value = []
    await loadSide()
  } finally {
    creating.value = false
  }
}

function onChatEvent() {
  loadConversations().catch(() => {})
}
function onResize() {
  narrow.value = window.innerWidth < 900
}

watch(activeId, (id) => router.replace({ query: { ...route.query, c: id || undefined } }))
// active 在会话列表加载后才确定，所以跟着它刷新右侧成员与待办
watch(() => active.value?.id, () => loadSide().catch(() => {}))
onMounted(() => {
  loadConversations().catch(() => { loaded.value = true })
  window.addEventListener('chat-event', onChatEvent)
  window.addEventListener('resize', onResize)
})
onBeforeUnmount(() => {
  window.removeEventListener('chat-event', onChatEvent)
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.cw { display: grid; grid-template-columns: 300px minmax(0, 1fr) 260px; height: calc(100vh - 230px); min-height: 520px; border: 1px solid #e3ebe6; border-radius: 16px; background: #fff; overflow: hidden; }
.cw-list { display: flex; flex-direction: column; padding: 14px; border-right: 1px solid #e6ece8; overflow-y: auto; }
.cw-list__head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; font-size: 16px; }
.cw-conv { display: flex; gap: 10px; align-items: center; width: 100%; padding: 10px 8px; border: 0; border-radius: 10px; background: none; text-align: left; cursor: pointer; }
.cw-conv:hover, .cw-conv--on { background: #eef8f3; }
.cw-conv__main { display: grid; flex: 1; min-width: 0; }
.cw-conv__main small { overflow: hidden; color: #7b877e; text-overflow: ellipsis; white-space: nowrap; }
.cw-conv__side { display: grid; justify-items: end; gap: 4px; color: #9aa59f; font-size: 12px; }
.cw-avatar { flex: none; display: grid; place-items: center; width: 36px; height: 36px; border-radius: 50%; background: #e8eef9; color: #3b5b9a; font-weight: 700; }
.cw-avatar--group { background: #e3f2ea; color: #1f6f55; }
.cw-main { display: flex; flex-direction: column; min-width: 0; min-height: 0; }
.cw-main__head { padding: 14px 20px; border-bottom: 1px solid #e6ece8; font-size: 16px; display: flex; align-items: center; gap: 8px; }
.cw-main__head small { color: #7b877e; font-size: 12px; }
.cw-empty { margin: auto; }
.cw-side { padding: 14px; border-left: 1px solid #e6ece8; overflow-y: auto; }
.cw-side h4 { display: flex; justify-content: space-between; margin: 14px 0 8px; font-weight: 600; }
.cw-box { display: grid; gap: 4px; margin-bottom: 8px; padding: 10px 12px; border-radius: 10px; background: #f5f9f7; font-size: 13px; }
.cw-members { display: flex; flex-wrap: wrap; gap: 6px; }
.cw-chip { padding: 3px 10px; border-radius: 999px; background: #f0f5f2; font-size: 12px; }
.cw-link { font-size: 12px; font-weight: 400; }
.cw-muted { color: #8a968f; font-size: 12px; }
.cw-privacy { display: grid; gap: 4px; margin-top: 14px; padding: 12px; border-radius: 10px; background: #fff8ec; color: #7a5a1f; font-size: 12px; }
.cw-hits { margin-bottom: 8px; }
.cw-hit { display: grid; width: 100%; padding: 6px 8px; border: 0; border-radius: 8px; background: #fffbe9; text-align: left; font-size: 12px; cursor: pointer; margin-top: 4px; }
@media (max-width: 1200px) { .cw { grid-template-columns: 260px minmax(0, 1fr); } .cw-side { display: none; } }
@media (max-width: 900px) {
  .cw { grid-template-columns: 1fr; height: calc(100vh - 200px); }
  .cw-list--hidden, .cw-main--hidden { display: none; }
}
</style>
