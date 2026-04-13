<template>
  <PageContainer :title="pageTitle" :subTitle="pageSubTitle">
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
          style="width: 220px"
          @search="searchStaff"
          @focus="() => !staffOptions.length && searchStaff('')"
        />
      </a-form-item>
      <a-form-item label="员工类别">
        <a-select v-model:value="query.staffCategory" :options="staffCategoryOptions" allow-clear style="width: 180px" />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <a-form-item label="排序">
        <a-select v-model:value="query.sortBy" :options="sortOptions" style="width: 150px" />
      </a-form-item>
      <template #extra>
        <a-space>
          <a-button type="primary" @click="openRedeem">积分兑现金</a-button>
          <a-button @click="fetchData">刷新</a-button>
        </a-space>
      </template>
    </SearchForm>

    <a-card :bordered="false" class="card-elevated" style="margin-bottom: 16px">
      <a-row :gutter="16">
        <a-col :xs="12" :lg="3"><a-statistic title="任务量" :value="summary.taskCount" /></a-col>
        <a-col :xs="12" :lg="3"><a-statistic title="完成" :value="summary.successCount" /></a-col>
        <a-col :xs="12" :lg="3"><a-statistic title="失败" :value="summary.failCount" /></a-col>
        <a-col :xs="12" :lg="3"><a-statistic title="可疑" :value="summary.suspiciousCount" /></a-col>
        <a-col :xs="12" :lg="3"><a-statistic title="综合满意度均分" :value="summary.avgScore || 0" :precision="1" /></a-col>
        <a-col :xs="12" :lg="3"><a-statistic title="积分余额" :value="summary.pointsBalance" /></a-col>
        <a-col :xs="12" :lg="3"><a-statistic title="可兑现金(元)" :value="summary.redeemableCash || 0" /></a-col>
        <a-col :xs="12" :lg="3"><a-statistic title="积分净增长" :value="summary.pointsEarned - summary.pointsDeducted" /></a-col>
      </a-row>
    </a-card>

    <a-row :gutter="12" style="margin-bottom: 12px">
      <a-col :xs="24" :lg="8">
        <a-card size="small" class="card-elevated" :bordered="false">
          <div class="bucket-head">护理人员绩效积分</div>
          <div class="bucket-score">{{ bucketStats.nursing }}</div>
          <div class="bucket-sub">金/银/铜按护理序列独立排名</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card size="small" class="card-elevated" :bordered="false">
          <div class="bucket-head">行政人员绩效积分</div>
          <div class="bucket-score">{{ bucketStats.admin }}</div>
          <div class="bucket-sub">行政流程、保障效率、制度执行</div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="8">
        <a-card size="small" class="card-elevated" :bordered="false">
          <div class="bucket-head">运营人员绩效积分</div>
          <div class="bucket-score">{{ bucketStats.operation }}</div>
          <div class="bucket-sub">营销、活动、转化与运营复盘</div>
        </a-card>
      </a-col>
    </a-row>

    <DataTable rowKey="rankNo" :columns="columns" :data-source="ranks" :loading="loading" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'rankNo'">
          <a-tag :color="record.rankNo <= 3 ? 'gold' : 'default'">#{{ record.rankNo }}</a-tag>
        </template>
        <template v-else-if="column.key === 'staffCategory'">
          <a-tag :color="categoryTagColor(record.staffCategory)">{{ categoryText(record.staffCategory) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'medalLevel'">
          <a-tag :color="medalColor(record.medalLevel)">{{ medalText(record.medalLevel) }}</a-tag>
        </template>
      </template>
    </DataTable>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px" title="积分兑现金记录（最近30条）">
      <a-table
        row-key="id"
        :columns="redeemColumns"
        :data-source="redeemRows"
        :loading="redeemLoading"
        :pagination="false"
        size="small"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === 'APPROVED' ? 'green' : record.status === 'REJECTED' ? 'red' : 'orange'">
              {{ redeemStatusText(record.status) }}
            </a-tag>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal v-model:open="redeemOpen" title="积分兑现金申请" @ok="submitRedeem" :confirm-loading="redeeming" width="520px">
      <a-form layout="vertical">
        <a-alert
          show-icon
          type="info"
          style="margin-bottom: 12px"
          message="规则：每 300 积分可兑换 10 元，提交后进入院长审批，审批通过后自动扣减积分余额。"
        />
        <a-form-item label="兑换员工" required>
          <a-select
            v-model:value="redeemForm.staffId"
            show-search
            :filter-option="false"
            :options="staffOptions"
            :loading="staffLoading"
            @search="searchStaff"
            @focus="() => !staffOptions.length && searchStaff('')"
            placeholder="选择员工"
          />
        </a-form-item>
        <a-form-item label="兑换积分" required>
          <a-input-number v-model:value="redeemForm.redeemPoints" :min="300" :step="300" style="width: 100%" />
        </a-form-item>
        <a-form-item label="可兑换现金(元)">
          <a-input-number :value="redeemCash" disabled style="width: 100%" />
        </a-form-item>
        <a-form-item label="备注">
          <a-textarea v-model:value="redeemForm.remark" :rows="2" placeholder="可备注用途或审批说明" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import { useRoute } from 'vue-router'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { getPerformanceSummary, getPerformanceRanking } from '../../api/hr'
import { createApproval, getApprovalPage } from '../../api/oa'
import { useStaffOptions } from '../../composables/useStaffOptions'
import { useUserStore } from '../../stores/user'
import type { StaffPerformanceRankItem, StaffPerformanceSummary, PageResult } from '../../types'

const userStore = useUserStore()
const route = useRoute()

const performanceScene = computed<'nursing' | 'sales' | 'admin' | 'generation' | 'reports'>(() => {
  const queryScene = String(route.query.scene || '').trim()
  if (queryScene === 'nursing') return 'nursing'
  if (queryScene === 'sales') return 'sales'
  if (queryScene === 'admin') return 'admin'
  if (queryScene === 'generation') return 'generation'
  if (queryScene === 'reports') return 'reports'
  if (route.name === 'HrPerformanceNursing') return 'nursing'
  if (route.name === 'HrPerformanceSales') return 'sales'
  if (route.name === 'HrPerformanceAdmin') return 'admin'
  if (route.name === 'HrPerformanceGeneration') return 'generation'
  return 'reports'
})

const pageTitle = computed(() => {
  if (performanceScene.value === 'nursing') return '护理绩效'
  if (performanceScene.value === 'sales') return '销售绩效'
  if (performanceScene.value === 'admin') return '行政绩效'
  if (performanceScene.value === 'generation') return '绩效生成'
  return '绩效报表'
})

const pageSubTitle = computed(() => {
  if (performanceScene.value === 'nursing') return '绩效考核 / 护理序列积分与质量排名'
  if (performanceScene.value === 'sales') return '绩效考核 / 销售与运营转化绩效排名'
  if (performanceScene.value === 'admin') return '绩效考核 / 行政执行与协同效率排名'
  if (performanceScene.value === 'generation') return '绩效考核 / 绩效生成与审批联动'
  return '绩效考核 / 全员积分看板与兑现金审批'
})

const query = reactive({
  staffId: undefined as string | undefined,
  staffCategory: undefined as string | undefined,
  range: undefined as [Dayjs, Dayjs] | undefined,
  sortBy: 'points'
})

const sortOptions = [
  { label: '按积分', value: 'points' },
  { label: '按任务量', value: 'tasks' }
]

const staffCategoryOptions = [
  { label: '护理人员', value: 'NURSING' },
  { label: '行政人员', value: 'ADMINISTRATION' },
  { label: '运营人员', value: 'OPERATION' }
]

const summary = reactive<StaffPerformanceSummary>({
  staffId: 0,
  staffName: '',
  taskCount: 0,
  successCount: 0,
  failCount: 0,
  suspiciousCount: 0,
  avgScore: 0,
  pointsBalance: 0,
  pointsEarned: 0,
  pointsDeducted: 0,
  redeemableCash: 0
})

const ranks = ref<StaffPerformanceRankItem[]>([])
const loading = ref(false)
const { staffOptions, staffLoading, searchStaff } = useStaffOptions({ pageSize: 120 })
const redeemRows = ref<Array<{
  id: string
  staffId?: string
  staffName: string
  redeemPoints: number
  redeemCash: number
  status?: string
  applicantName?: string
  createTime?: string
}>>([])
const redeemLoading = ref(false)

const columns = [
  { title: '总排名', dataIndex: 'rankNo', key: 'rankNo', width: 90 },
  { title: '分组排名', dataIndex: 'categoryRankNo', key: 'categoryRankNo', width: 100 },
  { title: '员工', dataIndex: 'staffName', key: 'staffName', width: 120 },
  { title: '类别', dataIndex: 'staffCategory', key: 'staffCategory', width: 120 },
  { title: '勋章', dataIndex: 'medalLevel', key: 'medalLevel', width: 120 },
  { title: '任务量', dataIndex: 'taskCount', key: 'taskCount', width: 100 },
  { title: '完成', dataIndex: 'successCount', key: 'successCount', width: 100 },
  { title: '综合满意度', dataIndex: 'avgScore', key: 'avgScore', width: 120 },
  { title: '周期积分', dataIndex: 'periodPoints', key: 'periodPoints', width: 120 },
  { title: '积分余额', dataIndex: 'pointsBalance', key: 'pointsBalance', width: 120 },
  { title: '可兑现金(元)', dataIndex: 'redeemableCash', key: 'redeemableCash', width: 130 }
]
const redeemColumns = [
  { title: '申请时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: '兑换员工', dataIndex: 'staffName', key: 'staffName', width: 140 },
  { title: '发起人', dataIndex: 'applicantName', key: 'applicantName', width: 120 },
  { title: '兑换积分', dataIndex: 'redeemPoints', key: 'redeemPoints', width: 120 },
  { title: '兑换现金(元)', dataIndex: 'redeemCash', key: 'redeemCash', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]

const redeemOpen = ref(false)
const redeeming = ref(false)
const redeemForm = reactive({
  staffId: undefined as string | undefined,
  redeemPoints: 300,
  remark: ''
})
const redeemCash = computed(() => Math.floor(Number(redeemForm.redeemPoints || 0) / 300) * 10)

const bucketStats = computed(() => {
  const stats = { nursing: 0, admin: 0, operation: 0 }
  ranks.value.forEach((item) => {
    const points = Number(item.periodPoints || 0)
    if (item.staffCategory === 'NURSING') stats.nursing += points
    else if (item.staffCategory === 'OPERATION') stats.operation += points
    else stats.admin += points
  })
  return stats
})

function categoryText(value?: string) {
  if (value === 'NURSING') return '护理'
  if (value === 'OPERATION') return '运营'
  return '行政'
}

function categoryTagColor(value?: string) {
  if (value === 'NURSING') return 'blue'
  if (value === 'OPERATION') return 'purple'
  return 'cyan'
}

function medalText(value?: string) {
  if (value === 'GOLD') return '金牌'
  if (value === 'SILVER') return '银牌'
  if (value === 'BRONZE') return '铜牌'
  return '-'
}

function medalColor(value?: string) {
  if (value === 'GOLD') return 'gold'
  if (value === 'SILVER') return 'default'
  if (value === 'BRONZE') return 'orange'
  return 'default'
}

function redeemStatusText(status?: string) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'REJECTED') return '已驳回'
  return '待审批'
}

function parseJsonMap(raw?: string) {
  if (!raw) return {} as Record<string, any>
  try {
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

function toInt(value: any, fallback = 0) {
  const num = Number(value)
  return Number.isFinite(num) ? Math.round(num) : fallback
}

async function fetchRedeemRecords() {
  redeemLoading.value = true
  try {
    const res = await getApprovalPage({ pageNo: 1, pageSize: 80, type: 'POINTS_CASH' })
    const mapped = (res.list || []).map((item: any) => {
      const formData = parseJsonMap(item.formData)
      const redeemPoints = toInt(formData.redeemPoints)
      const redeemCash = toInt(formData.redeemCash, Math.floor(redeemPoints / 300) * 10)
      const staffId = formData.staffId ? String(formData.staffId) : undefined
      return {
        id: String(item.id),
        staffId,
        staffName: String(formData.staffName || item.applicantName || '-'),
        redeemPoints,
        redeemCash,
        status: item.status,
        applicantName: item.applicantName,
        createTime: item.createTime || item.updateTime || ''
      }
    })
    const filtered = query.staffId ? mapped.filter((item) => item.staffId === String(query.staffId)) : mapped
    redeemRows.value = filtered.slice(0, 30)
  } catch {
    redeemRows.value = []
  } finally {
    redeemLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {}
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    params.sortBy = query.sortBy
    params.staffCategory = query.staffCategory
    const rankRes = await getPerformanceRanking(params)
    ranks.value = rankRes

    if (query.staffId) {
      const summaryRes = await getPerformanceSummary({
        staffId: query.staffId,
        dateFrom: params.dateFrom,
        dateTo: params.dateTo
      })
      Object.assign(summary, summaryRes)
    } else {
      summary.taskCount = 0
      summary.successCount = 0
      summary.failCount = 0
      summary.suspiciousCount = 0
      summary.avgScore = 0
      summary.pointsBalance = 0
      summary.pointsEarned = 0
      summary.pointsDeducted = 0
      summary.redeemableCash = 0
    }
    await fetchRedeemRecords()
  } finally {
    loading.value = false
  }
}

function onReset() {
  query.staffId = undefined
  query.staffCategory = undefined
  query.range = undefined
  query.sortBy = 'points'
  fetchData()
}

function openRedeem() {
  redeemForm.staffId = query.staffId
  redeemForm.redeemPoints = 300
  redeemForm.remark = ''
  redeemOpen.value = true
}

async function submitRedeem() {
  if (!redeemForm.staffId) {
    message.warning('请先选择兑换员工')
    return
  }
  if (!redeemForm.redeemPoints || redeemForm.redeemPoints < 300 || redeemForm.redeemPoints % 300 !== 0) {
    message.warning('兑换积分需按 300 的整数倍填写')
    return
  }
  if (redeemCash.value <= 0) {
    message.warning('当前兑换积分不足以换算现金')
    return
  }
  redeeming.value = true
  try {
    const staffName = staffOptions.value.find((item) => item.value === redeemForm.staffId)?.label || '未识别员工'
    await createApproval({
      approvalType: 'POINTS_CASH',
      title: `积分兑现金申请-${staffName}`,
      applicantId: userStore.staffInfo?.id,
      applicantName: userStore.staffInfo?.realName || userStore.staffInfo?.username || '系统用户',
      amount: redeemCash.value,
      status: 'PENDING',
      formData: JSON.stringify({
        staffId: redeemForm.staffId,
        staffName,
        redeemPoints: redeemForm.redeemPoints,
        redeemCash: redeemCash.value,
        exchangeRate: '300:10',
        remark: redeemForm.remark || undefined
      }),
      remark: redeemForm.remark || `申请将 ${redeemForm.redeemPoints} 积分兑换为 ${redeemCash.value} 元`
    })
    message.success('积分兑现金申请已提交院长审批')
    redeemOpen.value = false
    await fetchRedeemRecords()
  } catch (error: any) {
    message.error(error?.message || '提交失败')
  } finally {
    redeeming.value = false
  }
}

searchStaff('')
fetchData()

watch(
  () => route.fullPath,
  () => {
    if (loading.value) return
    fetchData()
  }
)
</script>

<style scoped>
.bucket-head {
  color: #64748b;
  font-size: 13px;
}
.bucket-score {
  font-size: 28px;
  line-height: 1.2;
  font-weight: 700;
  margin: 8px 0 4px;
}
.bucket-sub {
  color: #94a3b8;
  font-size: 12px;
}
</style>
