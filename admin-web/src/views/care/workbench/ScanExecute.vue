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
              <a-button type="primary" @click="goTaskBoard">查看任务</a-button>
              <a-button @click="go('/logistics/assets/room-state-map')">房态图</a-button>
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
import { normalizeResidentId } from '../../../utils/id'
import type { CareTaskItem, Id } from '../../../types'
import { resolveCareError } from '../careError'

const route = useRoute()
const router = useRouter()

const residentId = computed<Id | undefined>(() => normalizeResidentId(route.query as Record<string, unknown>))
const dateValue = ref(dayjs().format('YYYY-MM-DD'))
const qrCode = ref(String(route.query.bedQrCode || ''))
const loading = ref(false)
const errorMessage = ref('')
const pendingRows = ref<CareTaskItem[]>([])

function go(path: string) {
  const [pathname, search] = path.split('?')
  const parsed = new URLSearchParams(search || '')
  const query: Record<string, any> = {}
  parsed.forEach((value, key) => {
    query[key] = value
  })
  const resident = route.query.residentId ?? route.query.elderId
  if (resident != null && query.residentId == null && query.elderId == null) {
    query.residentId = resident
    query.elderId = resident
  }
  router.push({ path: pathname, query })
}

function goTaskBoard() {
  const query: Record<string, any> = { date: dateValue.value }
  if (residentId.value != null) {
    query.residentId = residentId.value
    query.elderId = residentId.value
  }
  router.push({ path: '/care/workbench/task-board', query })
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
  } catch (error) {
    errorMessage.value = resolveCareError(error, '加载待执行任务失败')
    message.error(errorMessage.value)
  } finally {
    loading.value = false
  }
}

onMounted(loadRows)
</script>
