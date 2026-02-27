<template>
  <PageContainer title="入住办理" subTitle="分床、押金账户、合同信息与二维码配置">
    <a-card class="card-elevated" :bordered="false">
      <h3>办理入住流程</h3>
      <a-form ref="formRef" :model="form" :rules="rules" layout="vertical">
        <a-form-item label="老人姓名" name="elderId">
          <a-select v-model:value="form.elderId" placeholder="请选择老人姓名">
            <a-select-option v-for="item in elders" :key="item.id" :value="item.id">{{ item.fullName }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="收费开始日期" name="admissionDate">
          <a-date-picker v-model:value="form.admissionDate" value-format="YYYY-MM-DD" style="width: 100%" />
        </a-form-item>
        <a-form-item label="合同号" name="contractNo">
          <a-input v-model:value="form.contractNo" placeholder="请输入合同号" />
        </a-form-item>
        <a-form-item label="初始余额(押金)" name="depositAmount">
          <a-input-number v-model:value="form.depositAmount" :min="0" style="width: 100%" placeholder="请输入押金金额" />
        </a-form-item>
        <a-form-item label="楼栋" name="buildingId">
          <a-select v-model:value="assetSelect.buildingId" allow-clear placeholder="请选择楼栋">
            <a-select-option v-for="item in buildingOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="楼层" name="floorId">
          <a-select v-model:value="assetSelect.floorId" allow-clear placeholder="请选择楼层">
            <a-select-option v-for="item in floorOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="房间" name="roomId">
          <a-select v-model:value="assetSelect.roomId" allow-clear placeholder="请选择房间">
            <a-select-option v-for="item in roomOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="床位" name="bedId">
          <a-select v-model:value="form.bedId" allow-clear placeholder="请选择床位">
            <a-select-option v-for="item in bedOptions" :key="item.value" :value="item.value">
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="备注" name="remark">
          <a-input v-model:value="form.remark" placeholder="请输入备注" />
        </a-form-item>
      </a-form>
      <div style="text-align: right; margin-top: 8px;">
        <a-button type="primary" :loading="submitting" @click="submit">提交入住办理</a-button>
      </div>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-form :model="recordQuery" layout="inline" class="search-bar">
        <a-form-item label="老人姓名">
          <a-input v-model:value="recordQuery.keyword" placeholder="请输入老人姓名" allow-clear />
        </a-form-item>
        <a-form-item label="合同号">
          <a-input v-model:value="recordQuery.contractNo" placeholder="请输入合同号" allow-clear />
        </a-form-item>
        <a-form-item label="入住状态">
          <a-select v-model:value="recordQuery.elderStatus" allow-clear style="width: 140px" placeholder="请选择入住状态">
            <a-select-option :value="1">在院</a-select-option>
            <a-select-option :value="2">请假</a-select-option>
            <a-select-option :value="3">离院</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="收费开始日期">
          <a-range-picker v-model:value="recordQuery.admissionDateRange" value-format="YYYY-MM-DD" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="fetchAdmissionRecords">搜索</a-button>
            <a-button @click="resetRecordQuery">清空</a-button>
            <a-button @click="exportRecords">导出</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card class="card-elevated" :bordered="false" style="margin-top: 16px;">
      <a-table :data-source="admissionRows" :columns="columns" :loading="recordLoading" :pagination="false" row-key="id">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'elderStatus'">
            <a-tag :color="record.elderStatus === 1 ? 'green' : record.elderStatus === 2 ? 'orange' : 'default'">
              {{ statusText(record.elderStatus) }}
            </a-tag>
          </template>
        </template>
      </a-table>
      <a-pagination
        style="margin-top: 16px; text-align: right;"
        :current="recordQuery.pageNo"
        :page-size="recordQuery.pageSize"
        :total="recordTotal"
        show-size-changer
        @change="onPageChange"
        @showSizeChange="onPageSizeChange"
      />
    </a-card>
  </PageContainer>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import type { FormInstance, FormRules } from 'ant-design-vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import PageContainer from '../../components/PageContainer.vue'
import { useLiveSyncRefresh } from '../../composables/useLiveSyncRefresh'
import { getElderPage } from '../../api/elder'
import { getBedList, getBuildingList, getFloorList, getRoomList } from '../../api/bed'
import { admitElder, exportAdmissionRecords, getAdmissionRecords } from '../../api/elderLifecycle'
import { getCrmLead, updateCrmLead } from '../../api/crm'
import type {
  AdmissionRecordItem,
  AdmissionRequest,
  BedItem,
  BuildingItem,
  ElderItem,
  FloorItem,
  PageResult,
  RoomItem
} from '../../types'

const route = useRoute()
const formRef = ref<FormInstance>()
const form = reactive<AdmissionRequest>({ elderId: 0, admissionDate: '', bedId: undefined, bedStartDate: undefined })
const submitting = ref(false)
const linkedLeadId = ref<number>()
const elders = ref<ElderItem[]>([])
const buildings = ref<BuildingItem[]>([])
const floors = ref<FloorItem[]>([])
const rooms = ref<RoomItem[]>([])
const beds = ref<BedItem[]>([])

const assetSelect = reactive({
  buildingId: undefined as number | undefined,
  floorId: undefined as number | undefined,
  roomId: undefined as number | undefined
})

const buildingOptions = computed(() =>
  buildings.value.map((b) => ({ label: b.name, value: b.id }))
)
const floorOptions = computed(() =>
  floors.value
    .filter((f) => (assetSelect.buildingId ? f.buildingId === assetSelect.buildingId : true))
    .map((f) => ({ label: f.floorNo, value: f.id }))
)
const roomOptions = computed(() =>
  rooms.value
    .filter((r) => (assetSelect.floorId ? r.floorId === assetSelect.floorId : true))
    .map((r) => ({ label: r.roomNo, value: r.id }))
)
const bedOptions = computed(() =>
  beds.value
    .filter((b) => (assetSelect.roomId ? b.roomId === assetSelect.roomId : true))
    .filter((b) => !b.elderId && (b.status === 1 || b.status === undefined))
    .map((b) => ({ label: b.bedNo, value: b.id }))
)

const rules: FormRules = {
  elderId: [{ required: true, message: '请选择老人姓名' }],
  admissionDate: [{ required: true, message: '请选择收费开始日期' }]
}

const recordLoading = ref(false)
const admissionRows = ref<AdmissionRecordItem[]>([])
const recordTotal = ref(0)
const recordQuery = reactive({
  keyword: undefined as string | undefined,
  contractNo: undefined as string | undefined,
  elderStatus: undefined as number | undefined,
  admissionDateRange: undefined as [string, string] | undefined,
  pageNo: 1,
  pageSize: 10
})

const columns = [
  { title: '老人姓名', dataIndex: 'elderName', key: 'elderName', width: 140 },
  { title: '合同号', dataIndex: 'contractNo', key: 'contractNo', width: 160 },
  { title: '收费开始日期', dataIndex: 'admissionDate', key: 'admissionDate', width: 140 },
  { title: '押金（元）', dataIndex: 'depositAmount', key: 'depositAmount', width: 120 },
  { title: '入住状态', key: 'elderStatus', width: 100 },
  { title: '备注', dataIndex: 'remark', key: 'remark' },
  { title: '登记时间', dataIndex: 'createTime', key: 'createTime', width: 180 }
]

function statusText(status?: number) {
  if (status === 1) return '在院'
  if (status === 2) return '请假'
  if (status === 3) return '离院'
  return '-'
}

async function loadElders() {
  const res: PageResult<ElderItem> = await getElderPage({ pageNo: 1, pageSize: 200 })
  elders.value = res.list
}

async function submit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    submitting.value = true
    if (form.bedId && !form.bedStartDate) {
      form.bedStartDate = form.admissionDate || new Date().toISOString().slice(0, 10)
    }
    await admitElder(form)
    await syncLeadStageAfterAdmission()
    message.success('入住办理成功')
    await fetchAdmissionRecords()
  } catch {
    message.error('提交失败')
  } finally {
    submitting.value = false
  }
}

async function fetchAdmissionRecords() {
  recordLoading.value = true
  try {
    const [admissionDateStart, admissionDateEnd] = recordQuery.admissionDateRange || []
    const res: PageResult<AdmissionRecordItem> = await getAdmissionRecords({
      pageNo: recordQuery.pageNo,
      pageSize: recordQuery.pageSize,
      keyword: recordQuery.keyword,
      contractNo: recordQuery.contractNo,
      elderStatus: recordQuery.elderStatus,
      admissionDateStart,
      admissionDateEnd
    })
    admissionRows.value = res.list
    recordTotal.value = res.total
  } finally {
    recordLoading.value = false
  }
}

function resetRecordQuery() {
  recordQuery.keyword = undefined
  recordQuery.contractNo = undefined
  recordQuery.elderStatus = undefined
  recordQuery.admissionDateRange = undefined
  recordQuery.pageNo = 1
  fetchAdmissionRecords()
}

async function exportRecords() {
  const [admissionDateStart, admissionDateEnd] = recordQuery.admissionDateRange || []
  await exportAdmissionRecords({
    keyword: recordQuery.keyword,
    contractNo: recordQuery.contractNo,
    elderStatus: recordQuery.elderStatus,
    admissionDateStart,
    admissionDateEnd
  })
}

async function loadAssets() {
  try {
    const [buildingList, floorList, roomList, bedList] = await Promise.all([
      getBuildingList(),
      getFloorList(),
      getRoomList(),
      getBedList()
    ])
    buildings.value = buildingList
    floors.value = floorList
    rooms.value = roomList
    beds.value = bedList
  } catch {
    buildings.value = []
    floors.value = []
    rooms.value = []
    beds.value = []
  }
}

function applyRoutePrefill() {
  const residentId = Number(route.query.residentId || 0)
  const leadId = Number(route.query.leadId || 0)
  const contractNo = String(route.query.contractNo || '').trim()
  if (residentId > 0) {
    form.elderId = residentId
    recordQuery.keyword = elders.value.find((item) => item.id === residentId)?.fullName
  }
  if (leadId > 0) {
    linkedLeadId.value = leadId
  }
  if (contractNo) {
    form.contractNo = contractNo
    recordQuery.contractNo = contractNo
  }
  if (!form.admissionDate) {
    form.admissionDate = dayjs().format('YYYY-MM-DD')
  }
}

async function syncLeadStageAfterAdmission() {
  if (!linkedLeadId.value) return
  try {
    const lead = await getCrmLead(linkedLeadId.value)
    if (!lead?.id) return
    await updateCrmLead(lead.id, {
      ...lead,
      contractNo: form.contractNo || lead.contractNo,
      reservationBedId: form.bedId || lead.reservationBedId,
      flowStage: 'PENDING_SIGN',
      currentOwnerDept: 'MARKETING',
      contractStatus: '待签署',
      status: 2
    })
  } catch {
    message.warning('入住成功，但合同流程状态同步失败，请返回合同签约页重试')
  }
}

function onPageChange(page: number) {
  recordQuery.pageNo = page
  fetchAdmissionRecords()
}

function onPageSizeChange(current: number, size: number) {
  recordQuery.pageNo = 1
  recordQuery.pageSize = size
  fetchAdmissionRecords()
}

watch(
  () => assetSelect.buildingId,
  () => {
    assetSelect.floorId = undefined
    assetSelect.roomId = undefined
    form.bedId = undefined
  }
)
watch(
  () => assetSelect.floorId,
  () => {
    assetSelect.roomId = undefined
    form.bedId = undefined
  }
)
watch(
  () => assetSelect.roomId,
  () => {
    form.bedId = undefined
  }
)

useLiveSyncRefresh({
  topics: ['elder', 'bed', 'lifecycle', 'finance', 'care'],
  refresh: () => {
    loadElders()
    loadAssets()
    fetchAdmissionRecords()
  },
  debounceMs: 700
})

onMounted(async () => {
  await loadElders()
  await loadAssets()
  applyRoutePrefill()
  await fetchAdmissionRecords()
})
</script>

<style scoped>
.search-bar {
  margin-bottom: 12px;
}
</style>
