<template>
  <PageContainer title="照护计划" subTitle="按长者查看服务计划、护理等级与执行规则">
    <template #extra>
      <a-space>
        <a-button @click="go('/care/service/service-plans')">全部计划</a-button>
        <a-button type="primary" @click="go('/care/elder-packages')">长者套餐</a-button>
      </a-space>
    </template>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="8">
        <a-card class="card-elevated" :bordered="false" title="当前长者">
          <div class="meta-line">长者ID：{{ residentId || '-' }}</div>
          <div class="meta-line">计划数量：{{ plans.length }}</div>
          <div class="meta-line">激活计划：{{ activeCount }}</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="16">
        <a-card class="card-elevated" :bordered="false" title="护理等级配置">
          <StatefulBlock :loading="loadingLevels" :error="levelError" :empty="!careLevels.length" empty-text="暂无护理等级" @retry="loadLevels">
            <a-space wrap>
              <a-tag v-for="item in careLevels" :key="item.id" color="blue">{{ item.levelCode }} / {{ item.levelName }}</a-tag>
            </a-space>
          </StatefulBlock>
        </a-card>
      </a-col>
    </a-row>

    <a-card class="card-elevated" :bordered="false" title="服务计划列表">
      <StatefulBlock :loading="loadingPlans" :error="planError" :empty="!plans.length" empty-text="暂无服务计划" @retry="loadPlans">
        <a-table :columns="columns" :data-source="plans" :pagination="false" row-key="id">
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'status'">
              <a-tag :color="record.status === 'ACTIVE' ? 'green' : 'default'">{{ record.status || '-' }}</a-tag>
            </template>
          </template>
        </a-table>
      </StatefulBlock>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getCareLevelList, getServicePlanPage } from '../../../api/nursing'
import type { CareLevelItem, ServicePlanItem } from '../../../types'

const route = useRoute()
const router = useRouter()
const residentId = computed(() => Number(route.query.residentId || 0) || undefined)

const loadingPlans = ref(false)
const loadingLevels = ref(false)
const planError = ref('')
const levelError = ref('')
const plans = ref<ServicePlanItem[]>([])
const careLevels = ref<CareLevelItem[]>([])

const activeCount = computed(() => plans.value.filter((p) => p.status === 'ACTIVE').length)

const columns = [
  { title: '计划名称', dataIndex: 'planName', key: 'planName' },
  { title: '服务项', dataIndex: 'serviceItemName', key: 'serviceItemName', width: 140 },
  { title: '护理等级', dataIndex: 'careLevelName', key: 'careLevelName', width: 120 },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 120 },
  { title: '结束日期', dataIndex: 'endDate', key: 'endDate', width: 120 },
  { title: '状态', key: 'status', width: 100 }
]

function go(path: string) {
  router.push(path)
}

async function loadPlans() {
  loadingPlans.value = true
  planError.value = ''
  try {
    const page = await getServicePlanPage({ pageNo: 1, pageSize: 50, elderId: residentId.value })
    plans.value = page.list || []
  } catch (error: any) {
    planError.value = error?.message || '加载服务计划失败'
    message.error(planError.value)
  } finally {
    loadingPlans.value = false
  }
}

async function loadLevels() {
  loadingLevels.value = true
  levelError.value = ''
  try {
    careLevels.value = await getCareLevelList({ enabled: 1 })
  } catch (error: any) {
    levelError.value = error?.message || '加载护理等级失败'
    message.error(levelError.value)
  } finally {
    loadingLevels.value = false
  }
}

onMounted(() => {
  loadPlans()
  loadLevels()
})
</script>

<style scoped>
.meta-line {
  line-height: 2;
  color: #334155;
}
</style>
