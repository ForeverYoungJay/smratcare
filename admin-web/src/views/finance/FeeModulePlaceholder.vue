<template>
  <PageContainer :title="props.moduleName" :subTitle="props.description || '已预留菜单与路由，待业务规则联调后上线'">
    <a-card :bordered="false" class="card-elevated">
      <a-result status="info" :title="`${props.moduleName}建设中`" sub-title="建议先通过右侧入口走通关联流程，后续会在该页补齐审核、台账、导出能力。">
        <template #extra>
          <a-space wrap>
            <a-button v-for="item in props.links" :key="item.to" @click="go(item.to)">{{ item.label }}</a-button>
          </a-space>
        </template>
      </a-result>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { withDefaults } from 'vue'
import { useRouter } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'

type QuickLink = {
  label: string
  to: string
}

const props = withDefaults(
  defineProps<{
    moduleName: string
    description?: string
    links?: QuickLink[]
  }>(),
  {
    description: '',
    links: () => []
  }
)

const router = useRouter()

function go(path: string) {
  router.push(path)
}
</script>
