<template>
  <PageContainer title="财务提醒中心" subTitle="下月代养费、押金未缴清、电费未登记/未缴的统一处置入口">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear placeholder="全部" style="width: 140px">
            <a-select-option value="PENDING">未处理</a-select-option>
            <a-select-option value="HANDLED">已处理</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="类型">
          <a-select v-model:value="query.reminderType" allow-clear placeholder="全部类型" style="width: 190px">
            <a-select-option value="NEXT_MONTH_CARE_FEE">下月代养费</a-select-option>
            <a-select-option value="DEPOSIT_SHORTFALL">押金未缴清</a-select-option>
            <a-select-option value="ELECTRICITY_UNRECORDED">电费未登记</a-select-option>
            <a-select-option value="ELECTRICITY_UNPAID">电费未缴</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="runSearch">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button :loading="generating" @click="doGenerate">立即检查生成</a-button>
            <a-button
              danger
              :disabled="!Number(summary?.pendingCount || 0)"
              @click="confirmHandleAll"
            >一键全部处理</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col :xs="24" :md="12" :xl="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic
            title="未处理提醒"
            :value="Number(summary?.pendingCount || 0)"
            :value-style="Number(summary?.pendingCount || 0) > 0 ? { color: '#cf1322' } : undefined"
            suffix="条"
          />
        </a-card>
      </a-col>
      <a-col v-for="item in typeCards" :key="item.type" :xs="24" :md="12" :xl="6">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic :title="item.label" :value="item.count" suffix="条" />
          <a-button
            v-if="item.count > 0"
            type="link"
            size="small"
            style="padding: 0"
            @click="handleAllOfType(item.type, item.label)"
          >处理该类全部</a-button>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table
        border
        stripe
        show-overflow="title"
        :loading="loading"
        :data="rows"
        height="520"
        :row-class-name="rowClassName"
      >
        <vxe-column field="createTime" title="生成时间" width="170" />
        <vxe-column field="reminderTypeText" title="类型" width="130" />
        <vxe-column field="title" title="提醒" min-width="230" />
        <vxe-column field="content" title="说明" min-width="280" />
        <vxe-column field="bizMonth" title="账期" width="100" />
        <vxe-column field="amount" title="金额 / 数量" width="120">
          <template #default="{ row }">{{ row.amount == null ? '-' : Number(row.amount).toFixed(2) }}</template>
        </vxe-column>
        <vxe-column field="statusText" title="状态" width="100">
          <template #default="{ row }">
            <a-tag :color="row.status === 'HANDLED' ? 'green' : 'red'">{{ row.statusText }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="180" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button v-if="row.actionPath" type="link" size="small" @click="goAction(row)">去处理</a-button>
              <a-button
                v-if="row.status !== 'HANDLED'"
                type="link"
                size="small"
                @click="markHandled(row)"
              >标记已处理</a-button>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>
      <div class="pager-row">
        <a-pagination
          :current="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </div>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import {
  generateReminders,
  getReminderPage,
  getReminderSummary,
  handleAllReminders,
  handleReminder,
  type FinanceReminderItem,
  type FinanceReminderSummary
} from '../../api/financeReminder'
import type { PageResult } from '../../types'

const router = useRouter()
const loading = ref(false)
const generating = ref(false)
const rows = ref<FinanceReminderItem[]>([])
const total = ref(0)
const summary = ref<FinanceReminderSummary | null>(null)
const query = reactive({
  status: 'PENDING' as string | undefined,
  reminderType: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const TYPE_LABELS: Array<{ type: string; label: string }> = [
  { type: 'NEXT_MONTH_CARE_FEE', label: '下月代养费' },
  { type: 'DEPOSIT_SHORTFALL', label: '押金未缴清' },
  { type: 'ELECTRICITY_UNPAID', label: '电费未缴' }
]

const typeCards = computed(() =>
  TYPE_LABELS.map((item) => ({
    ...item,
    count: Number(summary.value?.pendingByType?.[item.type] || 0)
  }))
)

function rowClassName({ row }: { row: FinanceReminderItem }) {
  return row?.status === 'HANDLED' ? '' : 'is-pending-row'
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<FinanceReminderItem> = await getReminderPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status || undefined,
      reminderType: query.reminderType || undefined
    })
    rows.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

async function fetchSummary() {
  summary.value = await getReminderSummary()
}

function runSearch() {
  query.pageNo = 1
  fetchData()
  fetchSummary()
}

function reset() {
  query.status = 'PENDING'
  query.reminderType = undefined
  runSearch()
}

function onPageChange(page: number, pageSize: number) {
  query.pageNo = page
  query.pageSize = pageSize
  fetchData()
}

function onPageSizeChange(_current: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

async function doGenerate() {
  generating.value = true
  try {
    const res = await generateReminders()
    const created = Number(res?.careFee || 0) + Number(res?.deposit || 0) + Number(res?.electricity || 0)
    message.success(created > 0 ? `新增 ${created} 条提醒` : '没有新的提醒需要生成')
    await Promise.all([fetchData(), fetchSummary()])
  } finally {
    generating.value = false
  }
}

async function markHandled(row: FinanceReminderItem) {
  await handleReminder(row.id)
  message.success('已标记处理')
  await Promise.all([fetchData(), fetchSummary()])
}

function confirmHandleAll() {
  Modal.confirm({
    title: `一键处理全部 ${Number(summary.value?.pendingCount || 0)} 条未处理提醒？`,
    content: '标记处理只改变提醒状态，不会自动完成收款、抄表等实际动作。',
    onOk: async () => {
      const res = await handleAllReminders(undefined, '一键全部处理')
      message.success(`已处理 ${res?.handled || 0} 条`)
      await Promise.all([fetchData(), fetchSummary()])
    }
  })
}

function handleAllOfType(type: string, label: string) {
  Modal.confirm({
    title: `处理「${label}」的全部未处理提醒？`,
    onOk: async () => {
      const res = await handleAllReminders(type, `一键处理${label}`)
      message.success(`已处理 ${res?.handled || 0} 条`)
      await Promise.all([fetchData(), fetchSummary()])
    }
  })
}

function goAction(row: FinanceReminderItem) {
  if (!row.actionPath) return
  router.push(row.actionPath)
}

onMounted(async () => {
  await Promise.all([fetchData(), fetchSummary()])
})
</script>

<style scoped>
.pager-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

:deep(.vxe-body--row.is-pending-row) td {
  background-color: rgba(207, 19, 34, 0.05) !important;
}
</style>
