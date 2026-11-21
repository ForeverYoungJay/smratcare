<template>
  <PageContainer title="账单模板/计费规则" subTitle="费用科目、价格表、计费规则与减免策略配置">
    <a-card class="card-elevated" :bordered="false">
      <QueryBar layout="inline" :model="query" @finish="loadData">
        <a-form-item label="生效月份">
          <a-date-picker v-model:value="query.month" :allow-clear="false" picker="month" style="width: 150px" />
        </a-form-item>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" allow-clear placeholder="收费项目/备注" style="width: 220px" />
        </a-form-item>
        <a-form-item label="打印备注">
          <a-input v-model:value="query.printRemark" allow-clear placeholder="例如：规则评审版" style="width: 180px" />
        </a-form-item>
        <template #actions>
          <ActionToolbar>
            <a-space>
              <a-button type="primary" html-type="submit">查询</a-button>
              <a-button @click="openEdit()">新增规则</a-button>
              <a-button @click="batchToggleStatus(1)">批量启用</a-button>
              <a-button @click="batchToggleStatus(0)">批量停用</a-button>
              <a-button @click="exportData">导出</a-button>
              <a-button @click="printCurrent">打印当前</a-button>
              <a-button @click="triggerImport">导入CSV</a-button>
              <a-button @click="go('/finance/config/change-log')">查看变更记录</a-button>
            </a-space>
          </ActionToolbar>
        </template>
      </QueryBar>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 12px;">
      <a-row :gutter="[12, 12]">
        <a-col :xs="24" :xl="12">
          <ActionToolbar wrap>
            <a-button :loading="saving" :disabled="loading || !!loadError" @click="applyTemplate('STANDARD')">补齐基础收费标准</a-button>
            <a-button v-if="canManageCareFees" @click="careFeesOpen = true">护理等级收费</a-button>
            <a-button :loading="saving" :disabled="loading || !!loadError" @click="applyTemplate('UTILITY')">补齐水电分摊标准</a-button>
          </ActionToolbar>
        </a-col>
        <a-col :xs="24" :xl="12">
          <a-alert
            :type="healthWarnings.length ? 'warning' : 'success'"
            show-icon
            :message="healthWarnings.length ? `规则健康检查发现 ${healthWarnings.length} 项风险` : '规则健康检查通过'"
            :description="healthWarnings.length ? healthWarnings.join('；') : '收费项目、启停状态和标准值均正常'"
          />
        </a-col>
      </a-row>
    </a-card>
    <a-alert style="margin-top: 12px" type="info" show-icon message="床位费按月维护；餐费月标准大于零时优先按月计费，否则使用日标准。护理费按护理等级单独维护，账单按实际计费天数折算。" />
    <a-alert v-if="legacyRules.length" style="margin-top: 12px" type="warning" show-icon :message="`发现 ${legacyRules.length} 条旧版收费模板规则`" description="BILL_ 开头的旧模板不作为现行床位费、餐费或护理费的计价来源。请核对现行标准后维护，原记录继续保留。" />
    <a-alert v-if="loadError" style="margin-top:12px" type="error" show-icon :message="loadError" />
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
      <vxe-table ref="tableRef" border stripe show-overflow="title" :loading="loading" :data="filteredRows" height="560">
        <vxe-column type="checkbox" width="50" />
        <vxe-column field="configKey" title="收费项目" min-width="230">
          <template #default="{ row }">{{ billingRuleLabel(row.configKey, row.remark) }}</template>
        </vxe-column>
        <vxe-column field="configValue" title="标准值" width="150" />
        <vxe-column title="计费单位" width="130"><template #default="{ row }">{{ billingRuleUnit(row.configKey) }}</template></vxe-column>
        <vxe-column field="effectiveMonth" title="生效月份" width="120" />
        <vxe-column field="status" title="状态" width="100">
          <template #default="{ row }">
            <a-tag :color="Number(row.status) === 1 ? 'green' : 'default'">{{ Number(row.status) === 1 ? '启用' : '停用' }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="remark" title="备注" min-width="220" />
        <vxe-column title="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="row-action-links">
              <a-button type="link" size="small" @click="openEdit(row)">编辑</a-button>
            </div>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>
    <CareLevelFeeEditor v-if="careFeesOpen" :month="dayjs(query.month).format('YYYY-MM')" @close="careFeesOpen = false" />

    <a-modal :mask-closable="false" v-model:open="editOpen" title="计费规则" @ok="submitEdit" :confirm-loading="saving">
      <a-form layout="vertical" :model="form">
        <a-form-item label="收费项目" required>
          <a-select v-if="!customRule" v-model:value="form.configKey" :options="billingRuleOptions" :disabled="editing" placeholder="选择要维护的收费项目" />
          <a-input v-else v-model:value="form.configKey" :disabled="editing" placeholder="请输入已有业务使用的规则编码" />
          <a-checkbox v-if="!editing" v-model:checked="customRule" @change="form.configKey = ''">维护其他业务规则</a-checkbox>
        </a-form-item>
        <a-form-item :label="`收费标准（${billingRuleUnit(form.configKey)}）`" required>
          <a-input-number v-model:value="form.configValue" style="width: 100%" :min="0" :precision="2" />
        </a-form-item>
        <a-form-item label="生效月份">
          <a-date-picker v-model:value="form.effectiveMonth" :allow-clear="false" picker="month" style="width: 100%" />
        </a-form-item>
        <a-form-item label="状态">
          <a-switch v-model:checked="formEnabled" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="form.remark" />
        </a-form-item>
        <a-alert type="info" show-icon :message="`${billingRuleLabel(form.configKey) || '请选择收费项目'}：${form.configValue ?? '待填写'} ${billingRuleUnit(form.configKey)}`" description="修改同一生效月份会更新该月标准；选择其他月份会维护对应月份版本。保存前将展示影响范围，已有账单不会自动重算。" />
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import ActionToolbar from '../../components/ActionToolbar.vue'
import QueryBar from '../../components/QueryBar.vue'
import CareLevelFeeEditor from './components/CareLevelFeeEditor.vue'
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
import { billingRuleOptions, billingRuleLabel, billingRuleUnit, isBusinessBillingRule } from '../../constants/billingRules'
import { useUserStore } from '../../stores/user'

const loading = ref(false)
const saving = ref(false)
const router = useRouter()
const user = useUserStore()
const canManageCareFees = computed(() => user.roles.some(role => ['FINANCE_MINISTER', 'DIRECTOR', 'SYS_ADMIN', 'ADMIN'].includes(role)))
const careFeesOpen = ref(false)
const customRule = ref(false)
const editing = ref(false)
const loadError = ref('')
const legacyRules = computed(() => rows.value.filter(row => /^BILL_(ROOM|NURSING|DEPOSIT)/.test(row.configKey)))
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
  return rows.value.filter(item => (`${billingRuleLabel(item.configKey)} ${item.configKey || ''} ${item.remark || ''}`).toLowerCase().includes(keyword))
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
  loadError.value = ''
  try {
    rows.value = await getFinanceBillingConfig({
      month: dayjs(query.value.month).format('YYYY-MM')
    })
  } catch (error: any) {
    rows.value = []
    loadError.value = error?.message || '收费规则加载失败，请重新查询'
  } finally {
    loading.value = false
  }
}

async function confirmWithImpact(configKeys: string[], actionTitle: string, content: string, month = dayjs(query.value.month).format('YYYY-MM')) {
  try {
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
  editing.value = !!row
  customRule.value = !!row && !isBusinessBillingRule(row.configKey)
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
    status: Number(row.status ?? 1),
    remark: row.remark || ''
  }
  editOpen.value = true
}

async function submitEdit() {
  if (saving.value) return
  const normalizedKey = normalizeConfigKey(form.value.configKey)
  if (!normalizedKey) {
    message.warning('请输入配置键')
    return
  }
  if (!validateConfigKey(normalizedKey)) {
    message.warning('配置键仅支持大写字母、数字、下划线，长度 3-64')
    return
  }
  if (form.value.configValue == null || !Number.isFinite(Number(form.value.configValue)) || Number(form.value.configValue) < 0 || !form.value.effectiveMonth?.isValid()) {
    message.warning('请填写不小于零的收费标准，并选择有效的生效月份')
    return
  }
  const confirmed = await confirmWithImpact(
    [normalizedKey],
    '确认保存计费规则？',
    `即将保存 ${billingRuleLabel(normalizedKey)}，生效月份 ${dayjs(form.value.effectiveMonth).format('YYYY-MM')}`,
    dayjs(form.value.effectiveMonth).format('YYYY-MM')
  )
  if (!confirmed) return
  if (saving.value) return
  saving.value = true
  try {
    await upsertFinanceBillingConfig({
      configKey: normalizedKey,
      configValue: Number(form.value.configValue || 0),
      effectiveMonth: dayjs(form.value.effectiveMonth).format('YYYY-MM'),
      status: Number(form.value.status ?? 1),
      remark: form.value.remark || undefined
    })
    message.success('保存成功')
    editOpen.value = false
    query.value.month = dayjs(form.value.effectiveMonth)
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
  if (saving.value) return
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

async function applyTemplate(type: 'STANDARD' | 'UTILITY') {
  const month = dayjs(query.value.month).format('YYYY-MM')
  const templates = {
    STANDARD: [
      { configKey: 'BED_FEE_MONTHLY', configValue: 1800, remark: '床位费月标准' },
      { configKey: 'MEAL_FEE_MONTHLY', configValue: 0, remark: '餐费月标准（0 时使用日标准）' },
      { configKey: 'MEAL_FEE_PER_DAY', configValue: 0, remark: '餐费日标准' }
    ],
    UTILITY: [
      { configKey: 'ALLOC_ELECTRIC_BASE_PRICE', configValue: 1, remark: '电费基础单价（元/度）' },
      { configKey: 'ALLOC_ELECTRIC_FREE_KWH_PER_PERSON', configValue: 10, remark: '每人每月免费电量（度）' },
      { configKey: 'ALLOC_WATER_BASE_PRICE', configValue: 0, remark: '水费基础单价（默认0）' }
    ]
  }[type].filter(item => !rows.value.some(row => row.configKey === item.configKey))
  if (!templates.length) {
    message.info('本月已包含这些收费标准，请直接编辑已有项目')
    return
  }
  const confirmed = await confirmWithImpact(
    templates.map(item => item.configKey),
    '确认应用模板规则？',
    `将补齐 ${month} 缺少的 ${templates.length} 项：${templates.map(item => `${billingRuleLabel(item.configKey)} ${item.configValue} ${billingRuleUnit(item.configKey)}`).join('；')}。已有项目不会被覆盖。`
  )
  if (!confirmed) return
  if (saving.value) return
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
    if (saving.value) return
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
