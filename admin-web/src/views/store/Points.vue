<template>
  <PageContainer title="积分账户" subTitle="积分余额与调整记录">
    <a-card class="card-elevated" :bordered="false">
      <a-form layout="inline" :model="query" class="search-form">
        <a-form-item label="关键词">
          <a-input v-model:value="query.keyword" placeholder="老人姓名" allow-clear />
        </a-form-item>
        <a-form-item label="状态">
          <a-select v-model:value="query.status" allow-clear style="width: 120px">
            <a-select-option :value="1">正常</a-select-option>
            <a-select-option :value="0">冻结</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchData">搜索</a-button>
            <a-button @click="reset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <div class="table-actions">
        <a-space>
          <a-button type="primary" @click="openAdjust">积分调整</a-button>
        </a-space>
      </div>

      <vxe-toolbar custom export></vxe-toolbar>
      <vxe-table
        border
        stripe
        show-overflow
        height="520"
        :loading="loading"
        :data="rows"
        :column-config="{ resizable: true }"
      >
        <vxe-column field="elderName" title="老人" min-width="160" />
        <vxe-column field="elderId" title="老人ID" width="120" />
        <vxe-column field="pointsBalance" title="积分余额" width="120" />
        <vxe-column field="status" title="状态" width="120">
          <template #default="{ row }">
            <a-tag :color="row.status === 1 ? 'green' : 'default'">
              {{ row.status === 1 ? '正常' : '冻结' }}
            </a-tag>
          </template>
        </vxe-column>
        <vxe-column field="updateTime" title="更新时间" width="180" />
        <vxe-column title="操作" width="140" fixed="right">
          <template #default="{ row }">
            <a @click="openLogs(row)">查看流水</a>
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

    <a-modal v-model:open="adjustOpen" title="积分调整" @ok="submitAdjust" :confirm-loading="adjusting">
      <a-form layout="vertical" :model="adjustForm" :rules="adjustRules" ref="adjustFormRef">
        <a-form-item label="老人ID" name="elderId">
          <a-input-number v-model:value="adjustForm.elderId" style="width: 100%" />
        </a-form-item>
        <a-form-item label="调整方向" name="direction">
          <a-select v-model:value="adjustForm.direction">
            <a-select-option value="CREDIT">增加</a-select-option>
            <a-select-option value="DEBIT">扣减</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="积分数量" name="points">
          <a-input-number v-model:value="adjustForm.points" style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-input v-model:value="adjustForm.remark" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer v-model:open="logOpen" title="积分流水" width="680">
      <a-table :data-source="logRows" :pagination="false" row-key="id">
        <a-table-column title="时间" data-index="createTime" />
        <a-table-column title="变更类型" data-index="changeType" />
        <a-table-column title="变更积分" data-index="changePoints" />
        <a-table-column title="余额" data-index="balanceAfter" />
        <a-table-column title="订单ID" data-index="refOrderId" />
        <a-table-column title="备注" data-index="remark" />
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="logQuery.pageNo"
        :page-size="logQuery.pageSize"
        :total="logTotal"
        show-size-changer
        @change="onLogPageChange"
        @showSizeChange="onLogPageSizeChange"
      />
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import { adjustPoints, getPointsAccountPage, getPointsLogPage } from '../../api/store'
import type { PageResult, PointsAccountItem, PointsAdjustRequest, PointsLogItem } from '../../types'

const loading = ref(false)
const rows = ref<PointsAccountItem[]>([])
const total = ref(0)
const query = reactive({
  keyword: '',
  status: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

const adjustOpen = ref(false)
const adjusting = ref(false)
const adjustFormRef = ref()
const adjustForm = reactive<PointsAdjustRequest>({
  elderId: 0,
  points: 0,
  direction: 'CREDIT',
  remark: ''
})
const adjustRules = {
  elderId: [{ required: true, message: '请输入老人ID' }],
  points: [{ required: true, message: '请输入积分数量' }],
  direction: [{ required: true, message: '请选择方向' }]
}

const logOpen = ref(false)
const logRows = ref<PointsLogItem[]>([])
const logTotal = ref(0)
const logQuery = reactive({
  elderId: undefined as number | undefined,
  pageNo: 1,
  pageSize: 10
})

async function fetchData() {
  loading.value = true
  try {
    const res: PageResult<PointsAccountItem> = await getPointsAccountPage({
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      status: query.status
    })
    rows.value = res.list
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function reset() {
  query.keyword = ''
  query.status = undefined
  query.pageNo = 1
  fetchData()
}

function onPageChange(page: number) {
  query.pageNo = page
  fetchData()
}

function onPageSizeChange(_page: number, size: number) {
  query.pageSize = size
  query.pageNo = 1
  fetchData()
}

function openAdjust() {
  Object.assign(adjustForm, { elderId: 0, points: 0, direction: 'CREDIT', remark: '' })
  adjustOpen.value = true
}

async function submitAdjust() {
  await adjustFormRef.value?.validate()
  adjusting.value = true
  try {
    await adjustPoints(adjustForm)
    message.success('调整成功')
    adjustOpen.value = false
    fetchData()
  } finally {
    adjusting.value = false
  }
}

async function openLogs(row: PointsAccountItem) {
  logQuery.elderId = row.elderId
  logQuery.pageNo = 1
  logOpen.value = true
  await fetchLogs()
}

async function fetchLogs() {
  const res: PageResult<PointsLogItem> = await getPointsLogPage({
    pageNo: logQuery.pageNo,
    pageSize: logQuery.pageSize,
    elderId: logQuery.elderId
  })
  logRows.value = res.list
  logTotal.value = res.total
}

function onLogPageChange(page: number) {
  logQuery.pageNo = page
  fetchLogs()
}

function onLogPageSizeChange(_page: number, size: number) {
  logQuery.pageSize = size
  logQuery.pageNo = 1
  fetchLogs()
}

onMounted(fetchData)
</script>

<style scoped>
.search-form {
  row-gap: 8px;
}
.table-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}
</style>
