<template>
  <PageContainer title="老人出入报表" subTitle="老人入院/离院明细查询">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline">
        <a-form-item label="开始日期">
          <a-date-picker v-model:value="query.fromDate" style="width: 150px" />
        </a-form-item>
        <a-form-item label="结束日期">
          <a-date-picker v-model:value="query.toDate" style="width: 150px" />
        </a-form-item>
        <a-form-item label="事件类型">
          <a-select v-model:value="query.eventType" allow-clear style="width: 140px">
            <a-select-option value="ADMISSION">入院</a-select-option>
            <a-select-option value="DISCHARGE">离院</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="老人姓名">
          <a-input v-model:value="query.keyword" allow-clear placeholder="请输入老人姓名" style="width: 180px" />
        </a-form-item>
        <a-form-item label="机构ID">
          <a-input-number v-model:value="query.orgId" :min="1" placeholder="默认当前机构" style="width: 160px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="search">查询</a-button>
            <a-button @click="exportCsvReport">导出报表</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <vxe-table border stripe show-overflow :data="rows" height="420">
        <vxe-column field="eventDate" title="日期" width="140" />
        <vxe-column field="eventType" title="事件类型" width="120">
          <template #default="{ row }">
            <a-tag :color="row.eventType === 'ADMISSION' ? 'blue' : 'orange'">
              {{ row.eventType === 'ADMISSION' ? '入院' : '离院' }}
            </a-tag>
          </template>
        </vxe-column>
        <vxe-column field="elderName" title="老人姓名" min-width="160">
          <template #default="{ row }">
            {{ row.elderName || '未知老人' }}
          </template>
        </vxe-column>
        <vxe-column field="remark" title="备注" min-width="260" />
      </vxe-table>
      <div style="margin-top: 12px; text-align: right;">
        <a-pagination
          v-model:current="query.pageNo"
          v-model:page-size="query.pageSize"
          :total="total"
          show-size-changer
          :page-size-options="['10', '20', '50', '100']"
          @change="loadData"
          @show-size-change="onPageSizeChange"
        />
      </div>
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { exportElderFlowReportCsv, getElderFlowReport } from '../../api/stats'
import type { FlowReportItem } from '../../types'
import { message } from 'ant-design-vue'

const query = ref({
  fromDate: dayjs().subtract(29, 'day') as Dayjs,
  toDate: dayjs() as Dayjs,
  eventType: undefined as 'ADMISSION' | 'DISCHARGE' | undefined,
  keyword: undefined as string | undefined,
  orgId: undefined as number | undefined,
  pageNo: 1,
  pageSize: 20
})

const rows = ref<FlowReportItem[]>([])
const total = ref(0)

async function loadData() {
  const res = await getElderFlowReport({
    fromDate: dayjs(query.value.fromDate).format('YYYY-MM-DD'),
    toDate: dayjs(query.value.toDate).format('YYYY-MM-DD'),
    eventType: query.value.eventType,
    keyword: query.value.keyword,
    pageNo: query.value.pageNo,
    pageSize: query.value.pageSize,
    orgId: query.value.orgId
  })
  rows.value = res.list || []
  total.value = Number(res.total || 0)
}

function search() {
  query.value.pageNo = 1
  loadData()
}

function reset() {
  query.value.fromDate = dayjs().subtract(29, 'day')
  query.value.toDate = dayjs()
  query.value.eventType = undefined
  query.value.keyword = undefined
  query.value.orgId = undefined
  query.value.pageNo = 1
  query.value.pageSize = 20
  loadData()
}

function onPageSizeChange(_: number, pageSize: number) {
  query.value.pageNo = 1
  query.value.pageSize = pageSize
  loadData()
}

async function exportCsvReport() {
  await exportElderFlowReportCsv({
    fromDate: dayjs(query.value.fromDate).format('YYYY-MM-DD'),
    toDate: dayjs(query.value.toDate).format('YYYY-MM-DD'),
    eventType: query.value.eventType,
    keyword: query.value.keyword,
    orgId: query.value.orgId
  })
  message.success('报表导出成功')
}

onMounted(loadData)
</script>
