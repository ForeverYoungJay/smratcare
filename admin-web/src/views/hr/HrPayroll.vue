<template>
  <PageContainer title="工资批次" sub-title="人事核算草稿 → 主管审核 → 财务提交银行 → 凭回执登记付款成功；核算人不能审核自己的批次">
    <template #extra>
      <a-space v-if="canPrepare">
        <a-date-picker v-model:value="newPeriod" picker="month" value-format="YYYY-MM" placeholder="工资期间" />
        <a-button type="primary" :disabled="!newPeriod" :loading="busy" @click="create">生成工资草稿</a-button>
      </a-space>
    </template>
    <a-alert type="warning" show-icon style="margin-bottom: 12px"
      message="“已审核”“已提交银行”“付款成功”是三个不同状态；付款成功须由财务凭银行回执登记，系统不会模拟付款。绩效、积分、宿舍费用不会自动扣工资。" />
    <a-table size="small" row-key="id" :data-source="batches" :columns="columns" :loading="loading" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="PAYROLL_STATUS[record.status]?.color">{{ PAYROLL_STATUS[record.status]?.label || record.status }}</a-tag>
          <div v-if="record.status === 'DRAFT' && record.rejectReason" class="hp-muted">退回：{{ record.rejectReason }}</div>
        </template>
        <template v-else-if="column.key === 'amount'">¥{{ money(record.totalNet) }} <span class="hp-muted">/ 应发 ¥{{ money(record.totalGross) }}</span></template>
        <template v-else-if="column.key === 'trail'">
          <div class="hp-muted">核算 {{ record.preparedByName || '-' }}{{ record.approvedByName ? ` · 审核 ${record.approvedByName}` : '' }}</div>
          <div v-if="record.paymentRef" class="hp-muted">回执 {{ record.paymentRef }}</div>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space size="small">
            <a @click="openLines(record)">工资单</a>
            <a v-for="item in payrollActions(record.status, roles)" :key="item.action" :class="{ 'hp-danger': item.danger }" @click="openAction(record, item)">{{ item.label }}</a>
          </a-space>
        </template>
      </template>
    </a-table>

    <a-drawer v-model:open="linesOpen" :title="`${current?.period || ''} 工资单 · ${PAYROLL_STATUS[current?.status || '']?.label || ''}`" width="92%">
      <a-table size="small" row-key="id" :data-source="lines" :columns="lineColumns" :pagination="{ pageSize: 20 }">
        <template #bodyCell="{ column, record }">
          <template v-if="AMOUNT_KEYS.includes(column.key)">
            <a-input-number v-if="editable" v-model:value="record[column.key]" :min="0" :precision="2" size="small" style="width: 100px" @change="dirty.add(record.id)" />
            <span v-else>{{ money(record[column.key]) }}</span>
          </template>
          <template v-else-if="column.key === 'net'"><b>{{ money(editable ? previewNet(record).net : record.netAmount) }}</b></template>
          <template v-else-if="column.key === 'save'">
            <a-button v-if="editable && dirty.has(record.id)" size="small" type="primary" @click="saveLine(record)">保存</a-button>
          </template>
        </template>
      </a-table>
      <p class="hp-muted">实发 = 基本 + 绩效 + 津补贴 + 加班 − 扣款 − 个人负担（社保公积金个税）；加班须已审批，扣款须有制度依据。只有核算草稿可修改。</p>
    </a-drawer>

    <a-modal v-model:open="actionOpen" :title="`${pending?.label || ''} · ${current?.period || ''}`" :confirm-loading="busy" @ok="submitAction">
      <a-input v-if="pending?.needRef" v-model:value="actionRef" placeholder="银行付款回执号 / 流水号（必填）" />
      <a-textarea v-else-if="pending?.needNote" v-model:value="actionNote" :rows="2" placeholder="退回原因（必填）" />
      <p v-else>确认{{ pending?.label }}？</p>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { createPayrollBatch, getPayrollBatches, getPayrollLines, payrollAction, updatePayrollLine, type PayrollBatch, type PayrollLine } from '../../api/hrOps'
import { useUserStore } from '../../stores/user'
import { PAYROLL_STATUS, payrollActions, previewNet } from './hrPayroll'

const AMOUNT_KEYS = ['baseAmount', 'performanceAmount', 'allowanceAmount', 'overtimeAmount', 'deductionAmount', 'personalAmount']
const columns = [
  { title: '工资期间', dataIndex: 'period', width: 100 }, { title: '状态', key: 'status', width: 170 },
  { title: '人数', dataIndex: 'headcount', width: 70 }, { title: '实发合计', key: 'amount', width: 220 },
  { title: '经办', key: 'trail' }, { title: '', key: 'action', width: 220 }
]
const lineColumns = [
  { title: '员工', dataIndex: 'staffName', width: 100 }, { title: '基本工资', key: 'baseAmount' }, { title: '绩效', key: 'performanceAmount' },
  { title: '津补贴', key: 'allowanceAmount' }, { title: '加班', key: 'overtimeAmount' }, { title: '扣款', key: 'deductionAmount' },
  { title: '个人负担', key: 'personalAmount' }, { title: '实发', key: 'net', width: 100 }, { title: '', key: 'save', width: 70 }
]

const user = useUserStore()
const roles = computed(() => user.roles || [])
const canPrepare = computed(() => roles.value.some((r) => ['HR_EMPLOYEE', 'HR_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(String(r).toUpperCase())))
const batches = ref<PayrollBatch[]>([])
const loading = ref(false)
const busy = ref(false)
const newPeriod = ref<string>()
const current = ref<PayrollBatch>()
const lines = ref<PayrollLine[]>([])
const linesOpen = ref(false)
const dirty = reactive(new Set<PayrollLine['id']>())
const editable = computed(() => current.value?.status === 'DRAFT' && canPrepare.value)
type Action = ReturnType<typeof payrollActions>[number]
const actionOpen = ref(false)
const pending = ref<Action>()
const actionNote = ref('')
const actionRef = ref('')

const money = (value?: number) => Number(value || 0).toFixed(2)

async function load() {
  loading.value = true
  try {
    batches.value = (await getPayrollBatches({ pageNo: 1, pageSize: 50 })).list || []
  } finally {
    loading.value = false
  }
}
async function create() {
  busy.value = true
  try {
    await createPayrollBatch(newPeriod.value!)
    message.success('已生成工资草稿，沿用上一期的基本工资和津补贴，请逐人核对')
    await load()
  } finally {
    busy.value = false
  }
}
async function openLines(batch: PayrollBatch) {
  current.value = batch
  dirty.clear()
  linesOpen.value = true
  lines.value = await getPayrollLines(batch.id)
}
async function saveLine(line: PayrollLine) {
  const saved = await updatePayrollLine(line)
  Object.assign(line, saved)
  dirty.delete(line.id)
  message.success('已保存')
  await load()
}
function openAction(batch: PayrollBatch, item: Action) {
  current.value = batch
  pending.value = item
  actionNote.value = ''
  actionRef.value = ''
  actionOpen.value = true
}
async function submitAction() {
  if (pending.value?.needRef && !actionRef.value.trim()) return message.warning('请填写银行回执号')
  if (pending.value?.needNote && !actionNote.value.trim()) return message.warning('请填写退回原因')
  busy.value = true
  try {
    await payrollAction(current.value!.id, pending.value!.action, { note: actionNote.value || undefined, paymentRef: actionRef.value || undefined })
    message.success(`${pending.value!.label}成功`)
    actionOpen.value = false
    await load()
  } catch {
    // 请求层已提示原因（如核算人不能自审），弹窗保持打开
  } finally {
    busy.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.hp-muted { color: #7b877e; font-size: 12px; }
.hp-danger { color: #c0392b; }
</style>
