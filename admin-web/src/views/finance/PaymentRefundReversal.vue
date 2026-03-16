<template>
  <PageContainer title="退款/冲正/作废" subTitle="集中查看作废账单与收款修正记录">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="月份">
          <a-date-picker v-model:value="query.month" picker="month" style="width: 160px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="exportData">导出</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
      <a-alert style="margin-top: 12px;" type="info" show-icon message="页面已切到后端专用接口，统一输出作废账单与收款修正记录，不再在前端自行推断。" />
    </a-card>

    <a-row :gutter="[12, 12]" style="margin-top: 16px;">
      <a-col :xs="24" :xl="8"><a-card class="card-elevated" :bordered="false"><a-statistic title="作废账单" :value="invalidBills.length" /></a-card></a-col>
      <a-col :xs="24" :xl="8"><a-card class="card-elevated" :bordered="false"><a-statistic title="修正收款" :value="adjustedPayments.length" /></a-card></a-col>
      <a-col :xs="24" :xl="8"><a-card class="card-elevated" :bordered="false"><a-statistic title="修正金额" :value="adjustedAmount" suffix="元" :precision="2" /></a-card></a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="作废账单">
      <vxe-table border stripe show-overflow :loading="loading" :data="invalidBills" height="260">
        <vxe-column field="occurredAt" title="发生时间" width="180" />
        <vxe-column field="billId" title="账单ID" width="120" />
        <vxe-column field="elderName" title="长者" min-width="140" />
        <vxe-column field="amount" title="账单金额" width="120" />
        <vxe-column field="detail" title="说明" min-width="220" />
        <vxe-column field="remark" title="备注" min-width="180" />
        <vxe-column field="approvalTitle" title="审批单" min-width="220">
          <template #default="{ row }">{{ row.approvalTitle || '-' }}</template>
        </vxe-column>
        <vxe-column title="审批" width="120">
          <template #default="{ row }">
            <a-tag :color="approvalColor(row.approvalStatus)">{{ row.approvalStatusLabel || '未提交' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="180" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button type="link" @click="go(`/finance/bill/${row.billId}`)">查看账单</a-button>
              <a-button type="link" :disabled="approvalDisabled(row.approvalStatus)" @click="openApproval(row)">{{ approvalActionText(row.approvalStatus) }}</a-button>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="收款修正记录">
      <vxe-table border stripe show-overflow :loading="loading" :data="adjustedPayments" height="260">
        <vxe-column field="occurredAt" title="发生时间" width="180" />
        <vxe-column field="billId" title="账单ID" width="120" />
        <vxe-column field="amount" title="金额" width="120" />
        <vxe-column field="detail" title="修正内容" min-width="220" />
        <vxe-column field="remark" title="备注" min-width="220" />
        <vxe-column field="approvalTitle" title="审批单" min-width="220">
          <template #default="{ row }">{{ row.approvalTitle || '-' }}</template>
        </vxe-column>
        <vxe-column title="审批" width="120">
          <template #default="{ row }">
            <a-tag :color="approvalColor(row.approvalStatus)">{{ row.approvalStatusLabel || '未提交' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="180" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button type="link" @click="go(`/finance/bill/${row.billId}`)">查看账单</a-button>
              <a-button type="link" :disabled="approvalDisabled(row.approvalStatus)" @click="openApproval(row)">{{ approvalActionText(row.approvalStatus) }}</a-button>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>

    <a-modal v-model:open="approvalVisible" title="提交退款/冲正审批" :confirm-loading="submitLoading" @ok="submitApproval">
      <a-form layout="vertical" :model="approvalForm">
        <a-form-item label="审批原因">
          <a-input v-model:value="approvalForm.reason" allow-clear />
        </a-form-item>
        <a-form-item label="审批备注">
          <a-textarea v-model:value="approvalForm.remark" :rows="4" />
        </a-form-item>
      </a-form>
      <a-alert
        type="info"
        show-icon
        :message="approvalRow ? `${approvalRow.typeLabel} · ${approvalRow.elderName || '未绑定长者'} · ${approvalRow.amount} 元` : '请选择审批项'"
        :description="approvalRow?.approvalTitle ? `${approvalRow.approvalTitle}；当前审批状态：${approvalRow.approvalStatusLabel}` : '提交后会进入现有 OA 财务审批流。'"
      />
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { exportFinancePaymentAdjustmentCsv, getFinancePaymentAdjustmentPage, requestFinancePaymentAdjustmentApproval } from '../../api/finance'
import type { FinancePaymentAdjustmentItem, PageResult } from '../../types'

const router = useRouter()
const loading = ref(false)
const rows = ref<FinancePaymentAdjustmentItem[]>([])
const approvalVisible = ref(false)
const submitLoading = ref(false)
const approvalRow = ref<FinancePaymentAdjustmentItem | null>(null)
const approvalForm = ref({
  reason: '',
  remark: ''
})
const invalidBills = computed(() => rows.value.filter(item => item.type === 'INVALID_BILL'))
const adjustedPayments = computed(() => rows.value.filter(item => item.type === 'PAYMENT_ADJUSTMENT'))
const adjustedAmount = computed(() => adjustedPayments.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))
const query = ref({
  month: dayjs().startOf('month')
})

function go(path: string) {
  router.push(path)
}

function approvalColor(status?: string) {
  if (status === 'APPROVED') return 'green'
  if (status === 'REJECTED') return 'red'
  if (status === 'PENDING') return 'gold'
  return 'default'
}

function approvalDisabled(status?: string) {
  return status === 'PENDING' || status === 'APPROVED'
}

function approvalActionText(status?: string) {
  if (status === 'REJECTED') return '重新提交'
  if (status === 'PENDING') return '审批中'
  if (status === 'APPROVED') return '已通过'
  return '提交审批'
}

function openApproval(row: FinancePaymentAdjustmentItem) {
  approvalRow.value = row
  approvalForm.value.reason = row.detail || row.remark || ''
  approvalForm.value.remark = row.remark || ''
  approvalVisible.value = true
}

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<FinancePaymentAdjustmentItem> = await getFinancePaymentAdjustmentPage({
      pageNo: 1,
      pageSize: 200,
      month: dayjs(query.value.month).format('YYYY-MM')
    })
    rows.value = res.list || []
  } finally {
    loading.value = false
  }
}

async function exportData() {
  try {
    await exportFinancePaymentAdjustmentCsv({
      month: dayjs(query.value.month).format('YYYY-MM')
    })
    message.success('退款/冲正清单已导出')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function reset() {
  query.value.month = dayjs().startOf('month')
  fetchData()
}

async function submitApproval() {
  if (!approvalRow.value) return
  submitLoading.value = true
  try {
    await requestFinancePaymentAdjustmentApproval({
      type: approvalRow.value.type,
      billId: approvalRow.value.billId,
      paymentId: approvalRow.value.paymentId,
      elderId: approvalRow.value.elderId,
      elderName: approvalRow.value.elderName,
      amount: Number(approvalRow.value.amount || 0),
      reason: approvalForm.value.reason,
      remark: approvalForm.value.remark
    })
    message.success('已提交到财务审批流')
    approvalVisible.value = false
    fetchData()
  } catch (error: any) {
    message.error(error?.message || '提交审批失败')
  } finally {
    submitLoading.value = false
  }
}

onMounted(fetchData)
</script>
