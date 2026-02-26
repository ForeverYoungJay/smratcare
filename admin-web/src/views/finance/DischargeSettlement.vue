<template>
  <PageContainer title="退住结算" subTitle="预存账户抵扣、退款与结算确认">
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="老人姓名">
        <a-input v-model:value="query.keyword" placeholder="输入老人/费用项/备注" allow-clear />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" allow-clear style="width: 180px" :options="statusOptions" />
      </a-form-item>
      <template #extra>
        <a-button type="primary" @click="openCreate">新建结算单</a-button>
      </template>
    </SearchForm>

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
import { useElderOptions } from '../../composables/useElderOptions'
import { getBaseConfigItemList } from '../../api/baseConfig'
import { createDischargeSettlement, getDischargeSettlementPage, confirmDischargeSettlement } from '../../api/financeFee'
import type { BaseConfigItem, DischargeSettlementItem, PageResult } from '../../types'

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
  { title: '前台签字', dataIndex: 'frontdeskSignedTime', key: 'frontdeskSignedTime', width: 170 },
  { title: '护理部签字', dataIndex: 'nursingSignedTime', key: 'nursingSignedTime', width: 170 },
  { title: '财务退款时间', dataIndex: 'financeRefundTime', key: 'financeRefundTime', width: 170 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 140 },
  { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '操作', key: 'action', width: 280 }
]

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
  const signerName = window.prompt('请输入护理部签字人', '') || undefined
  await confirmDischargeSettlement(record.id, {
    action: 'NURSING_APPROVE',
    signerName,
    remark: '护理部审核签字'
  })
  message.success('护理部签字已完成')
  fetchData()
}

onMounted(async () => {
  await loadOptions()
  await fetchData()
})
</script>
