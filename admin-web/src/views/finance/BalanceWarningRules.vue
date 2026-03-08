<template>
  <PageContainer title="余额预警规则" subTitle="押金/预存阈值、通知对象与催缴策略配置">
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
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="欠费长者" :value="summary.totalCount" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="待充值预警" :value="summary.pendingCount" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="到期合同风险" :value="summary.exceptionCount" /></a-card></a-col>
        <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="欠费金额" :value="summary.monthAmount" suffix="元" :precision="2" /></a-card></a-col>
      </a-row>
      <a-alert v-if="summary.warningMessage" style="margin-top: 12px;" type="warning" show-icon :message="summary.warningMessage" />
      <a-alert
        style="margin-top: 12px;"
        :type="healthIssues.length ? 'warning' : 'success'"
        show-icon
        :message="healthIssues.length ? `规则健康检查发现 ${healthIssues.length} 项风险` : '规则健康检查通过'"
        :description="healthIssues.length ? healthIssues.join('；') : '阈值、通知对象、催缴联动配置均正常'"
      />
      <a-card class="card-elevated" :bordered="false" style="margin-top: 12px;" title="风险摘要明细">
        <vxe-table border stripe show-overflow :data="summary.topItems || []" height="220">
          <vxe-column field="label" title="维度" min-width="180" />
          <vxe-column field="count" title="数量" width="120" />
          <vxe-column field="amount" title="金额（元）" width="160" />
        </vxe-table>
      </a-card>

      <a-row :gutter="[16, 16]" style="margin-top: 16px;">
        <a-col :xs="24" :xl="8">
          <a-card class="card-elevated" :bordered="false" title="阈值配置">
            <a-form layout="vertical">
              <a-form-item label="账户余额预警阈值（元）">
                <a-input-number v-model:value="thresholdValue" style="width: 100%" :min="0" />
              </a-form-item>
              <a-form-item>
                <a-button type="primary" :loading="saving" @click="saveThreshold">保存阈值</a-button>
              </a-form-item>
            </a-form>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="8">
          <a-card class="card-elevated" :bordered="false" title="通知对象">
            <a-space direction="vertical" style="width: 100%">
              <a-checkbox v-model:checked="notifyOps">通知运营</a-checkbox>
              <a-checkbox v-model:checked="notifyFamily">通知家属端</a-checkbox>
              <a-checkbox v-model:checked="notifyCashier">通知财务收银</a-checkbox>
              <a-button type="primary" :loading="saving" @click="saveNotifyTargets">保存通知配置</a-button>
            </a-space>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="8">
          <a-card class="card-elevated" :bordered="false" title="联动入口">
            <a-space direction="vertical" style="width: 100%">
              <a-button @click="go('/finance/accounts/list?filter=low_balance')">低余额账户</a-button>
              <a-button @click="go('/finance/bills/auto-deduct?filter=active')">自动催缴管理</a-button>
              <a-button @click="go('/finance/workbench')">返回财务工作台</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="规则快照">
        <vxe-table border stripe show-overflow :data="rows" height="420">
          <vxe-column field="configKey" title="配置键" min-width="280" />
          <vxe-column field="configValue" title="配置值" width="140" />
          <vxe-column field="effectiveMonth" title="生效月份" width="120" />
          <vxe-column field="status" title="状态" width="100">
            <template #default="{ row }">
              <a-tag :color="Number(row.status) === 1 ? 'green' : 'default'">{{ Number(row.status) === 1 ? '启用' : '停用' }}</a-tag>
            </template>
          </vxe-column>
          <vxe-column field="remark" title="备注" min-width="200" />
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
const rows = ref<FinanceBillingConfigEntry[]>([])
const summary = ref<FinanceModuleEntrySummary>({
  moduleKey: 'BALANCE_WARN',
  bizDate: '',
  todayAmount: 0,
  monthAmount: 0,
  totalCount: 0,
  pendingCount: 0,
  exceptionCount: 0,
  warningMessage: '',
  topItems: []
})
const query = ref({
  month: dayjs()
})
const importInputRef = ref<HTMLInputElement | null>(null)

const thresholdValue = ref<number>(0)
const notifyOps = ref<boolean>(true)
const notifyFamily = ref<boolean>(false)
const notifyCashier = ref<boolean>(true)
const healthIssues = ref<string[]>([])

function go(path: string) {
  router.push(path)
}

function effectiveMonth() {
  return dayjs(query.value.month).format('YYYY-MM')
}

async function loadData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const [configRows, summaryData] = await Promise.all([
      getFinanceBillingConfig({
        month: effectiveMonth(),
        keyPrefix: 'ACCOUNT_WARN'
      }),
      getFinanceModuleEntrySummary({
        moduleKey: 'BALANCE_WARN'
      })
    ])
    rows.value = configRows || []
    summary.value = summaryData
    thresholdValue.value = Number(findValue('ACCOUNT_WARN_THRESHOLD') || 0)
    notifyOps.value = Number(findValue('ACCOUNT_WARN_NOTIFY_OPS') || 0) === 1
    notifyFamily.value = Number(findValue('ACCOUNT_WARN_NOTIFY_FAMILY') || 0) === 1
    notifyCashier.value = Number(findValue('ACCOUNT_WARN_NOTIFY_CASHIER') || 0) === 1
    healthIssues.value = buildHealthIssues()
  } catch (error: any) {
    errorMessage.value = error?.message || '加载余额预警配置失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

function buildHealthIssues() {
  const issues: string[] = []
  if (Number(thresholdValue.value || 0) <= 0) {
    issues.push('余额预警阈值为 0，无法提前触发预警')
  }
  if (!notifyOps.value && !notifyFamily.value && !notifyCashier.value) {
    issues.push('通知对象全部关闭，预警无法触达')
  }
  if (summary.value.pendingCount > 0 && !notifyCashier.value) {
    issues.push('低余额预警较多但未通知收银，可能影响扣费成功率')
  }
  return issues
}

function findValue(key: string) {
  return rows.value.find(item => item.configKey === key)?.configValue
}

async function saveThreshold() {
  saving.value = true
  try {
    await upsertFinanceBillingConfig({
      configKey: 'ACCOUNT_WARN_THRESHOLD',
      configValue: Number(thresholdValue.value || 0),
      effectiveMonth: effectiveMonth(),
      status: 1,
      remark: '余额预警阈值'
    })
    message.success('阈值已保存')
    loadData()
  } finally {
    saving.value = false
  }
}

async function saveNotifyTargets() {
  saving.value = true
  try {
    await Promise.all([
      upsertFinanceBillingConfig({
        configKey: 'ACCOUNT_WARN_NOTIFY_OPS',
        configValue: notifyOps.value ? 1 : 0,
        effectiveMonth: effectiveMonth(),
        status: 1,
        remark: '是否通知运营'
      }),
      upsertFinanceBillingConfig({
        configKey: 'ACCOUNT_WARN_NOTIFY_FAMILY',
        configValue: notifyFamily.value ? 1 : 0,
        effectiveMonth: effectiveMonth(),
        status: 1,
        remark: '是否通知家属端'
      }),
      upsertFinanceBillingConfig({
        configKey: 'ACCOUNT_WARN_NOTIFY_CASHIER',
        configValue: notifyCashier.value ? 1 : 0,
        effectiveMonth: effectiveMonth(),
        status: 1,
        remark: '是否通知财务收银'
      })
    ])
    message.success('通知配置已保存')
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
    `余额预警规则-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
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
      effectiveMonth: monthIndex >= 0 && row[monthIndex] ? row[monthIndex] : effectiveMonth(),
      status: statusIndex >= 0 ? Number(row[statusIndex] || 1) : 1,
      remark: remarkIndex >= 0 ? row[remarkIndex] : undefined
    })).filter(item => /^ACCOUNT_WARN_[A-Z0-9_]{2,48}$/.test(item.configKey))
    if (!items.length) {
      message.warning('CSV 无有效余额预警配置')
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
