<template>
  <PageContainer title="发票/收据管理" subTitle="按收款流水关联发票夹与收据凭证">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
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
          <a-select v-model:value="query.invoiceStatus" allow-clear style="width: 140px">
            <a-select-option value="LINKED">已关联</a-select-option>
            <a-select-option value="UNLINKED">未关联</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" allow-clear placeholder="长者/收据号/备注" style="width: 200px" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：财务复核版" style="width: 180px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">查询</a-button>
            <a-button @click="exportCsvReport">导出CSV</a-button>
            <a-button @click="printCurrent">打印当前结果</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table border stripe show-overflow :loading="loading" :data="rows" height="520">
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
        <vxe-column field="remark" title="备注" min-width="180" />
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
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { exportFinanceInvoiceReceiptCsv, getFinanceInvoiceReceiptPage } from '../../api/finance'
import type { FinanceInvoiceReceiptItem, PageResult } from '../../types'
import { printTableReport } from '../../utils/print'

const loading = ref(false)
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

async function fetchData() {
  loading.value = true
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
      title: '发票/收据管理',
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

onMounted(fetchData)
</script>
