<template>
  <PageContainer :title="title" :subTitle="description">
    <a-card :bordered="false" class="card-elevated">
      <a-result status="info" :title="`${title}建设中`" sub-title="菜单、路由与权限已预留，后续将逐步补齐完整业务流程。">
        <template #extra>
          <a-button type="primary" @click="goBack">返回上一页</a-button>
        </template>
      </a-result>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'

withDefaults(
  defineProps<{
    title: string
    description?: string
  }>(),
  {
    description: '该功能已纳入规划，等待业务规则与接口联调。'
  }
)

const router = useRouter()

function goBack() {
  router.back()
}
</script>
