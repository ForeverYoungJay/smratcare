<template>
  <PageContainer title="长者管理" subTitle="长者全周期管理：售前跟进 → 入院办理 → 在院服务 → 变更与退院">
    <a-alert
      type="info"
      show-icon
      message="主页用于统一查看长者状态、风险、护理、合同、费用与跨部门协同事件。"
      style="margin-bottom: 16px"
    />

    <a-card v-if="elder" class="card-elevated resident-hero" :bordered="false" style="margin-bottom: 16px">
      <div class="resident-hero__main">
        <div>
          <div class="resident-hero__title">
            <strong>{{ elder.fullName || '未命名长者' }}</strong>
            <a-tag :color="statusColor(elder.lifecycleStatus)">{{ statusLabel(elder.lifecycleStatus) }}</a-tag>
            <a-tag v-if="elder.departureType === 'DEATH'" color="magenta">死亡离场</a-tag>
          </div>
          <div class="resident-hero__meta">
            <span>床位：{{ elder.roomNo || '-' }} / {{ elder.bedNo || '未分配' }}</span>
            <span>护理等级：{{ elder.careLevel || '未评定' }}</span>
            <span>联系电话：{{ elder.phone || '未登记' }}</span>
          </div>
        </div>
        <a-space wrap>
          <a-button type="primary" @click="go(`/elder/detail/${residentId}`)">查看档案</a-button>
          <a-button @click="go(`/elder/assessment/ability/archive?elderId=${residentId}`)">评估档案</a-button>
          <a-button @click="go(`/elder/contracts-invoices?elderId=${residentId}`)">合同票据</a-button>
          <a-button @click="go(`/elder/status-change/center?residentId=${residentId}`)">状态变更</a-button>
        </a-space>
      </div>
    </a-card>

    <a-card v-if="careProfile" class="card-elevated care-profile" :bordered="false" style="margin-bottom: 16px">
      <div class="care-profile__header">
        <div>
          <div class="care-profile__title">护理等级建议</div>
          <div class="care-profile__reason">{{ careProfile.recommendationReason || '暂无等级建议' }}</div>
        </div>
        <a-space wrap>
          <a-tag :color="careProfile.adjustmentRequired ? 'orange' : 'green'">
            {{ careProfile.adjustmentRequired ? '建议调整' : '等级匹配' }}
          </a-tag>
          <a-button
            v-if="careProfile.adjustmentRequired && careProfile.suggestedCareLevel"
            type="primary"
            :loading="careLevelUpdating"
            @click="applySuggestedCareLevel"
          >
            应用建议等级
          </a-button>
        </a-space>
      </div>
      <a-row :gutter="16" class="care-profile__body">
        <a-col :xs="24" :md="8">
          <div class="care-profile__label">当前等级</div>
          <div class="care-profile__value">{{ careProfile.currentCareLevel || '未评定' }}</div>
        </a-col>
        <a-col :xs="24" :md="8">
          <div class="care-profile__label">建议等级</div>
          <div class="care-profile__value">{{ careProfile.suggestedCareLevel || '待评估' }}</div>
        </a-col>
        <a-col :xs="24" :md="8">
          <div class="care-profile__label">风险标签</div>
          <a-space wrap>
            <a-tag v-for="tag in careProfile.riskTags || []" :key="tag" color="red">{{ tag }}</a-tag>
            <span v-if="!(careProfile.riskTags || []).length" class="care-profile__muted">暂无高风险标签</span>
          </a-space>
        </a-col>
      </a-row>
      <a-divider style="margin: 12px 0" />
      <a-row :gutter="16">
        <a-col :xs="24" :lg="14">
          <div class="care-profile__section-title">最近评估依据</div>
          <div v-for="item in careProfile.latestAssessments || []" :key="item.id" class="care-profile__line">
            <span>{{ item.assessmentDate || '-' }}</span>
            <span>{{ item.assessmentType || '评估' }}</span>
            <a-tag>{{ item.levelCode || '未分级' }}</a-tag>
            <span>{{ item.resultSummary || item.suggestion || '暂无摘要' }}</span>
          </div>
          <a-empty v-if="!(careProfile.latestAssessments || []).length" description="暂无评估记录" />
        </a-col>
        <a-col :xs="24" :lg="10">
          <div class="care-profile__section-title">等级调整轨迹</div>
          <div v-for="item in careProfile.changeLogs || []" :key="item.id" class="care-profile__line">
            <span>{{ item.createTime || '-' }}</span>
            <span>{{ item.beforeValue || '未评定' }} → {{ item.afterValue || '未评定' }}</span>
          </div>
          <a-empty v-if="!(careProfile.changeLogs || []).length" description="暂无调整记录" />
        </a-col>
      </a-row>
    </a-card>

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
import { adjustElderCareLevel, getElderCareProfile, getElderDetail, getElderPage } from '../../../api/elder'
import { useLiveSyncRefresh } from '../../../composables/useLiveSyncRefresh'
import type { ElderCareProfile, ElderItem, MedicalResidentOverview } from '../../../types'

const router = useRouter()
const route = useRoute()
const residentId = ref('')

const loading = ref(false)
const errorMessage = ref('')
const overview = ref<MedicalResidentOverview>()
const elder = ref<ElderItem>()
const careProfile = ref<ElderCareProfile>()
const careLevelUpdating = ref(false)

const cards = computed(() => overview.value?.cards || [])

function go(path: string) {
  const currentResidentId = String(residentId.value || '').trim()
  if (!currentResidentId || /([?&])residentId=/.test(path)) {
    router.push(path)
    return
  }
  const targetPath = path.includes('?') ? `${path}&residentId=${encodeURIComponent(currentResidentId)}` : `${path}?residentId=${encodeURIComponent(currentResidentId)}`
  router.push(targetPath)
}

async function resolveResidentId() {
  const fromRoute = String(route.query.residentId || '').trim()
  if (fromRoute) {
    residentId.value = fromRoute
    return
  }
  const page = await getElderPage({ pageNo: 1, pageSize: 1 })
  const first = page.list?.[0]
  residentId.value = String(first?.id || '')
}

async function loadOverview() {
  loading.value = true
  errorMessage.value = ''
  try {
    await resolveResidentId()
    if (!residentId.value.trim()) {
      overview.value = undefined
      errorMessage.value = "暂无可用长者，请先创建长者档案"
      return
    }
    const [elderDetail, residentOverview, careProfileData] = await Promise.all([
      getElderDetail(residentId.value),
      getResidentOverview(residentId.value),
      getElderCareProfile(residentId.value)
    ])
    elder.value = elderDetail
    overview.value = residentOverview
    careProfile.value = careProfileData
  } catch (error: any) {
    errorMessage.value = error?.message || '加载长者总览失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

async function applySuggestedCareLevel() {
  if (!residentId.value || !careProfile.value?.suggestedCareLevel) return
  const latestAssessment = careProfile.value.latestAssessments?.[0]
  careLevelUpdating.value = true
  try {
    const updated = await adjustElderCareLevel(residentId.value, {
      careLevel: careProfile.value.suggestedCareLevel,
      assessmentRecordId: latestAssessment?.id,
      reason: careProfile.value.recommendationReason || '根据照护画像建议调整'
    })
    elder.value = updated
    await loadOverview()
    message.success('护理等级已调整')
  } catch (error: any) {
    message.error(error?.message || '护理等级调整失败')
  } finally {
    careLevelUpdating.value = false
  }
}

function statusLabel(status?: string) {
  if (status === 'INTENT') return '意向'
  if (status === 'TRIAL') return '试住'
  if (status === 'IN_HOSPITAL') return '在住'
  if (status === 'OUTING') return '外出'
  if (status === 'MEDICAL_OUTING') return '外出就医'
  if (status === 'DISCHARGE_PENDING') return '待退住'
  if (status === 'DISCHARGED') return '已退住'
  if (status === 'DECEASED') return '已身故'
  return '未标记'
}

function statusColor(status?: string) {
  if (status === 'INTENT') return 'gold'
  if (status === 'TRIAL') return 'cyan'
  if (status === 'IN_HOSPITAL') return 'green'
  if (status === 'OUTING') return 'orange'
  if (status === 'MEDICAL_OUTING') return 'volcano'
  if (status === 'DISCHARGE_PENDING') return 'blue'
  if (status === 'DISCHARGED') return 'red'
  if (status === 'DECEASED') return 'magenta'
  return 'default'
}

onMounted(loadOverview)

useLiveSyncRefresh({
  topics: ['elder', 'lifecycle', 'bed', 'care', 'health', 'dining', 'finance', 'marketing'],
  refresh: () => {
    if (loading.value) return
    loadOverview().catch(() => {})
  },
  debounceMs: 1000
})
</script>

<style scoped>
.line-item {
  line-height: 1.9;
  color: var(--ink);
}

.resident-hero__main {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.resident-hero__title {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 8px;
}

.resident-hero__title strong {
  font-size: 18px;
  color: var(--ink);
}

.resident-hero__meta {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  color: var(--muted);
}

.care-profile__header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.care-profile__title,
.care-profile__section-title {
  font-weight: 600;
  color: var(--ink);
}

.care-profile__reason,
.care-profile__muted {
  margin-top: 4px;
  color: var(--muted);
}

.care-profile__body {
  margin-top: 14px;
}

.care-profile__label {
  color: var(--muted-2);
  margin-bottom: 4px;
}

.care-profile__value {
  font-size: 18px;
  font-weight: 600;
  color: var(--ink);
}

.care-profile__line {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
  line-height: 1.9;
  color: var(--muted);
}
</style>
