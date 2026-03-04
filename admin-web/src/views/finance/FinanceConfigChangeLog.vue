<template>
  <PageContainer title="配置变更记录" subTitle="查看财务配置变更轨迹（谁在何时改了什么）">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query">
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="query.from" style="width: 150px" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="query.to" style="width: 150px" />
        </a-form-item>
        <a-form-item label="关键字">
          <a-input v-model:value="query.keyword" allow-clear placeholder="操作人/动作/详情" style="width: 220px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="loadData">查询</a-button>
            <a-button @click="reset">重置</a-button>
            <a-button @click="exportData">导出</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table border stripe show-overflow :loading="loading" :data="rows" height="560">
        <vxe-column field="createTime" title="时间" width="180" />
        <vxe-column field="actorName" title="操作人" width="140">
          <template #default="{ row }">{{ row.actorName || '-' }}</template>
        </vxe-column>
        <vxe-column field="actionType" title="动作" width="220">
          <template #default="{ row }">
            <a-tag color="blue">{{ row.actionType }}</a-tag>
          </template>
        </vxe-column>
        <vxe-column field="entityType" title="实体类型" width="140" />
        <vxe-column field="entityId" title="实体ID" width="140" />
        <vxe-column field="detail" title="变更详情" min-width="300" />
        <vxe-column title="操作" width="160" fixed="right">
          <template #default="{ row }">
            <a-space>
              <a-button type="link" @click="rollback(row)">回滚</a-button>
            </a-space>
          </template>
        </vxe-column>
      </vxe-table>

      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="query.pageNo"
        :page-size="query.pageSize"
        :total="total"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { getFinanceConfigChangeLogPage, rollbackFinanceBillingConfig } from '../../api/finance'
import type { FinanceConfigChangeLogItem, PageResult } from '../../types'
import { exportCsv } from '../../utils/export'

const loading = ref(false)
const rows = ref<FinanceConfigChangeLogItem[]>([])
const total = ref(0)

const query = reactive({
  from: dayjs().subtract(30, 'day'),
  to: dayjs(),
  keyword: '',
  pageNo: 1,
  pageSize: 20
})

async function loadData() {
  loading.value = true
  try {
    const result: PageResult<FinanceConfigChangeLogItem> = await getFinanceConfigChangeLogPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      from: query.from ? dayjs(query.from).format('YYYY-MM-DD') : undefined,
      to: query.to ? dayjs(query.to).format('YYYY-MM-DD') : undefined,
      keyword: query.keyword || undefined
    })
    rows.value = result.list || []
    total.value = Number(result.total || 0)
  } finally {
    loading.value = false
  }
}

function reset() {
  query.from = dayjs().subtract(30, 'day')
  query.to = dayjs()
  query.keyword = ''
  query.pageNo = 1
  loadData()
}

function onPageChange(page: number) {
  query.pageNo = page
  loadData()
}

function onPageSizeChange(_: number, size: number) {
  query.pageNo = 1
  query.pageSize = size
  loadData()
}

onMounted(loadData)

async function rollback(row: FinanceConfigChangeLogItem) {
  if (!row?.id) return
  try {
    await rollbackFinanceBillingConfig({ logId: Number(row.id) })
    message.success('回滚成功')
    loadData()
  } catch (error: any) {
    message.error(error?.message || '回滚失败')
  }
}

function exportData() {
  exportCsv(
    rows.value.map(item => ({
      时间: item.createTime || '',
      操作人: item.actorName || '',
      动作: item.actionType || '',
      实体类型: item.entityType || '',
      实体ID: item.entityId || '',
      详情: item.detail || ''
    })),
    `财务配置变更记录-${dayjs().format('YYYYMMDD-HHmmss')}.csv`
  )
}
</script>
