<template>
  <PageContainer title="护工扫码执行" subTitle="Resident 360 跳转页：扫码后定位到床位/房间的今日任务">
    <a-card class="card-elevated" :bordered="false">
      <a-alert type="warning" show-icon :message="`当前长者ID：${residentId || '-'}。移动端扫码后可带入床位/房间二维码参数。`" />
      <a-space style="margin-top: 12px" wrap>
        <a-button type="primary" @click="go('/care/workbench/task-board?date=today')">进入今日任务</a-button>
        <a-button @click="go('/elder/bed-panorama')">返回床态全景</a-button>
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

function go(path: string) {
  router.push(path)
}
</script>
