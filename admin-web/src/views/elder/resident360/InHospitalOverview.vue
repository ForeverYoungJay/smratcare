<template>
  <PageContainer title="在院服务总览" subTitle="护理/膳食/医疗/活动/费用一体化视图">
    <StatefulBlock :loading="loading" :error="errorMessage" :empty="!modules.length" empty-text="暂无在院服务数据" @retry="loadModules">
      <a-row :gutter="16">
        <a-col v-for="item in modules" :key="item.title" :xs="24" :sm="12" :lg="8" style="margin-bottom: 16px">
          <a-card :title="item.title" class="card-elevated" :bordered="false">
            <template #extra>
              <a-tag :color="item.tagColor">{{ item.tag }}</a-tag>
            </template>
            <div class="desc">{{ item.desc }}</div>
            <a-space wrap style="margin-top: 12px">
              <a-button v-for="action in item.actions" :key="action.label" :type="action.primary ? 'primary' : 'default'" @click="go(action.path)">
                {{ action.label }}
              </a-button>
            </a-space>
          </a-card>
        </a-col>
      </a-row>
    </StatefulBlock>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getResidentOverview } from '../../../api/medicalCare'
import type { MedicalResidentOverview } from '../../../types'

const router = useRouter()
const route = useRoute()
const residentId = Number(route.query.residentId || 1001)
const loading = ref(false)
const errorMessage = ref('')
const overview = ref<MedicalResidentOverview>()

const modules = computed(() =>
  (overview.value?.cards || []).map((card) => ({
    title: card.title.replace(/^卡片\d+：/, ''),
    desc: card.description || card.lines?.[0] || '暂无说明',
    tag: card.tag,
    tagColor: card.tagColor || 'blue',
    actions: card.actions || []
  }))
)

function go(path: string) {
  router.push(path)
}

async function loadModules() {
  loading.value = true
  errorMessage.value = ''
  try {
    overview.value = await getResidentOverview(residentId)
  } catch (error: any) {
    errorMessage.value = error?.message || '加载在院服务总览失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadModules)
</script>

<style scoped>
.desc {
  color: #595959;
  line-height: 1.8;
  min-height: 54px;
}
</style>
