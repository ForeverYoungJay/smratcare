<template>
  <PageContainer title="账单模板/计费规则" subTitle="费用科目、价格表、计费规则与减免策略配置">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="生效月份">
          <a-date-picker v-model:value="query.month" picker="month" style="width: 150px" />
        </a-form-item>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" allow-clear placeholder="配置键/备注" style="width: 220px" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：规则评审版" style="width: 180px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
            <a-button @click="openEdit()">新增规则</a-button>
            <a-button @click="batchToggleStatus(1)">批量启用</a-button>
            <a-button @click="batchToggleStatus(0)">批量停用</a-button>
            <a-button @click="exportData">导出</a-button>
            <a-button @click="printCurrent">打印当前</a-button>
            <a-button @click="triggerImport">导入CSV</a-button>
            <a-button @click="go('/finance/config/change-log')">查看变更记录</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 12px;">
      <a-row :gutter="[12, 12]">
        <a-col :xs="24" :xl="12">
          <a-space wrap>
            <a-button :loading="saving" @click="applyTemplate('STANDARD')">应用标准规则模板</a-button>
            <a-button :loading="saving" @click="applyTemplate('NURSING')">应用护理分级模板</a-button>
            <a-button :loading="saving" @click="applyTemplate('UTILITY')">应用水电分摊模板</a-button>
          </a-space>
        </a-col>
        <a-col :xs="24" :xl="12">
          <a-alert
            :type="healthWarnings.length ? 'warning' : 'success'"
            show-icon
            :message="healthWarnings.length ? `规则健康检查发现 ${healthWarnings.length} 项风险` : '规则健康检查通过'"
            :description="healthWarnings.length ? healthWarnings.join('；') : '配置键、启停状态、数值范围均正常'"
          />
        </a-col>
      </a-row>
    </a-card>
    <a-card v-if="impactPreview" class="card-elevated" :bordered="false" style="margin-top: 12px;">
      <a-alert
        type="info"
        show-icon
        :message="`最近一次影响预览：${impactPreview.month}（${impactPreview.configKeyCount} 个配置键）`"
        :description="`账单 ${impactPreview.activeBillCount} 条，欠费 ${impactPreview.highRiskBillCount} 条，待审批 ${impactPreview.pendingApprovalCount} 条`"
      />
    </a-card>
    <input ref="importInputRef" type="file" accept=".csv,text/csv" style="display: none" @change="onImportFileChange" />

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table ref="tableRef" border stripe show-overflow :loading="loading" :data="filteredRows" height="560">
        <vxe-column type="checkbox" width="50" />
        <vxe-column field="configKey" title="配置键" min-width="280" />
        <vxe-column field="configValue" title="配置值" width="150" />
        <vxe-column field="effectiveMonth" title="生效月份" width="120" />
        <vxe-column field="status" title="状态" width="100">
          <template #default="{ row }">
            <a-tag :color="Number(row.status) === 1 ? 'green' : 'default'">{{ Number(row.status) === 1 ? '启用' : '停用' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="remark" title="备注" min-width="220" />
        <vxe-column title="操作" width="160" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button type="link" @click="openEdit(row)">编辑</a-button>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>

    <a-modal v-model:open="editOpen" title="计费规则" @ok="submitEdit" :confirm-loading="saving">
      <a-form layout="vertical" :model="form">
        <a-form-item label="配置键">
          <a-input v-model:value="form.configKey" placeholder="如 BILL_ROOM_PRICE / DISCOUNT_LIMIT" />
        </a-form-item>
        <a-form-item label="配置值">
          <a-input-number v-model:value="form.configValue" style="width: 100%" :min="0" />
        </a-form-item>
        <a-form-item label="生效月份">
          <a-date-picker v-model:value="form.effectiveMonth" picker="month" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="formEnabled" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
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
import { printTableReport } from '../../utils/print'
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
  keyword: '',
  printRemark: ''
})

const editOpen = ref(false)
const form = ref({
  configKey: '',
  configValue: 0,
  effectiveMonth: dayjs(),
  status: 1,
  remark: ''
})

const formEnabled = computed({
  get: () => Number(form.value.status) === 1,
  set: (checked: boolean) => {
    form.value.status = checked ? 1 : 0
  }
})

const filteredRows = computed(() => {
  const keyword = query.value.keyword.trim().toLowerCase()
  if (!keyword) return rows.value
  return rows.value.filter(item => (`${item.configKey || ''} ${item.remark || ''}`).toLowerCase().includes(keyword))
})
const healthWarnings = computed(() => {
  const warnings: string[] = []
  const enabled = rows.value.filter(item => Number(item.status || 0) === 1)
  if (!enabled.length) {
    warnings.push('当前月份无启用计费规则')
  }
  const negativeItems = enabled.filter(item => Number(item.configValue || 0) < 0)
  if (negativeItems.length) {
    warnings.push(`存在 ${negativeItems.length} 条规则值为负数`)
  }
  const duplicatedKey = new Set<string>()
  const seen = new Set<string>()
  rows.value.forEach(item => {
    const key = `${item.configKey || ''}@${item.effectiveMonth || ''}`
    if (seen.has(key)) duplicatedKey.add(key)
    seen.add(key)
  })
  if (duplicatedKey.size) {
    warnings.push(`存在 ${duplicatedKey.size} 组重复规则键`)
  }
  return warnings
})

function go(path: string) {
  router.push(path)
}

function normalizeConfigKey(raw: string) {
  return raw.trim().toUpperCase().replace(/[^A-Z0-9_]/g, '_')
}

function validateConfigKey(key: string) {
  return /^[A-Z0-9_]{3,64}$/.test(key)
}

async function loadData() {
  loading.value = true
  try {
    rows.value = await getFinanceBillingConfig({
      month: dayjs(query.value.month).format('YYYY-MM')
    })
  } finally {
    loading.value = false
  }
}

async function confirmWithImpact(configKeys: string[], actionTitle: string, content: string) {
  try {
    const month = dayjs(query.value.month).format('YYYY-MM')
    const preview = await getFinanceConfigImpactPreview({
      month,
      configKeys
    })
    impactPreview.value = preview
    const impactItems = [
      `账单数：${preview.activeBillCount}；长者数：${preview.activeElderCount}`,
      `欠费账单：${preview.highRiskBillCount}；待审批：${preview.pendingApprovalCount}`,
      ...preview.impactedItems.slice(0, 4).map(item => `${item.moduleLabel}：${item.affectedCount}（${item.impactHint}）`),
      ...preview.riskTips.slice(0, 2)
    ]
    return confirmAction({
      title: actionTitle,
      content,
      impactItems,
      okText: '确认继续'
    })
  } catch (error: any) {
    message.warning(error?.message || '影响预览加载失败，已切换普通确认')
    return confirmAction({
      title: actionTitle,
      content,
      okText: '确认继续'
    })
  }
}

function openEdit(row?: FinanceBillingConfigEntry) {
  if (!row) {
    form.value = {
      configKey: '',
      configValue: 0,
      effectiveMonth: dayjs(query.value.month),
      status: 1,
      remark: ''
    }
    editOpen.value = true
    return
  }
  form.value = {
    configKey: row.configKey,
    configValue: Number(row.configValue || 0),
    effectiveMonth: dayjs(row.effectiveMonth),
    status: Number(row.status || 1),
    remark: row.remark || ''
  }
  editOpen.value = true
}

async function submitEdit() {
  const normalizedKey = normalizeConfigKey(form.value.configKey)
  if (!normalizedKey) {
    message.warning('请输入配置键')
    return
  }
  if (!validateConfigKey(normalizedKey)) {
    message.warning('配置键仅支持大写字母、数字、下划线，长度 3-64')
    return
  }
  const confirmed = await confirmWithImpact(
    [normalizedKey],
    '确认保存计费规则？',
    `即将保存规则 ${normalizedKey}@${dayjs(form.value.effectiveMonth).format('YYYY-MM')}`
  )
  if (!confirmed) return
  saving.value = true
  try {
    await upsertFinanceBillingConfig({
      configKey: normalizedKey,
      configValue: Number(form.value.configValue || 0),
      effectiveMonth: dayjs(form.value.effectiveMonth).format('YYYY-MM'),
      status: Number(form.value.status || 1),
      remark: form.value.remark || undefined
    })
    message.success('保存成功')
    editOpen.value = false
    loadData()
  } finally {
    saving.value = false
  }
}

function selectedRows(): FinanceBillingConfigEntry[] {
  return tableRef.value?.getCheckboxRecords?.() || []
}

async function batchToggleStatus(status: 0 | 1) {
  const selected = selectedRows()
  if (!selected.length) {
    message.warning('请先勾选要操作的规则')
    return
  }
  const confirmed = await confirmWithImpact(
    selected.map(item => item.configKey),
    `确认批量${status === 1 ? '启用' : '停用'}？`,
    `本次将操作 ${selected.length} 条规则`
  )
  if (!confirmed) return
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

async function applyTemplate(type: 'STANDARD' | 'NURSING' | 'UTILITY') {
  const month = dayjs(query.value.month).format('YYYY-MM')
  const templates = {
    STANDARD: [
      { configKey: 'BILL_ROOM_BASE_PRICE', configValue: 1800, remark: '房费基础单价（月）' },
      { configKey: 'BILL_NURSING_BASE_PRICE', configValue: 1600, remark: '护理费基础单价（月）' },
      { configKey: 'BILL_DEPOSIT_MIN', configValue: 5000, remark: '入住押金下限' }
    ],
    NURSING: [
      { configKey: 'BILL_NURSING_L1_PRICE', configValue: 1200, remark: '护理一级价格' },
      { configKey: 'BILL_NURSING_L2_PRICE', configValue: 1800, remark: '护理二级价格' },
      { configKey: 'BILL_NURSING_L3_PRICE', configValue: 2600, remark: '护理三级价格' }
    ],
    UTILITY: [
      { configKey: 'ALLOC_ELECTRIC_BASE_PRICE', configValue: 1, remark: '电费基础单价（元/度）' },
      { configKey: 'ALLOC_ELECTRIC_FREE_KWH_PER_PERSON', configValue: 10, remark: '每人每月免费电量（度）' },
      { configKey: 'ALLOC_WATER_BASE_PRICE', configValue: 0, remark: '水费基础单价（默认0）' }
    ]
  }[type]
  const confirmed = await confirmWithImpact(
    templates.map(item => item.configKey),
    '确认应用模板规则？',
    `模板类型：${type}，共 ${templates.length} 条规则`
  )
  if (!confirmed) return
  saving.value = true
  try {
    await batchUpsertFinanceBillingConfig({
      items: templates.map(item => ({
        configKey: item.configKey,
        configValue: item.configValue,
        effectiveMonth: month,
        status: 1,
        remark: item.remark
      }))
    })
    message.success('模板已应用')
    loadData()
  } finally {
    saving.value = false
  }
}

function exportData() {
  exportCsv(
    filteredRows.value.map(item => ({
      配置键: item.configKey,
      配置值: item.configValue,
      生效月份: item.effectiveMonth,
      状态: Number(item.status) === 1 ? '启用' : '停用',
      备注: item.remark || ''
    })),
    `账单计费规则-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}

function printCurrent() {
  try {
    printTableReport({
      title: '账单模板/计费规则',
      subtitle: `生效月份：${dayjs(query.value.month).format('YYYY-MM')}；关键字：${query.value.keyword || '-'}；备注：${query.value.printRemark || '-'}`,
      columns: [
        { key: 'configKey', title: '配置键' },
        { key: 'configValue', title: '配置值' },
        { key: 'effectiveMonth', title: '生效月份' },
        { key: 'statusText', title: '状态' },
        { key: 'remark', title: '备注' }
      ],
      rows: filteredRows.value.map(item => ({
        configKey: item.configKey || '-',
        configValue: item.configValue ?? 0,
        effectiveMonth: item.effectiveMonth || '-',
        statusText: Number(item.status) === 1 ? '启用' : '停用',
        remark: item.remark || '-'
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
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
    const keyIndex = columns.findIndex(item => item === 'configKey')
    const valueIndex = columns.findIndex(item => item === 'configValue')
    const monthIndex = columns.findIndex(item => item === 'effectiveMonth')
    const statusIndex = columns.findIndex(item => item === 'status')
    const remarkIndex = columns.findIndex(item => item === 'remark')
    if (keyIndex < 0 || valueIndex < 0 || monthIndex < 0) {
      message.warning('CSV 必须包含 configKey, configValue, effectiveMonth 列')
      return
    }
    const items = body.map(line => parseCsvLine(line)).map(row => ({
      configKey: normalizeConfigKey(row[keyIndex] || ''),
      configValue: Number(row[valueIndex] || 0),
      effectiveMonth: row[monthIndex] || dayjs(query.value.month).format('YYYY-MM'),
      status: statusIndex >= 0 ? Number(row[statusIndex] || 1) : 1,
      remark: remarkIndex >= 0 ? row[remarkIndex] : undefined
    })).filter(item => item.configKey && validateConfigKey(item.configKey))
    if (!items.length) {
      message.warning('CSV 无有效记录')
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
