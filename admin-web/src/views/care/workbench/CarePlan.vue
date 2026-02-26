<template>
  <PageContainer title="照护计划" subTitle="Resident 360 跳转页：查看长者护理计划与服务规则">
    <a-card class="card-elevated" :bordered="false">
      <a-alert type="success" show-icon :message="`当前长者ID：${residentId || '-'}，可在服务计划中调整护理频次与任务模板。`" />
      <a-space style="margin-top: 12px" wrap>
        <a-button type="primary" @click="go('/care/service/service-plans')">服务计划</a-button>
        <a-button @click="go('/care/service/care-levels')">护理等级</a-button>
        <a-button @click="go('/care/elder-packages')">长者套餐</a-button>
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
