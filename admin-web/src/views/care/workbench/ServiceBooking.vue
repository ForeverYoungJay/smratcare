<template>
  <PageContainer title="服务预约" subTitle="Resident 360 跳转页：按长者发起预约类服务">
    <a-card class="card-elevated" :bordered="false">
      <a-alert type="info" show-icon :message="`当前长者ID：${residentId || '-'}，模式：${mode || '-'}。`" />
      <a-space style="margin-top: 12px" wrap>
        <a-button type="primary" @click="go('/care/service/service-bookings')">进入服务预定</a-button>
        <a-button @click="go('/care/service/service-items')">查看服务项目</a-button>
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
const mode = computed(() => route.query.mode)

function go(path: string) {
  router.push(path)
}
</script>
