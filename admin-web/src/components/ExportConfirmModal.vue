<template>
  <a-modal
    :open="open"
    title="导出二次确认"
    :confirm-loading="submitting"
    ok-text="确认并导出"
    cancel-text="取消"
    @ok="onOk"
    @cancel="onCancel"
  >
    <a-alert
      type="warning"
      show-icon
      style="margin-bottom: 16px"
      message="导出内容可能包含敏感个人信息"
      description="导出行为将被系统留痕审计（操作人 / 时间 / 模块 / 范围 / 行数）。请确认导出用途合规，并妥善保管导出文件。"
    />
    <a-descriptions :column="1" size="small" style="margin-bottom: 12px">
      <a-descriptions-item label="导出模块">{{ moduleLabel || module }}</a-descriptions-item>
      <a-descriptions-item v-if="scope" label="导出范围">{{ scope }}</a-descriptions-item>
      <a-descriptions-item v-if="estimatedRows != null" label="预计行数">{{ estimatedRows }}</a-descriptions-item>
    </a-descriptions>
    <a-form layout="vertical">
      <a-form-item label="导出用途" required :help="purposeError" :validate-status="purposeError ? 'error' : undefined">
        <a-input
          v-model:value="purpose"
          :maxlength="100"
          placeholder="例如：民政月报 / 内部核对（必填，将记入审计）"
          @change="purposeError = ''"
        />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { confirmExport } from '../api/complianceSecurity'

// 导出二次确认弹窗：调 /api/compliance/export/confirm 签发一次性 exportTicket，
// 确认成功后通过 confirmed 事件把 ticket 交给调用方执行实际导出，
// 导出完成后调用方应调 completeExport({ exportTicket, rowCount }) 回填行数。
const props = defineProps<{
  open: boolean
  /** 导出模块标识（记入审计），如 ELDER_LIST */
  module: string
  /** 模块中文名（展示用） */
  moduleLabel?: string
  /** 导出范围描述（筛选条件等，记入审计） */
  scope?: string
  /** 预计导出行数 */
  estimatedRows?: number
}>()

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'confirmed', exportTicket: string): void
}>()

const submitting = ref(false)
const purpose = ref('')
const purposeError = ref('')

async function onOk() {
  if (!purpose.value.trim()) {
    purposeError.value = '请填写导出用途'
    return
  }
  submitting.value = true
  try {
    const ticket = await confirmExport({
      module: props.module,
      scope: props.scope,
      purpose: purpose.value.trim(),
      estimatedRows: props.estimatedRows
    })
    emit('update:open', false)
    emit('confirmed', ticket.exportTicket)
    purpose.value = ''
  } catch (e) {
    message.error('导出确认失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function onCancel() {
  purpose.value = ''
  purposeError.value = ''
  emit('update:open', false)
}
</script>
