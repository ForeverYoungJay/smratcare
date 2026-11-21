<template>
  <div class="tv">
    <div class="tv-cards">
      <button v-for="c in cards" :key="c.key" type="button" class="tv-card" :class="{ 'tv-card--on': status === c.status }" @click="status = c.status">
        <span>{{ c.label }}</span><b>{{ c.value }}</b>
      </button>
    </div>
    <section class="tv-panel">
      <div class="tv-bar">
        <a-radio-group v-model:value="view" button-style="solid" @change="load">
          <a-radio-button value="all">全部</a-radio-button>
          <a-radio-button value="mine">我负责</a-radio-button>
          <a-radio-button value="created">我发起</a-radio-button>
          <a-radio-button value="involved">我参与</a-radio-button>
        </a-radio-group>
        <a-input-search v-model:value="keyword" placeholder="搜索任务编号或标题" allow-clear style="width: 240px" />
        <span class="tv-muted">只显示与你有关的任务；状态以统一任务中心为准</span>
      </div>
      <a-table size="middle" row-key="taskId" :data-source="rows" :columns="columns" :loading="loading" :pagination="{ pageSize: 10 }"
        :row-class-name="(r: ChatTaskRow) => (r.taskId === highlight ? 'tv-row--hl' : '')" :scroll="{ x: 900 }">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'title'"><b>{{ record.title }}</b><div class="tv-muted">#{{ record.taskId }} · {{ record.planCategory }}</div></template>
          <template v-else-if="column.key === 'source'">{{ record.conversationTitle }}</template>
          <template v-else-if="column.key === 'priority'"><a-tag :color="PRIORITY[record.priority]?.color">{{ PRIORITY[record.priority]?.label || '-' }}</a-tag></template>
          <template v-else-if="column.key === 'end'">
            <span :class="{ 'tv-late': record.status !== 'DONE' && record.endTime && dayjs(record.endTime).isBefore(dayjs()) }">{{ record.endTime ? dayjs(record.endTime).format('MM-DD HH:mm') : '-' }}</span>
          </template>
          <template v-else-if="column.key === 'status'"><a-tag :color="TASK_STATUS[record.status]?.color">{{ TASK_STATUS[record.status]?.label || record.status }}</a-tag></template>
          <template v-else-if="column.key === 'action'">
            <a-space size="small">
              <a v-if="record.mine && record.status === 'OPEN'" @click="act(record, 'submit')">提交验收</a>
              <template v-if="record.created && record.status === 'SUBMITTED'">
                <a @click="act(record, 'pass')">验收通过</a><a @click="act(record, 'reject')">退回</a>
              </template>
              <a v-if="record.conversationId" @click="emit('open-chat', record.conversationId)">回到聊天</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { getChatTasks, type ChatTaskRow } from '../../api/chat'
import { acceptOaTask, submitOaTask } from '../../api/oa'
import { PRIORITY, TASK_STATUS, taskCounts } from './chatModel'

const props = defineProps<{ highlight?: string }>()
const emit = defineEmits<{ 'open-chat': [conversationId: string] }>()
const columns = [
  { title: '任务 / 编号', key: 'title', width: 240 }, { title: '来源会话', key: 'source', width: 160 },
  { title: '负责人', dataIndex: 'assigneeName', width: 100 }, { title: '优先级', key: 'priority', width: 80 },
  { title: '截止时间', key: 'end', width: 120 }, { title: '状态', key: 'status', width: 90 }, { title: '操作', key: 'action', width: 180 }
]

const all = ref<ChatTaskRow[]>([])
const loading = ref(false)
const view = ref('all')
const status = ref<string | undefined>()
const keyword = ref('')
const highlight = computed(() => props.highlight)
const counts = computed(() => taskCounts(all.value))
const cards = computed(() => [
  { key: 'all', label: '全部聊天任务', value: counts.value.all, status: undefined },
  { key: 'open', label: '处理中', value: counts.value.open, status: 'OPEN' },
  { key: 'submitted', label: '待验收', value: counts.value.submitted, status: 'SUBMITTED' },
  { key: 'done', label: '已完成', value: counts.value.done, status: 'DONE' }
])
const rows = computed(() => all.value
  .filter((r) => !status.value || r.status === status.value)
  .filter((r) => !keyword.value || r.title.includes(keyword.value) || r.taskId.includes(keyword.value)))

async function load() {
  loading.value = true
  try {
    all.value = await getChatTasks({ view: view.value })
  } finally {
    loading.value = false
  }
}

async function act(row: ChatTaskRow, action: 'submit' | 'pass' | 'reject') {
  if (action === 'submit') await submitOaTask(row.taskId)
  else await acceptOaTask(row.taskId, action === 'pass')
  message.success(action === 'submit' ? '已提交验收' : action === 'pass' ? '已验收完成' : '已退回处理')
  await load()
}

onMounted(load)
defineExpose({ reload: load })
</script>

<style scoped>
.tv-cards { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px; margin-bottom: 16px; }
.tv-card { display: grid; gap: 4px; padding: 16px 18px; border: 1px solid #e3ebe6; border-radius: 14px; background: #fff; text-align: left; cursor: pointer; }
.tv-card span { color: #5c6f65; }
.tv-card b { font-size: 26px; color: #1f6f55; }
.tv-card--on { border-color: #1f9d74; box-shadow: 0 0 0 2px rgba(31, 157, 116, 0.12); }
.tv-panel { padding: 16px; border-radius: 14px; background: #fff; border: 1px solid #e6ece8; }
.tv-bar { display: flex; flex-wrap: wrap; align-items: center; gap: 12px; margin-bottom: 12px; }
.tv-muted { color: #8a968f; font-size: 12px; }
.tv-late { color: #c0392b; }
:deep(.tv-row--hl) td { background: #eef8f3 !important; }
@media (max-width: 900px) { .tv-cards { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>
