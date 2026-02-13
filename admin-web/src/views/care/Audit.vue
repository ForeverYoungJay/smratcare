<template>
  <PageContainer title="防作弊审计" subTitle="跨房间连续完成校验">
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
        <vxe-column field="staffId" title="护工" width="140" fixed="left">
          <template #default="{ row }">
            <span>{{ staffName(row.staffId) }}</span>
          </template>
        </vxe-column>
        <vxe-column field="elderName" title="老人" width="140"></vxe-column>
        <vxe-column field="roomNo" title="房间" width="100"></vxe-column>
        <vxe-column field="taskName" title="任务" min-width="160"></vxe-column>
        <vxe-column field="planTime" title="执行时间" width="160"></vxe-column>
        <vxe-column field="suspiciousFlag" title="可疑" width="100">
          <template #default="{ row }">
            <a-badge :status="row.suspiciousFlag ? 'error' : 'success'" :text="row.suspiciousFlag ? '可疑' : '正常'" />
          </template>
        </vxe-column>
        <vxe-column title="操作" width="140" fixed="right">
          <template #default="{ row }">
            <a @click="openDetail(row)">查看</a>
          </template>
        </vxe-column>
      </vxe-table>
    </a-card>

    <a-drawer v-model:open="detailOpen" title="审计详情" width="520">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="护工">{{ staffName(current?.staffId) }}</a-descriptions-item>
        <a-descriptions-item label="老人">{{ current?.elderName }}</a-descriptions-item>
        <a-descriptions-item label="房间">{{ current?.roomNo }}</a-descriptions-item>
        <a-descriptions-item label="任务">{{ current?.taskName }}</a-descriptions-item>
        <a-descriptions-item label="执行时间">{{ current?.planTime }}</a-descriptions-item>
        <a-descriptions-item label="可疑标记">{{ current?.suspiciousFlag ? '可疑' : '正常' }}</a-descriptions-item>
      </a-descriptions>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import PageContainer from '../../components/PageContainer.vue'
import { getTaskPage } from '../../api/care'
import { getStaffPage } from '../../api/rbac'
import type { CareTaskItem, PageResult } from '../../types'
import { exportCsv } from '../../utils/export'

const list = ref<CareTaskItem[]>([])
const loading = ref(false)
const detailOpen = ref(false)
const current = ref<CareTaskItem | null>(null)
const staffMap = ref<Record<number, string>>({})

async function search() {
  loading.value = true
  try {
    const res: PageResult<CareTaskItem> = await getTaskPage({ pageNo: 1, pageSize: 50 })
    list.value = res.list.filter((t) => t.suspiciousFlag)
    await loadStaffOptions()
  } finally {
    loading.value = false
  }
}

function openDetail(row: CareTaskItem) {
  current.value = row
  detailOpen.value = true
}

function exportCsvData() {
  exportCsv(
    list.value.map((t) => ({
      护工: staffName(t.staffId),
      老人: t.elderName,
      房间: t.roomNo,
      任务: t.taskName,
      执行时间: t.planTime,
      可疑: t.suspiciousFlag ? '是' : '否'
    })),
    '审计任务'
  )
}

async function loadStaffOptions() {
  const res = await getStaffPage({ pageNo: 1, pageSize: 200 })
  staffMap.value = res.list.reduce((acc: Record<number, string>, item: any) => {
    acc[item.id] = item.realName || item.username || `员工#${item.id}`
    return acc
  }, {})
}

function staffName(staffId?: number) {
  if (!staffId) return '未分配'
  return staffMap.value[staffId] || '未知护理员'
}

search()
</script>

<style scoped>
.table-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}
</style>
