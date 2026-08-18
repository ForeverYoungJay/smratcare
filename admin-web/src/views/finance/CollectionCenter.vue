<template>
  <PageContainer
    title="欠费与提醒中心"
    subTitle="系统自动生成的提醒，和人工推进的催缴动作，收在同一个入口里"
  >
    <a-tabs v-model:activeKey="activeTab" @change="syncTabToRoute">
      <a-tab-pane key="reminder" tab="自动提醒">
        <ReminderPanel />
      </a-tab-pane>
      <a-tab-pane key="follow-up" tab="催缴跟进">
        <CollectionFollowUpPanel />
      </a-tab-pane>
    </a-tabs>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import ReminderPanel from './components/ReminderPanel.vue'
import CollectionFollowUpPanel from './components/CollectionFollowUpPanel.vue'

type TabKey = 'reminder' | 'follow-up'

const route = useRoute()
const router = useRouter()
const activeTab = ref<TabKey>('follow-up')

function normalizeTab(value: unknown): TabKey {
  return String(value || '').trim() === 'reminder' ? 'reminder' : 'follow-up'
}

// 标签写进 query，便于从提醒相关入口直达对应标签，也让刷新后停留原处
function syncTabToRoute(key: unknown) {
  const next = normalizeTab(key)
  if (normalizeTab(route.query.tab) === next) return
  router.replace({ path: route.path, query: { ...route.query, tab: next } }).catch(() => {})
}

onMounted(() => {
  activeTab.value = normalizeTab(route.query.tab)
})
</script>
