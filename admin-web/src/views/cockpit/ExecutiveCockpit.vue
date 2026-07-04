<template>
  <PageContainer title="经营驾驶舱" subTitle="机构核心经营指标总览（每日预聚合，实时补数）">
    <template #extra>
      <a-space wrap>
        <a-select v-model:value="trendDays" style="width: 120px" @change="loadData">
          <a-select-option :value="7">近 7 天</a-select-option>
          <a-select-option :value="30">近 30 天</a-select-option>
          <a-select-option :value="90">近 90 天</a-select-option>
        </a-select>
        <a-button :loading="refreshing" @click="refresh">重算今日</a-button>
        <a-button type="primary" :loading="loading" @click="loadData">刷新</a-button>
      </a-space>
    </template>

    <a-spin :spinning="loading">
      <a-row :gutter="16" style="margin-bottom: 16px;">
        <a-col :span="6">
          <a-card class="card-elevated" :bordered="false">
            <a-statistic title="入住率 (%)" :value="overview.occupancyRate || 0" :precision="2" />
            <div class="sub">已用 {{ overview.occupiedBeds || 0 }} / 总 {{ overview.totalBeds || 0 }} 床</div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="card-elevated" :bordered="false">
            <a-statistic title="在住长者" :value="overview.residentCount || 0" />
            <div class="sub">今日入住 {{ overview.admissions || 0 }} · 离院 {{ overview.discharges || 0 }}</div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="card-elevated" :bordered="false">
            <a-statistic title="床位周转率 (%)" :value="overview.turnoverRate || 0" :precision="2" />
            <div class="sub">近 {{ trendDays }} 日累计入住 / 期末在住</div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="card-elevated" :bordered="false">
            <a-statistic title="人效 (长者/员工)" :value="overview.residentPerStaff || 0" :precision="2" />
            <div class="sub">在职员工 {{ overview.staffCount || 0 }} 人</div>
          </a-card>
        </a-col>
      </a-row>

      <a-row :gutter="16" style="margin-bottom: 16px;">
        <a-col :span="6">
          <a-card class="card-elevated" :bordered="false">
            <a-statistic title="本月营收 (元)" :value="overview.revenueAmount || 0" :precision="2" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="card-elevated" :bordered="false">
            <a-statistic title="本月回款 (元)" :value="overview.paidAmount || 0" :precision="2" />
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="card-elevated" :bordered="false">
            <a-statistic title="回款率 (%)" :value="overview.collectionRate || 0" :precision="2" />
            <div class="sub">欠费 {{ Number(overview.outstandingAmount || 0).toFixed(2) }} 元</div>
          </a-card>
        </a-col>
        <a-col :span="6">
          <a-card class="card-elevated" :bordered="false">
            <a-statistic title="告警响应率 (%)" :value="overview.alertRespRate || 0" :precision="2" />
            <div class="sub">今日告警 {{ overview.alertTotal || 0 }} · 超时升级 {{ overview.alertTimeout || 0 }}</div>
          </a-card>
        </a-col>
      </a-row>

      <a-card class="card-elevated" :bordered="false" title="趋势明细">
        <a-table
          :columns="columns"
          :data-source="overview.trend || []"
          :pagination="{ pageSize: 15 }"
          row-key="statDate"
          size="small"
        />
      </a-card>
    </a-spin>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getExecutiveOverview, refreshExecutiveSnapshot } from '../../api/cockpit'
import type { CockpitOverviewResponse } from '../../api/cockpit'

const loading = ref(false)
const refreshing = ref(false)
const trendDays = ref(30)
const overview = reactive<Partial<CockpitOverviewResponse>>({})

const columns = [
  { title: '日期', dataIndex: 'statDate', key: 'statDate' },
  { title: '入住率(%)', dataIndex: 'occupancyRate', key: 'occupancyRate' },
  { title: '在住长者', dataIndex: 'residentCount', key: 'residentCount' },
  { title: '营收(元)', dataIndex: 'revenueAmount', key: 'revenueAmount' },
  { title: '回款率(%)', dataIndex: 'collectionRate', key: 'collectionRate' },
  { title: '告警响应率(%)', dataIndex: 'alertRespRate', key: 'alertRespRate' }
]

async function loadData() {
  loading.value = true
  try {
    const data = await getExecutiveOverview({ trendDays: trendDays.value })
    Object.assign(overview, data)
  } catch (e) {
    message.error('加载经营指标失败')
  } finally {
    loading.value = false
  }
}

async function refresh() {
  refreshing.value = true
  try {
    const data = await refreshExecutiveSnapshot({ trendDays: trendDays.value })
    Object.assign(overview, data)
    message.success('今日指标已重算')
  } catch (e) {
    message.error('重算失败')
  } finally {
    refreshing.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.sub {
  margin-top: 6px;
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}
</style>
