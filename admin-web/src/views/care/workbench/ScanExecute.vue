<template>
  <PageContainer title="护工扫码执行" subTitle="按长者拉取待执行任务，扫码后快速定位执行">
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="10">
        <a-card class="card-elevated" :bordered="false" title="扫码参数">
          <a-form layout="vertical">
            <a-form-item label="长者ID">
              <a-input :value="residentId ? String(residentId) : '-'" disabled />
            </a-form-item>
            <a-form-item label="床位二维码">
              <a-input v-model:value="qrCode" placeholder="扫码后自动填充" />
            </a-form-item>
            <a-space>
              <a-button type="primary" @click="go(`/care/workbench/task-board?residentId=${residentId}&date=${dateValue}`)">查看任务</a-button>
              <a-button @click="go('/elder/bed-panorama')">床态全景</a-button>
            </a-space>
          </a-form>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="14">
        <a-card class="card-elevated" :bordered="false" title="待执行任务（今日）">
          <StatefulBlock :loading="loading" :error="errorMessage" :empty="!pendingRows.length" empty-text="暂无待执行任务" @retry="loadRows">
            <a-list :data-source="pendingRows">
              <template #renderItem="{ item }">
                <a-list-item>
                  <a-list-item-meta :title="`${item.taskName || '照护任务'} · ${item.planTime || '-'}`" :description="`${item.elderName || '-'} / ${item.roomNo || '-'}`" />
                </a-list-item>
              </template>
            </a-list>
          </StatefulBlock>
        </a-card>
      </a-col>
    </a-row>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../../components/PageContainer.vue'
import StatefulBlock from '../../../components/StatefulBlock.vue'
import { getTaskPage } from '../../../api/care'
import type { CareTaskItem } from '../../../types'

const route = useRoute()
const router = useRouter()

const residentId = computed(() => Number(route.query.residentId || 0) || undefined)
const dateValue = ref(dayjs().format('YYYY-MM-DD'))
const qrCode = ref(String(route.query.bedQrCode || ''))
const loading = ref(false)
const errorMessage = ref('')
const pendingRows = ref<CareTaskItem[]>([])

function go(path: string) {
  router.push(path)
}

async function loadRows() {
  loading.value = true
  errorMessage.value = ''
  try {
    const page = await getTaskPage({
      pageNo: 1,
      pageSize: 30,
      date: dateValue.value,
      elderId: residentId.value,
      status: 'PENDING'
    })
    pendingRows.value = page.list || []
  } catch (error: any) {
    errorMessage.value = error?.message || '加载待执行任务失败'
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadRows)
</script>
