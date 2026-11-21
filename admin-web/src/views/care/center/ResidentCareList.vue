<template>
  <PageContainer class="rm-page" title="护理档案与评估" sub-title="与长者管理共用同一份长者资料；点「护理档案」查看评估、护理计划、服务记录与异常。">
    <section class="rm-card">
      <div class="rc-bar">
        <a-input-search v-model:value="keyword" allow-clear placeholder="姓名、房间或床位" style="width: 260px" @search="search" />
        <a-radio-group v-model:value="filter" button-style="solid" @change="applyFilter">
          <a-radio-button value="">全部在住</a-radio-button>
          <a-radio-button value="reassess">待复评{{ reassessCount ? ` (${reassessCount})` : '' }}</a-radio-button>
        </a-radio-group>
      </div>
      <a-table row-key="key" :loading="loading" :data-source="rows" :columns="columns" size="middle"
        :pagination="{ current: page.pageNo, pageSize: page.pageSize, total: page.total, showSizeChanger: true, showTotal: (total: number) => `共 ${total} 人` }"
        @change="onTableChange">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <span class="rc-name"><span class="rm-avatar">{{ (record.elder.fullName || '长').slice(0, 1) }}</span><a class="rm-link" @click="open(record.elder.id)">{{ record.elder.fullName }}</a></span>
          </template>
          <template v-else-if="column.key === 'bed'">{{ [record.elder.roomNo, record.elder.bedNo].filter(Boolean).join(' / ') || '--' }}</template>
          <template v-else-if="column.key === 'level'">{{ record.elder.careLevel || '--' }}</template>
          <template v-else-if="column.key === 'staff'">{{ record.elder.responsibleStaffName || record.elder.caregiverNames?.join('、') || record.elder.caregiverName || '--' }}</template>
          <template v-else-if="column.key === 'assessment'">
            <template v-if="record.latestAssessmentDate">
              {{ record.latestLevelCode || '已评估' }}<small class="rc-muted">{{ record.latestAssessmentDate }}</small>
            </template>
            <span v-else class="rc-muted">未评估</span>
          </template>
          <template v-else-if="column.key === 'next'">
            <span :class="{ 'rc-due': isDue(record.nextAssessmentDate) }">{{ record.nextAssessmentDate || '--' }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="open(record.elder.id)">护理档案</a-button>
          </template>
        </template>
      </a-table>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import { getNursingResidentPage } from '../../../api/nursing'
import type { Id, NursingResidentItem } from '../../../types'
import '../../elder/residentUi.css'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const keyword = ref('')
const filter = ref(route.query.filter === 'reassess' ? 'reassess' : '')
const rows = ref<Array<NursingResidentItem & { key: string }>>([])
const page = reactive({ pageNo: 1, pageSize: 20, total: 0 })
const reassessCount = ref(0)
const today = dayjs().format('YYYY-MM-DD')

const columns = [
  { title: '长者', key: 'name', width: 180 },
  { title: '房间 / 床位', key: 'bed', width: 140 },
  { title: '护理等级', key: 'level', width: 110 },
  { title: '责任人员', key: 'staff', width: 140 },
  { title: '最新评估', key: 'assessment', width: 170 },
  { title: '下次复评', key: 'next', width: 120 },
  { title: '操作', key: 'action', width: 100 }
]

function isDue(date?: string) { return !!date && date <= today }
function open(id: Id) { router.push({ path: `/care/residents/${id}`, query: { residentId: String(id) } }) }
function applyFilter() { router.replace({ query: filter.value ? { filter: filter.value } : {} }); search() }
function search() { page.pageNo = 1; load() }
function onTableChange(pag: { current?: number; pageSize?: number }) {
  page.pageNo = pag.current || 1
  page.pageSize = pag.pageSize || 20
  load()
}

async function load() {
  loading.value = true
  try {
    const params = { pageNo: page.pageNo, pageSize: page.pageSize, keyword: keyword.value || undefined }
    const [res, due] = await Promise.all([
      getNursingResidentPage({ ...params, reassessDue: filter.value === 'reassess' }),
      // 「待复评」按钮上的人数
      getNursingResidentPage({ pageNo: 1, pageSize: 1, reassessDue: true }).catch(() => null)
    ])
    rows.value = (res.list || []).map((item) => ({ ...item, key: String(item.elder.id) }))
    page.total = res.total
    reassessCount.value = due ? Number(due.total) : 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.rc-name { display: inline-flex; align-items: center; gap: 8px; }
.rc-bar { display: flex; flex-wrap: wrap; gap: 12px; margin-bottom: 12px; }
.rc-muted { margin-left: 6px; color: #8a968f; font-size: 12px; }
.rc-due { color: #c9443d; font-weight: 600; }
</style>
