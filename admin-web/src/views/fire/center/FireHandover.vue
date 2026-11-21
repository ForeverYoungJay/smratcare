<template>
  <div class="rm-split fo-grid">
    <section class="rm-card">
      <div class="rm-card__head">
        <span class="rm-card__title">交接记录 <small class="rm-card__sub">交班人发起，接班人本人确认后生效</small></span>
        <a-radio-group v-model:value="status" size="small" @change="load">
          <a-radio-button value="">全部</a-radio-button><a-radio-button value="PENDING">待接班确认</a-radio-button>
          <a-radio-button value="CONFIRMED">已交接</a-radio-button><a-radio-button value="REJECTED">已退回</a-radio-button>
        </a-radio-group>
      </div>
      <a-table size="small" row-key="id" :data-source="rows" :columns="columns" :loading="loading" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'people'">{{ record.handoverName }} → {{ record.takeoverName }}</template>
          <template v-else-if="column.key === 'time'">{{ fmt(record.handoverAt) }}<br><small v-if="record.takeoverAt" class="fo-muted">确认 {{ fmt(record.takeoverAt) }}</small></template>
          <template v-else-if="column.key === 'status'"><span class="rm-pill" :class="STATUS[record.status as HandoverStatus].tone">{{ STATUS[record.status as HandoverStatus].label }}</span></template>
          <template v-else-if="column.key === 'action'">
            <a-space v-if="isTakeover(record)">
              <a-button type="link" size="small" @click="confirm(record, true)">确认接班</a-button>
              <a-button type="link" size="small" danger @click="openReject(record)">退回</a-button>
            </a-space>
            <a v-else class="rm-link" @click="detail = record">查看</a>
          </template>
        </template>
      </a-table>
    </section>

    <section class="rm-card">
      <div class="rm-card__head"><span class="rm-card__title">发起交班</span></div>
      <a-form layout="vertical">
        <a-form-item label="班次" required><a-input v-model:value="form.shiftName" placeholder="如：夜班 16:00—00:00" /></a-form-item>
        <a-form-item label="接班人" required>
          <a-select v-model:value="form.takeoverStaffId" show-search :filter-option="false" :options="staffOptions" placeholder="搜索姓名" @search="searchStaff" />
        </a-form-item>
        <a-form-item label="交接清单">
          <a-checkbox-group v-model:value="form.checked" class="fo-checks" :options="HANDOVER_ITEMS" />
        </a-form-item>
        <a-form-item label="未关闭故障与待处理事项"><a-textarea v-model:value="form.openIssues" :rows="3" /></a-form-item>
        <a-button type="primary" block :loading="saving" @click="submit">提交交班，等待接班人确认</a-button>
      </a-form>
      <p class="fo-muted">交班与接班人员分别确认，保留控制器状态、未解决故障和待处理事项。交班人不能替接班人确认。</p>
    </section>

    <a-modal v-model:open="rejectOpen" title="退回交接" @ok="confirm(rejectTarget!, false)">
      <a-textarea v-model:value="rejectNote" :rows="3" placeholder="写明退回原因（如清单与现场不符）" />
    </a-modal>
    <a-modal :open="!!detail" title="交接详情" :footer="null" @cancel="detail = null">
      <template v-if="detail">
        <p>{{ detail.shiftName }} · {{ detail.handoverName }} → {{ detail.takeoverName }}</p>
        <ul><li v-for="item in parseChecklist(detail.checklist)" :key="item.item">{{ item.checked ? '✓' : '✗' }} {{ item.item }}</li></ul>
        <p v-if="detail.openIssues">待处理：{{ detail.openIssues }}</p>
        <p v-if="detail.takeoverNote">接班说明：{{ detail.takeoverNote }}</p>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useUserStore } from '../../../stores/user'
import { confirmFireHandover, createFireHandover, getFireHandovers, getHandoverStaffOptions, type FireDutyHandover } from '../../../api/fireOps'
import { HANDOVER_ITEMS, buildChecklist, parseChecklist } from './fireModel'
import '../../elder/residentUi.css'

type HandoverStatus = FireDutyHandover['status']
const STATUS: Record<HandoverStatus, { label: string; tone: string }> = {
  PENDING: { label: '待接班确认', tone: 'rm-pill--warning' },
  CONFIRMED: { label: '已交接', tone: 'rm-pill--info' },
  REJECTED: { label: '已退回', tone: 'rm-pill--danger' }
}
const columns = [
  { title: '日期', dataIndex: 'shiftDate', width: 110 },
  { title: '班次', dataIndex: 'shiftName' },
  { title: '交班 → 接班', key: 'people' },
  { title: '时间', key: 'time', width: 150 },
  { title: '状态', key: 'status', width: 110 },
  { title: '', key: 'action', width: 140 }
]

const user = useUserStore()
const rows = ref<FireDutyHandover[]>([])
const loading = ref(false)
const saving = ref(false)
const status = ref('')
const staffOptions = ref<{ value: string; label: string }[]>([])
const form = reactive({ shiftName: '', takeoverStaffId: undefined as string | undefined, checked: [...HANDOVER_ITEMS], openIssues: '' })
const rejectOpen = ref(false)
const rejectNote = ref('')
const rejectTarget = ref<FireDutyHandover | null>(null)
const detail = ref<FireDutyHandover | null>(null)

const fmt = (value?: string) => (value ? dayjs(value).format('MM-DD HH:mm') : '')
const isTakeover = (row: FireDutyHandover) => row.status === 'PENDING' && String(row.takeoverStaffId) === String(user.staffInfo?.id)

async function load() {
  loading.value = true
  try {
    rows.value = (await getFireHandovers({ pageNo: 1, pageSize: 30, status: status.value || undefined })).list || []
  } finally {
    loading.value = false
  }
}

async function searchStaff(keyword?: string) {
  staffOptions.value = await getHandoverStaffOptions(keyword)
}

async function submit() {
  if (!form.shiftName.trim() || !form.takeoverStaffId) return message.warning('请填写班次并选择接班人')
  saving.value = true
  try {
    await createFireHandover({ shiftName: form.shiftName, takeoverStaffId: form.takeoverStaffId, checklist: buildChecklist(form.checked), openIssues: form.openIssues })
    message.success('已提交，等待接班人确认')
    Object.assign(form, { shiftName: '', takeoverStaffId: undefined, checked: [...HANDOVER_ITEMS], openIssues: '' })
    await load()
  } finally {
    saving.value = false
  }
}

function openReject(row: FireDutyHandover) {
  rejectTarget.value = row
  rejectNote.value = ''
  rejectOpen.value = true
}

async function confirm(row: FireDutyHandover, accept: boolean) {
  if (!accept && !rejectNote.value.trim()) return message.warning('请写明退回原因')
  await confirmFireHandover(row.id, accept, accept ? undefined : rejectNote.value)
  rejectOpen.value = false
  message.success(accept ? '已确认接班' : '已退回')
  await load()
}

onMounted(() => { load(); searchStaff() })
</script>

<style scoped>
.fo-grid { grid-template-columns: minmax(0, 1.6fr) minmax(300px, 1fr); }
.fo-checks { display: grid; gap: 6px; }
.fo-muted { margin: 10px 0 0; color: #8a968f; font-size: 12px; }
@media (max-width: 1100px) { .fo-grid { grid-template-columns: 1fr; } }
</style>
