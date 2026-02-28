<template>
  <PageContainer title="长者管理 Resident 360" subTitle="长者全周期管理：售前跟进 → 入院办理 → 在院服务 → 变更与退院">
    <a-alert
      type="info"
      show-icon
      message="主页用于统一查看长者状态、风险、护理、合同、费用与跨部门协同事件。"
      style="margin-bottom: 16px"
    />

    <StatefulBlock :loading="loading" :error="errorMessage" :empty="!cards.length" empty-text="暂无长者总览数据" @retry="loadOverview">
      <a-row :gutter="16">
        <a-col v-for="card in cards" :key="card.key" :xs="24" :lg="12" style="margin-bottom: 16px">
          <a-card :title="card.title" class="card-elevated" :bordered="false">
            <template #extra>
              <a-tag :color="card.tagColor">{{ card.tag }}</a-tag>
            </template>

            <div v-for="line in card.lines" :key="line" class="line-item">{{ line }}</div>

            <a-space wrap style="margin-top: 12px">
              <a-button v-for="action in card.actions" :key="action.label" :type="action.primary ? 'primary' : 'default'" @click="go(action.path)">
                {{ action.label }}
              </a-button>
              <a-button
                v-if="card.key === 'status-event' && overview?.currentStatus === 'OUT'"
                @click="go('/elder/outing?mode=return')"
              >
                外出返院登记
              </a-button>
              <a-button
                v-if="card.key === 'status-event' && overview?.hasUnclosedIncident"
                danger
                @click="go('/elder/incident?tab=unclosed')"
              >
                事故结案
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getResidentOverview } from '../../../api/medicalCare'
import { getElderPage } from '../../../api/elder'
import type { MedicalResidentOverview } from '../../../types'

const router = useRouter()
const route = useRoute()
const residentId = ref(0)

const loading = ref(false)
const errorMessage = ref('')
const overview = ref<MedicalResidentOverview>()

const cards = computed(() => overview.value?.cards || [])

function go(path: string) {
  router.push(path)
}

async function resolveResidentId() {
  const fromRoute = Number(route.query.residentId || 0)
  if (fromRoute > 0) {
    residentId.value = fromRoute
    return
  }
  const page = await getElderPage({ pageNo: 1, pageSize: 1 })
  const first = page.list?.[0]
  residentId.value = Number(first?.id || 0)
}

async function loadOverview() {
  loading.value = true
  errorMessage.value = ''
  try {
    await resolveResidentId()
    if (!residentId.value) {
      overview.value = undefined
      errorMessage.value = "暂无可用长者，请先创建长者档案"
      return
    }
    overview.value = await getResidentOverview(residentId.value)
  } catch (error: any) {
    errorMessage.value = error?.message || '加载 Resident 360 失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadOverview)
</script>

<style scoped>
.line-item {
  line-height: 1.9;
  color: #262626;
}
</style>
