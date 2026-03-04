<template>
  <PageContainer title="财务主数据配置中心" subTitle="费用科目、计费规则、支付渠道与审批流配置总览">
    <a-card class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-date-picker v-model:value="query.month" picker="month" style="width: 150px" />
        <a-button type="primary" @click="loadData">刷新</a-button>
        <a-button @click="go('/finance/bills/rules')">计费规则入口</a-button>
        <a-button @click="go('/finance/accounts/warning-rules')">余额预警规则</a-button>
        <a-button @click="go('/finance/config/fee-subjects')">费用科目字典</a-button>
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
            <a-button type="primary" @click="go('/finance/config/fee-subjects')">进入维护</a-button>
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
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getFinanceBillingConfigSnapshots, getFinanceMasterDataOverview } from '../../api/finance'
import type { FinanceBillingConfigSnapshotItem, FinanceMasterDataOverview } from '../../types'

const router = useRouter()
const loading = ref(false)
const query = ref({
  month: dayjs()
})
const overview = ref<FinanceMasterDataOverview | null>(null)
const snapshots = ref<FinanceBillingConfigSnapshotItem[]>([])

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
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>
