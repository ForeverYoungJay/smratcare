<template>
  <PageContainer class="rm-page" title="护理档案与评估" sub-title="与长者管理共用同一份长者资料；点「护理档案」查看评估、护理计划、服务记录与异常。">
    <section class="rm-card">
      <div class="rc-bar">
        <a-input-search v-model:value="keyword" allow-clear placeholder="姓名、房间或床位" style="width: 260px" @search="load" />
        <a-radio-group v-model:value="filter" button-style="solid" @change="applyFilter">
          <a-radio-button value="">全部在住</a-radio-button>
          <a-radio-button value="reassess">待复评 {{ reassessIds.size ? `(${reassessIds.size})` : '' }}</a-radio-button>
        </a-radio-group>
      </div>
      <a-table row-key="id" :loading="loading" :data-source="visibleRows" :columns="columns" :pagination="{ pageSize: 20, showSizeChanger: false }" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'name'">
            <span class="rc-name"><span class="rm-avatar">{{ (record.fullName || '长').slice(0, 1) }}</span><a class="rm-link" @click="open(record.id)">{{ record.fullName }}</a></span>
          </template>
          <template v-else-if="column.key === 'bed'">{{ [record.roomNo, record.bedNo].filter(Boolean).join(' / ') || '--' }}</template>
          <template v-else-if="column.key === 'staff'">{{ record.responsibleStaffName || record.caregiverNames?.join('、') || record.caregiverName || '--' }}</template>
          <template v-else-if="column.key === 'assessment'">
            <template v-if="latest.get(String(record.id))">
              {{ latest.get(String(record.id))!.levelCode || '已评估' }}
              <small class="rc-muted">{{ latest.get(String(record.id))!.assessmentDate }}</small>
            </template>
            <span v-else class="rc-muted">未评估</span>
          </template>
          <template v-else-if="column.key === 'next'">
            <span :class="{ 'rc-due': reassessIds.has(String(record.id)) }">{{ latest.get(String(record.id))?.nextAssessmentDate || '--' }}</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" size="small" @click="open(record.id)">护理档案</a-button>
          </template>
        </template>
      </a-table>
    </section>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import PageContainer from '../../../components/PageContainer.vue'
import { getElderPage } from '../../../api/elder'
import { getAssessmentRecordPage } from '../../../api/assessment'
import type { AssessmentRecord, ElderItem, Id } from '../../../types'
import { latestAssessmentByElder } from './residentCare'
import '../../elder/residentUi.css'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const keyword = ref('')
const filter = ref(route.query.filter === 'reassess' ? 'reassess' : '')
const rows = ref<ElderItem[]>([])
const latest = ref(new Map<string, AssessmentRecord>())
const today = dayjs().format('YYYY-MM-DD')

const reassessIds = computed(() => new Set([...latest.value.entries()]
  .filter(([, record]) => record.nextAssessmentDate && record.nextAssessmentDate <= today).map(([id]) => id)))
const visibleRows = computed(() => filter.value === 'reassess' ? rows.value.filter((row) => reassessIds.value.has(String(row.id))) : rows.value)

const columns = [
  { title: '长者', key: 'name', width: 180 },
  { title: '房间 / 床位', key: 'bed', width: 140 },
  { title: '护理等级', dataIndex: 'careLevel', width: 110 },
  { title: '责任人员', key: 'staff', width: 140 },
  { title: '最新评估', key: 'assessment', width: 170 },
  { title: '下次复评', key: 'next', width: 120 },
  { title: '操作', key: 'action', width: 100 }
]

function open(id: Id) { router.push({ path: `/care/residents/${id}`, query: { residentId: String(id) } }) }
function applyFilter() { router.replace({ query: filter.value ? { filter: filter.value } : {} }) }

async function load() {
  loading.value = true
  try {
    // shortcut: 一次取前 500 位在住长者和最近 500 条评估，在前端配对；机构超过 500 人时改成后端分页 + 复评筛选
    const [elders, records] = await Promise.all([
      getElderPage({ pageNo: 1, pageSize: 500, lifecycleStatus: 'IN_HOSPITAL', keyword: keyword.value || undefined }),
      getAssessmentRecordPage({ pageNo: 1, pageSize: 500 }).catch(() => ({ list: [] as AssessmentRecord[] }))
    ])
    rows.value = elders.list || []
    latest.value = latestAssessmentByElder(records.list || [])
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
