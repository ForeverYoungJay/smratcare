<template>
  <div class="fh">
    <div class="rm-kpis fh-kpis">
      <div class="rm-kpi"><span class="rm-kpi__label">待整改隐患</span><span class="rm-kpi__value">{{ total }}<small>项</small></span><span class="rm-kpi__hint">日巡、夜巡、月检、设施、值班登记的问题</span></div>
      <div class="rm-kpi"><span class="rm-kpi__label">已逾期</span><span class="rm-kpi__value">{{ overdueCount }}<small>项</small></span><span class="rm-kpi__hint">超过下次检查/整改期限</span></div>
      <div class="rm-kpi"><span class="rm-kpi__label">整改中</span><span class="rm-kpi__value">{{ runningCount }}<small>项</small></span><span class="rm-kpi__hint">已开始处置、待复核关闭</span></div>
    </div>

    <div class="rm-split fh-grid">
      <section class="rm-card">
        <div class="rm-card__head">
          <span class="rm-card__title">隐患清单 · 从发现到关闭</span>
          <a-space>
            <a-select v-model:value="recordType" :options="typeOptions" allow-clear placeholder="全部来源" style="width: 140px" @change="load" />
            <a-input-search v-model:value="keyword" placeholder="位置 / 问题 / 责任人" style="width: 200px" @search="load" />
          </a-space>
        </div>
        <a-table size="small" row-key="id" :data-source="rows" :columns="columns" :loading="loading" :pagination="pagination"
          :custom-row="(record: FireSafetyRecord) => ({ onClick: () => (selectedId = record.id), class: record.id === selected?.id ? 'fh-row--active' : '' })"
          @change="(p: { current?: number }) => { pageNo = p.current || 1; load() }">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'source'">{{ RECORD_TYPE_LABEL[record.recordType as FireSafetyRecordType] }}</template>
            <template v-else-if="column.key === 'issue'"><b>{{ record.location || record.title }}</b> · {{ record.issueDescription }}</template>
            <template v-else-if="column.key === 'due'">{{ record.nextCheckDate || '--' }}</template>
            <template v-else-if="column.key === 'status'">
              <span class="rm-pill" :class="isOverdue(record) ? 'rm-pill--danger' : 'rm-pill--warning'">{{ isOverdue(record) ? '已逾期' : record.status === 'RUNNING' ? '整改中' : '待整改' }}</span>
            </template>
          </template>
        </a-table>
      </section>

      <section class="rm-card">
        <div class="rm-card__head"><span class="rm-card__title">隐患跟踪{{ selected ? ` · ${selected.title}` : '' }}</span></div>
        <div v-if="!selected" class="fh-empty">点击左侧隐患查看处置进度</div>
        <template v-else>
          <div class="fh-detail">
            <b>{{ selected.location || selected.title }}</b>
            <span>来源：{{ RECORD_TYPE_LABEL[selected.recordType] }} · 责任人：{{ selected.inspectorName }}</span>
            <span>发现时间：{{ selected.checkTime ? dayjs(selected.checkTime).format('YYYY-MM-DD HH:mm') : '--' }}</span>
            <span>整改期限：{{ selected.nextCheckDate || '未设置' }}</span>
          </div>
          <a-timeline class="fh-timeline">
            <a-timeline-item color="red">发现问题：{{ selected.issueDescription }}</a-timeline-item>
            <a-timeline-item :color="selected.actionTaken ? 'green' : 'gray'">处置措施：{{ selected.actionTaken || '尚未填写' }}</a-timeline-item>
            <a-timeline-item color="gray">复核关闭：{{ canClose ? '由主管确认后关闭' : '由消防主管复核后关闭' }}</a-timeline-item>
          </a-timeline>
          <a-space wrap>
            <a-button type="primary" @click="router.push(RECORD_TYPE_PATH[selected.recordType])">去原记录登记处置</a-button>
            <a-tooltip :title="selected.actionTaken ? '' : '先在原记录填写处置措施和整改结果'">
              <a-button v-if="canClose" :disabled="!selected.actionTaken" :loading="closing" @click="close(selected)">复核并关闭</a-button>
            </a-tooltip>
          </a-space>
        </template>
      </section>
    </div>

    <section class="rm-card fh-emergency">
      <div class="rm-card__head"><span class="rm-card__title">消防应急快速参考</span><span class="rm-card__sub">本系统不会自动拨打 119</span></div>
      <div class="fh-steps">
        <div v-for="(step, index) in EMERGENCY_STEPS" :key="step.title"><b>{{ index + 1 }}. {{ step.title }}</b><span>{{ step.text }}</span></div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { closeFireSafetyRecord, getFireSafetyRecordPage } from '../../../api/fire'
import { getFireHazardStats, type FireHazardStats } from '../../../api/fireOps'
import { useUserStore } from '../../../stores/user'
import type { FireSafetyRecord, FireSafetyRecordType } from '../../../types'
import { EMERGENCY_STEPS, RECORD_TYPE_LABEL, RECORD_TYPE_PATH, canCloseHazard } from './fireModel'
import '../../elder/residentUi.css'

const router = useRouter()
const user = useUserStore()
const canClose = computed(() => canCloseHazard(user.roles || []))
const rows = ref<FireSafetyRecord[]>([])
const total = ref(0)
const loading = ref(false)
const closing = ref(false)
const pageNo = ref(1)
const keyword = ref('')
const recordType = ref<FireSafetyRecordType | undefined>()
const selectedId = ref<number | undefined>()
const selected = computed(() => rows.value.find((r) => r.id === selectedId.value) || rows.value[0])
const pagination = reactive({ current: computed(() => pageNo.value), pageSize: 10, total: computed(() => total.value), showSizeChanger: false })
const typeOptions = Object.entries(RECORD_TYPE_LABEL).map(([value, label]) => ({ value, label }))
const columns = [
  { title: '来源', key: 'source', width: 100 },
  { title: '问题及位置', key: 'issue' },
  { title: '责任人', dataIndex: 'inspectorName', width: 100 },
  { title: '期限', key: 'due', width: 110 },
  { title: '状态', key: 'status', width: 90 }
]
const isOverdue = (record: FireSafetyRecord) => Boolean(record.nextCheckDate && dayjs(record.nextCheckDate).isBefore(dayjs(), 'day'))
const stats = ref<FireHazardStats | null>(null)
const overdueCount = computed(() => Number(stats.value?.overdue ?? 0))
const runningCount = computed(() => Number(stats.value?.running ?? 0))

async function load() {
  loading.value = true
  try {
    const page = await getFireSafetyRecordPage({ pageNo: pageNo.value, pageSize: 10, status: 'ACTIVE', issueOnly: true, recordType: recordType.value, keyword: keyword.value || undefined })
    rows.value = page.list || []
    total.value = Number(page.total || 0)
    stats.value = await getFireHazardStats().catch(() => null)
  } finally {
    loading.value = false
  }
}

async function close(record: FireSafetyRecord) {
  closing.value = true
  try {
    await closeFireSafetyRecord(record.id)
    message.success('已复核关闭')
    await load()
  } finally {
    closing.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.fh-kpis { grid-template-columns: repeat(3, minmax(0, 1fr)); }
.fh-grid { grid-template-columns: minmax(0, 1.6fr) minmax(300px, 1fr); margin-top: 16px; }
.fh-empty { padding: 24px 8px; color: #7b877e; text-align: center; }
.fh-detail { display: grid; gap: 4px; margin-bottom: 14px; padding: 12px; border-radius: 10px; background: #fff7ef; font-size: 13px; color: #5d6b63; }
.fh-detail b { color: #2c3a33; font-size: 14px; }
.fh-timeline { margin-top: 8px; }
.fh-emergency { margin-top: 16px; }
.fh-steps { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 10px; }
.fh-steps div { display: grid; gap: 4px; padding: 12px; border: 1px solid #f1dede; border-radius: 10px; background: #fffafa; font-size: 13px; }
.fh-steps span { color: #6b7a74; }
:deep(.fh-row--active) td { background: #eef8f3 !important; }
@media (max-width: 1100px) { .fh-grid, .fh-steps { grid-template-columns: 1fr; } }
</style>
