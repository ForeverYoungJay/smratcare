<template>
  <a-drawer :open="open" title="从聊天创建待办" :width="480" @close="close">
    <p class="cd-muted">把对话转为正式工作事项，保留来源并进入统一任务中心（我的待办、协同任务同一条记录）；负责人不会因此看到整段群聊。</p>
    <div v-if="message" class="cd-source">
      <b>{{ message.senderName }} · {{ dayjs(message.createTime).format('HH:mm') }}{{ conversationTitle ? ` · ${conversationTitle}` : '' }}</b>
      <span>{{ message.body }}</span>
    </div>
    <a-alert v-if="duplicateIds.length" type="warning" show-icon style="margin-bottom: 12px"
      :message="`这条消息已转过 ${duplicateIds.length} 个任务（#${duplicateIds.join('、#')}）`"
      description="确认需要另建一张时勾选下方选项，否则请直接跟进已有任务。" />
    <a-form layout="vertical">
      <a-form-item label="任务标题" required><a-input v-model:value="form.title" :maxlength="100" /></a-form-item>
      <a-form-item label="任务类型" required>
        <a-select v-model:value="form.planCategory" :options="TYPES" />
      </a-form-item>
      <a-form-item label="负责人" required>
        <a-select v-model:value="form.assigneeId" show-search :filter-option="false" :options="staffOptions" :loading="staffLoading"
          placeholder="搜索同事姓名" @search="searchStaff" @focus="() => !staffOptions.length && searchStaff('')" />
      </a-form-item>
      <a-row :gutter="12">
        <a-col :span="14"><a-form-item label="截止时间"><a-date-picker v-model:value="form.endTime" show-time value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" /></a-form-item></a-col>
        <a-col :span="10"><a-form-item label="优先级"><a-select v-model:value="form.priority" :options="PRIORITIES" /></a-form-item></a-col>
      </a-row>
      <a-form-item label="任务说明"><a-textarea v-model:value="form.description" :rows="3" :maxlength="500" /></a-form-item>
      <a-checkbox v-if="duplicateIds.length" v-model:checked="confirmDuplicate">我确认需要再建一张新任务</a-checkbox>
    </a-form>
    <a-alert type="info" style="margin-top: 12px" message="紧急情况（跌倒、急救、火警）请立即执行应急预案，不要等待聊天待办。" />
    <template #footer>
      <a-space style="float: right">
        <a-button @click="close">取消</a-button>
        <a-button type="primary" :loading="saving" @click="submit">创建任务</a-button>
      </a-space>
    </template>
  </a-drawer>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { message as toast } from 'ant-design-vue'
import { convertToTask, type ChatMessage } from '../../api/chat'
import { useStaffOptions } from '../../composables/useStaffOptions'

const props = defineProps<{ open: boolean; message?: ChatMessage; conversationTitle?: string }>()
const emit = defineEmits<{ 'update:open': [value: boolean]; created: [taskId: string] }>()

// 任务类型只是分类标签，正式办理仍在 oa_task；需要正式维修/护理单据时由负责人在原业务系统建单
const TYPES = ['后勤维修', '护理事项', '医护协同', '行政事项', '物资申领', '其他'].map((v) => ({ value: v, label: v }))
const PRIORITIES = [{ value: 'HIGH', label: '高' }, { value: 'NORMAL', label: '中' }, { value: 'LOW', label: '低' }]

const { staffOptions, staffLoading, searchStaff } = useStaffOptions({ pageSize: 50 })
const saving = ref(false)
const duplicateIds = ref<string[]>([])
const confirmDuplicate = ref(false)
const form = reactive({ title: '', planCategory: '其他', assigneeId: undefined as string | undefined, endTime: undefined as string | undefined, priority: 'NORMAL', description: '' })

watch(() => [props.open, props.message?.id] as const, ([open]) => {
  if (!open || !props.message) return
  Object.assign(form, { title: (props.message.body || '').slice(0, 30), planCategory: '其他', assigneeId: undefined, endTime: undefined, priority: 'NORMAL', description: props.message.body || '' })
  duplicateIds.value = []
  confirmDuplicate.value = false
})

function close() {
  emit('update:open', false)
}

async function submit() {
  if (!props.message) return
  if (!form.title.trim()) return toast.warning('请填写任务标题')
  if (!form.assigneeId) return toast.warning('请选择负责人')
  if (duplicateIds.value.length && !confirmDuplicate.value) return toast.warning('请确认是否需要再建一张任务')
  saving.value = true
  try {
    const result = await convertToTask(props.message.id, { ...form, title: form.title.trim(), assigneeId: form.assigneeId, confirmDuplicate: confirmDuplicate.value })
    if (result.duplicate) {
      duplicateIds.value = result.existingTaskIds || []
      return
    }
    toast.success(`已创建任务 #${result.taskId}，并写回会话`)
    emit('created', String(result.taskId))
    close()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.cd-muted { color: #7b877e; font-size: 12px; }
.cd-source { display: grid; gap: 6px; margin-bottom: 16px; padding: 12px 14px; border-left: 3px solid #1f9d74; border-radius: 8px; background: #f0f9f3; font-size: 13px; }
.cd-source b { color: #1f6f55; font-weight: 600; }
</style>
