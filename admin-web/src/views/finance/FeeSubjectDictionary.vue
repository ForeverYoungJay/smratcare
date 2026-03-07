<template>
  <PageContainer title="费用科目字典" subTitle="维护费用科目（含水电电视网络等）并控制启停状态">
    <a-card class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-date-picker v-model:value="query.month" picker="month" style="width: 150px" />
        <a-input v-model:value="query.keyword" allow-clear placeholder="科目编码/名称" style="width: 220px" />
        <a-button type="primary" @click="loadData">查询</a-button>
        <a-button @click="openEdit()">新增科目</a-button>
        <a-button @click="batchToggleStatus(1)">批量启用</a-button>
        <a-button @click="batchToggleStatus(0)">批量停用</a-button>
        <a-button @click="exportData">导出</a-button>
        <a-button @click="triggerImport">导入CSV</a-button>
        <a-button @click="go('/finance/config/change-log')">查看变更记录</a-button>
      </a-space>
    </a-card>
    <a-card v-if="impactPreview" class="card-elevated" :bordered="false" style="margin-top: 12px;">
      <a-alert
        type="info"
        show-icon
        :message="`最近预览：${impactPreview.month}（${impactPreview.configKeyCount} 个科目键）`"
        :description="`账单 ${impactPreview.activeBillCount} 条，长者 ${impactPreview.activeElderCount} 人，欠费 ${impactPreview.highRiskBillCount} 条`"
      />
    </a-card>
    <input ref="importInputRef" type="file" accept=".csv,text/csv" style="display: none" @change="onImportFileChange" />

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table ref="tableRef" border stripe show-overflow :loading="loading" :data="filteredRows" height="560">
        <vxe-column type="checkbox" width="50" />
        <vxe-column field="configKey" title="科目键" min-width="260" />
        <vxe-column field="remark" title="科目名称" min-width="160" />
        <vxe-column field="configValue" title="默认单价/比例" width="140" />
        <vxe-column field="effectiveMonth" title="生效月份" width="120" />
        <vxe-column field="status" title="状态" width="100">
          <template #default="{ row }">
            <a-tag :color="Number(row.status) === 1 ? 'green' : 'default'">{{ Number(row.status) === 1 ? '启用' : '停用' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="120" fixed="right">
          <template #default="{ row }">
            <a-button type="link" @click="openEdit(row)">编辑</a-button>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>

    <a-modal v-model:open="editOpen" title="费用科目" @ok="submitEdit" :confirm-loading="saving">
      <a-form layout="vertical" :model="form">
        <a-form-item label="科目编码（可选，自动补前缀 FEE_SUBJECT_）">
          <a-input v-model:value="form.keySuffix" placeholder="留空自动生成；或输入 ROOM_FEE / ELECTRICITY_FEE" />
        </a-form-item>
        <a-form-item label="科目名称">
          <a-input v-model:value="form.subjectName" placeholder="支持中文，如 房费/水费/电费/网络费" />
        </a-form-item>
        <a-form-item label="默认单价/比例">
          <a-input-number v-model:value="form.configValue" style="width: 100%" :min="0" />
        </a-form-item>
        <a-form-item label="生效月份">
          <a-date-picker v-model:value="form.effectiveMonth" picker="month" style="width: 100%" />
        </a-form-item>
        <a-form-item label="启用">
          <a-switch v-model:checked="formEnabled" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { batchUpsertFinanceBillingConfig, getFinanceBillingConfig, getFinanceConfigImpactPreview, upsertFinanceBillingConfig } from '../../api/finance'
import type { FinanceBillingConfigEntry, FinanceConfigImpactPreview } from '../../types'
import { exportCsv } from '../../utils/export'
import { confirmAction } from '../../utils/actionConfirm'

const loading = ref(false)
const saving = ref(false)
const router = useRouter()
const rows = ref<FinanceBillingConfigEntry[]>([])
const tableRef = ref<any>()
const importInputRef = ref<HTMLInputElement | null>(null)
const impactPreview = ref<FinanceConfigImpactPreview | null>(null)
const query = ref({
  month: dayjs(),
  keyword: ''
})

const editOpen = ref(false)
const form = ref({
  keySuffix: '',
  subjectName: '',
  configValue: 0,
  effectiveMonth: dayjs(),
  status: 1
})

const formEnabled = computed({
  get: () => Number(form.value.status) === 1,
  set: (checked: boolean) => { form.value.status = checked ? 1 : 0 }
})

const filteredRows = computed(() => {
  const keyword = query.value.keyword.trim().toLowerCase()
  if (!keyword) return rows.value
  return rows.value.filter(item => (`${item.configKey || ''} ${item.remark || ''}`).toLowerCase().includes(keyword))
})

function go(path: string) {
  router.push(path)
}

async function loadData() {
  loading.value = true
  try {
    rows.value = await getFinanceBillingConfig({
      month: dayjs(query.value.month).format('YYYY-MM'),
      keyPrefix: 'FEE_SUBJECT_'
    })
  } finally {
    loading.value = false
  }
}

function openEdit(row?: FinanceBillingConfigEntry) {
  if (!row) {
    form.value = {
      keySuffix: '',
      subjectName: '',
      configValue: 0,
      effectiveMonth: dayjs(query.value.month),
      status: 1
    }
    editOpen.value = true
    return
  }
  form.value = {
    keySuffix: String(row.configKey || '').replace(/^FEE_SUBJECT_/, ''),
    subjectName: row.remark || '',
    configValue: Number(row.configValue || 0),
    effectiveMonth: dayjs(row.effectiveMonth),
    status: Number(row.status || 1)
  }
  editOpen.value = true
}

async function submitEdit() {
  if (!form.value.subjectName.trim()) {
    message.warning('请输入科目名称')
    return
  }
  const keySuffix = normalizeKeySuffix(form.value.keySuffix, form.value.subjectName)
  if (!/^[A-Z0-9_]{2,48}$/.test(keySuffix)) {
    message.warning('科目编码格式非法，请使用字母/数字/下划线，长度 2-48')
    return
  }
  const configKey = `FEE_SUBJECT_${keySuffix}`
  let preview: FinanceConfigImpactPreview
  let fallbackConfirmed = false
  try {
    preview = await getFinanceConfigImpactPreview({
      month: dayjs(form.value.effectiveMonth).format('YYYY-MM'),
      configKeys: [configKey]
    })
    impactPreview.value = preview
  } catch (error: any) {
    message.warning(error?.message || '影响预览加载失败，已切换普通确认')
    const confirmedFallback = await confirmAction({
      title: '确认保存费用科目？',
      content: `即将保存 ${form.value.subjectName.trim()}（${configKey}）`,
      okText: '确认保存'
    })
    if (!confirmedFallback) return
    fallbackConfirmed = true
    preview = {
      month: dayjs(form.value.effectiveMonth).format('YYYY-MM'),
      activeBillCount: 0,
      activeElderCount: 0,
      highRiskBillCount: 0,
      pendingApprovalCount: 0,
      recentPaymentCount: 0,
      allocationTaskCount: 0,
      configKeyCount: 1,
      impactedItems: [],
      riskTips: []
    }
  }
  if (!fallbackConfirmed) {
    const confirmed = await confirmAction({
      title: '确认保存费用科目？',
      content: `即将保存 ${form.value.subjectName.trim()}（${configKey}）`,
      impactItems: [
        `影响账单 ${preview.activeBillCount} 条，涉及长者 ${preview.activeElderCount} 人`,
        ...preview.riskTips.slice(0, 2)
      ],
      okText: '确认保存'
    })
    if (!confirmed) return
  }
  saving.value = true
  try {
    await upsertFinanceBillingConfig({
      configKey,
      configValue: Number(form.value.configValue || 0),
      effectiveMonth: dayjs(form.value.effectiveMonth).format('YYYY-MM'),
      status: Number(form.value.status || 1),
      remark: form.value.subjectName.trim()
    })
    message.success('科目已保存')
    editOpen.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

function normalizeKeySuffix(rawKey: string, subjectName: string) {
  const direct = String(rawKey || '').trim().toUpperCase().replace(/[^A-Z0-9_]/g, '_').replace(/_{2,}/g, '_').replace(/^_+|_+$/g, '')
  if (direct.length >= 2 && direct.length <= 48) {
    return direct
  }
  const fromName = String(subjectName || '').trim().toUpperCase().replace(/[^A-Z0-9_]/g, '_').replace(/_{2,}/g, '_').replace(/^_+|_+$/g, '')
  if (fromName.length >= 2) {
    return fromName.slice(0, 48)
  }
  return `SUBJECT_${dayjs().format('HHmmssSSS')}`
}

function selectedRows(): FinanceBillingConfigEntry[] {
  return tableRef.value?.getCheckboxRecords?.() || []
}

async function batchToggleStatus(status: 0 | 1) {
  const selected = selectedRows()
  if (!selected.length) {
    message.warning('请先勾选要操作的科目')
    return
  }
  let preview: FinanceConfigImpactPreview
  let fallbackConfirmed = false
  try {
    preview = await getFinanceConfigImpactPreview({
      month: dayjs(query.value.month).format('YYYY-MM'),
      configKeys: selected.map(item => item.configKey)
    })
    impactPreview.value = preview
  } catch (error: any) {
    message.warning(error?.message || '影响预览加载失败，已切换普通确认')
    const fallbackOk = await confirmAction({
      title: `确认批量${status === 1 ? '启用' : '停用'}科目？`,
      content: `共 ${selected.length} 条科目`,
      okText: '确认执行'
    })
    if (!fallbackOk) return
    fallbackConfirmed = true
    preview = {
      month: dayjs(query.value.month).format('YYYY-MM'),
      activeBillCount: 0,
      activeElderCount: 0,
      highRiskBillCount: 0,
      pendingApprovalCount: 0,
      recentPaymentCount: 0,
      allocationTaskCount: 0,
      configKeyCount: selected.length,
      impactedItems: [],
      riskTips: []
    }
  }
  if (!fallbackConfirmed) {
    const confirmed = await confirmAction({
      title: `确认批量${status === 1 ? '启用' : '停用'}科目？`,
      content: `共 ${selected.length} 条科目`,
      impactItems: [
        `影响账单 ${preview.activeBillCount} 条，欠费账单 ${preview.highRiskBillCount} 条`,
        ...preview.riskTips.slice(0, 2)
      ],
      okText: '确认执行'
    })
    if (!confirmed) return
  }
  saving.value = true
  try {
    await batchUpsertFinanceBillingConfig({
      items: selected.map(item => ({
        configKey: item.configKey,
        configValue: Number(item.configValue || 0),
        effectiveMonth: item.effectiveMonth,
        status,
        remark: item.remark
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
    filteredRows.value.map(item => ({
      科目键: item.configKey,
      科目名称: item.remark || '',
      默认值: item.configValue,
      生效月份: item.effectiveMonth,
      状态: Number(item.status) === 1 ? '启用' : '停用'
    })),
    `费用科目字典-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
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
    const keyIndex = columns.findIndex(item => item === 'keySuffix')
    const nameIndex = columns.findIndex(item => item === 'subjectName')
    const valueIndex = columns.findIndex(item => item === 'configValue')
    const monthIndex = columns.findIndex(item => item === 'effectiveMonth')
    const statusIndex = columns.findIndex(item => item === 'status')
    if (keyIndex < 0 || nameIndex < 0 || valueIndex < 0) {
      message.warning('CSV 必须包含 keySuffix, subjectName, configValue 列')
      return
    }
    const items = body.map(line => parseCsvLine(line)).map(row => {
      const keySuffix = String(row[keyIndex] || '').trim().toUpperCase()
      return {
        configKey: `FEE_SUBJECT_${keySuffix}`,
        configValue: Number(row[valueIndex] || 0),
        effectiveMonth: row[monthIndex] || dayjs(query.value.month).format('YYYY-MM'),
        status: statusIndex >= 0 ? Number(row[statusIndex] || 1) : 1,
        remark: row[nameIndex] || ''
      }
    }).filter(item => /^FEE_SUBJECT_[A-Z0-9_]{2,48}$/.test(item.configKey))
    if (!items.length) {
      message.warning('CSV 无有效科目数据')
      return
    }
    saving.value = true
    await batchUpsertFinanceBillingConfig({ items })
    message.success(`导入成功，共 ${items.length} 条`)
    loadData()
  } finally {
    saving.value = false
    if (target) target.value = ''
  }
}

onMounted(loadData)
</script>
