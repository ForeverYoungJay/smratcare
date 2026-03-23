<template>
  <PageContainer title="活动计划" subTitle="从计划、方案、审批到执行复盘的一体化工作台">
    <div class="activity-hero">
      <div class="hero-copy">
        <span class="hero-kicker">Activity Studio</span>
        <h2>养老活动不再只是一张日历，而是一条可追踪的执行链。</h2>
        <p>支持活动方案上传、费用审批、执行签到、照片留存与复盘归档，按钮会根据状态自动变化。</p>
      </div>
      <div class="hero-stats">
        <div class="hero-stat">
          <strong>{{ summary.pendingScheme }}</strong>
          <span>待提交方案</span>
        </div>
        <div class="hero-stat">
          <strong>{{ summary.approving }}</strong>
          <span>审批中</span>
        </div>
        <div class="hero-stat">
          <strong>{{ summary.executing }}</strong>
          <span>执行中</span>
        </div>
        <div class="hero-stat">
          <strong>{{ summary.completed }}</strong>
          <span>已完成</span>
        </div>
      </div>
    </div>

    <SearchForm :model="query" @search="fetchData" @reset="resetQuery">
      <a-form-item label="关键词">
        <a-input v-model:value="query.keyword" placeholder="活动名称/地点/组织人" allow-clear />
      </a-form-item>
      <a-form-item label="活动类型">
        <a-select v-model:value="query.activityType" :options="activityTypeOptions" allow-clear style="width: 180px" />
      </a-form-item>
      <a-form-item label="状态">
        <a-select v-model:value="query.status" :options="statusOptions" allow-clear style="width: 180px" />
      </a-form-item>
      <a-form-item label="组织人">
        <a-input v-model:value="query.organizer" placeholder="请输入组织人" allow-clear />
      </a-form-item>
      <a-form-item label="日期范围">
        <a-range-picker v-model:value="query.range" />
      </a-form-item>
      <template #extra>
        <a-space wrap>
          <a-button type="primary" :disabled="!canCreatePlan" @click="openPlanner()">新增计划</a-button>
          <a-button :disabled="!selectedSingle" @click="openDetail(selectedSingle)">查看详情</a-button>
          <a-button @click="downloadExport">导出 CSV</a-button>
        </a-space>
      </template>
    </SearchForm>

    <div class="status-strip">
      <button
        v-for="item in quickStatuses"
        :key="item.value"
        type="button"
        class="status-pill"
        :class="{ active: quickStatus === item.value }"
        @click="applyQuickStatus(item.value)"
      >
        <span>{{ item.label }}</span>
        <strong>{{ countByStatus(item.value) }}</strong>
      </button>
    </div>

    <DataTable
      rowKey="id"
      :row-selection="rowSelection"
      :columns="columns"
      :data-source="rows"
      :loading="loading"
      :pagination="pagination"
      @change="handleTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'activity'">
          <div class="activity-cell">
            <strong>{{ record.title }}</strong>
            <span>{{ record.activityType || '未分类' }}</span>
          </div>
        </template>
        <template v-else-if="column.key === 'status'">
          <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'riskLevel'">
          <a-tag :color="riskColor(record.riskLevel)">{{ riskText(record.riskLevel) }}</a-tag>
        </template>
        <template v-else-if="column.key === 'budgetAmount'">
          <span>{{ budgetText(record) }}</span>
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap size="small">
            <a-button size="small" @click="openDetail(record)">查看 / 编辑</a-button>
            <a-button
              v-if="record.status === 'PLAN_PENDING' || record.status === 'REJECTED' || record.status === 'DRAFT'"
              size="small"
              type="link"
              :disabled="!canEditRecord(record)"
              @click="openPlanner(record)"
            >
              编辑
            </a-button>
            <a-button
              v-if="record.status === 'APPROVED'"
              size="small"
              type="link"
              :disabled="!canOperateLifecycle(record)"
              @click="startPlan(record)"
            >
              开始活动
            </a-button>
            <a-button
              v-if="record.status === 'EXECUTING'"
              size="small"
              type="link"
              :disabled="!canOperateLifecycle(record)"
              @click="completePlan(record)"
            >
              完成
            </a-button>
          </a-space>
        </template>
      </template>
    </DataTable>

    <a-modal
      v-model:open="plannerOpen"
      :title="plannerForm.id ? '编辑活动计划' : '新增活动计划'"
      width="920px"
      :confirm-loading="saving"
      @ok="submitPlanner"
    >
      <div class="planner-headline">
        <div>
          <strong>基础信息</strong>
          <span>保存后默认状态为“待提交方案”，后续再进入详情页上传方案并发起审批。</span>
        </div>
        <a-tag color="gold">{{ statusText(plannerForm.status) }}</a-tag>
      </div>
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="活动名称" required>
              <a-input v-model:value="plannerForm.title" placeholder="如：春日生日会" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="活动类型">
              <a-select v-model:value="plannerForm.activityType" :options="activityTypeOptions" placeholder="请选择活动类型" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="活动时间" required>
              <a-range-picker v-model:value="plannerRange" show-time style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="地点">
              <a-input v-model:value="plannerForm.location" placeholder="请输入活动地点" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="组织人">
              <a-input v-model:value="plannerForm.organizer" placeholder="请输入组织人" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="参与对象">
              <a-input v-model:value="plannerForm.participantTarget" placeholder="如：护理二区长者" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="选择老人（支持标签）">
              <a-select
                v-model:value="participantElderTags"
                mode="tags"
                style="width: 100%"
                placeholder="输入老人姓名或编号标签"
              />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="参与标签">
              <a-select
                v-model:value="participantTags"
                mode="tags"
                style="width: 100%"
                placeholder="如：生日老人 / 失智专区 / 家属参与"
              />
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item label="人数预估">
              <a-input-number v-model:value="plannerForm.estimatedCount" :min="0" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="是否需要医护人员">
              <a-switch v-model:checked="plannerForm.needMedicalStaff" checked-children="需要" un-checked-children="不需要" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item label="风险等级">
              <a-segmented v-model:value="plannerForm.riskLevel" :options="riskOptions" block />
            </a-form-item>
          </a-col>
        </a-row>
        <div class="section-slab">
          <div class="section-slab-head">
            <strong>预算信息</strong>
            <a-switch v-model:checked="plannerForm.needBudget" checked-children="需要费用" un-checked-children="无需费用" />
          </div>
          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item label="预算金额">
                <a-input-number
                  v-model:value="plannerForm.budgetAmount"
                  :min="0"
                  :precision="2"
                  style="width: 100%"
                  :disabled="!plannerForm.needBudget"
                />
              </a-form-item>
            </a-col>
            <a-col :span="16">
              <a-form-item label="费用说明">
                <a-input v-model:value="plannerForm.budgetDescription" :disabled="!plannerForm.needBudget" placeholder="如：水果、舞台布置、摄影" />
              </a-form-item>
            </a-col>
          </a-row>
        </div>
        <a-form-item label="计划备注">
          <a-textarea v-model:value="plannerForm.remark" :rows="3" placeholder="这里写基础说明，详细活动方案请在详情页维护" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-drawer
      v-model:open="detailOpen"
      title="活动方案详情页"
      width="980"
      :destroy-on-close="false"
    >
      <template v-if="detailRecord">
        <div class="drawer-head">
          <div>
            <div class="drawer-title-row">
              <h3>{{ detailRecord.title }}</h3>
              <a-tag :color="statusColor(detailRecord.status)">{{ statusText(detailRecord.status) }}</a-tag>
            </div>
            <p>{{ detailRecord.activityType || '未分类' }} · {{ detailRecord.location || '未填写地点' }} · {{ dateRangeText(detailRecord) }}</p>
          </div>
          <a-space wrap>
            <a-button v-if="canShowEditButton(detailRecord)" @click="openPlanner(detailRecord)">编辑</a-button>
            <a-button
              v-if="detailRecord.status === 'DRAFT' || detailRecord.status === 'PLAN_PENDING' || detailRecord.status === 'REJECTED'"
              type="primary"
              :disabled="!canEditRecord(detailRecord)"
              @click="submitScheme"
            >
              {{ detailRecord.status === 'REJECTED' ? '修改后重新提交' : '提交方案' }}
            </a-button>
            <a-button
              v-if="detailRecord.status === 'APPROVED'"
              type="primary"
              :disabled="!canOperateLifecycle(detailRecord)"
              @click="startPlan(detailRecord)"
            >
              开始活动
            </a-button>
            <a-button
              v-if="detailRecord.status === 'EXECUTING'"
              type="primary"
              :disabled="!canOperateLifecycle(detailRecord)"
              @click="completePlan(detailRecord)"
            >
              完成
            </a-button>
          </a-space>
        </div>

        <div class="drawer-grid">
          <a-card class="glass-card" :bordered="false" title="① 基本信息">
            <div class="info-grid">
              <div><span>活动类型</span><strong>{{ detailRecord.activityType || '-' }}</strong></div>
              <div><span>组织人</span><strong>{{ detailRecord.organizer || '-' }}</strong></div>
              <div><span>参与对象</span><strong>{{ detailRecord.participantTarget || '-' }}</strong></div>
              <div><span>人数预估</span><strong>{{ detailRecord.estimatedCount ?? '-' }}</strong></div>
              <div><span>风险等级</span><strong>{{ riskText(detailRecord.riskLevel) }}</strong></div>
              <div><span>医护支持</span><strong>{{ detailRecord.needMedicalStaff ? '需要' : '不需要' }}</strong></div>
              <div><span>预算</span><strong>{{ budgetText(detailRecord) }}</strong></div>
              <div><span>当前审批人</span><strong>{{ detailRecord.currentApproverName || '-' }}</strong></div>
            </div>
            <div class="tag-row">
              <a-tag v-for="tag in parsedArray(detailRecord.participantTags)" :key="tag" color="gold">{{ tag }}</a-tag>
              <a-tag v-for="tag in parsedArray(detailRecord.participantElderIds)" :key="`elder-${tag}`">{{ tag }}</a-tag>
            </div>
          </a-card>

          <a-card class="glass-card" :bordered="false" title="② 活动方案">
            <a-form layout="vertical">
              <a-form-item label="方案详情">
                <a-textarea
                  v-model:value="schemeForm.content"
                  :rows="10"
                  :disabled="!canEditScheme(detailRecord)"
                  placeholder="建议填写：活动目的、活动流程、人员安排、注意事项、应急预案"
                />
              </a-form-item>
              <a-form-item label="上传 Word / PDF 方案">
                <a-space direction="vertical" style="width: 100%">
                  <a-upload :show-upload-list="false" accept=".doc,.docx,.pdf" :before-upload="beforeUploadScheme">
                    <a-button :disabled="!canEditScheme(detailRecord)">上传方案附件</a-button>
                  </a-upload>
                  <a v-if="schemeForm.schemeAttachmentUrl" :href="schemeForm.schemeAttachmentUrl" target="_blank">
                    {{ schemeForm.schemeAttachmentName || '查看方案附件' }}
                  </a>
                </a-space>
              </a-form-item>
              <a-button type="primary" :disabled="!canEditScheme(detailRecord)" :loading="schemeSaving" @click="saveScheme">
                保存方案
              </a-button>
            </a-form>
          </a-card>

          <a-card class="glass-card" :bordered="false" title="③ 审批流程">
            <div class="approval-flow">
              <div
                v-for="item in approvalSteps"
                :key="`${item.stepNo}-${item.roleCode}`"
                class="approval-step"
                :class="approvalClass(item.status)"
              >
                <div class="approval-badge">{{ approvalIcon(item.status) }}</div>
                <div class="approval-content">
                  <strong>{{ item.label }}</strong>
                  <p>{{ approvalStatusText(item.status) }}</p>
                  <span>审批人：{{ item.approverName || item.expectedApproverName || roleLabel(item.roleCode) }}</span>
                  <span>时间：{{ item.actionTime || '-' }}</span>
                  <span>意见：{{ item.opinion || '-' }}</span>
                </div>
              </div>
            </div>
            <div v-if="detailRecord.status === 'APPROVING' && canApproveCurrent(detailRecord)" class="approval-action">
              <a-textarea v-model:value="approvalOpinion" :rows="3" placeholder="填写审批意见" />
              <a-space>
                <a-button type="primary" :loading="approvalSaving" @click="approveCurrent">通过</a-button>
                <a-button danger :loading="approvalSaving" @click="rejectCurrent">驳回</a-button>
              </a-space>
            </div>
          </a-card>

          <a-card class="glass-card" :bordered="false" title="④ 执行管理">
            <a-form layout="vertical">
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="实际人数">
                    <a-input-number
                      v-model:value="executionForm.actualCount"
                      :min="0"
                      style="width: 100%"
                      :disabled="!canManageExecution(detailRecord)"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="16">
                  <a-form-item label="签到名单">
                    <a-select
                      v-model:value="executionForm.signInRecords"
                      mode="tags"
                      style="width: 100%"
                      :disabled="!canManageExecution(detailRecord)"
                      placeholder="录入已签到长者姓名"
                    />
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item label="活动照片">
                <div class="photo-tools">
                  <a-upload :show-upload-list="false" accept="image/*" :before-upload="beforeUploadPhoto">
                    <a-button :disabled="!canManageExecution(detailRecord)">上传照片</a-button>
                  </a-upload>
                  <span class="muted">支持活动当天边执行边补照片。</span>
                </div>
                <div class="photo-wall">
                  <div v-for="photo in executionForm.photoUrls" :key="photo" class="photo-chip">
                    <img :src="photo" alt="活动照片" />
                    <button v-if="canManageExecution(detailRecord)" type="button" @click="removePhoto(photo)">移除</button>
                  </div>
                </div>
              </a-form-item>
              <a-form-item label="备注记录">
                <a-textarea
                  v-model:value="executionForm.executionRemark"
                  :rows="4"
                  :disabled="!canManageExecution(detailRecord)"
                  placeholder="记录签到、现场情况、老人反馈摘要等"
                />
              </a-form-item>
              <a-space wrap>
                <a-button :loading="executionSaving" :disabled="!canManageExecution(detailRecord)" @click="saveExecution">
                  保存执行信息
                </a-button>
                <a-button
                  v-if="detailRecord.status === 'APPROVED'"
                  type="primary"
                  :disabled="!canOperateLifecycle(detailRecord)"
                  @click="startPlan(detailRecord)"
                >
                  开始活动
                </a-button>
                <a-button
                  v-if="detailRecord.status === 'EXECUTING'"
                  type="primary"
                  :disabled="!canOperateLifecycle(detailRecord)"
                  @click="completePlan(detailRecord)"
                >
                  完成
                </a-button>
              </a-space>
            </a-form>
          </a-card>

          <a-card class="glass-card" :bordered="false" title="⑤ 复盘 / 归档">
            <a-form layout="vertical">
              <a-row :gutter="16">
                <a-col :span="8">
                  <a-form-item label="实际花费">
                    <a-input-number
                      v-model:value="reviewForm.actualExpense"
                      :min="0"
                      :precision="2"
                      style="width: 100%"
                      :disabled="!canManageReview(detailRecord)"
                    />
                  </a-form-item>
                </a-col>
                <a-col :span="16">
                  <a-form-item label="效果评价">
                    <a-radio-group v-model:value="reviewForm.effectEvaluation" :disabled="!canManageReview(detailRecord)">
                      <a-radio-button value="GOOD">好</a-radio-button>
                      <a-radio-button value="NORMAL">一般</a-radio-button>
                      <a-radio-button value="BAD">差</a-radio-button>
                    </a-radio-group>
                  </a-form-item>
                </a-col>
              </a-row>
              <a-form-item label="老人反馈">
                <a-textarea v-model:value="reviewForm.elderFeedback" :rows="3" :disabled="!canManageReview(detailRecord)" />
              </a-form-item>
              <a-form-item label="风险情况">
                <a-textarea v-model:value="reviewForm.riskSituation" :rows="3" :disabled="!canManageReview(detailRecord)" />
              </a-form-item>
              <a-form-item label="活动报告">
                <a-textarea v-model:value="reviewForm.reportContent" :rows="8" :disabled="!canManageReview(detailRecord)" />
              </a-form-item>
              <a-space wrap>
                <a-button :disabled="!canManageReview(detailRecord)" @click="generateReport">生成活动报告</a-button>
                <a-button type="primary" :loading="reviewSaving" :disabled="!canManageReview(detailRecord)" @click="saveReview">
                  保存复盘
                </a-button>
              </a-space>
            </a-form>
          </a-card>
        </div>
      </template>
    </a-drawer>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import dayjs, { type Dayjs } from 'dayjs'
import { message } from 'ant-design-vue'
import PageContainer from '../../components/PageContainer.vue'
import SearchForm from '../../components/SearchForm.vue'
import DataTable from '../../components/DataTable.vue'
import { useUserStore } from '../../stores/user'
import { hasAnyRole, hasMinisterOrHigher } from '../../utils/roleAccess'
import {
  approveActivityPlan,
  createActivityPlan,
  exportActivityPlan,
  getActivityPlan,
  getActivityPlanPage,
  rejectActivityPlan,
  saveActivityPlanExecution,
  saveActivityPlanReview,
  startActivityPlan,
  completeActivityPlan,
  submitActivityPlanScheme,
  updateActivityPlan,
  uploadOaFile
} from '../../api/oa'
import type { OaActivityPlan, PageResult } from '../../types'

type ApprovalStep = {
  stepNo: number
  roleCode: string
  label: string
  status: string
  expectedApproverName?: string
  approverName?: string
  actionTime?: string
  opinion?: string
}

const userStore = useUserStore()
const loading = ref(false)
const saving = ref(false)
const schemeSaving = ref(false)
const approvalSaving = ref(false)
const executionSaving = ref(false)
const reviewSaving = ref(false)
const rows = ref<OaActivityPlan[]>([])
const selectedRowKeys = ref<string[]>([])
const plannerOpen = ref(false)
const detailOpen = ref(false)
const detailRecord = ref<OaActivityPlan | null>(null)
const plannerRange = ref<[Dayjs, Dayjs] | undefined>()
const participantTags = ref<string[]>([])
const participantElderTags = ref<string[]>([])
const approvalOpinion = ref('')

const query = reactive({
  keyword: '',
  activityType: undefined as string | undefined,
  status: undefined as string | undefined,
  organizer: '',
  range: undefined as [Dayjs, Dayjs] | undefined,
  pageNo: 1,
  pageSize: 10
})
const pagination = reactive({ current: 1, pageSize: 10, total: 0, showSizeChanger: true })
const quickStatus = ref('ALL')

const plannerForm = reactive<Partial<OaActivityPlan>>({
  status: 'PLAN_PENDING',
  riskLevel: 'LOW',
  needMedicalStaff: false,
  needBudget: false
})
const schemeForm = reactive({
  content: '',
  schemeAttachmentName: '',
  schemeAttachmentUrl: ''
})
const executionForm = reactive({
  actualCount: undefined as number | undefined,
  signInRecords: [] as string[],
  photoUrls: [] as string[],
  executionRemark: ''
})
const reviewForm = reactive({
  actualExpense: undefined as number | undefined,
  effectEvaluation: 'GOOD',
  elderFeedback: '',
  riskSituation: '',
  reportContent: ''
})

const activityTypeOptions = [
  { label: '生日活动', value: 'BIRTHDAY' },
  { label: '康复训练', value: 'REHABILITATION' },
  { label: '文娱互动', value: 'ENTERTAINMENT' },
  { label: '节日活动', value: 'FESTIVAL' },
  { label: '家属联动', value: 'FAMILY' },
  { label: '外出参访', value: 'OUTING' }
]
const statusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '待提交方案', value: 'PLAN_PENDING' },
  { label: '审批中', value: 'APPROVING' },
  { label: '已批准', value: 'APPROVED' },
  { label: '执行中', value: 'EXECUTING' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已取消', value: 'CANCELLED' }
]
const riskOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'MEDIUM' },
  { label: '高', value: 'HIGH' }
]
const quickStatuses = [
  { label: '全部', value: 'ALL' },
  { label: '待提交方案', value: 'PLAN_PENDING' },
  { label: '审批中', value: 'APPROVING' },
  { label: '已批准', value: 'APPROVED' },
  { label: '执行中', value: 'EXECUTING' },
  { label: '已完成', value: 'COMPLETED' }
]
const columns = [
  { title: '活动', key: 'activity', width: 220 },
  { title: '时间', dataIndex: 'planDate', key: 'planDate', width: 130 },
  { title: '组织人', dataIndex: 'organizer', key: 'organizer', width: 120 },
  { title: '地点', dataIndex: 'location', key: 'location', width: 160 },
  { title: '风险等级', dataIndex: 'riskLevel', key: 'riskLevel', width: 110 },
  { title: '预算', dataIndex: 'budgetAmount', key: 'budgetAmount', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 120 },
  { title: '操作', key: 'action', width: 220, fixed: 'right' }
]

const roles = computed(() => (userStore.roles || []).map((item) => String(item || '').toUpperCase()))
const currentStaffId = computed(() => String(userStore.staffInfo?.id || ''))
const canCreatePlan = computed(() =>
  hasAnyRole(roles.value, ['NURSING_EMPLOYEE', 'NURSING_MINISTER', 'STAFF']) || hasMinisterOrHigher(roles.value)
)
const selectedRecords = computed(() => rows.value.filter((item) => selectedRowKeys.value.includes(String(item.id))))
const selectedSingle = computed(() => (selectedRecords.value.length === 1 ? selectedRecords.value[0] : null))
const summary = computed(() => ({
  pendingScheme: rows.value.filter((item) => item.status === 'PLAN_PENDING').length,
  approving: rows.value.filter((item) => item.status === 'APPROVING').length,
  executing: rows.value.filter((item) => item.status === 'EXECUTING').length,
  completed: rows.value.filter((item) => item.status === 'COMPLETED').length
}))
const rowSelection = computed(() => ({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: Array<string | number>) => {
    selectedRowKeys.value = keys.map((item) => String(item))
  }
}))
const approvalSteps = computed<ApprovalStep[]>(() => {
  if (!detailRecord.value) return []
  const parsed = parsedApprovalSteps(detailRecord.value.approvalLogsJson)
  if (parsed.length) return parsed
  const fallback: ApprovalStep[] = [
    { stepNo: 1, roleCode: 'NURSE_MANAGER', label: '护士长审核', status: 'WAITING', expectedApproverName: '护士长' },
    ...(detailRecord.value.needBudget ? [{ stepNo: 2, roleCode: 'FINANCE', label: '财务审批', status: 'WAITING', expectedApproverName: '财务审批人' }] : []),
    { stepNo: detailRecord.value.needBudget ? 3 : 2, roleCode: 'DEAN', label: '院长审批', status: 'WAITING', expectedApproverName: '院长' }
  ]
  return fallback.map((item) => ({
    ...item,
    status: item.stepNo === 1 && detailRecord.value?.status === 'APPROVING' ? 'PENDING' : item.status
  }))
})

function statusText(status?: string) {
  if (status === 'DRAFT') return '草稿'
  if (status === 'PLAN_PENDING') return '待提交方案'
  if (status === 'APPROVING') return '审批中'
  if (status === 'APPROVED') return '已批准'
  if (status === 'EXECUTING') return '执行中'
  if (status === 'COMPLETED') return '已完成'
  if (status === 'REJECTED') return '已驳回'
  if (status === 'CANCELLED') return '已取消'
  return '待提交方案'
}

function statusColor(status?: string) {
  if (status === 'COMPLETED') return 'green'
  if (status === 'EXECUTING') return 'blue'
  if (status === 'APPROVED') return 'cyan'
  if (status === 'APPROVING') return 'gold'
  if (status === 'REJECTED') return 'red'
  if (status === 'CANCELLED') return 'default'
  return 'orange'
}

function riskText(value?: string) {
  if (value === 'HIGH') return '高'
  if (value === 'MEDIUM') return '中'
  return '低'
}

function riskColor(value?: string) {
  if (value === 'HIGH') return 'red'
  if (value === 'MEDIUM') return 'gold'
  return 'green'
}

function budgetText(record: Partial<OaActivityPlan>) {
  if (!record.needBudget) return '无需费用'
  return record.budgetAmount != null ? `¥${Number(record.budgetAmount).toFixed(2)}` : '待填写'
}

function approvalStatusText(status?: string) {
  if (status === 'APPROVED') return '已通过'
  if (status === 'PENDING') return '审批中'
  if (status === 'REJECTED') return '已驳回'
  return '待处理'
}

function approvalIcon(status?: string) {
  if (status === 'APPROVED') return '✔'
  if (status === 'PENDING') return '⏳'
  if (status === 'REJECTED') return '✖'
  return '•'
}

function approvalClass(status?: string) {
  if (status === 'APPROVED') return 'approved'
  if (status === 'PENDING') return 'pending'
  if (status === 'REJECTED') return 'rejected'
  return 'waiting'
}

function roleLabel(roleCode?: string) {
  if (roleCode === 'NURSE_MANAGER') return '护士长'
  if (roleCode === 'FINANCE') return '财务审批'
  if (roleCode === 'DEAN') return '院长审批'
  return '审批人'
}

function parsedArray(value?: string) {
  if (!value) return []
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed.map((item) => String(item)).filter(Boolean) : []
  } catch {
    return String(value)
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean)
  }
}

function parsedApprovalSteps(value?: string) {
  if (!value) return []
  try {
    const parsed = JSON.parse(value)
    return Array.isArray(parsed) ? parsed as ApprovalStep[] : []
  } catch {
    return []
  }
}

function countByStatus(status: string) {
  if (status === 'ALL') return rows.value.length
  return rows.value.filter((item) => item.status === status).length
}

function dateRangeText(record: Partial<OaActivityPlan>) {
  const start = record.startTime ? dayjs(record.startTime) : null
  const end = record.endTime ? dayjs(record.endTime) : null
  if (start?.isValid() && end?.isValid()) {
    return `${start.format('YYYY-MM-DD HH:mm')} - ${end.format('HH:mm')}`
  }
  if (record.planDate) return record.planDate
  return '未设置时间'
}

function canEditRecord(record: Partial<OaActivityPlan>) {
  if (!record) return false
  const owner = String(record.createdBy || '') === currentStaffId.value
  return (owner || hasMinisterOrHigher(roles.value))
    && ['DRAFT', 'PLAN_PENDING', 'REJECTED'].includes(String(record.status || ''))
}

function canShowEditButton(record: Partial<OaActivityPlan>) {
  return ['DRAFT', 'PLAN_PENDING', 'REJECTED'].includes(String(record.status || '')) && canEditRecord(record)
}

function canOperateLifecycle(record: Partial<OaActivityPlan>) {
  const owner = String(record.createdBy || '') === currentStaffId.value
  return owner || hasMinisterOrHigher(roles.value)
}

function canEditScheme(record: Partial<OaActivityPlan>) {
  return canEditRecord(record)
}

function canApproveCurrent(record: Partial<OaActivityPlan>) {
  if (record.status !== 'APPROVING') return false
  if (record.currentApproverId && String(record.currentApproverId) === currentStaffId.value) return true
  if (record.currentApproverRole === 'NURSE_MANAGER') {
    return hasAnyRole(roles.value, ['NURSING_MINISTER', 'SUPERVISOR', 'MANAGER']) || hasMinisterOrHigher(roles.value)
  }
  if (record.currentApproverRole === 'FINANCE') {
    return hasAnyRole(roles.value, ['FINANCE_EMPLOYEE', 'FINANCE_MINISTER', 'ACCOUNTANT', 'CASHIER']) || hasMinisterOrHigher(roles.value)
  }
  if (record.currentApproverRole === 'DEAN') {
    return hasAnyRole(roles.value, ['DIRECTOR', 'SYS_ADMIN', 'ADMIN'])
  }
  return false
}

function canManageExecution(record: Partial<OaActivityPlan>) {
  return canOperateLifecycle(record) && ['APPROVED', 'EXECUTING', 'COMPLETED'].includes(String(record.status || ''))
}

function canManageReview(record: Partial<OaActivityPlan>) {
  return canOperateLifecycle(record) && ['EXECUTING', 'COMPLETED'].includes(String(record.status || ''))
}

async function fetchData() {
  loading.value = true
  try {
    const params: any = {
      pageNo: query.pageNo,
      pageSize: query.pageSize,
      keyword: query.keyword || undefined,
      activityType: query.activityType || undefined,
      status: query.status || undefined,
      organizer: query.organizer || undefined
    }
    if (query.range) {
      params.dateFrom = query.range[0].format('YYYY-MM-DD')
      params.dateTo = query.range[1].format('YYYY-MM-DD')
    }
    const res: PageResult<OaActivityPlan> = await getActivityPlanPage(params)
    rows.value = (res.list || []).map(normalizePlan)
    pagination.total = res.total || rows.value.length
    selectedRowKeys.value = []
  } finally {
    loading.value = false
  }
}

function normalizePlan(item: OaActivityPlan) {
  return {
    ...item,
    id: String(item.id)
  }
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  query.pageNo = pag.current
  query.pageSize = pag.pageSize
  fetchData()
}

function resetQuery() {
  query.keyword = ''
  query.activityType = undefined
  query.status = undefined
  query.organizer = ''
  query.range = undefined
  quickStatus.value = 'ALL'
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function applyQuickStatus(value: string) {
  quickStatus.value = value
  query.status = value === 'ALL' ? undefined : value
  query.pageNo = 1
  pagination.current = 1
  fetchData()
}

function openPlanner(record?: OaActivityPlan) {
  Object.assign(plannerForm, {
    id: record?.id,
    title: record?.title || '',
    activityType: record?.activityType || undefined,
    location: record?.location || '',
    organizer: record?.organizer || userStore.staffInfo?.realName || userStore.staffInfo?.username || '',
    participantTarget: record?.participantTarget || '',
    estimatedCount: record?.estimatedCount,
    needMedicalStaff: Boolean(record?.needMedicalStaff),
    riskLevel: record?.riskLevel || 'LOW',
    needBudget: Boolean(record?.needBudget),
    budgetAmount: record?.budgetAmount,
    budgetDescription: record?.budgetDescription || '',
    remark: record?.remark || '',
    status: record?.status || 'PLAN_PENDING'
  })
  participantTags.value = parsedArray(record?.participantTags)
  participantElderTags.value = parsedArray(record?.participantElderIds)
  const start = record?.startTime ? dayjs(record.startTime) : (record?.planDate ? dayjs(record.planDate).hour(9).minute(0) : dayjs().hour(9).minute(0))
  const end = record?.endTime ? dayjs(record.endTime) : start.add(2, 'hour')
  plannerRange.value = [start, end]
  plannerOpen.value = true
}

async function submitPlanner() {
  if (!plannerForm.title || !plannerRange.value?.[0] || !plannerRange.value?.[1]) {
    message.warning('请完整填写活动名称和活动时间')
    return
  }
  saving.value = true
  try {
    const payload: Partial<OaActivityPlan> = {
      ...plannerForm,
      planDate: plannerRange.value[0].format('YYYY-MM-DD'),
      startTime: plannerRange.value[0].format('YYYY-MM-DDTHH:mm:ss'),
      endTime: plannerRange.value[1].format('YYYY-MM-DDTHH:mm:ss'),
      participantTags: JSON.stringify(participantTags.value),
      participantElderIds: JSON.stringify(participantElderTags.value),
      status: plannerForm.status || 'PLAN_PENDING'
    }
    if (!payload.needBudget) {
      payload.budgetAmount = undefined
      payload.budgetDescription = ''
    }
    if (plannerForm.id) {
      await updateActivityPlan(plannerForm.id, payload)
      message.success('活动计划已更新')
    } else {
      await createActivityPlan(payload)
      message.success('活动计划已创建，状态为待提交方案')
    }
    plannerOpen.value = false
    await fetchData()
    if (detailRecord.value?.id === plannerForm.id) {
      await refreshDetail(String(plannerForm.id))
    }
  } finally {
    saving.value = false
  }
}

async function openDetail(record: OaActivityPlan) {
  await refreshDetail(String(record.id))
  detailOpen.value = true
}

async function refreshDetail(id: string) {
  const detail = normalizePlan(await getActivityPlan(id))
  detailRecord.value = detail
  hydrateDetailForms(detail)
}

function hydrateDetailForms(record: OaActivityPlan) {
  schemeForm.content = record.content || ''
  schemeForm.schemeAttachmentName = record.schemeAttachmentName || ''
  schemeForm.schemeAttachmentUrl = record.schemeAttachmentUrl || ''
  executionForm.actualCount = record.actualCount
  executionForm.signInRecords = parsedArray(record.signInRecordsJson)
  executionForm.photoUrls = parsedArray(record.photoUrlsJson)
  executionForm.executionRemark = record.executionRemark || ''
  reviewForm.actualExpense = record.actualExpense
  reviewForm.effectEvaluation = record.effectEvaluation || 'GOOD'
  reviewForm.elderFeedback = record.elderFeedback || ''
  reviewForm.riskSituation = record.riskSituation || ''
  reviewForm.reportContent = record.reportContent || ''
  approvalOpinion.value = ''
}

async function saveScheme() {
  if (!detailRecord.value) return
  schemeSaving.value = true
  try {
    await updateActivityPlan(detailRecord.value.id, {
      title: detailRecord.value.title,
      activityType: detailRecord.value.activityType,
      planDate: detailRecord.value.planDate,
      startTime: detailRecord.value.startTime,
      endTime: detailRecord.value.endTime,
      location: detailRecord.value.location,
      organizer: detailRecord.value.organizer,
      participantTarget: detailRecord.value.participantTarget,
      participantTags: detailRecord.value.participantTags,
      participantElderIds: detailRecord.value.participantElderIds,
      estimatedCount: detailRecord.value.estimatedCount,
      needMedicalStaff: detailRecord.value.needMedicalStaff,
      riskLevel: detailRecord.value.riskLevel,
      needBudget: detailRecord.value.needBudget,
      budgetAmount: detailRecord.value.budgetAmount,
      budgetDescription: detailRecord.value.budgetDescription,
      remark: detailRecord.value.remark,
      status: detailRecord.value.status,
      content: schemeForm.content,
      schemeAttachmentName: schemeForm.schemeAttachmentName,
      schemeAttachmentUrl: schemeForm.schemeAttachmentUrl
    })
    message.success('活动方案已保存')
    await refreshDetail(String(detailRecord.value.id))
    await fetchData()
  } finally {
    schemeSaving.value = false
  }
}

async function beforeUploadScheme(file: File) {
  if (!detailRecord.value) return false
  schemeSaving.value = true
  try {
    const uploaded = await uploadOaFile(file, 'oa-activity-scheme')
    schemeForm.schemeAttachmentName = uploaded.originalFileName || uploaded.fileName || file.name
    schemeForm.schemeAttachmentUrl = uploaded.fileUrl || ''
    message.success('方案附件上传成功')
  } finally {
    schemeSaving.value = false
  }
  return false
}

async function submitScheme() {
  if (!detailRecord.value) return
  if (!schemeForm.content && !schemeForm.schemeAttachmentUrl) {
    message.warning('请先填写方案详情或上传方案附件')
    return
  }
  await saveScheme()
  await submitActivityPlanScheme(detailRecord.value.id)
  message.success('方案已提交审批')
  await refreshDetail(String(detailRecord.value.id))
  await fetchData()
}

async function approveCurrent() {
  if (!detailRecord.value) return
  approvalSaving.value = true
  try {
    await approveActivityPlan(detailRecord.value.id, approvalOpinion.value || undefined)
    message.success('审批已通过')
    await refreshDetail(String(detailRecord.value.id))
    await fetchData()
  } finally {
    approvalSaving.value = false
  }
}

async function rejectCurrent() {
  if (!detailRecord.value) return
  approvalSaving.value = true
  try {
    await rejectActivityPlan(detailRecord.value.id, approvalOpinion.value || undefined)
    message.success('活动方案已驳回')
    await refreshDetail(String(detailRecord.value.id))
    await fetchData()
  } finally {
    approvalSaving.value = false
  }
}

async function saveExecution() {
  if (!detailRecord.value) return
  executionSaving.value = true
  try {
    await saveActivityPlanExecution(detailRecord.value.id, {
      actualCount: executionForm.actualCount,
      signInRecords: executionForm.signInRecords,
      photoUrls: executionForm.photoUrls,
      executionRemark: executionForm.executionRemark
    })
    message.success('执行信息已保存')
    await refreshDetail(String(detailRecord.value.id))
    await fetchData()
  } finally {
    executionSaving.value = false
  }
}

async function beforeUploadPhoto(file: File) {
  if (!detailRecord.value) return false
  executionSaving.value = true
  try {
    const uploaded = await uploadOaFile(file, 'oa-activity-photo')
    if (uploaded.fileUrl) {
      executionForm.photoUrls = [...executionForm.photoUrls, uploaded.fileUrl]
      message.success('活动照片已上传')
    }
  } finally {
    executionSaving.value = false
  }
  return false
}

function removePhoto(url: string) {
  executionForm.photoUrls = executionForm.photoUrls.filter((item) => item !== url)
}

async function saveReview() {
  if (!detailRecord.value) return
  reviewSaving.value = true
  try {
    await saveActivityPlanReview(detailRecord.value.id, { ...reviewForm })
    message.success('复盘内容已保存')
    await refreshDetail(String(detailRecord.value.id))
  } finally {
    reviewSaving.value = false
  }
}

function generateReport() {
  if (!detailRecord.value) return
  reviewForm.reportContent = [
    `活动名称：${detailRecord.value.title || ''}`,
    `活动类型：${detailRecord.value.activityType || ''}`,
    `活动时间：${dateRangeText(detailRecord.value)}`,
    `组织人：${detailRecord.value.organizer || ''}`,
    `活动地点：${detailRecord.value.location || ''}`,
    `实际人数：${executionForm.actualCount ?? ''}`,
    `实际花费：${reviewForm.actualExpense ?? ''}`,
    `效果评价：${reviewForm.effectEvaluation === 'GOOD' ? '好' : reviewForm.effectEvaluation === 'BAD' ? '差' : '一般'}`,
    `老人反馈：${reviewForm.elderFeedback || ''}`,
    `风险情况：${reviewForm.riskSituation || ''}`,
    `执行备注：${executionForm.executionRemark || ''}`
  ].join('\n')
}

async function startPlan(record: OaActivityPlan) {
  await startActivityPlan(record.id)
  message.success('活动已进入执行中')
  if (detailRecord.value?.id === record.id) {
    await refreshDetail(String(record.id))
  }
  await fetchData()
}

async function completePlan(record: OaActivityPlan) {
  if (detailRecord.value?.id === record.id && !reviewForm.reportContent) {
    generateReport()
    await saveReview()
  }
  await completeActivityPlan(record.id)
  message.success('活动已完成')
  if (detailRecord.value?.id === record.id) {
    await refreshDetail(String(record.id))
  }
  await fetchData()
}

async function downloadExport() {
  const params: any = {
    keyword: query.keyword || undefined,
    organizer: query.organizer || undefined,
    activityType: query.activityType || undefined,
    status: query.status || undefined
  }
  if (query.range) {
    params.dateFrom = query.range[0].format('YYYY-MM-DD')
    params.dateTo = query.range[1].format('YYYY-MM-DD')
  }
  const blob = await exportActivityPlan(params)
  const href = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = href
  link.download = `oa-activity-plan-${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(href)
}

fetchData()
</script>

<style scoped>
.activity-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.3fr) minmax(280px, 0.9fr);
  gap: 18px;
  margin-bottom: 18px;
  padding: 24px 26px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, rgba(255, 214, 153, 0.32), transparent 38%),
    radial-gradient(circle at bottom right, rgba(74, 161, 255, 0.22), transparent 35%),
    linear-gradient(135deg, #fff8ee 0%, #fff 42%, #f4f8ff 100%);
  border: 1px solid rgba(214, 199, 173, 0.46);
}

.hero-kicker {
  display: inline-block;
  margin-bottom: 10px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(179, 112, 35, 0.12);
  color: #9f5b14;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-copy h2 {
  margin: 0 0 10px;
  color: #172033;
  font-size: 28px;
  line-height: 1.2;
}

.hero-copy p {
  margin: 0;
  max-width: 720px;
  color: #5f6b7a;
  line-height: 1.8;
}

.hero-stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.hero-stat {
  padding: 18px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(216, 225, 234, 0.9);
  box-shadow: 0 14px 28px rgba(15, 35, 60, 0.06);
}

.hero-stat strong {
  display: block;
  font-size: 28px;
  color: #172033;
}

.hero-stat span {
  color: #687587;
  font-size: 13px;
}

.status-strip {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.status-pill {
  min-width: 130px;
  padding: 12px 14px;
  border: 1px solid #eadfc7;
  border-radius: 18px;
  background: linear-gradient(180deg, #fffdf8 0%, #fff7ea 100%);
  color: #754c24;
  text-align: left;
  cursor: pointer;
}

.status-pill.active {
  border-color: #b26e1e;
  box-shadow: 0 10px 22px rgba(178, 110, 30, 0.16);
}

.status-pill span,
.status-pill strong {
  display: block;
}

.status-pill strong {
  margin-top: 2px;
  font-size: 20px;
}

.activity-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.activity-cell strong {
  color: #172033;
}

.activity-cell span {
  color: #6f7b8a;
  font-size: 12px;
}

.planner-headline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
  padding: 14px 16px;
  border-radius: 18px;
  background: #fff8ed;
}

.planner-headline span {
  display: block;
  margin-top: 4px;
  color: #6f7b8a;
  font-size: 12px;
}

.section-slab {
  margin-bottom: 16px;
  padding: 16px;
  border-radius: 18px;
  background: #f8fbff;
  border: 1px solid #e3ecf7;
}

.section-slab-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.drawer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.drawer-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.drawer-title-row h3 {
  margin: 0;
  font-size: 24px;
  color: #182034;
}

.drawer-head p {
  margin: 0;
  color: #677485;
}

.drawer-grid {
  display: grid;
  gap: 16px;
}

.glass-card {
  border-radius: 22px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(248, 251, 255, 0.96) 100%);
  box-shadow: 0 18px 40px rgba(19, 34, 59, 0.08);
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.info-grid div {
  padding: 12px;
  border-radius: 14px;
  background: #f7f9fc;
}

.info-grid span {
  display: block;
  margin-bottom: 6px;
  color: #748092;
  font-size: 12px;
}

.info-grid strong {
  color: #172033;
}

.tag-row {
  margin-top: 14px;
}

.approval-flow {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.approval-step {
  display: grid;
  grid-template-columns: 36px minmax(0, 1fr);
  gap: 12px;
  padding: 14px;
  border-radius: 18px;
  background: #f8fbff;
  border: 1px solid #e7eef7;
}

.approval-step.pending {
  background: #fff9e9;
  border-color: #f4d479;
}

.approval-step.approved {
  background: #f3fbf4;
  border-color: #bfe6c2;
}

.approval-step.rejected {
  background: #fff3f2;
  border-color: #f2b8b5;
}

.approval-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #fff;
  font-weight: 700;
}

.approval-content {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.approval-content p,
.approval-content span {
  margin: 0;
  color: #677485;
}

.approval-action {
  margin-top: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.photo-tools {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.muted {
  color: #748092;
  font-size: 12px;
}

.photo-wall {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 12px;
}

.photo-chip {
  overflow: hidden;
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  background: #fff;
}

.photo-chip img {
  display: block;
  width: 100%;
  height: 110px;
  object-fit: cover;
}

.photo-chip button {
  width: 100%;
  padding: 8px 10px;
  border: 0;
  background: #fff4f3;
  color: #b44339;
  cursor: pointer;
}

@media (max-width: 1200px) {
  .activity-hero,
  .info-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .hero-stats {
    grid-template-columns: 1fr 1fr;
  }

  .drawer-head,
  .planner-headline,
  .section-slab-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
