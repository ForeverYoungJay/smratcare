<template>
  <PageContainer title="权限与审批流配置" subTitle="减免/退款/冲正审批流与审批角色配置">
    <a-card class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-date-picker v-model:value="query.month" picker="month" style="width: 150px" />
        <a-button type="primary" @click="loadData">刷新</a-button>
        <a-button @click="exportData">导出</a-button>
        <a-button @click="triggerImport">导入CSV</a-button>
        <a-button @click="go('/finance/config/change-log')">查看变更记录</a-button>
      </a-space>
    </a-card>
    <input ref="importInputRef" type="file" accept=".csv,text/csv" style="display: none" @change="onImportFileChange" />

    <StatefulBlock style="margin-top: 16px;" :loading="loading" :error="errorMessage" @retry="loadData">
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="审批待办总数" :value="summary.totalCount" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="待处理事项" :value="summary.pendingCount" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="今日关联金额" :value="summary.todayAmount" suffix="元" :precision="2" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="本月关联金额" :value="summary.monthAmount" suffix="元" :precision="2" /></a-card></a-col>
      </a-row>
      <a-alert v-if="summary.warningMessage" style="margin-top: 12px;" type="warning" show-icon :message="summary.warningMessage" />
      <a-card class="card-elevated" :bordered="false" style="margin-top: 12px;" title="审批摘要明细">
        <vxe-table border stripe show-overflow :data="summary.topItems || []" height="220">
          <vxe-column field="label" title="维度" min-width="180" />
          <vxe-column field="count" title="数量" width="120" />
          <vxe-column field="amount" title="金额（元）" width="160" />
        </vxe-table>
      </a-card>

      <a-row :gutter="[16, 16]" style="margin-top: 16px;">
        <a-col :xs="24" :xl="8">
          <a-card class="card-elevated" :bordered="false" title="审批角色">
            <a-form layout="vertical">
              <a-form-item label="减免审批角色">
                <a-input v-model:value="roles.discountRole" placeholder="如 FINANCE_MANAGER" />
              </a-form-item>
              <a-form-item label="退款审批角色">
                <a-input v-model:value="roles.refundRole" placeholder="如 FINANCE_MANAGER" />
              </a-form-item>
              <a-form-item label="冲正审批角色">
                <a-input v-model:value="roles.reversalRole" placeholder="如 ADMIN" />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" :loading="saving" @click="saveRoles">保存角色配置</a-button>
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="8">
          <a-card class="card-elevated" :bordered="false" title="审批级数">
            <a-form layout="vertical">
              <a-form-item label="减免审批级数">
                <a-input-number v-model:value="levels.discountLevel" style="width: 100%" :min="1" :max="5" />
              </a-form-item>
              <a-form-item label="退款审批级数">
                <a-input-number v-model:value="levels.refundLevel" style="width: 100%" :min="1" :max="5" />
              </a-form-item>
              <a-form-item label="冲正审批级数">
                <a-input-number v-model:value="levels.reversalLevel" style="width: 100%" :min="1" :max="5" />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" :loading="saving" @click="saveLevels">保存级数配置</a-button>
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="8">
          <a-card class="card-elevated" :bordered="false" title="审批联动">
            <a-space direction="vertical" style="width: 100%">
              <a-button @click="go('/oa/approval?module=finance&status=pending')">审批中心（财务）</a-button>
              <a-button @click="go('/finance/reconcile-exception')">对账异常处理</a-button>
              <a-button @click="go('/finance/config/change-log')">查看配置变更记录</a-button>
              <a-button @click="go('/finance/workbench')">返回财务工作台</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="配置快照">
        <vxe-table border stripe show-overflow :data="rows" height="420">
          <vxe-column field="configKey" title="配置键" min-width="280" />
          <vxe-column field="configValue" title="配置值" width="120" />
          <vxe-column field="effectiveMonth" title="生效月份" width="120" />
          <vxe-column field="remark" title="备注" min-width="220" />
        </vxe-table>
      </a-card>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { batchUpsertFinanceBillingConfig, getFinanceBillingConfig, getFinanceModuleEntrySummary, upsertFinanceBillingConfig } from '../../api/finance'
import type { FinanceBillingConfigEntry, FinanceModuleEntrySummary } from '../../types'
import { exportCsv } from '../../utils/export'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const query = ref({ month: dayjs() })
const rows = ref<FinanceBillingConfigEntry[]>([])
const importInputRef = ref<HTMLInputElement | null>(null)
const summary = ref<FinanceModuleEntrySummary>({
  moduleKey: 'APPROVAL_FLOW',
  bizDate: '',
  todayAmount: 0,
  monthAmount: 0,
  totalCount: 0,
  pendingCount: 0,
  exceptionCount: 0,
  warningMessage: '',
  topItems: []
})

const roles = ref({
  discountRole: '',
  refundRole: '',
  reversalRole: ''
})

const levels = ref({
  discountLevel: 1,
  refundLevel: 1,
  reversalLevel: 1
})

function go(path: string) {
  router.push(path)
}

function monthText() {
  return dayjs(query.value.month).format('YYYY-MM')
}

function findValue(key: string) {
  return rows.value.find(item => item.configKey === key)
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [configRows, summaryData] = await Promise.all([
      getFinanceBillingConfig({
        month: monthText(),
        keyPrefix: 'FIN_APPROVAL_'
      }),
      getFinanceModuleEntrySummary({
        moduleKey: 'APPROVAL_FLOW'
      })
    ])
    rows.value = configRows || []
    summary.value = summaryData
    roles.value.discountRole = findValue('FIN_APPROVAL_DISCOUNT_ROLE')?.remark || 'FINANCE_MANAGER'
    roles.value.refundRole = findValue('FIN_APPROVAL_REFUND_ROLE')?.remark || 'FINANCE_MANAGER'
    roles.value.reversalRole = findValue('FIN_APPROVAL_REVERSAL_ROLE')?.remark || 'ADMIN'
    levels.value.discountLevel = Number(findValue('FIN_APPROVAL_DISCOUNT_LEVEL')?.configValue || 1)
    levels.value.refundLevel = Number(findValue('FIN_APPROVAL_REFUND_LEVEL')?.configValue || 1)
    levels.value.reversalLevel = Number(findValue('FIN_APPROVAL_REVERSAL_LEVEL')?.configValue || 1)
  } catch (error: any) {
    errorMessage.value = error?.message || '加载审批流配置失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function saveRoles() {
  const discountRole = String(roles.value.discountRole || '').trim().toUpperCase() || 'FINANCE_MANAGER'
  const refundRole = String(roles.value.refundRole || '').trim().toUpperCase() || 'FINANCE_MANAGER'
  const reversalRole = String(roles.value.reversalRole || '').trim().toUpperCase() || 'ADMIN'
  if (![discountRole, refundRole, reversalRole].every(item => /^[A-Z0-9_]{3,32}$/.test(item))) {
    message.warning('审批角色仅支持大写字母、数字、下划线，长度 3-32')
    return
  }
  saving.value = true
  try {
    await Promise.all([
      upsertFinanceBillingConfig({
        configKey: 'FIN_APPROVAL_DISCOUNT_ROLE',
        configValue: 0,
        effectiveMonth: monthText(),
        status: 1,
        remark: discountRole
      }),
      upsertFinanceBillingConfig({
        configKey: 'FIN_APPROVAL_REFUND_ROLE',
        configValue: 0,
        effectiveMonth: monthText(),
        status: 1,
        remark: refundRole
      }),
      upsertFinanceBillingConfig({
        configKey: 'FIN_APPROVAL_REVERSAL_ROLE',
        configValue: 0,
        effectiveMonth: monthText(),
        status: 1,
        remark: reversalRole
      })
    ])
    message.success('审批角色已保存')
    loadData()
  } finally {
    saving.value = false
  }
}

async function saveLevels() {
  saving.value = true
  try {
    await Promise.all([
      upsertFinanceBillingConfig({
        configKey: 'FIN_APPROVAL_DISCOUNT_LEVEL',
        configValue: Number(levels.value.discountLevel || 1),
        effectiveMonth: monthText(),
        status: 1,
        remark: '减免审批级数'
      }),
      upsertFinanceBillingConfig({
        configKey: 'FIN_APPROVAL_REFUND_LEVEL',
        configValue: Number(levels.value.refundLevel || 1),
        effectiveMonth: monthText(),
        status: 1,
        remark: '退款审批级数'
      }),
      upsertFinanceBillingConfig({
        configKey: 'FIN_APPROVAL_REVERSAL_LEVEL',
        configValue: Number(levels.value.reversalLevel || 1),
        effectiveMonth: monthText(),
        status: 1,
        remark: '冲正审批级数'
      })
    ])
    message.success('审批级数已保存')
    loadData()
  } finally {
    saving.value = false
  }
}

function exportData() {
  exportCsv(
    rows.value.map(item => ({
      configKey: item.configKey,
      configValue: item.configValue,
      effectiveMonth: item.effectiveMonth,
      status: Number(item.status || 0),
      remark: item.remark || ''
    })),
    `审批流配置-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
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
    const keyIndex = columns.findIndex(item => item === 'configKey')
    const valueIndex = columns.findIndex(item => item === 'configValue')
    const monthIndex = columns.findIndex(item => item === 'effectiveMonth')
    const statusIndex = columns.findIndex(item => item === 'status')
    const remarkIndex = columns.findIndex(item => item === 'remark')
    if (keyIndex < 0 || valueIndex < 0) {
      message.warning('CSV 必须包含 configKey, configValue 列')
      return
    }
    const items = body.map(line => parseCsvLine(line)).map(row => ({
      configKey: String(row[keyIndex] || '').trim().toUpperCase(),
      configValue: Number(row[valueIndex] || 0),
      effectiveMonth: monthIndex >= 0 && row[monthIndex] ? row[monthIndex] : monthText(),
      status: statusIndex >= 0 ? Number(row[statusIndex] || 1) : 1,
      remark: remarkIndex >= 0 ? row[remarkIndex] : undefined
    })).filter(item => /^FIN_APPROVAL_[A-Z0-9_]{2,64}$/.test(item.configKey))
    if (!items.length) {
      message.warning('CSV 无有效审批流配置')
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
