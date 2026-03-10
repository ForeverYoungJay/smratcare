<template>
  <PageContainer title="退住结算" subTitle="预存账户抵扣、退款与结算确认">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人姓名">
        <ElderNameAutocomplete v-model:value="query.keyword" placeholder="老人姓名(编号)" width="220px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新建结算单</a-button>
      </template>
    </SearchForm>

    <a-row :gutter="[12, 12]" style="margin-bottom: 12px;">
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="结算单数" :value="rows.length" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="待退款数" :value="pendingRefundCount" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="应退款金额" :value="refundTotal" suffix="元" :precision="2" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="流程阻塞单数" :value="blockedCount" /></a-card></a-col>
      <a-col :span="24" v-if="blockedCount > 0">
        <a-alert type="warning" show-icon :message="`存在 ${blockedCount} 条退住结算流程阻塞（签字/退款未完成）`" />
      </a-col>
    </a-row>

    <DataTable
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'status'">
          <a-tag :color="record.status === 'SETTLED' ? 'green' : 'orange'">
            {{ record.status }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'reconcileStatus'">
          <a-tag :color="extractLinkage(record.remark, '自动对账:').includes('失败') ? 'red' : 'blue'">
            {{ extractLinkage(record.remark, '自动对账:') || (record.financeRefunded === 1 ? '已触发' : '待结算') }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'depositProcess'">
          <span>{{ extractLinkage(record.remark, '押金处理:') || '待结算' }}</span>
        </template>
        <template v-else-if="column.key === 'bedRelease'">
          <a-tag :color="extractLinkage(record.remark, '床位释放:').includes('已释放') ? 'green' : 'default'">
            {{ extractLinkage(record.remark, '床位释放:') || (record.financeRefunded === 1 ? '已回写' : '待回写') }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'riskLevel'">
          <a-tag :color="riskColor(record)">{{ riskText(record) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" :disabled="record.frontdeskApproved === 1" @click="approveFrontdesk(record)">
              前台签字
            </a-button>
            <a-button type="link" :disabled="record.nursingApproved === 1" @click="approveNursing(record)">
              护理部签字
            </a-button>
            <a-button
              type="link"
              :disabled="record.frontdeskApproved !== 1 || record.nursingApproved !== 1 || record.financeRefunded === 1"
              @click="confirmSettlement(record)"
            >
              财务退款
            </a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal
      v-model:open="createOpen"
      title="新建退住结算单"
      :confirm-loading="creating"
      @ok="submitCreate"
    >
      <a-form layout="vertical">
        <a-form-item label="老人" required>
          <a-select
            v-model:value="createForm.elderId"
            show-search
            placeholder="输入姓名搜索"
            :filter-option="false"
            :options="elderOptions"
            @search="searchElders"
            @focus="() => !elderOptions.length && searchElders('')"
          />
        </a-form-item>
        <a-form-item label="退住申请ID">
          <a-input-number v-model:value="createForm.dischargeApplyId" :min="1" style="width: 100%" />
        </a-form-item>
        <a-form-item label="应收金额" required>
          <a-input-number v-model:value="createForm.payableAmount" :min="0" :precision="2" style="width: 100%" />
        </a-form-item>
        <a-form-item label="费用项" required>
          <a-select v-model:value="createForm.feeItem" placeholder="请选择费用项">
            <a-select-option v-for="item in feeItemOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="退住费用设置" required>
          <a-select v-model:value="createForm.dischargeFeeConfig" placeholder="请选择退住费用设置">
            <a-select-option v-for="item in dischargeFeeConfigOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="createForm.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import ElderNameAutocomplete from '../../components/ElderNameAutocomplete.vue'
import { useElderOptions } from '../../composables/useElderOptions'
import { getBaseConfigItemList } from '../../api/baseConfig'
import { createDischargeSettlement, getDischargeSettlementPage, confirmDischargeSettlement } from '../../api/financeFee'
import type { BaseConfigItem, DischargeSettlementItem, PageResult } from '../../types'
import { confirmAction } from '../../utils/actionConfirm'
import { computed } from 'vue'

const loading = ref(false)
const rows = ref<DischargeSettlementItem[]>([])

const query = reactive({
  keyword: '',
  status: undefined as string | undefined,
  pageNo: 1,
  pageSize: 10
})

const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const statusOptions = [
  { label: '待确认', value: 'PENDING_CONFIRM' },
  { label: '已结算', value: 'SETTLED' },
  { label: '已取消', value: 'CANCELLED' }
]

const columns = [
  { title: '退住明细单号', dataIndex: 'detailNo', key: 'detailNo', width: 180 },
  { title: '老人', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '费用项', dataIndex: 'feeItem', key: 'feeItem', width: 140 },
  { title: '退住费用设置', dataIndex: 'dischargeFeeConfig', key: 'dischargeFeeConfig', width: 160 },
  { title: '应收金额', dataIndex: 'payableAmount', key: 'payableAmount', width: 120 },
  { title: '押金抵扣', dataIndex: 'fromDepositAmount', key: 'fromDepositAmount', width: 120 },
  { title: '应退款', dataIndex: 'refundAmount', key: 'refundAmount', width: 120 },
  { title: '需补缴', dataIndex: 'supplementAmount', key: 'supplementAmount', width: 120 },
  { title: '押金处理', key: 'depositProcess', width: 220 },
  { title: '自动对账', key: 'reconcileStatus', width: 220 },
  { title: '床位释放', key: 'bedRelease', width: 180 },
  { title: '风险等级', key: 'riskLevel', width: 100 },
  { title: '前台签字', dataIndex: 'frontdeskSignedTime', key: 'frontdeskSignedTime', width: 170 },
  { title: '护理部签字', dataIndex: 'nursingSignedTime', key: 'nursingSignedTime', width: 170 },
  { title: '财务退款时间', dataIndex: 'financeRefundTime', key: 'financeRefundTime', width: 170 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 140 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 280 }
]
const pendingRefundCount = computed(() => rows.value.filter(item => item.financeRefunded !== 1).length)
const refundTotal = computed(() => rows.value.reduce((sum, item) => sum + Number(item.refundAmount || 0), 0))
const blockedCount = computed(() => rows.value.filter(item =>
  item.status !== 'SETTLED'
  && (item.frontdeskApproved !== 1 || item.nursingApproved !== 1 || item.financeRefunded !== 1)).length)

const createOpen = ref(false)
const creating = ref(false)
const createForm = reactive({
  elderId: undefined as number | undefined,
  dischargeApplyId: undefined as number | undefined,
  payableAmount: 0,
  feeItem: '',
  dischargeFeeConfig: '',
  remark: ''
})
const { elderOptions, searchElders: searchElderOptions } = useElderOptions({ pageSize: 20 })
const feeItemOptions = ref<{ label: string; value: string }[]>([])
const dischargeFeeConfigOptions = ref<{ label: string; value: string }[]>([])

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<DischargeSettlementItem> = await getDischargeSettlementPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      status: query.status,
      keyword: query.keyword || undefined
    })
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.keyword = ''
  query.status = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openCreate() {
  createForm.elderId = undefined
  createForm.dischargeApplyId = undefined
  createForm.payableAmount = 0
  createForm.feeItem = ''
  createForm.dischargeFeeConfig = ''
  createForm.remark = ''
  createOpen.value = true
}

async function loadOptions() {
  const [feeItems, configs] = await Promise.all([
    getBaseConfigItemList({ configGroup: 'FEE_TYPE', status: 1 }),
    getBaseConfigItemList({ configGroup: 'DISCHARGE_FEE_CONFIG', status: 1 })
  ])
  feeItemOptions.value = (feeItems || []).map((item: BaseConfigItem) => ({ label: item.itemName, value: item.itemName }))
  dischargeFeeConfigOptions.value = (configs || []).map((item: BaseConfigItem) => ({
    label: item.itemName,
    value: item.itemName
  }))
}

async function searchElders(keyword: string) {
  await searchElderOptions(keyword)
}

async function submitCreate() {
  if (!createForm.elderId) {
    message.error('请选择老人')
    return
  }
  if (createForm.payableAmount < 0) {
    message.error('应收金额不能小于0')
    return
  }
  if (!createForm.feeItem) {
    message.error('请选择费用项')
    return
  }
  if (!createForm.dischargeFeeConfig) {
    message.error('请选择退住费用设置')
    return
  }
  creating.value = true
  try {
    await createDischargeSettlement({
      elderId: createForm.elderId,
      dischargeApplyId: createForm.dischargeApplyId,
      payableAmount: createForm.payableAmount,
      feeItem: createForm.feeItem,
      dischargeFeeConfig: createForm.dischargeFeeConfig,
      remark: createForm.remark
    })
    message.success('结算单已创建')
    createOpen.value = false
    fetchData()
  } finally {
    creating.value = false
  }
}

async function confirmSettlement(record: DischargeSettlementItem) {
  const confirmed = await confirmAction({
    title: '确认执行财务退款？',
    content: `结算单：${record.detailNo || '-'} / ${record.elderName || '-'}`,
    impactItems: ['将写入退款完成时间', '触发床态与档案状态回写'],
    okText: '确认退款'
  })
  if (!confirmed) return
  const signerName = window.prompt('请输入财务签字人', '') || undefined
  await confirmDischargeSettlement(record.id, {
    action: 'FINANCE_REFUND',
    signerName,
    remark: '财务退款完成'
  })
  message.success('财务退款已完成')
  fetchData()
}

async function approveFrontdesk(record: DischargeSettlementItem) {
  const confirmed = await confirmAction({
    title: '确认前台签字？',
    content: `结算单：${record.detailNo || '-'} / ${record.elderName || '-'}`,
    okText: '确认签字'
  })
  if (!confirmed) return
  const signerName = window.prompt('请输入前台签字人', '') || undefined
  await confirmDischargeSettlement(record.id, {
    action: 'FRONTDESK_APPROVE',
    signerName,
    remark: '前台审核签字'
  })
  message.success('前台签字已完成')
  fetchData()
}

async function approveNursing(record: DischargeSettlementItem) {
  const confirmed = await confirmAction({
    title: '确认护理部签字？',
    content: `结算单：${record.detailNo || '-'} / ${record.elderName || '-'}`,
    okText: '确认签字'
  })
  if (!confirmed) return
  const signerName = window.prompt('请输入护理部签字人', '') || undefined
  await confirmDischargeSettlement(record.id, {
    action: 'NURSING_APPROVE',
    signerName,
    remark: '护理部审核签字'
  })
  message.success('护理部签字已完成')
  fetchData()
}

function extractLinkage(remark: string | undefined, prefix: string) {
  if (!remark) {
    return ''
  }
  const parts = remark.split('；')
  const matched = parts.find((item) => item.trim().startsWith(prefix))
  return matched ? matched.trim().slice(prefix.length).trim() : ''
}

function riskText(item: DischargeSettlementItem) {
  if (item.status === 'SETTLED') return '低'
  if (item.frontdeskApproved !== 1 || item.nursingApproved !== 1) return '高'
  if (item.financeRefunded !== 1) return '中'
  return '低'
}

function riskColor(item: DischargeSettlementItem) {
  const level = riskText(item)
  if (level === '高') return 'red'
  if (level === '中') return 'orange'
  return 'green'
}

onMounted(async () => {
  await loadOptions()
  await fetchData()
})
</script>
