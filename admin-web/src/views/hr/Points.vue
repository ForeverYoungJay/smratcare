<template>
  <PageContainer title="积分台账" subTitle="任务积分、奖励扣减与人工调整。">
    <template #extra>
      <a-button type="primary" @click="openAdjust">积分调整</a-button>
    </template>
    <SearchForm :model="query" @search="fetchData" @reset="onReset">
      <a-form-item label="员工">
        <a-select
          v-model:value="query.staffId"
          allow-clear
          show-search
          :filter-option="false"
          :options="staffOptions"
          :loading="staffLoading"
          placeholder="选择员工"
          style="width: 200px"
          @search="searchStaff"
          @focus="() => !staffOptions.length && searchStaff('')"
        />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>

    </SearchForm>

    <DataTable
      :filter-model="query"
      @clear-filters="fetchData"
      rowKey="id"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'changePoints'">
          <span :class="record.changePoints > 0 ? 'pos' : 'neg'">
            {{ record.changePoints > 0 ? '+' : '' }}{{ record.changePoints }}
          </span>
        </template>
        <template v-else-if="column.key === 'reverse'">
          <a v-if="record.sourceType !== 'REVERSAL'" @click="reverse(record)">冲正</a>
          <span v-else class="neg">冲正记录</span>
        </template>
      </template>
    </DataTable>

    <a-modal :mask-closable="false" v-model:open="adjustOpen" title="积分调整" @ok="submitAdjust" :confirm-loading="adjusting">
      <a-form layout="vertical">
        <a-form-item label="员工" required>
          <a-select
            v-model:value="adjustForm.staffId"
            allow-clear
            show-search
            :filter-option="false"
            :options="staffOptions"
            :loading="staffLoading"
            placeholder="选择员工"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
          />
        </a-form-item>
        <a-form-item label="调整类型" required>
          <a-select v-model:value="adjustForm.changeType" :options="changeTypeOptions" />
        </a-form-item>
        <a-form-item label="积分数量" required>
          <a-input-number v-model:value="adjustForm.changePoints" style="width: 100%" :min="1" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="adjustForm.remark" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { h, reactive, ref } from 'vue'
import { Input, message, Modal } from 'ant-design-vue'
import type { Dayjs } from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { adjustStaffPoints, getStaffPointsLog } from '../../api/hr'
import { reversePointsLog } from '../../api/hrOps'
import { useStaffOptions } from '../../composables/useStaffOptions'
import type { StaffPointsLog, PageResult } from '../../types'

const query = reactive({
  staffId: undefined as number | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const rows = ref<StaffPointsLog[]>([])
const loading = ref(false)
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })

const columns = [
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '类型', dataIndex: 'changeType', key: 'changeType', width: 100 },
  { title: '变动', dataIndex: 'changePoints', key: 'changePoints', width: 100 },
  { title: '余额', dataIndex: 'balanceAfter', key: 'balanceAfter', width: 100 },
  { title: '来源', dataIndex: 'sourceType', key: 'sourceType', width: 120 },
  { title: '时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '备注', dataIndex: 'remark', key: 'remark', width: 200 },
  { title: '', key: 'reverse', width: 90 }
]

/** 更正积分用冲正：追加相反分值流水，原记录保留 */
function reverse(record: StaffPointsLog) {
  let reason = ''
  Modal.confirm({
    title: `冲正 ${record.staffName || ''} 的 ${record.changePoints > 0 ? '+' : ''}${record.changePoints} 分`,
    content: () => h(Input.TextArea, { rows: 2, placeholder: '冲正原因（必填）', onChange: (e: Event) => { reason = (e.target as HTMLTextAreaElement).value } }),
    okText: '确认冲正',
    async onOk() {
      if (!reason.trim()) {
        message.warning('请填写冲正原因')
        return Promise.reject(new Error('reason required'))
      }
      await reversePointsLog(String(record.id), reason.trim())
      message.success('已冲正')
      await fetchData()
    }
  })
}

const adjustOpen = ref(false)
const adjusting = ref(false)
const adjustForm = reactive({ staffId: undefined as number | undefined, changeType: 'EARN', changePoints: 1, remark: '' })
const { staffOptions, staffLoading, searchStaff } = useStaffOptions({ pageSize: 120 })
const changeTypeOptions = [
  { label: '奖励', value: 'EARN' },
  { label: '扣减', value: 'DEDUCT' },
  { label: '调整', value: 'ADJUST' }
]

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      staffId: query.staffId
    }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<StaffPointsLog> = await getStaffPointsLog(params)
    rows.value = res.list
    pagination.total = res.total || res.list.length
  } catch {
    rows.value = []
  } finally {
    loading.value = false
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function onReset() {
  query.staffId = undefined
  query.range = undefined
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openAdjust() {
  adjustForm.staffId = undefined
  adjustForm.changeType = 'EARN'
  adjustForm.changePoints = 1
  adjustForm.remark = ''
  adjustOpen.value = true
}

async function submitAdjust() {
  if (!adjustForm.staffId) {
    message.error('请选择员工')
    return
  }
  adjusting.value = true
  try {
    await adjustStaffPoints({
      staffId: adjustForm.staffId,
      changeType: adjustForm.changeType,
      changePoints: adjustForm.changePoints,
      remark: adjustForm.remark
    })
    message.success('调整成功')
    adjustOpen.value = false
    fetchData()
  } catch {
    message.error('调整失败')
  } finally {
    adjusting.value = false
  }
}

searchStaff('')
fetchData()
</script>

<style scoped>
.pos {
  color: var(--primary);
  font-weight: 600;
}

.neg {
  color: var(--danger);
  font-weight: 600;
}
</style>
