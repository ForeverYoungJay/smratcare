<template>
  <PageContainer title="财务主数据配置中心" subTitle="费用科目、计费规则、支付渠道与审批流配置总览">
    <a-card class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-date-picker v-model:value="query.month" picker="month" style="width: 150px" />
        <a-button type="primary" @click="loadData">刷新</a-button>
        <a-input v-model:value="query.printRemark" allow-clear placeholder="打印备注" style="width: 180px" />
        <a-button @click="exportCurrent">导出当前</a-button>
        <a-button @click="printCurrent">打印当前</a-button>
        <a-button @click="go('/finance/bills/rules')">计费规则入口</a-button>
        <a-button @click="go('/finance/accounts/warning-rules')">余额预警规则</a-button>
        <a-button @click="go('/finance/allocation/subjects')">费用科目字典</a-button>
        <a-button @click="go('/finance/config/payment-channels')">缴费渠道/收款账户</a-button>
        <a-button @click="go('/finance/config/approval-flow')">权限与审批流配置</a-button>
        <a-button @click="go('/finance/config/change-log')">配置变更记录</a-button>
      </a-space>
    </a-card>

    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="费用科目数" :value="overview?.feeSubjectCount || 0" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="计费规则数" :value="overview?.billingRuleCount || 0" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="支付渠道数" :value="overview?.paymentChannelCount || 0" /></a-card></a-col>
      <a-col :xs="24" :xl="6"><a-card class="card-elevated" :bordered="false"><a-statistic title="待审批配置" :value="overview?.pendingApprovalCount || 0" /></a-card></a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col :xs="24" :xl="8">
        <a-card class="card-elevated" :bordered="false" title="费用科目字典">
          <a-space direction="vertical" style="width: 100%">
            <span style="color: #64748b;">管理房费/护理费/医护费/水电电视网络等科目</span>
            <a-button type="primary" @click="go('/finance/allocation/subjects')">进入维护</a-button>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="8">
        <a-card class="card-elevated" :bordered="false" title="缴费渠道与收款账户">
          <a-space direction="vertical" style="width: 100%">
            <span style="color: #64748b;">管理渠道启停、收款户名与账户号</span>
            <a-button type="primary" @click="go('/finance/config/payment-channels')">进入维护</a-button>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="8">
        <a-card class="card-elevated" :bordered="false" title="权限与审批流配置">
          <a-space direction="vertical" style="width: 100%">
            <span style="color: #64748b;">配置减免/退款/冲正审批角色与审批级数</span>
            <a-button type="primary" @click="go('/finance/config/approval-flow')">进入维护</a-button>
          </a-space>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top: 16px;">
      <a-col :xs="24" :xl="8">
        <a-card class="card-elevated" :bordered="false" title="缴费渠道与收款账户">
          <a-space wrap>
            <a-tag v-for="channel in (overview?.paymentChannels || [])" :key="channel" color="blue">{{ channel }}</a-tag>
          </a-space>
        </a-card>
      </a-col>
      <a-col :xs="24" :xl="16">
        <a-card class="card-elevated" :bordered="false" title="近期主数据配置">
          <vxe-table border stripe show-overflow :loading="loading" :data="overview?.recentConfigs || []" height="360">
            <vxe-column field="configKey" title="配置键" min-width="240" />
            <vxe-column field="valueText" title="配置值" width="130" />
            <vxe-column field="effectiveMonth" title="生效月份" width="120" />
            <vxe-column field="status" title="状态" width="100">
              <template #default="{ row }">
                <a-tag :color="Number(row.status) === 1 ? 'green' : 'default'">{{ Number(row.status) === 1 ? '启用' : '停用' }}</a-tag>
              </template>
            </vxe-column>
            <vxe-column field="remark" title="备注" min-width="180" />
          </vxe-table>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="配置快照（月度版本）">
      <vxe-table border stripe show-overflow :loading="loading" :data="snapshots" height="260">
        <vxe-column field="effectiveMonth" title="月份" width="140" />
        <vxe-column field="configCount" title="配置数" width="120" />
        <vxe-column field="latestUpdateTime" title="最近更新时间" min-width="200" />
      </vxe-table>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;" title="配置变更影响模拟">
      <a-space wrap>
        <a-checkbox-group v-model:value="impactModules" :options="impactModuleOptions" />
        <a-button type="primary" :loading="impactLoading" @click="loadImpactPreview">模拟影响范围</a-button>
      </a-space>
      <a-alert
        v-if="impactPreview"
        style="margin-top: 12px;"
        type="info"
        show-icon
        :message="`模拟月份 ${impactPreview.month}：账单 ${impactPreview.activeBillCount} 条，长者 ${impactPreview.activeElderCount} 人`"
        :description="`高风险欠费 ${impactPreview.highRiskBillCount} 条，待审批 ${impactPreview.pendingApprovalCount} 条`"
      />
      <a-space v-if="impactPreview?.riskTips?.length" wrap style="margin-top: 12px;">
        <a-tag v-for="tip in impactPreview.riskTips" :key="tip" color="orange">{{ tip }}</a-tag>
      </a-space>
      <vxe-table v-if="impactPreview" border stripe show-overflow :data="impactPreview.impactedItems || []" height="260" style="margin-top: 12px;">
        <vxe-column field="configKey" title="配置键" min-width="260" />
        <vxe-column field="moduleLabel" title="影响模块" width="180" />
        <vxe-column field="affectedCount" title="影响数量" width="120" />
        <vxe-column field="impactHint" title="影响说明" min-width="240" />
      </vxe-table>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getFinanceBillingConfigSnapshots, getFinanceConfigImpactPreview, getFinanceMasterDataOverview } from '../../api/finance'
import type { FinanceBillingConfigSnapshotItem, FinanceConfigImpactPreview, FinanceMasterDataOverview } from '../../types'
import { exportCsv } from '../../utils/export'
import { printTableReport } from '../../utils/print'

const router = useRouter()
const loading = ref(false)
const query = ref({
  month: dayjs(),
  printRemark: ''
})
const overview = ref<FinanceMasterDataOverview | null>(null)
const snapshots = ref<FinanceBillingConfigSnapshotItem[]>([])
const impactLoading = ref(false)
const impactPreview = ref<FinanceConfigImpactPreview | null>(null)
const impactModules = ref<string[]>(['FEE_SUBJECT_', 'PAYMENT_CHANNEL_', 'BILL_'])
const impactModuleOptions = [
  { label: '费用科目', value: 'FEE_SUBJECT_' },
  { label: '支付渠道', value: 'PAYMENT_CHANNEL_' },
  { label: '账单计费', value: 'BILL_' },
  { label: '分摊规则', value: 'ALLOC_' }
]

function go(path: string) {
  router.push(path)
}

async function loadData() {
  loading.value = true
  try {
    const [overviewData, snapshotData] = await Promise.all([
      getFinanceMasterDataOverview({
        month: dayjs(query.value.month).format('YYYY-MM')
      }),
      getFinanceBillingConfigSnapshots()
    ])
    overview.value = overviewData
    snapshots.value = snapshotData || []
  } catch (error: any) {
    message.error(error?.message || '加载主数据配置失败')
  } finally {
    loading.value = false
  }
}

function exportCurrent() {
  exportCsv(
    (overview.value?.recentConfigs || []).map(item => ({
      配置键: item.configKey || '',
      配置值: item.valueText || '',
      生效月份: item.effectiveMonth || '',
      状态: Number(item.status) === 1 ? '启用' : '停用',
      备注: item.remark || ''
    })),
    `财务主数据配置-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}

function printCurrent() {
  try {
    printTableReport({
      title: '财务主数据配置中心',
      subtitle: `月份：${dayjs(query.value.month).format('YYYY-MM')}；费用科目：${overview.value?.feeSubjectCount || 0}；计费规则：${overview.value?.billingRuleCount || 0}；支付渠道：${overview.value?.paymentChannelCount || 0}；备注：${query.value.printRemark || '-'}`,
      columns: [
        { key: 'configKey', title: '配置键' },
        { key: 'valueText', title: '配置值' },
        { key: 'effectiveMonth', title: '生效月份' },
        { key: 'statusText', title: '状态' },
        { key: 'remark', title: '备注' }
      ],
      rows: (overview.value?.recentConfigs || []).map(item => ({
        configKey: item.configKey || '-',
        valueText: item.valueText || '-',
        effectiveMonth: item.effectiveMonth || '-',
        statusText: Number(item.status) === 1 ? '启用' : '停用',
        remark: item.remark || '-'
      }))
    })
  } catch (error: any) {
    message.error(error?.message || '打印失败')
  }
}

async function loadImpactPreview() {
  const selectedPrefixes = impactModules.value || []
  if (!selectedPrefixes.length) {
    message.warning('请至少选择一个配置类别')
    return
  }
  const keys = selectedPrefixes.map(prefix => {
    if (prefix === 'FEE_SUBJECT_') return 'FEE_SUBJECT_ROOM_FEE'
    if (prefix === 'PAYMENT_CHANNEL_') return 'PAYMENT_CHANNEL_WECHAT_ENABLED'
    if (prefix === 'ALLOC_') return 'ALLOC_ELECTRIC_BASE_PRICE'
    return 'BILL_ROOM_BASE_PRICE'
  })
  impactLoading.value = true
  try {
    impactPreview.value = await getFinanceConfigImpactPreview({
      month: dayjs(query.value.month).format('YYYY-MM'),
      configKeys: keys
    })
  } catch (error: any) {
    message.error(error?.message || '加载影响模拟失败')
  } finally {
    impactLoading.value = false
  }
}

onMounted(loadData)
</script>
