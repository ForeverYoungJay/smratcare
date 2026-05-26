<template>
  <LeadPipelineTable
    :mode="mode"
    :title="title"
    :sub-title="subTitle"
    :scenario="scenario"
    :callback-type="callbackType"
  />
</template>

<script setup lang="ts">
import LeadPipelineTable from './components/LeadPipelineTable.vue'

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
</script>
