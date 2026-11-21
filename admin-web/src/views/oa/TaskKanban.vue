<template>
  <div class="tk">
    <a-alert type="info" show-icon style="margin-bottom: 12px"
      message="行政负责协同与督办：执行人在本部门完成后提交验收，由任务发起人验收；同一事件使用同一任务，不重复建单。" />
    <div class="tk-board">
      <section v-for="col in TASK_COLUMNS" :key="col.key" class="tk-col">
        <header>{{ col.label }} <span>{{ grouped[col.key].length }}</span></header>
        <div v-if="!grouped[col.key].length" class="tk-empty">暂无</div>
        <article v-for="task in grouped[col.key]" :key="String(task.id)" class="tk-card">
          <b>{{ task.title }}</b>
          <small>{{ task.assigneeName || '未分派' }}{{ task.endTime ? ` · 截止 ${dayjs(task.endTime).format('MM-DD HH:mm')}` : '' }}</small>
          <a-space v-if="col.key !== 'done'" size="small">
            <a-button v-if="col.key === 'unassigned'" size="small" @click="emit('edit', task)">去分派</a-button>
            <a-button v-if="col.key === 'doing'" size="small" type="primary" ghost @click="act(() => submitOaTask(task.id))">提交验收</a-button>
            <template v-if="col.key === 'submitted'">
              <a-button size="small" type="primary" @click="act(() => acceptOaTask(task.id, true))">验收通过</a-button>
              <a-button size="small" @click="act(() => acceptOaTask(task.id, false))">退回</a-button>
            </template>
          </a-space>
        </article>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { acceptOaTask, getOaTaskPage, submitOaTask } from '../../api/oa'
import type { OaTask } from '../../types'
import { TASK_COLUMNS, taskColumn } from './center/adminTabs'

const emit = defineEmits<{ edit: [task: OaTask]; changed: [] }>()
const tasks = ref<OaTask[]>([])
const grouped = computed(() => {
  const groups: Record<string, OaTask[]> = { unassigned: [], doing: [], submitted: [], done: [] }
  tasks.value.forEach((task) => groups[taskColumn(task)].push(task))
  groups.done = groups.done.slice(0, 20)
  return groups
})

async function load() {
  // shortcut: 看板一次取最近 200 条，任务量更大时改为按列分页
  const page = await getOaTaskPage({ pageNo: 1, pageSize: 200 })
  tasks.value = page.list || []
}

async function act(fn: () => Promise<unknown>) {
  await fn()
  message.success('已更新')
  emit('changed')
  await load()
}

defineExpose({ load })
onMounted(load)
</script>

<style scoped>
.tk-board { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 12px; }
.tk-col { padding: 10px; border-radius: 10px; background: #f5f8f6; min-height: 200px; }
.tk-col header { display: flex; justify-content: space-between; margin-bottom: 8px; font-weight: 600; color: #33433a; }
.tk-col header span { color: #6b7a74; font-weight: 400; }
.tk-card { display: grid; gap: 6px; margin-bottom: 8px; padding: 10px; border: 1px solid #e3ebe6; border-radius: 8px; background: #fff; }
.tk-card small { color: #6b7a74; }
.tk-empty { padding: 16px 0; color: #9aa59f; text-align: center; font-size: 12px; }
@media (max-width: 1100px) { .tk-board { grid-template-columns: 1fr 1fr; } }
</style>
