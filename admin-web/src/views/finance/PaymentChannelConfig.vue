<template>
  <PageContainer title="缴费渠道与收款账户" subTitle="配置收款渠道、账户号、户名与渠道启停">
    <a-card class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-date-picker v-model:value="query.month" picker="month" style="width: 150px" />
        <a-button type="primary" @click="loadData">刷新</a-button>
        <a-button @click="openEdit()">新增渠道</a-button>
        <a-button @click="batchToggleStatus(1)">批量启用</a-button>
        <a-button @click="batchToggleStatus(0)">批量停用</a-button>
        <a-button @click="exportData">导出</a-button>
        <a-button @click="triggerImport">导入CSV</a-button>
        <a-button @click="go('/finance/config/change-log')">查看变更记录</a-button>
      </a-space>
    </a-card>
    <input ref="importInputRef" type="file" accept=".csv,text/csv" style="display: none" @change="onImportFileChange" />

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table ref="tableRef" border stripe show-overflow :loading="loading" :data="rows" height="560">
        <vxe-column type="checkbox" width="50" />
        <vxe-column field="channelCode" title="渠道编码" width="160" />
        <vxe-column field="channelName" title="渠道名称" width="140" />
        <vxe-column field="accountName" title="收款户名" min-width="160" />
        <vxe-column field="accountNo" title="收款账号" min-width="200" />
        <vxe-column field="status" title="状态" width="100">
          <template #default="{ row }">
            <a-tag :color="row.status === 1 ? 'green' : 'default'">{{ row.status === 1 ? '启用' : '停用' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="120" fixed="right">
          <template #default="{ row }">
            <a-button type="link" @click="openEdit(row)">编辑</a-button>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>

    <a-modal v-model:open="editOpen" title="缴费渠道" @ok="submitEdit" :confirm-loading="saving">
      <a-form layout="vertical" :model="form">
        <a-form-item label="渠道编码">
          <a-input v-model:value="form.channelCode" placeholder="如 ALIPAY / WECHAT / BANK" />
        </a-form-item>
        <a-form-item label="渠道名称">
          <a-input v-model:value="form.channelName" placeholder="如 支付宝/微信/银行转账" />
        </a-form-item>
        <a-form-item label="收款户名">
          <a-input v-model:value="form.accountName" />
        </a-form-item>
        <a-form-item label="收款账号">
          <a-input v-model:value="form.accountNo" />
        </a-form-item>
        <a-form-item label="启用">
          <a-switch v-model:checked="formEnabled" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { batchUpsertFinanceBillingConfig, getFinanceBillingConfig, upsertFinanceBillingConfig } from '../../api/finance'
import type { FinanceBillingConfigEntry } from '../../types'
import { exportCsv } from '../../utils/export'

type ChannelRow = {
  channelCode: string
  channelName: string
  accountName: string
  accountNo: string
  status: number
}

const loading = ref(false)
const saving = ref(false)
const router = useRouter()
const query = ref({ month: dayjs() })
const entries = ref<FinanceBillingConfigEntry[]>([])
const rows = ref<ChannelRow[]>([])
const tableRef = ref<any>()
const importInputRef = ref<HTMLInputElement | null>(null)

const editOpen = ref(false)
const form = ref({
  channelCode: '',
  channelName: '',
  accountName: '',
  accountNo: '',
  status: 1
})

const formEnabled = computed({
  get: () => Number(form.value.status) === 1,
  set: (checked: boolean) => { form.value.status = checked ? 1 : 0 }
})

function go(path: string) {
  router.push(path)
}

function monthText() {
  return dayjs(query.value.month).format('YYYY-MM')
}

function normalizeChannelCode(raw: string) {
  return raw.trim().toUpperCase().replace(/[^A-Z0-9_]/g, '_')
}

function validateChannelCode(code: string) {
  return /^[A-Z0-9_]{2,32}$/.test(code)
}

function configFor(code: string, suffix: string) {
  return entries.value.find(item => item.configKey === `PAYMENT_CHANNEL_${code}_${suffix}`)
}

function rebuildRows() {
  const channelCodes = Array.from(new Set(entries.value
    .map(item => String(item.configKey || '').match(/^PAYMENT_CHANNEL_([A-Z0-9_]+)_/i)?.[1] || '')
    .filter(Boolean)))
  rows.value = channelCodes.map(code => ({
    channelCode: code,
    channelName: configFor(code, 'NAME')?.remark || code,
    accountName: configFor(code, 'ACCOUNT_NAME')?.remark || '',
    accountNo: configFor(code, 'ACCOUNT_NO')?.remark || '',
    status: Number(configFor(code, 'ENABLED')?.configValue || 0)
  }))
}

async function loadData() {
  loading.value = true
  try {
    entries.value = await getFinanceBillingConfig({
      month: monthText(),
      keyPrefix: 'PAYMENT_CHANNEL_'
    })
    rebuildRows()
  } finally {
    loading.value = false
  }
}

function openEdit(row?: ChannelRow) {
  if (!row) {
    form.value = {
      channelCode: '',
      channelName: '',
      accountName: '',
      accountNo: '',
      status: 1
    }
    editOpen.value = true
    return
  }
  form.value = {
    channelCode: row.channelCode,
    channelName: row.channelName,
    accountName: row.accountName,
    accountNo: row.accountNo,
    status: row.status
  }
  editOpen.value = true
}

async function submitEdit() {
  const code = normalizeChannelCode(form.value.channelCode)
  if (!code) {
    message.warning('请输入渠道编码')
    return
  }
  if (!validateChannelCode(code)) {
    message.warning('渠道编码仅支持大写字母、数字、下划线，长度 2-32')
    return
  }
  if (!form.value.channelName.trim()) {
    message.warning('请输入渠道名称')
    return
  }
  if (form.value.accountNo && !/^[A-Za-z0-9\-_\s]{4,64}$/.test(form.value.accountNo.trim())) {
    message.warning('收款账号格式不正确（4-64 位字母数字/中划线/下划线）')
    return
  }
  saving.value = true
  try {
    await Promise.all([
      upsertFinanceBillingConfig({
        configKey: `PAYMENT_CHANNEL_${code}_ENABLED`,
        configValue: Number(form.value.status || 0),
        effectiveMonth: monthText(),
        status: 1,
        remark: form.value.channelName.trim()
      }),
      upsertFinanceBillingConfig({
        configKey: `PAYMENT_CHANNEL_${code}_NAME`,
        configValue: 0,
        effectiveMonth: monthText(),
        status: 1,
        remark: form.value.channelName.trim()
      }),
      upsertFinanceBillingConfig({
        configKey: `PAYMENT_CHANNEL_${code}_ACCOUNT_NAME`,
        configValue: 0,
        effectiveMonth: monthText(),
        status: 1,
        remark: form.value.accountName.trim()
      }),
      upsertFinanceBillingConfig({
        configKey: `PAYMENT_CHANNEL_${code}_ACCOUNT_NO`,
        configValue: 0,
        effectiveMonth: monthText(),
        status: 1,
        remark: form.value.accountNo.trim()
      })
    ])
    message.success('渠道配置已保存')
    editOpen.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

function selectedRows(): ChannelRow[] {
  return tableRef.value?.getCheckboxRecords?.() || []
}

async function batchToggleStatus(status: 0 | 1) {
  const selected = selectedRows()
  if (!selected.length) {
    message.warning('请先勾选要操作的渠道')
    return
  }
  saving.value = true
  try {
    await batchUpsertFinanceBillingConfig({
      items: selected.map(item => ({
        configKey: `PAYMENT_CHANNEL_${item.channelCode}_ENABLED`,
        configValue: status,
        effectiveMonth: monthText(),
        status: 1,
        remark: item.channelName
      }))
    })
    message.success(`批量${status === 1 ? '启用' : '停用'}成功`)
    loadData()
  } finally {
    saving.value = false
  }
}

function exportData() {
  exportCsv(
    rows.value.map(item => ({
      渠道编码: item.channelCode,
      渠道名称: item.channelName,
      收款户名: item.accountName,
      收款账号: item.accountNo,
      状态: Number(item.status) === 1 ? '启用' : '停用'
    })),
    `缴费渠道收款账户-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}

function triggerImport() {
  importInputRef.value?.click()
}

function parseCsvLine(line: string) {
  return line.split(',').map(item => item.trim().replace(/^"|"$/g, ''))
}

async function onImportFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target?.files?.[0]
  if (!file) return
  try {
    const text = await file.text()
    const lines = text.split(/\r?\n/).map(item => item.trim()).filter(Boolean)
    if (lines.length <= 1) {
      message.warning('CSV 内容为空')
      return
    }
    const [header, ...body] = lines
    const columns = parseCsvLine(header)
    const codeIndex = columns.findIndex(item => item === 'channelCode')
    const nameIndex = columns.findIndex(item => item === 'channelName')
    const accountNameIndex = columns.findIndex(item => item === 'accountName')
    const accountNoIndex = columns.findIndex(item => item === 'accountNo')
    const statusIndex = columns.findIndex(item => item === 'status')
    const monthIndex = columns.findIndex(item => item === 'effectiveMonth')
    if (codeIndex < 0 || nameIndex < 0) {
      message.warning('CSV 必须包含 channelCode, channelName 列')
      return
    }
    const channelRows = body.map(line => parseCsvLine(line)).map(row => {
      const channelCode = normalizeChannelCode(row[codeIndex] || '')
      const channelName = String(row[nameIndex] || '').trim()
      const accountName = accountNameIndex >= 0 ? String(row[accountNameIndex] || '').trim() : ''
      const accountNo = accountNoIndex >= 0 ? String(row[accountNoIndex] || '').trim() : ''
      const status = statusIndex >= 0 ? Number(row[statusIndex] || 1) : 1
      const effectiveMonth = monthIndex >= 0 ? String(row[monthIndex] || '').trim() : ''
      return {
        channelCode,
        channelName,
        accountName,
        accountNo,
        status: status === 0 ? 0 : 1,
        effectiveMonth: effectiveMonth || monthText()
      }
    }).filter(item => validateChannelCode(item.channelCode) && Boolean(item.channelName))
    if (!channelRows.length) {
      message.warning('CSV 无有效渠道数据')
      return
    }
    const items = channelRows.flatMap(item => ([
      {
        configKey: `PAYMENT_CHANNEL_${item.channelCode}_ENABLED`,
        configValue: Number(item.status || 0),
        effectiveMonth: item.effectiveMonth,
        status: 1,
        remark: item.channelName
      },
      {
        configKey: `PAYMENT_CHANNEL_${item.channelCode}_NAME`,
        configValue: 0,
        effectiveMonth: item.effectiveMonth,
        status: 1,
        remark: item.channelName
      },
      {
        configKey: `PAYMENT_CHANNEL_${item.channelCode}_ACCOUNT_NAME`,
        configValue: 0,
        effectiveMonth: item.effectiveMonth,
        status: 1,
        remark: item.accountName
      },
      {
        configKey: `PAYMENT_CHANNEL_${item.channelCode}_ACCOUNT_NO`,
        configValue: 0,
        effectiveMonth: item.effectiveMonth,
        status: 1,
        remark: item.accountNo
      }
    ]))
    saving.value = true
    await batchUpsertFinanceBillingConfig({ items })
    message.success(`导入成功，共 ${channelRows.length} 个渠道`)
    loadData()
  } finally {
    saving.value = false
    if (target) target.value = ''
  }
}

onMounted(loadData)
</script>
