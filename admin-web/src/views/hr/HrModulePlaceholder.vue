<template>
  <PageContainer :title="title" :subTitle="subTitle">
    <a-card :bordered="false" class="card-elevated">
      <a-empty description="模块已创建，可继续接入业务接口与流程配置" />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'

const route = useRoute()
const title = computed(() => String(route.meta.title || '人事行政模块'))
const subTitle = computed(() => {
  const section = typeof route.query.section === 'string' ? route.query.section : '人事行政工作台'
  return `${section} / ${String(route.meta.title || '')}`.replace(/\s+\/\s*$/, '')
})
</script>
