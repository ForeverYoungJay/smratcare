<template>
  <PageContainer title="异常任务中心" subTitle="任务异常快速处置">
    <a-card :bordered="false" class="card-elevated">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="search">刷新</a-button>
          <a-button @click="exportCsvData">导出CSV</a-button>
        </a-space>
      </div>
      <vxe-table
        border
        stripe
        height="520"
        :loading="loading"
        :data="list"
        :column-config="{ resizable: true }"
      >
        <vxe-column field="elderName" title="老人" width="140" fixed="left"></vxe-column>
        <vxe-column field="roomNo" title="房间" width="100"></vxe-column>
        <vxe-column field="taskName" title="任务" min-width="160"></vxe-column>
        <vxe-column field="planTime" title="计划时间" width="160"></vxe-column>
        <vxe-column field="staffId" title="护工" width="140">
          <template #default="{ row }">
            <span>{{ staffName(row.staffId) }}</span>
          </template>
        </vxe-column>
        <vxe-column title="操作" width="140" fixed="right">
          <template #default="{ row }">
            <div class="row-action-links">
              <a-button type="link" size="small" @click="openDetail(row)">查看</a-button>
              <a-button type="link" size="small" @click="openResolve(row)">处理闭环</a-button>
            </div>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>

    <a-drawer v-model:open="detailOpen" title="异常详情" width="520">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="老人">{{ current?.elderName }}</a-descriptions-item>
        <a-descriptions-item label="房间">{{ current?.roomNo }}</a-descriptions-item>
        <a-descriptions-item label="任务">{{ current?.taskName }}</a-descriptions-item>
        <a-descriptions-item label="计划时间">{{ current?.planTime }}</a-descriptions-item>
        <a-descriptions-item label="护工">{{ staffName(current?.staffId) }}</a-descriptions-item>
        <a-descriptions-item label="异常原因">{{ current?.status === 'EXCEPTION' ? '执行异常' : '未知' }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>

    <a-modal v-model:open="resolveOpen" title="异常任务处理闭环" :confirm-loading="resolving" @ok="submitResolve">
      <a-form layout="vertical">
        <a-form-item label="任务">
          <a-input :value="resolveTarget?.taskName || '-'" disabled />
        </a-form-item>
        <a-form-item label="处理护工">
          <a-select
            v-model:value="resolveForm.staffId"
            show-search
            :filter-option="false"
            :options="staffOptions"
            placeholder="选择实际处理人"
            @search="searchStaff"
          />
        </a-form-item>
        <a-form-item label="处理评分">
          <a-rate v-model:value="resolveForm.score" />
        </a-form-item>
        <a-form-item label="处理措施">
          <a-textarea
            v-model:value="resolveForm.resolution"
            :rows="4"
            :maxlength="500"
            show-count
            placeholder="例如：已复核老人状态，补做翻身护理并同步护士长复盘。"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import { getTaskPage, resolveCareTaskException } from '../../api/care'
import { useStaffOptions } from '../../composables/useStaffOptions'
import type { CareTaskItem, PageResult } from '../../types'
import { exportCsv } from '../../utils/export'
import { normalizeResidentId } from '../../utils/id'
import { resolveCareError } from './careError'
import { careExceptionExportColumns, mapCareExportRows } from '../../constants/careExport'

const list = ref<CareTaskItem[]>([])
const route = useRoute()
const loading = ref(false)
const detailOpen = ref(false)
const resolveOpen = ref(false)
const resolving = ref(false)
const current = ref<CareTaskItem | null>(null)
const resolveTarget = ref<CareTaskItem | null>(null)
const staffLoaded = ref(false)
const { staffOptions, searchStaff, findStaffName } = useStaffOptions({ pageSize: 220, preloadSize: 500 })
const resolveForm = reactive({
  staffId: undefined as number | undefined,
  score: 5,
  resolution: ''
})
const selectedElderId = computed(() => normalizeResidentId(route.query as Record<string, unknown>))

async function search() {
  loading.value = true
  try {
    const res: PageResult<CareTaskItem> = await getTaskPage({
      pageNo: 1,
      pageSize: 50,
      status: 'EXCEPTION',
      elderId: selectedElderId.value
    })
    list.value = res.list
    await ensureStaffLoaded()
  } catch (error) {
    message.error(resolveCareError(error, '加载异常任务失败'))
    list.value = []
  } finally {
    loading.value = false
  }
}

function openDetail(row: CareTaskItem) {
  current.value = row
  detailOpen.value = true
}

async function openResolve(row: CareTaskItem) {
  resolveTarget.value = row
  resolveForm.staffId = row.staffId
  resolveForm.score = 5
  resolveForm.resolution = ''
  resolveOpen.value = true
  await ensureStaffLoaded()
}

async function submitResolve() {
  if (!resolveTarget.value) return
  if (!resolveForm.staffId) {
    message.warning('请选择处理护工')
    return
  }
  resolving.value = true
  try {
    await resolveCareTaskException({
      taskDailyId: resolveTarget.value.taskDailyId,
      staffId: Number(resolveForm.staffId),
      score: Number(resolveForm.score || 5),
      resolution: resolveForm.resolution || undefined
    })
    message.success('异常任务已闭环')
    resolveOpen.value = false
    await search()
  } catch (error) {
    message.error(resolveCareError(error, '异常任务处理失败'))
  } finally {
    resolving.value = false
  }
}

function exportCsvData() {
  if (!list.value.length) {
    message.warning('暂无可导出数据')
    return
  }
  const rows = mapCareExportRows(list.value, careExceptionExportColumns).map((item, index) => ({
    ...item,
    护工: staffName(list.value[index]?.staffId)
  }))
  exportCsv(rows, '异常任务')
  message.success('CSV导出成功')
}

async function ensureStaffLoaded() {
  if (staffLoaded.value) return
  try {
    await searchStaff('')
    staffLoaded.value = true
  } catch (error) {
    message.error(resolveCareError(error, '加载护工信息失败'))
  }
}

function staffName(staffId?: number) {
  if (!staffId) return '未分配'
  return findStaffName(staffId) || '未识别员工'
}

search()
watch(() => route.query, search)
</script>

<style scoped>
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
