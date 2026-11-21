<template>
  <PageContainer class="rm-page" title="长者一体化详情" sub-title="一人一档，集中健康、护理、家属、费用和业务操作" kicker="RESIDENT MANAGEMENT / 03">
    <section class="rm-card detail-hero">
      <div class="detail-hero__avatar">{{ (elder?.fullName || '?').slice(0, 1) }}</div>
      <div class="detail-hero__main">
        <div class="detail-hero__name">
          <h2>{{ elder?.fullName || '--' }}</h2>
          <span v-if="elder" class="rm-pill" :class="statusPillClass">{{ statusText(elder) }}</span>
          <span v-if="elder?.careLevel" class="rm-pill rm-pill--info">{{ elder.careLevel }}</span>
        </div>
        <div class="detail-hero__meta">
          <span>{{ genderText }} · {{ age ?? '--' }}岁</span>
          <span>编号 {{ elder?.elderCode || '--' }}</span>
          <span>{{ bedText }}</span>
          <span>{{ admissionDate ? `${admissionDate} 入住` : '入住日期 --' }}</span>
          <span>责任护理员：{{ elder?.responsibleStaffName || '--' }}</span>
        </div>
      </div>
      <div class="detail-hero__actions">
        <a-button :disabled="!elder" @click="editOpen = true"><EditOutlined />编辑资料</a-button>
        <a-button @click="router.push({ path: '/elder/bed-panorama', query: { elderId } })"><SwapOutlined />调床 / 状态变更</a-button>
      </div>
    </section>

    <div class="rm-split detail-body">
      <section class="rm-card detail-tabs">
        <a-tabs v-model:activeKey="activeKey">
          <a-tab-pane key="base" tab="基本资料">
            <div class="detail-base">
              <div>
                <h3 class="rm-section-title">基本资料</h3>
                <dl class="detail-grid">
                  <div v-for="item in baseFields" :key="item.label">
                    <dt>{{ item.label }}</dt>
                    <dd>{{ item.value || '--' }}</dd>
                  </div>
                </dl>
              </div>
              <div>
                <h3 class="rm-section-title">当前管理状态</h3>
                <dl class="detail-state">
                  <div><dt>能力评估</dt><dd>{{ latestAssessment?.levelCode || '--' }}</dd></div>
                  <div><dt>护理等级</dt><dd>{{ elder?.careLevel || '--' }}</dd></div>
                  <div><dt>房间床位</dt><dd>{{ bedText }}</dd></div>
                  <div><dt>合同状态</dt><dd>{{ contractStatusText(linkage?.contractStatus) }}</dd></div>
                </dl>
              </div>
            </div>
          </a-tab-pane>

          <a-tab-pane key="health" tab="健康评估">
            <h3 class="rm-section-title">最近评估</h3>
            <a-table :data-source="careProfile?.latestAssessments || []" :columns="assessmentColumns" :pagination="false" row-key="id" size="small" />
            <h3 class="rm-section-title detail-gap">疾病与禁忌</h3>
            <a-space direction="vertical" style="width: 100%">
              <a-select v-model:value="diseaseIds" mode="multiple" show-search option-filter-prop="label" :options="diseaseOptions" style="width: 100%" placeholder="选择疾病标签" />
              <a-button type="primary" @click="saveDiseases">保存疾病</a-button>
            </a-space>
          </a-tab-pane>

          <a-tab-pane key="plan" tab="护理计划">
            <a-table :data-source="servicePlans" :columns="planColumns" :loading="tabLoading.plan" :pagination="false" row-key="id" size="small" />
          </a-tab-pane>

          <a-tab-pane key="service" tab="服务记录">
            <a-table :data-source="serviceRecords" :columns="serviceColumns" :loading="tabLoading.service" :pagination="false" :row-key="(r: NursingRecordItem) => `${r.recordType}-${r.sourceId}`" size="small" />
          </a-tab-pane>

          <a-tab-pane key="family" tab="家属联系人">
            <div class="table-actions">
              <a-space>
                <a-input v-model:value="familyQuery.keyword" placeholder="搜索姓名/电话" allow-clear style="width: 200px" />
                <a-button type="primary" @click="openFamilyBind">绑定家属</a-button>
              </a-space>
            </div>
            <a-table :data-source="pagedFamilies" :columns="familyColumns" :pagination="false" row-key="familyUserId" @change="onFamilyTableChange">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'isPrimary'">
                  <a-tag :color="record.isPrimary ? 'green' : 'default'">{{ record.isPrimary ? '是' : '否' }}</a-tag>
                </template>
                <template v-else-if="column.key === 'action'">
                  <a-button type="link" size="small" danger @click="removeFamily(record)">解除绑定</a-button>
                </template>
              </template>
            </a-table>
            <a-pagination
              style="margin-top: 12px; text-align: right"
              :current="familyQuery.pageNo"
              :page-size="familyQuery.pageSize"
              :total="filteredFamilies.length"
              show-size-changer
              @change="onFamilyPageChange"
              @showSizeChange="onFamilyPageSizeChange"
            />
          </a-tab-pane>

          <a-tab-pane key="contract" tab="合同费用">
            <a-empty v-if="!linkage" description="暂无已签署合同" />
            <template v-else>
              <a-descriptions :column="2" size="small" bordered>
                <a-descriptions-item label="合同编号">{{ linkage.contractNo || '--' }}</a-descriptions-item>
                <a-descriptions-item label="合同状态">{{ contractStatusText(linkage.contractStatus) }}</a-descriptions-item>
                <a-descriptions-item label="签约时间">{{ linkage.contractSignedAt || '--' }}</a-descriptions-item>
                <a-descriptions-item label="到期日期">{{ linkage.contractExpiryDate || '--' }}</a-descriptions-item>
                <a-descriptions-item label="入住日期">{{ linkage.admissionDate || '--' }}</a-descriptions-item>
                <a-descriptions-item label="押金">{{ money(linkage.depositAmount) }}</a-descriptions-item>
                <a-descriptions-item label="账单数">{{ linkage.billCount ?? '--' }}</a-descriptions-item>
                <a-descriptions-item label="应收合计">{{ money(linkage.billTotalAmount) }}</a-descriptions-item>
                <a-descriptions-item label="已收">{{ money(linkage.billPaidAmount) }}</a-descriptions-item>
                <a-descriptions-item label="欠费">{{ money(linkage.billOutstandingAmount) }}</a-descriptions-item>
                <a-descriptions-item label="归档完整度" :span="2">
                  <template v-if="completeness">
                    {{ completeness.score }}% · {{ completeness.level }}
                    <span v-if="linkage.generatedAt" class="detail-muted">（规则评估时间 {{ formatChineseDateTime(linkage.generatedAt, '--') }}）</span>
                  </template>
                  <template v-else>--</template>
                </a-descriptions-item>
              </a-descriptions>
            </template>
            <button type="button" class="rm-link detail-gap" @click="router.push({ path: '/elder/status-change/discharge-settlement', query: { tab: 'fee', elderId } })">查看费用明细与账单 →</button>

            <h3 class="rm-section-title detail-gap">合同资料与评估报告</h3>
            <div class="table-actions">
              <a-space wrap>
                <a-select v-model:value="docFilter.kind" :options="CONTRACT_DOC_KIND_OPTIONS" style="width: 140px" />
                <a-input v-model:value="docFilter.keyword" allow-clear placeholder="文件名/类型" style="width: 200px" @press-enter="applyDocFilter" />
                <a-button type="primary" @click="applyDocFilter">搜索</a-button>
                <a-button :disabled="!filteredDocs.length" @click="exportDocs">导出清单</a-button>
                <a-button @click="goAttachmentUpload('CONTRACT')">{{ isHistoricalImport ? '完善历史合同' : '上传合同附件' }}</a-button>
                <a-button @click="goAttachmentUpload('HOUSEHOLD')">上传证件资料</a-button>
              </a-space>
            </div>
            <a-table :data-source="filteredDocs" :columns="docColumns" :loading="tabLoading.contract" :pagination="false" row-key="id" size="small">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'kind'">
                  <a-tag :color="record.kind === 'ASSESSMENT' ? 'purple' : 'blue'">{{ record.kind === 'ASSESSMENT' ? '评估报告' : '附件资料' }}</a-tag>
                </template>
                <template v-else-if="column.key === 'name'">
                  <a v-if="record.url" :href="record.url" target="_blank" rel="noopener noreferrer">{{ record.name }}</a>
                  <a v-else-if="record.route" @click.prevent="router.push(record.route)">{{ record.name }}</a>
                  <span v-else>{{ record.name }}</span>
                </template>
              </template>
            </a-table>
          </a-tab-pane>

          <a-tab-pane key="archive" tab="档案附件">
            <div class="table-actions">
              <a-space>
                <a-select v-model:value="uploadCategory" :options="categoryOptions" style="width: 120px" />
                <a-upload :before-upload="beforeUploadArchive" :show-upload-list="false">
                  <a-button type="primary" :loading="uploading">上传归档</a-button>
                </a-upload>
              </a-space>
            </div>
            <a-table :data-source="archives" :columns="archiveColumns" :loading="tabLoading.archive" :pagination="false" row-key="id" size="small">
              <template #bodyCell="{ column, record }">
                <template v-if="column.key === 'category'">{{ categoryText(record.category) }}</template>
                <template v-else-if="column.key === 'action'">
                  <a-button type="link" size="small" @click="openFile(record.fileUrl)">查看</a-button>
                  <a-button type="link" size="small" danger @click="removeArchive(record)">删除</a-button>
                </template>
              </template>
            </a-table>
            <div v-if="profileFiles.length" class="detail-gap">
              <h3 class="rm-section-title">档案登记附件</h3>
              <a-space wrap>
                <a-button v-for="file in profileFiles" :key="file.label" size="small" @click="openFile(file.url)">{{ file.label }}</a-button>
              </a-space>
            </div>
          </a-tab-pane>
        </a-tabs>
      </section>

      <aside class="detail-side">
        <section class="rm-card">
          <div class="rm-card__head"><h3 class="rm-card__title">重点关注提醒</h3></div>
          <div v-for="item in alerts" :key="item.text" class="detail-alert" :class="`detail-alert--${item.level}`">
            <WarningOutlined />{{ item.text }}
          </div>
          <div v-if="!alerts.length" class="detail-muted">暂无风险提醒</div>
          <div class="rm-note">风险来自评估、体征、巡检、事件与护理任务记录。</div>
        </section>

        <section class="rm-card">
          <div class="rm-card__head"><h3 class="rm-card__title">重要资料状态</h3></div>
          <div v-for="item in archiveChecks" :key="item.text" class="detail-check">
            <CheckCircleFilled v-if="item.ok" class="detail-check__ok" />
            <ExclamationCircleFilled v-else class="detail-check__todo" />
            {{ item.text }}
          </div>
          <div v-if="!archiveChecks.length" class="detail-muted">--</div>
        </section>

        <section class="rm-card">
          <div class="rm-card__head"><h3 class="rm-card__title">常用业务操作</h3></div>
          <div class="rm-actions">
            <button v-for="item in actionLinks" :key="item.key" type="button" class="rm-action" @click="router.push({ path: item.path, query: item.query })">
              <component :is="actionIcons[item.key]" />{{ item.label }}
            </button>
          </div>
        </section>
      </aside>
    </div>

    <a-drawer v-model:open="editOpen" :title="`编辑档案 · ${elder?.fullName || ''}`" width="820" destroy-on-close>
      <ElderEdit v-if="editOpen" embedded :elder-id="elderId" @saved="onEdited" @cancel="editOpen = false" />
    </a-drawer>

    <a-modal v-model:open="familyBindOpen" :mask-closable="false" title="绑定家属" width="420px" @ok="submitFamilyBind" @cancel="() => (familyBindOpen = false)">
      <a-form ref="familyBindRef" :model="familyBind" :rules="familyBindRules" layout="vertical">
        <a-form-item label="家属" name="familyUserId">
          <a-select
            v-model:value="familyBind.familyUserId"
            allow-clear
            show-search
            :filter-option="false"
            :options="familyOptions"
            placeholder="输入姓名/手机号搜索"
            @search="loadFamilyUsers"
            @focus="() => loadFamilyUsers()"
          />
        </a-form-item>
        <a-form-item label="关系" name="relation">
          <a-select v-model:value="familyBind.relation" :options="familyRelationOptions" placeholder="请选择关系" />
        </a-form-item>
        <a-form-item label="主联系人">
          <a-switch v-model:checked="familyBind.isPrimary" />
        </a-form-item>
      </a-form>
    </a-modal>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import type { FormInstance } from 'ant-design-vue'
import type { Rule } from 'ant-design-vue/es/form'
import { message, Modal } from 'ant-design-vue'
import {
  AuditOutlined,
  CheckCircleFilled,
  EditOutlined,
  ExclamationCircleFilled,
  ExportOutlined,
  FileTextOutlined,
  MedicineBoxOutlined,
  MoneyCollectOutlined,
  SwapOutlined,
  WarningOutlined,
  ApartmentOutlined
} from '@ant-design/icons-vue'
import PageContainer from '../../components/PageContainer.vue'
import './residentUi.css'
import { bindFamily, getElderCareProfile, getElderDetail, getElderDiseases, updateElderDiseases, uploadElderFile } from '../../api/elder'
import { getDiseaseList } from '../../api/store'
import { getFamilyRelations, getFamilyUserPage, removeFamilyRelation } from '../../api/family'
import { getContractAssessmentOverview, getContractLinkageByElder } from '../../api/marketing'
import { getResidentRiskCard } from '../../api/medicalCare'
import { getNursingRecordPage, getServicePlanPage } from '../../api/nursing'
import { createElderFileArchive, deleteElderFileArchive, getElderFileArchives, type ElderFileArchiveItem } from '../../api/elderFileArchive'
import type {
  ContractAssessmentOverview,
  ContractLinkageSummary,
  DiseaseItem,
  ElderCareProfile,
  ElderItem,
  FamilyBindRequest,
  FamilyRelationItem,
  Id,
  MedicalResidentRiskCard,
  NursingRecordItem,
  ServicePlanItem
} from '../../types'
import { ageOf, residentStatusTag, residentStatusText as statusText } from '../../utils/elderStatus'
import { formatChineseDateTime } from '../../utils/dateLocale'
import { exportCsv } from '../../utils/export'
import {
  archiveCompleteness,
  buildArchiveChecks,
  buildContractDocuments,
  CONTRACT_DOC_KIND_OPTIONS,
  contractDocFilterFromQuery,
  buildDetailAlerts,
  contractStatusText,
  filterContractDocuments,
  normalizeDetailTab,
  residentActionLinks,
  type DetailTab
} from './detailView'

const ElderEdit = defineAsyncComponent(() => import('./Edit.vue'))

const route = useRoute()
const router = useRouter()
const elderId = computed(() => String(route.params.id || ''))

const elder = ref<ElderItem | null>(null)
const careProfile = ref<ElderCareProfile | null>(null)
const riskCard = ref<MedicalResidentRiskCard | null>(null)
const linkage = ref<ContractLinkageSummary | null>(null)
// 旧「合同与票据」链接只带 docKind/docKeyword 时也落到合同费用页
const initialTab = route.query.tab || (route.query.docKind || route.query.docKeyword ? 'contract' : '')
const activeKey = ref<DetailTab>(normalizeDetailTab(initialTab))
const editOpen = ref(false)

const age = computed(() => ageOf(elder.value?.birthDate))
const genderText = computed(() => (elder.value?.gender === 1 ? '男' : elder.value?.gender === 2 ? '女' : '--'))
const admissionDate = computed(() => elder.value?.admissionDate || elder.value?.checkInDate || '')
const bedText = computed(() => {
  const roomNo = elder.value?.roomNo || elder.value?.currentBed?.roomNo
  const bedNo = elder.value?.bedNo || elder.value?.currentBed?.bedNo
  return bedNo ? `${roomNo ? `${roomNo}室 · ` : ''}${bedNo}床` : '未分配床位'
})
const statusPillClass = computed(() => {
  const color = residentStatusTag(elder.value || undefined)
  if (color === 'green') return 'rm-pill--success'
  if (['orange', 'volcano', 'gold'].includes(color)) return 'rm-pill--warning'
  if (['red', 'magenta'].includes(color)) return 'rm-pill--danger'
  return 'rm-pill--info'
})
const latestAssessment = computed(() => careProfile.value?.latestAssessments?.[0])
// 身份证/电话/地址由后端 @SensitiveField 按机构开关与角色脱敏，这里原样展示
const baseFields = computed(() => [
  { label: '出生日期', value: elder.value?.birthDate },
  { label: '电话', value: elder.value?.phone },
  { label: '身份证', value: elder.value?.idCardNo },
  { label: '家庭地址', value: elder.value?.homeAddress },
  { label: '责任护理员', value: elder.value?.responsibleStaffName },
  { label: '护理员', value: elder.value?.caregiverNames?.join('、') || elder.value?.caregiverName },
  { label: '档案来源', value: sourceTypeText(elder.value?.sourceType) },
  { label: '风险预担', value: riskPrecommitText(elder.value?.riskPrecommit) },
  { label: '备注', value: elder.value?.remark }
])
const alerts = computed(() => buildDetailAlerts(careProfile.value, riskCard.value))
const archiveChecks = computed(() => buildArchiveChecks(linkage.value))
const actionLinks = computed(() => residentActionLinks(elderId.value))
const actionIcons: Record<string, unknown> = {
  admission: FileTextOutlined,
  bed: ApartmentOutlined,
  outing: ExportOutlined,
  medical: MedicineBoxOutlined,
  fee: MoneyCollectOutlined,
  discharge: AuditOutlined
}
const profileFiles = computed(() =>
  [
    { label: '历史合同', url: elder.value?.historicalContractFileUrl },
    { label: '医保卡复印件', url: elder.value?.medicalInsuranceCopyUrl },
    { label: '户口本复印件', url: elder.value?.householdCopyUrl },
    { label: '病历资料', url: elder.value?.medicalRecordFileUrl }
  ].filter((item): item is { label: string; url: string } => Boolean(item.url))
)

function riskPrecommitText(value?: string) {
  if (value === 'RESCUE_FIRST') return '第一时间抢救'
  if (value === 'NOTIFY_FAMILY_FIRST') return '第一时间通知家属'
  return ''
}

function sourceTypeText(value?: string) {
  if (value === 'HISTORICAL_IMPORT') return '历史补录'
  if (value === 'MARKETING_CONTRACT') return '合同流程'
  return ''
}

function money(value?: number) {
  return value == null ? '--' : `¥${Number(value).toFixed(2)}`
}

function openFile(url?: string) {
  if (url) window.open(url, '_blank', 'noopener')
}

async function load() {
  try {
    elder.value = await getElderDetail(elderId.value)
  } catch {
    message.error('加载失败')
  }
}

async function onEdited() {
  editOpen.value = false
  await load()
}

// 头部与右侧栏用到的汇总数据；失败时对应区域显示 "--"
async function loadSummaries() {
  const [profile, risk, link] = await Promise.allSettled([
    getElderCareProfile(elderId.value),
    getResidentRiskCard(elderId.value),
    getContractLinkageByElder(elderId.value)
  ])
  careProfile.value = profile.status === 'fulfilled' ? profile.value : null
  riskCard.value = risk.status === 'fulfilled' ? risk.value : null
  linkage.value = link.status === 'fulfilled' && link.value?.contractId ? link.value : null
}

/* ---------- 懒加载标签页 ---------- */
const tabLoading = reactive({ plan: false, service: false, archive: false, contract: false })
const loadedTabs = new Set<DetailTab>()
const servicePlans = ref<ServicePlanItem[]>([])
const serviceRecords = ref<NursingRecordItem[]>([])
const archives = ref<ElderFileArchiveItem[]>([])
const assessmentOverview = ref<ContractAssessmentOverview | null>(null)

const tabLoaders: Partial<Record<DetailTab, () => Promise<void>>> = {
  health: loadDiseases,
  family: loadFamilies,
  plan: async () => {
    tabLoading.plan = true
    try {
      servicePlans.value = (await getServicePlanPage({ pageNo: 1, pageSize: 50, elderId: elderId.value })).list
    } finally {
      tabLoading.plan = false
    }
  },
  service: async () => {
    tabLoading.service = true
    try {
      serviceRecords.value = (await getNursingRecordPage({ pageNo: 1, pageSize: 50, elderId: elderId.value })).list
    } finally {
      tabLoading.service = false
    }
  },
  archive: loadArchives,
  contract: async () => {
    tabLoading.contract = true
    try {
      assessmentOverview.value = await getContractAssessmentOverview({ elderId: elderId.value })
    } finally {
      tabLoading.contract = false
    }
  }
}

watch(
  activeKey,
  (key) => {
    const loader = tabLoaders[key]
    if (!loader || loadedTabs.has(key)) return
    loadedTabs.add(key)
    loader().catch(() => loadedTabs.delete(key))
  },
  { immediate: true }
)
watch(
  () => route.query.tab,
  (tab) => {
    if (tab) activeKey.value = normalizeDetailTab(tab)
  }
)

const assessmentColumns = [
  { title: '评估类型', dataIndex: 'assessmentType', key: 'assessmentType' },
  { title: '等级', dataIndex: 'levelCode', key: 'levelCode', width: 100 },
  { title: '分数', dataIndex: 'score', key: 'score', width: 80 },
  { title: '评估日期', dataIndex: 'assessmentDate', key: 'assessmentDate', width: 120 },
  { title: '结论', dataIndex: 'resultSummary', key: 'resultSummary', ellipsis: true }
]
const planColumns = [
  { title: '计划名称', dataIndex: 'planName', key: 'planName' },
  { title: '服务项目', dataIndex: 'serviceItemName', key: 'serviceItemName' },
  { title: '频次', dataIndex: 'frequency', key: 'frequency', width: 80 },
  { title: '执行人', dataIndex: 'defaultStaffName', key: 'defaultStaffName', width: 110 },
  { title: '开始日期', dataIndex: 'startDate', key: 'startDate', width: 120 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 90 }
]
const serviceColumns = [
  { title: '时间', dataIndex: 'recordTime', key: 'recordTime', width: 170 },
  { title: '服务', dataIndex: 'serviceName', key: 'serviceName' },
  { title: '计划', dataIndex: 'planName', key: 'planName' },
  { title: '执行人', dataIndex: 'staffName', key: 'staffName', width: 110 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100 }
]

/* ---------- 健康评估：疾病与禁忌 ---------- */
const diseaseIds = ref<number[]>([])
const diseaseOptions = ref<{ label: string; value: number }[]>([])

async function loadDiseases() {
  const [list, current] = await Promise.all([getDiseaseList(), getElderDiseases(elderId.value)])
  diseaseOptions.value = (list || []).map((d: DiseaseItem) => ({ label: d.diseaseName, value: d.id }))
  diseaseIds.value = current.map((d) => Number(d.diseaseId))
}

async function saveDiseases() {
  try {
    await updateElderDiseases(elderId.value, diseaseIds.value)
    message.success('保存成功')
  } catch {
    message.error('保存失败')
  }
}

/* ---------- 家属联系人 ---------- */
const families = ref<FamilyRelationItem[]>([])
const familyQuery = reactive({ keyword: '', pageNo: 1, pageSize: 5, sortBy: 'familyUserId', sortOrder: 'asc' })
const familyColumns = [
  { title: '家属ID', dataIndex: 'familyUserId', key: 'familyUserId', width: 120, sorter: true },
  { title: '姓名', dataIndex: 'realName', key: 'realName', width: 160, sorter: true },
  { title: '关系', dataIndex: 'relation', key: 'relation', width: 120, sorter: true },
  { title: '电话', dataIndex: 'phone', key: 'phone', width: 160, sorter: true },
  { title: '主联系人', dataIndex: 'isPrimary', key: 'isPrimary', width: 120 },
  { title: '操作', key: 'action', width: 120 }
]
const familyBindOpen = ref(false)
const familyBindRef = ref<FormInstance>()
const familyBind = reactive<FamilyBindRequest>({ familyUserId: '', elderId: '', relation: '', isPrimary: false })
const familyRelationOptions = ['配偶', '子女', '孙辈', '兄弟姐妹', '监护人', '亲属', '朋友', '其他'].map((value) => ({ label: value, value }))
const familyBindRules: Record<string, Rule[]> = {
  familyUserId: [{ required: true, message: '请选择家属' }],
  relation: [{ required: true, message: '请选择关系' }]
}
const familyOptions = ref<Array<{ label: string; value: Id }>>([])

const filteredFamilies = computed(() => {
  const keyword = familyQuery.keyword.trim()
  const list = keyword
    ? families.value.filter(
        (f) => f.realName?.includes(keyword) || f.phone?.includes(keyword) || String(f.familyUserId).includes(keyword)
      )
    : families.value
  const factor = familyQuery.sortOrder === 'asc' ? 1 : -1
  const key = familyQuery.sortBy as keyof FamilyRelationItem
  return [...list].sort((a, b) => {
    const av = a[key] ?? ''
    const bv = b[key] ?? ''
    if (av === bv) return 0
    return av > bv ? factor : -factor
  })
})
const pagedFamilies = computed(() => {
  const start = (familyQuery.pageNo - 1) * familyQuery.pageSize
  return filteredFamilies.value.slice(start, start + familyQuery.pageSize)
})

async function loadFamilies() {
  try {
    families.value = await getFamilyRelations(elderId.value)
  } catch {
    families.value = []
  }
}

async function loadFamilyUsers(keyword?: string) {
  try {
    const res = await getFamilyUserPage({ pageNo: 1, pageSize: 50, realName: keyword, phone: keyword, status: 1 })
    familyOptions.value = res.list.map((item) => ({
      value: item.id,
      label: `${item.realName || '未命名'}${item.phone ? `（${item.phone}）` : ''}`
    }))
  } catch {
    familyOptions.value = []
  }
}

function openFamilyBind() {
  Object.assign(familyBind, { familyUserId: '', relation: '', isPrimary: false })
  loadFamilyUsers()
  familyBindOpen.value = true
}

async function submitFamilyBind() {
  if (!familyBindRef.value) return
  try {
    await familyBindRef.value.validate()
    familyBind.elderId = elderId.value
    await bindFamily(familyBind)
    message.success('绑定成功')
    familyBindOpen.value = false
    await loadFamilies()
  } catch {
    message.error('绑定失败')
  }
}

function removeFamily(record: FamilyRelationItem) {
  if (!record?.id) return
  const familyName = record.realName || `ID:${record.familyUserId}`
  Modal.confirm({
    title: '确认解除绑定',
    content: `将解除老人与家属 ${familyName} 的关联关系，是否继续？`,
    okText: '确认解除',
    cancelText: '取消',
    async onOk() {
      try {
        await removeFamilyRelation(record.id)
        message.success('已解除绑定')
        await loadFamilies()
      } catch {
        message.error('解除绑定失败')
      }
    }
  })
}

function onFamilyTableChange(_: unknown, __: unknown, sorter: any) {
  if (Array.isArray(sorter)) sorter = sorter[0]
  familyQuery.sortBy = sorter?.field || 'familyUserId'
  familyQuery.sortOrder = sorter?.order === 'descend' ? 'desc' : 'asc'
}

function onFamilyPageChange(page: number) {
  familyQuery.pageNo = page
}

function onFamilyPageSizeChange(_: number, size: number) {
  familyQuery.pageNo = 1
  familyQuery.pageSize = size
}

/* ---------- 合同资料与评估报告（原合同与票据页） ---------- */
const docFilter = reactive({ kind: 'ALL', keyword: '' })
const appliedDocFilter = reactive({ kind: 'ALL', keyword: '' })
const completeness = computed(() => archiveCompleteness(linkage.value))
const isHistoricalImport = computed(() => elder.value?.sourceType === 'HISTORICAL_IMPORT')
const contractDocs = computed(() => buildContractDocuments(linkage.value, elder.value, assessmentOverview.value))
const filteredDocs = computed(() => filterContractDocuments(contractDocs.value, appliedDocFilter.kind, appliedDocFilter.keyword))
const docColumns = [
  { title: '来源', dataIndex: 'kind', key: 'kind', width: 100 },
  { title: '资料类型', dataIndex: 'type', key: 'type', width: 120 },
  { title: '文件名', dataIndex: 'name', key: 'name' },
  { title: '大小', dataIndex: 'size', key: 'size', width: 90 },
  { title: '时间', dataIndex: 'time', key: 'time', width: 150 }
]

watch(
  () => [route.query.docKind, route.query.docKeyword],
  () => {
    const filter = contractDocFilterFromQuery(route.query)
    Object.assign(docFilter, filter)
    Object.assign(appliedDocFilter, filter)
  },
  { immediate: true }
)

function applyDocFilter() {
  Object.assign(appliedDocFilter, docFilter)
  const query = { ...route.query, tab: 'contract', docKind: docFilter.kind === 'ALL' ? undefined : docFilter.kind, docKeyword: docFilter.keyword.trim() || undefined }
  router.replace({ path: route.path, query }).catch(() => {})
}

function exportDocs() {
  exportCsv(
    filteredDocs.value.map((item) => ({
      来源: item.kind === 'ASSESSMENT' ? '评估报告' : '附件资料',
      资料类型: item.type,
      文件名: item.name,
      时间: item.time
    })),
    `${elder.value?.fullName || elderId.value}-合同资料.csv`
  )
}

// 历史补录档案的合同在档案编辑里上传；签约档案去合同签约页补附件
function goAttachmentUpload(attachmentType: 'CONTRACT' | 'HOUSEHOLD') {
  if (isHistoricalImport.value) {
    editOpen.value = true
    return
  }
  const contractNo = String(linkage.value?.contractNo || '').trim()
  const query: Record<string, string> = { openAttachment: '1', attachmentType }
  if (contractNo) query.contractNo = contractNo
  else query.elderId = elderId.value
  router.push({ path: '/marketing/contract-signing', query })
}

/* ---------- 档案附件 ---------- */
const categoryOptions = [
  { label: '合同', value: 'CONTRACT' },
  { label: '医疗', value: 'MEDICAL' },
  { label: '证件', value: 'CERTIFICATE' },
  { label: '其他', value: 'OTHER' }
]
const uploadCategory = ref('OTHER')
const uploading = ref(false)
const archiveColumns = [
  { title: '分类', dataIndex: 'category', key: 'category', width: 90 },
  { title: '文件名', dataIndex: 'fileName', key: 'fileName' },
  { title: '归档时间', dataIndex: 'uploadedAt', key: 'uploadedAt', width: 170 },
  { title: '操作', key: 'action', width: 130 }
]

function categoryText(value?: string) {
  return categoryOptions.find((item) => item.value === value)?.label || value || '--'
}

async function loadArchives() {
  tabLoading.archive = true
  try {
    archives.value = await getElderFileArchives(elderId.value)
  } finally {
    tabLoading.archive = false
  }
}

async function beforeUploadArchive(file: File) {
  uploading.value = true
  try {
    const uploaded = await uploadElderFile(file, 'elder-file-archive')
    if (!uploaded?.fileUrl) {
      message.error('上传失败：未返回文件地址')
      return false
    }
    await createElderFileArchive({
      elderId: elderId.value,
      category: uploadCategory.value,
      fileName: file.name,
      fileUrl: uploaded.fileUrl,
      fileSize: file.size
    })
    message.success('已归档')
    await loadArchives()
  } catch (error: any) {
    message.error(error?.message || '上传失败')
  } finally {
    uploading.value = false
  }
  return false
}

function removeArchive(row: ElderFileArchiveItem) {
  Modal.confirm({
    title: `删除归档文件「${row.fileName}」？`,
    content: '仅移除归档记录，不影响已上传的文件本体。',
    okType: 'danger',
    onOk: async () => {
      await deleteElderFileArchive(row.id)
      message.success('已删除')
      await loadArchives()
    }
  })
}

onMounted(() => {
  load()
  loadSummaries()
})
</script>

<style scoped>
.detail-hero {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 20px;
  margin-bottom: 16px;
}

.detail-hero__avatar {
  display: grid;
  place-items: center;
  width: 68px;
  height: 68px;
  flex: none;
  border-radius: 50%;
  background: var(--rm-green-soft);
  color: var(--rm-green);
  font-size: 28px;
  font-weight: 600;
}

.detail-hero__main { flex: 1; min-width: 260px; }
.detail-hero__name { display: flex; align-items: center; gap: 10px; }
.detail-hero__name h2 { margin: 0; font-size: 22px; color: var(--rm-text); }
.detail-hero__meta { display: flex; flex-wrap: wrap; gap: 4px 16px; margin-top: 8px; color: var(--rm-muted); font-size: 13px; }
.detail-hero__actions { display: flex; flex-wrap: wrap; gap: 8px; }

.detail-tabs { min-width: 0; }
.detail-side { display: flex; flex-direction: column; gap: 16px; }
.detail-gap { margin-top: 16px; }

.detail-base {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(240px, 1fr);
  gap: 24px;
}

@media (max-width: 900px) {
  .detail-base { grid-template-columns: minmax(0, 1fr); }
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 16px 20px;
  margin: 0;
}

.detail-grid dt, .detail-state dt { color: var(--rm-muted); font-size: 12px; }
.detail-grid dd { margin: 4px 0 0; color: var(--rm-text); font-weight: 500; word-break: break-all; }

.detail-state {
  margin: 0;
  padding: 4px 16px;
  border: 1px solid var(--rm-border);
  border-radius: 10px;
  background: #fafcfb;
}

.detail-state > div { display: flex; justify-content: space-between; gap: 12px; padding: 12px 0; border-bottom: 1px solid var(--rm-border); }
.detail-state > div:last-child { border-bottom: 0; }
.detail-state dd { margin: 0; font-weight: 600; color: var(--rm-text); }

.detail-alert {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 13px;
}

.detail-alert--danger { border: 1px solid #f5c2c0; background: #fff6f5; color: var(--rm-danger); }
.detail-alert--warning { border: 1px solid #f5dfaa; background: #fff8e8; color: var(--rm-warning); }

.detail-check { display: flex; align-items: center; gap: 8px; padding: 10px 0; border-bottom: 1px dashed var(--rm-border); font-size: 13px; }
.detail-check:last-child { border-bottom: 0; }
.detail-check__ok { color: var(--rm-green); }
.detail-check__todo { color: var(--rm-warning); }
.detail-muted { color: var(--rm-muted); font-size: 13px; }
.table-actions { margin-bottom: 12px; }
</style>
