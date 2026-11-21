<template>
  <a-modal v-model:open="transferOpen" :title="`转交待办 · ${target?.title || ''}`" :confirm-loading="busy" ok-text="确认转交" @ok="submitTransfer">
    <a-form layout="vertical">
      <a-form-item label="转交给" required>
        <a-select v-model:value="staffId" show-search :filter-option="false" :options="staffOptions" :loading="staffLoading"
          placeholder="搜索同事姓名" @search="searchStaff" @focus="() => !staffOptions.length && searchStaff('')" />
      </a-form-item>
      <a-form-item label="转交说明"><a-textarea v-model:value="note" :rows="2" placeholder="如：今天休息，请代为处理" /></a-form-item>
    </a-form>
    <p class="tf-muted">转交会通知新负责人并留下记录；原业务的审批权限不会因转交而改变。</p>
  </a-modal>

  <a-drawer v-model:open="historyOpen" :title="`处理历史 · ${target?.title || ''}`" :width="420">
    <a-empty v-if="!history.length" description="暂无转交、催办记录" />
    <a-timeline>
      <a-timeline-item v-for="(h, i) in history" :key="i" :color="h.action === 'URGE' ? 'orange' : 'blue'">
        <b>{{ ACTION[h.action] || h.action }}</b> · {{ h.actor }} · {{ dayjs(h.time).format('MM-DD HH:mm') }}
        <div class="tf-muted">{{ h.detail }}</div>
      </a-timeline-item>
    </a-timeline>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { getTodoHistory, transferTodo, urgeTodo } from '../../api/oa'
import type { OaTodo } from '../../types'
import { useStaffOptions } from '../../composables/useStaffOptions'

const emit = defineEmits<{ changed: [] }>()
const ACTION: Record<string, string> = { TRANSFER: '转交', URGE: '催办' }
const { staffOptions, staffLoading, searchStaff } = useStaffOptions({ pageSize: 50 })
const target = ref<OaTodo>()
const transferOpen = ref(false)
const historyOpen = ref(false)
const busy = ref(false)
const staffId = ref<string>()
const note = ref('')
const history = ref<Awaited<ReturnType<typeof getTodoHistory>>>([])

function openTransfer(todo: OaTodo) {
  target.value = todo
  staffId.value = undefined
  note.value = ''
  transferOpen.value = true
}

async function submitTransfer() {
  if (!staffId.value) return message.warning('请选择转交对象')
  busy.value = true
  try {
    await transferTodo(target.value!.id, { staffId: staffId.value, note: note.value || undefined })
    message.success('已转交并通知对方')
    transferOpen.value = false
    emit('changed')
  } finally {
    busy.value = false
  }
}

async function urge(todo: OaTodo) {
  await urgeTodo(todo.id)
  message.success('已催办，负责人会收到提醒')
}

async function openHistory(todo: OaTodo) {
  target.value = todo
  historyOpen.value = true
  history.value = await getTodoHistory(todo.id)
}

defineExpose({ openTransfer, urge, openHistory })
</script>

<style scoped>
.tf-muted { color: #8a968f; font-size: 12px; }
</style>
