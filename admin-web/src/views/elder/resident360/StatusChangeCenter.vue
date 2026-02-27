<template>
  <PageContainer title="入住状态变更" subTitle="外出/来访/试住/退住/外出就医/死亡统一入口，自动触发跨部门任务包">
    <a-alert type="error" show-icon :message="pendingAlertMessage" style="margin-bottom: 16px" />

    <StatefulBlock :loading="loading" :error="errorMessage" @retry="fetchRealStats">
      <a-row :gutter="16" style="margin-bottom: 16px">
        <a-col v-for="item in statusItems" :key="item.status" :xs="12" :sm="8" :md="6" :lg="4" style="margin-bottom: 12px">
          <a-card class="card-elevated" :bordered="false" hoverable @click="go(item.path)">
            <a-statistic :title="item.status" :value="item.count" />
          </a-card>
        </a-col>
      </a-row>
    </StatefulBlock>

    <a-card title="状态变更动作" class="card-elevated" :bordered="false">
      <a-space wrap>
        <a-button type="primary" @click="go('/elder/outing')">外出登记</a-button>
        <a-button @click="go('/elder/visit-register')">来访登记</a-button>
        <a-button @click="go('/elder/trial-stay')">试住登记</a-button>
        <a-button danger @click="go('/elder/discharge-apply')">退住申请</a-button>
        <a-button @click="go('/elder/medical-outing')">外出就医登记</a-button>
        <a-button danger @click="go('/elder/death-register')">死亡登记</a-button>
      </a-space>

      <a-divider />
      <a-typography-paragraph>
        触发规则：提交状态变更后自动推送护工/医护/财务/后勤消息，生成任务包（退住交接、事故处置等），并按规则调整费用、床位与餐饮计划。
      </a-typography-paragraph>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getResidenceStatusSummary } from '../../../api/elderResidence'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import type { ResidenceStatusSummary } from '../../../types'

const router = useRouter()
const loading = ref(false)
const errorMessage = ref('')
const statusItems = ref([
  { status: '意向', count: 0, path: '/elder/list' },
  { status: '试住', count: 0, path: '/elder/trial-stay' },
  { status: '在住', count: 0, path: '/elder/list?status=1' },
  { status: '外出', count: 0, path: '/elder/outing?status=OUT' },
  { status: '外出就医', count: 0, path: '/elder/medical-outing' },
  { status: '退住办理中', count: 0, path: '/elder/discharge-apply?status=PENDING' },
  { status: '已退住', count: 0, path: '/elder/list?status=3' },
  { status: '死亡', count: 0, path: '/elder/death-register' }
])

const pendingStats = ref({
  openIncidentCount: 0,
  pendingDischargeCount: 0,
  overdueOutingCount: 0
})

const pendingAlertMessage = computed(
  () =>
    `待处理事件：未闭环事故 ${pendingStats.value.openIncidentCount}、待审批退住 ${pendingStats.value.pendingDischargeCount}、外出超时未返院 ${pendingStats.value.overdueOutingCount}`
)

function go(path: string) {
  router.push(path)
}

async function fetchRealStats() {
  loading.value = true
  errorMessage.value = ''
  try {
    const data: ResidenceStatusSummary = await getResidenceStatusSummary()
    statusItems.value = [
      { status: '意向', count: Number(data.intentCount || 0), path: '/elder/list' },
      { status: '试住', count: Number(data.trialCount || 0), path: '/elder/trial-stay' },
      { status: '在住', count: Number(data.inHospitalCount || 0), path: '/elder/list?status=1' },
      { status: '外出', count: Number(data.outingCount || 0), path: '/elder/outing?status=OUT' },
      { status: '外出就医', count: Number(data.medicalOutingCount || 0), path: '/elder/medical-outing' },
      { status: '退住办理中', count: Number(data.dischargePendingCount || 0), path: '/elder/discharge-apply?status=PENDING' },
      { status: '已退住', count: Number(data.dischargedCount || 0), path: '/elder/list?status=3' },
      { status: '死亡', count: Number(data.deathCount || 0), path: '/elder/death-register' }
    ]
    pendingStats.value = {
      openIncidentCount: Number(data.openIncidentCount || 0),
      pendingDischargeCount: Number(data.dischargePendingCount || 0),
      overdueOutingCount: Number(data.overdueOutingCount || 0)
    }
  } catch (error: any) {
    errorMessage.value = error?.message || '加载状态统计失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRealStats()
})

useLiveSyncRefresh({
  topics: ['elder', 'lifecycle', 'system'],
  refresh: fetchRealStats,
  debounceMs: 800
})
</script>
