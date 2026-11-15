<template>
  <nav v-if="mode === 'callback' && followupEntry !== 'records'" class="followup-view-nav" aria-label="客户跟进视图">
    <a-radio-group :value="followupEntry" button-style="solid" @change="switchFollowupView">
      <a-radio-button value="today">今日</a-radio-button>
      <a-radio-button value="due">待回访</a-radio-button>
      <a-radio-button value="overdue">已逾期</a-radio-button>
    </a-radio-group>
  </nav>
  <LeadPipelineTable
    :mode="mode"
    :title="title"
    :sub-title="subTitle"
    :scenario="scenario"
    :callback-type="callbackType"
  />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import LeadPipelineTable from './components/LeadPipelineTable.vue'
import { buildFollowupRoute, followupEntryFromPath, type FollowupEntry } from '../../utils/marketingNav'

withDefaults(
  defineProps<{
    mode: 'consultation' | 'intent' | 'reservation' | 'invalid' | 'callback' | 'pipeline'
    title: string
    subTitle: string
    scenario?: string
    callbackType?: 'checkin' | 'trial' | 'discharge'
  }>(),
  {
    mode: 'consultation',
    scenario: '',
    callbackType: undefined
  }
)

const route = useRoute()
const router = useRouter()
const followupEntry = computed(() => followupEntryFromPath(route.path))

function switchFollowupView(event: { target?: { value?: FollowupEntry } }) {
  const entry = event.target?.value
  if (!entry || entry === followupEntry.value) return
  router.push(buildFollowupRoute(entry, route.query))
}
</script>

<style scoped>
.followup-view-nav {
  margin-bottom: 12px;
}
</style>
