<template>
  <LeadPipelineTable mode="callback" :title="props.title" :sub-title="props.subTitle" :callback-type="props.type" />
</template>

<script setup lang="ts">
import LeadPipelineTable from './components/LeadPipelineTable.vue'

const props = withDefaults(defineProps<{
  type: 'checkin' | 'trial' | 'discharge'
  title?: string
  subTitle?: string
}>(), {
  title: '回访列表',
  subTitle: '回访任务与执行情况'
})
</script>
