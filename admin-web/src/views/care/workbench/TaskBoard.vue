<template>
  <PageContainer title="照护任务看板" subTitle="Resident 360 跳转页：按长者过滤今日任务与异常任务">
    <a-card class="card-elevated" :bordered="false">
      <a-alert type="info" show-icon :message="`当前长者ID：${residentId || '-'}，日期：${date || 'today'}，筛选：${filter || 'all'}`" />
      <a-space style="margin-top: 12px" wrap>
        <a-button type="primary" @click="go('/care/today')">进入今日任务</a-button>
        <a-button @click="go('/care/exception')">查看异常任务</a-button>
        <a-button @click="go('/care/audit')">查看审计记录</a-button>
      </a-space>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'

const route = useRoute()
const router = useRouter()

const residentId = computed(() => route.query.residentId)
const date = computed(() => route.query.date)
const filter = computed(() => route.query.filter)

function go(path: string) {
  router.push(path)
}
</script>
