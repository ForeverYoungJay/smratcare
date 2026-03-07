<template>
  <PageContainer title="权限与审批流配置" subTitle="减免/退款/冲正审批流与审批角色配置">
    <a-card class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-date-picker v-model:value="query.month" picker="month" style="width: 150px" />
        <a-input v-model:value="query.printRemark" allow-clear placeholder="打印备注" style="width: 180px" />
        <a-button type="primary" @click="loadData">刷新</a-button>
        <a-button @click="exportData">导出</a-button>
        <a-button @click="printCurrent">打印当前</a-button>
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
              <a-button @click="go('/finance/reconcile/exception')">对账异常处理</a-button>
              <a-button @click="go('/finance/config/change-log')">查看配置变更记录</a-button>
              <a-button @click="go('/finance/workbench')">返回财务工作台</a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="[16, 16]" style="margin-top: 16px;">
        <a-col :xs="24" :xl="12">
          <a-card class="card-elevated" :bordered="false" title="审批流模板推荐">
            <a-space wrap>
              <a-button :loading="saving" @click="applyRecommendedTemplate('STANDARD')">标准模板（推荐）</a-button>
              <a-button :loading="saving" @click="applyRecommendedTemplate('STRICT')">严格模板</a-button>
              <a-button :loading="saving" @click="applyRecommendedTemplate('FAST')">快速模板</a-button>
            </a-space>
            <a-divider />
            <a-descriptions :column="1" size="small" bordered>
              <a-descriptions-item label="标准模板">减免2级 / 退款2级 / 冲正2级，角色：财务经理+管理员</a-descriptions-item>
              <a-descriptions-item label="严格模板">减免3级 / 退款3级 / 冲正2级，适合大额管控</a-descriptions-item>
              <a-descriptions-item label="快速模板">减免1级 / 退款1级 / 冲正1级，适合小机构高时效</a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>
        <a-col :xs="24" :xl="12">
          <a-card class="card-elevated" :bordered="false" title="配置健康检查">
            <a-alert
              :type="healthIssues.length ? 'warning' : 'success'"
              show-icon
              :message="healthIssues.length ? `发现 ${healthIssues.length} 条配置风险` : '审批流配置健康'"
              :description="healthIssues.length ? healthIssues.join('；') : '角色、级数、流程口径均已通过基础校验'"
            />
            <a-divider />
            <a-space wrap>
              <a-tag color="blue">减免级数 {{ levels.discountLevel }}</a-tag>
              <a-tag color="purple">退款级数 {{ levels.refundLevel }}</a-tag>
              <a-tag color="orange">冲正级数 {{ levels.reversalLevel }}</a-tag>
              <a-tag color="green">建议角色唯一化</a-tag>
            </a-space>
          </a-card>
        </a-col>
      </a-row>
      <a-card v-if="impactPreview" class="card-elevated" :bordered="false" style="margin-top: 12px;">
        <a-alert
          type="info"
          show-icon
          :message="`最近预览：${impactPreview.month}，影响键 ${impactPreview.configKeyCount} 个`"
          :description="`账单 ${impactPreview.activeBillCount} 条，欠费 ${impactPreview.highRiskBillCount} 条，待审批 ${impactPreview.pendingApprovalCount} 条`"
        />
      </a-card>

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
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import StatefulBlock from '../../components/StatefulBlock.vue'
import { batchUpsertFinanceBillingConfig, getFinanceBillingConfig, getFinanceConfigImpactPreview, getFinanceModuleEntrySummary, upsertFinanceBillingConfig } from '../../api/finance'
import type { FinanceBillingConfigEntry, FinanceConfigImpactPreview, FinanceModuleEntrySummary } from '../../types'
import { exportCsv } from '../../utils/export'
import { printTableReport } from '../../utils/print'
import { confirmAction } from '../../utils/actionConfirm'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const errorMessage = ref('')
const query = ref({ month: dayjs(), printRemark: '' })
const rows = ref<FinanceBillingConfigEntry[]>([])
const importInputRef = ref<HTMLInputElement | null>(null)
const impactPreview = ref<FinanceConfigImpactPreview | null>(null)
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

const healthIssues = computed(() => {
  const issues: string[] = []
  const roleList = [roles.value.discountRole, roles.value.refundRole, roles.value.reversalRole]
    .map(item => String(item || '').trim().toUpperCase())
    .filter(Boolean)
  const uniqueRoleCount = new Set(roleList).size
  if (uniqueRoleCount <= 1) {
    issues.push('三类审批角色完全一致，建议至少拆分退款与冲正角色')
  }
  if (levels.value.refundLevel > levels.value.discountLevel + 1) {
    issues.push('退款级数显著高于减免级数，建议复核审批效率')
  }
  if (levels.value.discountLevel >= 4 || levels.value.refundLevel >= 4 || levels.value.reversalLevel >= 4) {
    issues.push('审批级数较高，可能导致审批超时')
  }
  if (levels.value.discountLevel <= 1 && levels.value.refundLevel <= 1 && levels.value.reversalLevel <= 1) {
    issues.push('全部为一级审批，存在风险集中放行')
  }
  return issues
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
  const roleKeys = ['FIN_APPROVAL_DISCOUNT_ROLE', 'FIN_APPROVAL_REFUND_ROLE', 'FIN_APPROVAL_REVERSAL_ROLE']
  let preview: FinanceConfigImpactPreview
  let fallbackConfirmed = false
  try {
    preview = await getFinanceConfigImpactPreview({
      month: monthText(),
      configKeys: roleKeys
    })
    impactPreview.value = preview
  } catch (error: any) {
    message.warning(error?.message || '影响预览加载失败，已切换普通确认')
    const confirmedFallback = await confirmAction({
      title: '确认保存审批角色配置？',
      content: `减免:${discountRole} / 退款:${refundRole} / 冲正:${reversalRole}`,
      okText: '确认保存'
    })
    if (!confirmedFallback) return
    fallbackConfirmed = true
    preview = {
      month: monthText(),
      activeBillCount: 0,
      activeElderCount: 0,
      highRiskBillCount: 0,
      pendingApprovalCount: 0,
      recentPaymentCount: 0,
      allocationTaskCount: 0,
      configKeyCount: roleKeys.length,
      impactedItems: [],
      riskTips: []
    }
  }
  if (!fallbackConfirmed) {
    const confirmed = await confirmAction({
      title: '确认保存审批角色配置？',
      content: `减免:${discountRole} / 退款:${refundRole} / 冲正:${reversalRole}`,
      impactItems: [
        `待审批 ${preview.pendingApprovalCount} 条，账单 ${preview.activeBillCount} 条`,
        ...preview.riskTips.slice(0, 2)
      ],
      okText: '确认保存'
    })
    if (!confirmed) return
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
  const levelKeys = ['FIN_APPROVAL_DISCOUNT_LEVEL', 'FIN_APPROVAL_REFUND_LEVEL', 'FIN_APPROVAL_REVERSAL_LEVEL']
  let preview: FinanceConfigImpactPreview
  let fallbackConfirmed = false
  try {
    preview = await getFinanceConfigImpactPreview({
      month: monthText(),
      configKeys: levelKeys
    })
    impactPreview.value = preview
  } catch (error: any) {
    message.warning(error?.message || '影响预览加载失败，已切换普通确认')
    const confirmedFallback = await confirmAction({
      title: '确认保存审批级数配置？',
      content: `减免:${levels.value.discountLevel}级 / 退款:${levels.value.refundLevel}级 / 冲正:${levels.value.reversalLevel}级`,
      okText: '确认保存'
    })
    if (!confirmedFallback) return
    fallbackConfirmed = true
    preview = {
      month: monthText(),
      activeBillCount: 0,
      activeElderCount: 0,
      highRiskBillCount: 0,
      pendingApprovalCount: 0,
      recentPaymentCount: 0,
      allocationTaskCount: 0,
      configKeyCount: levelKeys.length,
      impactedItems: [],
      riskTips: []
    }
  }
  if (!fallbackConfirmed) {
    const confirmed = await confirmAction({
      title: '确认保存审批级数配置？',
      content: `减免:${levels.value.discountLevel}级 / 退款:${levels.value.refundLevel}级 / 冲正:${levels.value.reversalLevel}级`,
      impactItems: [
        `待审批 ${preview.pendingApprovalCount} 条，欠费 ${preview.highRiskBillCount} 条`,
        ...preview.riskTips.slice(0, 2)
      ],
      okText: '确认保存'
    })
    if (!confirmed) return
  }
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

async function applyRecommendedTemplate(type: 'STANDARD' | 'STRICT' | 'FAST') {
  const mapping = {
    STANDARD: {
      discountRole: 'FINANCE_MANAGER',
      refundRole: 'FINANCE_MANAGER',
      reversalRole: 'ADMIN',
      discountLevel: 2,
      refundLevel: 2,
      reversalLevel: 2
    },
    STRICT: {
      discountRole: 'FINANCE_MANAGER',
      refundRole: 'FINANCE_DIRECTOR',
      reversalRole: 'ADMIN',
      discountLevel: 3,
      refundLevel: 3,
      reversalLevel: 2
    },
    FAST: {
      discountRole: 'FINANCE_MANAGER',
      refundRole: 'FINANCE_MANAGER',
      reversalRole: 'FINANCE_MANAGER',
      discountLevel: 1,
      refundLevel: 1,
      reversalLevel: 1
    }
  }[type]
  roles.value.discountRole = mapping.discountRole
  roles.value.refundRole = mapping.refundRole
  roles.value.reversalRole = mapping.reversalRole
  levels.value.discountLevel = mapping.discountLevel
  levels.value.refundLevel = mapping.refundLevel
  levels.value.reversalLevel = mapping.reversalLevel
  const templateKeys = [
    'FIN_APPROVAL_DISCOUNT_ROLE',
    'FIN_APPROVAL_REFUND_ROLE',
    'FIN_APPROVAL_REVERSAL_ROLE',
    'FIN_APPROVAL_DISCOUNT_LEVEL',
    'FIN_APPROVAL_REFUND_LEVEL',
    'FIN_APPROVAL_REVERSAL_LEVEL'
  ]
  let preview: FinanceConfigImpactPreview
  let fallbackConfirmed = false
  try {
    preview = await getFinanceConfigImpactPreview({
      month: monthText(),
      configKeys: templateKeys
    })
    impactPreview.value = preview
  } catch (error: any) {
    message.warning(error?.message || '影响预览加载失败，已切换普通确认')
    const confirmedFallback = await confirmAction({
      title: '确认应用审批模板？',
      content: `模板：${type}，将覆盖角色与级数配置`,
      okText: '确认应用'
    })
    if (!confirmedFallback) return
    fallbackConfirmed = true
    preview = {
      month: monthText(),
      activeBillCount: 0,
      activeElderCount: 0,
      highRiskBillCount: 0,
      pendingApprovalCount: 0,
      recentPaymentCount: 0,
      allocationTaskCount: 0,
      configKeyCount: templateKeys.length,
      impactedItems: [],
      riskTips: []
    }
  }
  if (!fallbackConfirmed) {
    const confirmed = await confirmAction({
      title: '确认应用审批模板？',
      content: `模板：${type}，将覆盖角色与级数配置`,
      impactItems: [
        `待审批 ${preview.pendingApprovalCount} 条，影响键 ${preview.configKeyCount} 个`,
        ...preview.riskTips.slice(0, 2)
      ],
      okText: '确认应用'
    })
    if (!confirmed) return
  }
  saving.value = true
  try {
    await batchUpsertFinanceBillingConfig({
      items: [
        { configKey: 'FIN_APPROVAL_DISCOUNT_ROLE', configValue: 0, effectiveMonth: monthText(), status: 1, remark: mapping.discountRole },
        { configKey: 'FIN_APPROVAL_REFUND_ROLE', configValue: 0, effectiveMonth: monthText(), status: 1, remark: mapping.refundRole },
        { configKey: 'FIN_APPROVAL_REVERSAL_ROLE', configValue: 0, effectiveMonth: monthText(), status: 1, remark: mapping.reversalRole },
        { configKey: 'FIN_APPROVAL_DISCOUNT_LEVEL', configValue: mapping.discountLevel, effectiveMonth: monthText(), status: 1, remark: '减免审批级数' },
        { configKey: 'FIN_APPROVAL_REFUND_LEVEL', configValue: mapping.refundLevel, effectiveMonth: monthText(), status: 1, remark: '退款审批级数' },
        { configKey: 'FIN_APPROVAL_REVERSAL_LEVEL', configValue: mapping.reversalLevel, effectiveMonth: monthText(), status: 1, remark: '冲正审批级数' }
      ]
    })
    message.success('模板已应用并保存')
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

function printCurrent() {
  try {
    printTableReport({
      title: '权限与审批流配置',
      subtitle: `月份：${monthText()}；备注：${query.value.printRemark || '-'}；待处理事项：${summary.value.pendingCount || 0}`,
      columns: [
        { key: 'configKey', title: '配置键' },
        { key: 'configValue', title: '配置值' },
        { key: 'effectiveMonth', title: '生效月份' },
        { key: 'remark', title: '备注' }
      ],
      rows: rows.value.map(item => ({
        configKey: item.configKey || '-',
        configValue: item.configValue ?? 0,
        effectiveMonth: item.effectiveMonth || '-',
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
