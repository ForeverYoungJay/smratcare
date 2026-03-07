<template>
  <PageContainer title="发票对账" subTitle="收款、账单与发票关联核验（实时口径）">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="日期">
          <a-date-picker v-model:value="query.date" style="width: 160px" />
        </a-form-item>
        <a-form-item label="支付方式">
          <a-select v-model:value="query.method" allow-clear style="width: 140px">
            <a-select-option value="CASH">现金</a-select-option>
            <a-select-option value="CARD">刷卡</a-select-option>
            <a-select-option value="BANK">转账</a-select-option>
            <a-select-option value="ALIPAY">支付宝</a-select-option>
            <a-select-option value="WECHAT">微信</a-select-option>
            <a-select-option value="WECHAT_OFFLINE">微信线下</a-select-option>
            <a-select-option value="QR_CODE">扫码</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关联状态">
          <a-select v-model:value="query.invoiceStatus" allow-clear style="width: 150px">
            <a-select-option value="LINKED">已关联</a-select-option>
            <a-select-option value="UNLINKED">未关联</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" allow-clear placeholder="长者/收据号/备注" style="width: 220px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button @click="exportCsvReport">导出CSV</a-button>
            <a-button @click="printCurrent">打印当前</a-button>
            <a-button @click="go('/finance/reconcile/center')">返回对账中心</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="fetchData">
      <a-row :gutter="[12, 12]" style="margin-bottom: 12px;">
        <a-col :xs="12" :lg="6"><a-card size="small"><a-statistic title="总收款笔数" :value="summary.totalCount" /></a-card></a-col>
        <a-col :xs="12" :lg="6"><a-card size="small"><a-statistic title="已关联发票" :value="summary.linkedCount" /></a-card></a-col>
        <a-col :xs="12" :lg="6"><a-card size="small"><a-statistic title="未关联发票" :value="summary.unlinkedCount" /></a-card></a-col>
        <a-col :xs="12" :lg="6"><a-card size="small"><a-statistic title="未关联金额" :value="summary.unlinkedAmount" :precision="2" suffix="元" /></a-card></a-col>
      </a-row>

      <a-alert
        v-if="summary.unlinkedCount > 0"
        type="warning"
        show-icon
        style="margin-bottom: 12px;"
        :message="`存在 ${summary.unlinkedCount} 笔收款未关联发票`"
        :description="'建议优先处理未关联记录，避免发票对账差异'"
      />

      <a-card class="card-elevated" :bordered="false">
        <vxe-table border stripe show-overflow :loading="loading" :data="rows" height="560">
          <vxe-column field="paidAt" title="收款时间" width="180" />
          <vxe-column field="elderName" title="长者" min-width="140">
            <template #default="{ row }">{{ row.elderName || '-' }}</template>
          </vxe-column>
          <vxe-column field="amount" title="金额" width="120" />
          <vxe-column field="payMethodLabel" title="支付方式" width="110" />
          <vxe-column field="receiptNo" title="收据号" min-width="180" />
          <vxe-column field="invoiceStatusLabel" title="发票关联" width="110">
            <template #default="{ row }">
              <a-tag :color="row.invoiceStatus === 'LINKED' ? 'green' : 'orange'">{{ row.invoiceStatusLabel }}</a-tag>
            </template>
          </vxe-column>
          <vxe-column field="remark" title="备注" min-width="200" />
          <vxe-column title="操作" width="220" fixed="right">
            <template #default="{ row }">
              <a-space>
                <a-button v-if="row.billId" type="link" @click="go(`/finance/bill/${row.billId}`)">查看账单</a-button>
                <a-button type="link" @click="go('/finance/payments/register?from=reconcile_invoice')">去补关联</a-button>
              </a-space>
            </template>
          </vxe-column>
        </vxe-table>

        <a-pagination
          style="margin-top: 16px; text-align: right;"
          :current="query.pageNo"
          :page-size="query.pageSize"
          :total="total"
          show-size-changer
          @change="onPageChange"
          @showSizeChange="onPageSizeChange"
        />
      </a-card>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { exportFinanceInvoiceReceiptCsv, getFinanceInvoiceReceiptPage } from '../../api/finance'
import type { FinanceInvoiceReceiptItem, PageResult } from '../../types'
import { printTableReport } from '../../utils/print'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const rows = ref<FinanceInvoiceReceiptItem[]>([])
const total = ref(0)
const query = reactive({
  date: dayjs(),
  method: undefined as string | undefined,
  invoiceStatus: undefined as string | undefined,
  keyword: '',
  printRemark: '',
  pageNo: 1,
  pageSize: 20
})

const summary = computed(() => {
  const all = rows.value || []
  const linked = all.filter(item => item.invoiceStatus === 'LINKED')
  const unlinked = all.filter(item => item.invoiceStatus === 'UNLINKED')
  return {
    totalCount: all.length,
    linkedCount: linked.length,
    unlinkedCount: unlinked.length,
    unlinkedAmount: unlinked.reduce((sum, item) => sum + Number(item.amount || 0), 0)
  }
})

function go(path: string) {
  router.push(path)
}

async function fetchData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result: PageResult<FinanceInvoiceReceiptItem> = await getFinanceInvoiceReceiptPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      date: dayjs(query.date).format('YYYY-MM-DD'),
      method: query.method,
      invoiceStatus: query.invoiceStatus,
      keyword: query.keyword || undefined
    })
    rows.value = result.list || []
    total.value = Number(result.total || 0)
  } catch (error: any) {
    errorMessage.value = error?.message || '加载发票对账失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

function reset() {
  query.date = dayjs()
  query.method = undefined
  query.invoiceStatus = undefined
  query.keyword = ''
  query.printRemark = ''
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  fetchData()
}

async function exportCsvReport() {
  try {
    await exportFinanceInvoiceReceiptCsv({
      date: dayjs(query.date).format('YYYY-MM-DD'),
      method: query.method,
      invoiceStatus: query.invoiceStatus,
      keyword: query.keyword || undefined
    })
    message.success('导出成功')
  } catch (error: any) {
    message.error(error?.message || '导出失败')
  }
}

function printCurrent() {
  try {
    printTableReport({
      title: '发票对账',
      subtitle: `日期：${dayjs(query.date).format('YYYY-MM-DD')}；支付方式：${query.method || '全部'}；关联状态：${query.invoiceStatus || '全部'}；关键字：${query.keyword || '-'}；备注：${query.printRemark || '-'}`,
      columns: [
        { key: 'paidAt', title: '收款时间' },
        { key: 'elderName', title: '长者' },
        { key: 'amount', title: '金额' },
        { key: 'payMethodLabel', title: '支付方式' },
        { key: 'receiptNo', title: '收据号' },
        { key: 'invoiceStatusLabel', title: '发票关联' },
        { key: 'remark', title: '备注' }
      ],
      rows: rows.value.map(item => ({
        paidAt: item.paidAt || '-',
        elderName: item.elderName || '-',
        amount: item.amount || 0,
        payMethodLabel: item.payMethodLabel || '-',
        receiptNo: item.receiptNo || '-',
        invoiceStatusLabel: item.invoiceStatusLabel || '-',
        remark: item.remark || '-'
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

onMounted(() => {
  if (String(route.query.filter || '').toLowerCase() === 'unlinked') {
    query.invoiceStatus = 'UNLINKED'
  }
  fetchData()
})
</script>
