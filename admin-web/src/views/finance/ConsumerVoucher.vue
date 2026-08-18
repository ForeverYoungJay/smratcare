<template>
  <PageContainer title="消费券管理" subTitle="发放、作废与核销追溯；收款登记时按剩余额度抵扣应收">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="长者">
          <a-select
            v-model:value="query.elderId"
            show-search
            allow-clear
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="按长者筛选"
            style="width: 220px"
            @search="searchElders"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear placeholder="全部状态" style="width: 140px">
            <a-select-option value="ACTIVE">可用</a-select-option>
            <a-select-option value="USED">已用完</a-select-option>
            <a-select-option value="EXPIRED">已过期</a-select-option>
            <a-select-option value="REVOKED">已作废</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" allow-clear placeholder="券号 / 券名称" style="width: 200px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="runSearch">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button type="primary" ghost @click="openIssue">发放消费券</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col :xs="24" :xl="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="可用券张数" :value="summary.activeCount" />
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="可用剩余额度" :value="summary.activeBalance" :precision="2" suffix="元" />
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="8">
        <a-card class="card-elevated" :bordered="false">
          <a-statistic title="本页面额合计" :value="summary.faceTotal" :precision="2" suffix="元" />
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table border stripe show-overflow="title" :loading="loading" :data="rows" height="520">
        <vxe-column field="voucherNo" title="券号" width="180" />
        <vxe-column field="voucherName" title="券名称" min-width="160" />
        <vxe-column field="elderName" title="绑定长者" width="130">
          <template #default="{ row }">{{ row.elderName || '通用券' }}</template>
        </vxe-column>
        <vxe-column field="faceAmount" title="面额" width="110">
          <template #default="{ row }">{{ formatAmount(row.faceAmount) }}</template>
        </vxe-column>
        <vxe-column field="balanceAmount" title="剩余额度" width="110">
          <template #default="{ row }">{{ formatAmount(row.balanceAmount) }}</template>
        </vxe-column>
        <vxe-column field="minBillAmount" title="使用门槛" width="110">
          <template #default="{ row }">{{ Number(row.minBillAmount || 0) > 0 ? `满 ${formatAmount(row.minBillAmount)}` : '无' }}</template>
        </vxe-column>
        <vxe-column field="validTo" title="有效期" width="190">
          <template #default="{ row }">{{ validityText(row) }}</template>
        </vxe-column>
        <vxe-column field="statusText" title="状态" width="100">
          <template #default="{ row }">
            <a-tag :color="statusColor(row.status)">{{ row.statusText || row.status }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="remark" title="备注" min-width="140" />
        <vxe-column title="操作" width="150" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button type="link" size="small" @click="openUsage(row)">核销流水</a-button>
              <a-button
                v-if="row.status === 'ACTIVE'"
                type="link"
                size="small"
                danger
                @click="confirmRevoke(row)"
              >作废</a-button>
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

    <a-modal v-model:open="issueOpen" title="发放消费券" width="620" :confirm-loading="issuing" @ok="submitIssue">
      <a-form layout="vertical" :model="issueForm" :rules="issueRules" ref="issueFormRef">
        <a-form-item label="券名称" name="voucherName">
          <a-input v-model:value="issueForm.voucherName" placeholder="如：中秋关怀券" />
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="面额（元）" name="faceAmount">
              <a-input-number v-model:value="issueForm.faceAmount" :min="0.01" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="使用门槛（账单应收满，0 为不限）">
              <a-input-number v-model:value="issueForm.minBillAmount" :min="0" :precision="2" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="有效期">
          <a-range-picker v-model:value="issueForm.validRange" style="width: 100%" />
        </a-form-item>
        <a-form-item label="允许拆分使用">
          <a-switch v-model:checked="issueForm.allowSplit" />
          <span class="form-tip">关闭后该券必须一次用完剩余额度</span>
        </a-form-item>
        <a-form-item label="发放对象">
          <a-select
            v-model:value="issueForm.elderIds"
            mode="multiple"
            show-search
            allow-clear
            :filter-option="false"
            :options="elderOptions"
            :loading="elderLoading"
            placeholder="留空则发放为机构通用券，可用于任意长者账单"
            style="width: 100%"
            @search="searchElders"
          />
          <span class="form-tip">选中 N 位长者会各自生成 1 张独立券号的券</span>
        </a-form-item>
        <a-row :gutter="12">
          <a-col :span="12">
            <a-form-item label="发放来源">
              <a-input v-model:value="issueForm.source" placeholder="如：营销活动 / 服务补偿" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="备注">
              <a-input v-model:value="issueForm.remark" />
            </a-form-item>
          </a-col>
        </a-row>
      </a-form>
    </a-modal>

    <a-drawer v-model:open="usageOpen" width="720" :title="`核销流水 · ${activeVoucher?.voucherNo || ''}`">
      <vxe-table border stripe show-overflow="title" :loading="usageLoading" :data="usageRows" height="520">
        <vxe-column field="createTime" title="时间" width="180" />
        <vxe-column field="directionText" title="方向" width="100" />
        <vxe-column field="amount" title="金额" width="110">
          <template #default="{ row }">{{ formatAmount(row.amount) }}</template>
        </vxe-column>
        <vxe-column field="elderName" title="长者" width="120" />
        <vxe-column field="billMonth" title="账单月份" width="120" />
        <vxe-column field="paymentRecordId" title="收款记录" width="180" />
        <vxe-column field="remark" title="备注" min-width="140" />
      </vxe-table>
      <a-empty v-if="!usageLoading && !usageRows.length" description="该券还没有核销记录" />
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import {
  getVoucherPage,
  getVoucherUsage,
  issueVouchers,
  revokeVoucher
} from '../../api/financeVoucher'
import type { FinanceVoucherItem, FinanceVoucherUsageItem, Id, PageResult } from '../../types'

const { elderOptions, elderLoading, searchElders } = useElderOptions({ pageSize: 80 })

const loading = ref(false)
const rows = ref<FinanceVoucherItem[]>([])
const total = ref(0)
const query = reactive({
  pageNo: 1,
  pageSize: 10,
  elderId: undefined as Id | undefined,
  status: undefined as string | undefined,
  keyword: ''
})

const summary = computed(() => {
  const active = rows.value.filter((item) => item.status === 'ACTIVE')
  return {
    activeCount: active.length,
    activeBalance: active.reduce((sum, item) => sum + Number(item.balanceAmount || 0), 0),
    faceTotal: rows.value.reduce((sum, item) => sum + Number(item.faceAmount || 0), 0)
  }
})

const issueOpen = ref(false)
const issuing = ref(false)
const issueFormRef = ref()
const issueForm = reactive({
  voucherName: '',
  faceAmount: undefined as number | undefined,
  minBillAmount: 0,
  allowSplit: true,
  validRange: undefined as any,
  source: '',
  remark: '',
  elderIds: [] as Id[]
})
const issueRules = {
  voucherName: [{ required: true, message: '请填写券名称' }],
  faceAmount: [{ required: true, message: '请填写券面额' }]
}

const usageOpen = ref(false)
const usageLoading = ref(false)
const usageRows = ref<FinanceVoucherUsageItem[]>([])
const activeVoucher = ref<FinanceVoucherItem | null>(null)

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<FinanceVoucherItem> = await getVoucherPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      elderId: query.elderId || undefined,
      status: query.status || undefined,
      keyword: query.keyword.trim() || undefined
    })
    rows.value = res.list || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

function runSearch() {
  query.pageNo = 1
  fetchData()
}

function reset() {
  query.pageNo = 1
  query.elderId = undefined
  query.status = undefined
  query.keyword = ''
  fetchData()
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

function openIssue() {
  issueForm.voucherName = ''
  issueForm.faceAmount = undefined
  issueForm.minBillAmount = 0
  issueForm.allowSplit = true
  issueForm.validRange = undefined
  issueForm.source = ''
  issueForm.remark = ''
  issueForm.elderIds = []
  issueOpen.value = true
}

async function submitIssue() {
  if (issuing.value) return
  await issueFormRef.value?.validate?.()
  issuing.value = true
  try {
    const range = issueForm.validRange || []
    const created = await issueVouchers({
      voucherName: issueForm.voucherName.trim(),
      faceAmount: Number(issueForm.faceAmount || 0),
      minBillAmount: Number(issueForm.minBillAmount || 0),
      allowSplit: issueForm.allowSplit,
      validFrom: range[0] ? dayjs(range[0]).format('YYYY-MM-DD') : undefined,
      validTo: range[1] ? dayjs(range[1]).format('YYYY-MM-DD') : undefined,
      source: issueForm.source.trim() || undefined,
      remark: issueForm.remark.trim() || undefined,
      elderIds: issueForm.elderIds.length ? issueForm.elderIds : undefined
    })
    message.success(`已发放 ${created?.length || 0} 张消费券`)
    issueOpen.value = false
    runSearch()
  } finally {
    issuing.value = false
  }
}

function confirmRevoke(row: FinanceVoucherItem) {
  Modal.confirm({
    title: `作废消费券 ${row.voucherNo}？`,
    content: '作废后不可再用于收款抵扣。已部分核销的券需先冲正相关收款。',
    okText: '确认作废',
    okType: 'danger',
    onOk: async () => {
      await revokeVoucher(row.id, '管理端手工作废')
      message.success('已作废')
      fetchData()
    }
  })
}

async function openUsage(row: FinanceVoucherItem) {
  activeVoucher.value = row
  usageOpen.value = true
  usageLoading.value = true
  try {
    usageRows.value = await getVoucherUsage(row.id)
  } finally {
    usageLoading.value = false
  }
}

function validityText(row: FinanceVoucherItem) {
  if (!row.validFrom && !row.validTo) return '长期有效'
  return `${row.validFrom || '不限'} ~ ${row.validTo || '不限'}`
}

function statusColor(status?: string) {
  switch (status) {
    case 'ACTIVE':
      return 'green'
    case 'USED':
      return 'blue'
    case 'EXPIRED':
      return 'orange'
    case 'REVOKED':
      return 'red'
    default:
      return 'default'
  }
}

function formatAmount(value?: number | string) {
  return Number(value || 0).toFixed(2)
}

onMounted(fetchData)
</script>

<style scoped>
.pager-row {
  display: flex;
  justify-content: flex-end;
  margin-top: 12px;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: var(--text-tertiary, #999);
}
</style>
